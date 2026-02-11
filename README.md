[![CI](https://github.com/rolandh15/krfiles/actions/workflows/ci.yml/badge.svg)](https://github.com/rolandh15/krfiles/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF.svg)](https://kotlinlang.org)
[![Rust](https://img.shields.io/badge/Rust-2024_edition-DEA584.svg)](https://www.rust-lang.org)

# krfiles

A **Kotlin Multiplatform** client library for the [Filebrowser](https://github.com/filebrowser/filebrowser) API, with a **Rust CLI** that consumes it via Kotlin/Native C FFI.

One library, four targets: JVM, JavaScript/Node.js, Native shared library, and a Rust command-line tool -- all sharing the same Kotlin business logic.

## Platform Support

| Target | Artifact | HTTP Engine | Use Case |
|--------|----------|-------------|----------|
| **JVM** | `.jar` (Maven) | Ktor CIO | Android, Spring, server-side JVM |
| **JS/Node** | npm package | Ktor JS (fetch) | Node.js scripts, TypeScript projects |
| **Native** | `.so` / `.dylib` | Ktor Curl | C, Rust, Go, or any FFI consumer |
| **CLI** | Rust binary | (via Native) | Interactive terminal usage |

## Quick Start

### Kotlin (JVM / Multiplatform)

```kotlin
val client = FilebrowserClient("https://files.example.com")

// Login
client.login("username", "password").getOrThrow()

// List files
val root = client.listDirectory("/").getOrThrow()
root.items?.forEach { println("${it.name} (${it.size} bytes)") }

// Upload
client.upload("/hello.txt", "Hello!".encodeToByteArray())

// Download
val bytes = client.download("/hello.txt").getOrThrow()

client.close()
```

### TypeScript / Node.js

```bash
npm install krfiles
```

```typescript
import krfiles from "krfiles";
const { JsFilebrowserClient } = krfiles.dev.rolandh.krfiles;

const client = new JsFilebrowserClient("https://files.example.com");
await client.login("username", "password");

const root = await client.listDirectory("/");
for (const item of root.items?.asJsReadonlyArrayView() ?? []) {
  console.log(`${item.name} (${item.size} bytes)`);
}

// Note: Kotlin ByteArray maps to Int8Array in JS
const content = new Int8Array(new TextEncoder().encode("Hello!").buffer);
await client.upload("/hello.txt", content);

client.close();
```

### CLI

```bash
# Authenticate (prompts for password)
krfiles login -s https://files.example.com -u admin

# List directory
KRFILES_TOKEN=eyJ... krfiles --server https://files.example.com ls /documents
  photos/
  report.pdf                                      1.2 MB
  notes.txt                                      340 B

  3 files, 1 directories

# Download a file
krfiles --server https://files.example.com get /documents/report.pdf

# Upload, mkdir, copy, move, search...
krfiles put ./local-file.txt /remote/path
krfiles mkdir /new-directory
krfiles cp /source /destination
krfiles mv /old-name /new-name
krfiles search "*.pdf" /documents
```

## Architecture

```
                          commonMain (Kotlin)
                   FilebrowserClient, Models, Auth
                    /           |            \
                   /            |             \
            jvmMain         jsMain         nativeMain
           (CIO engine)  (JS engine)     (Curl engine)
              |              |                |
              v              v                v
           .jar           npm pkg        .so / .dylib
         (Maven)        (TypeScript       (C header)
                        definitions)         |
                                             v
                                    krfiles_shim.c
                                  (flattens vtable)
                                             |
                                             v
                                      ffi.rs (safe Rust)
                                             |
                                             v
                                     main.rs (clap CLI)
```

## Kotlin/Native C Interop: Design Decisions

This project solves three real limitations of Kotlin/Native's C interop. If you're building cross-language FFI with Kotlin/Native, these patterns may be useful.

### 1. No suspend function export

Kotlin `suspend` functions simply don't appear in the generated C header. They can't be called from C, Rust, or any other FFI consumer.

**Workaround:** Top-level wrapper functions in `nativeMain` that call suspend functions via `runBlocking`:

```kotlin
// This appears in the C header; the suspend client.login() does not
fun nativeLogin(username: String, password: String): String? =
    runBlocking {
        client.login(username, password).fold(
            onSuccess = { token -> token },
            onFailure = { e -> lastError = e.message; null }
        )
    }
```

The Rust CLI compensates for the blocking nature by calling these functions from `tokio::task::spawn_blocking`, keeping the async runtime free.

### 2. Opaque pointers, not C structs

Kotlin objects are exposed as `void*` handles, not real C structs with accessible fields. To read a single field, you call a getter through the vtable:

```c
const char* name = symbols->kotlin.root...Resource.get_name(handle);
double size      = symbols->kotlin.root...Resource.get_size(handle);
```

For a directory with N files, accessing all fields requires O(N x fields) FFI calls plus vtable-based List iteration.

**Workaround:** Serialize complex return types to **JSON strings** across the FFI boundary. One FFI call returns all data, and the consumer deserializes natively (serde in Rust, `encoding/json` in Go, cJSON in C). React Native used a similar JSON bridge for years.

### 3. Vtable access pattern

All exported symbols live in a deeply nested struct:
```c
libkrfiles_symbols()->kotlin.root.dev.rolandh.krfiles.nativeLogin(...)
```

**Workaround:** A thin C shim ([`cli/native/krfiles_shim.c`](cli/native/krfiles_shim.c)) includes the Kotlin header and provides flat function names:
```c
const char* krfiles_login(const char* u, const char* p) {
    return KR.nativeLogin(u, p);
}
```

## Building from Source

### Prerequisites

- JDK 17+
- Rust 1.85+ (2024 edition)
- `libcurl-dev` (for Kotlin/Native Curl engine)
  - Ubuntu/Debian: `apt install libcurl4-openssl-dev`
  - macOS: included with Xcode
  - Arch/Manjaro: `pacman -S curl`

### Kotlin library

```bash
# Run all unit tests (JVM, JS, Native)
./gradlew unitTest

# Build all artifacts (JVM jar, JS library, Native shared lib)
./gradlew buildAll

# Format, lint, coverage
./gradlew ktlintFormat
./gradlew check-all

# Generate API documentation
./gradlew dokkaGenerate
```

### Rust CLI

```bash
# 1. Build the Kotlin native shared library
./gradlew linkKrfilesReleaseSharedLinuxX64

# 2. Copy the C header to the CLI
cp lib/build/bin/linuxX64/krfilesReleaseShared/libkrfiles_api.h cli/native/

# 3. Build the Rust CLI
cd cli && cargo build

# 4. Run (needs LD_LIBRARY_PATH for libkrfiles.so)
LD_LIBRARY_PATH=../lib/build/bin/linuxX64/krfilesReleaseShared \
  ./target/debug/krfiles --help
```

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
