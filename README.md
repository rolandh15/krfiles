[![CI](https://github.com/rolandh15/krfiles/actions/workflows/ci.yml/badge.svg)](https://github.com/rolandh15/krfiles/actions/workflows/ci.yml)
[![Docs](https://github.com/rolandh15/krfiles/actions/workflows/docs.yml/badge.svg)](https://rolandh15.github.io/krfiles/)
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
       (CIO engine)  (JS engine)          |
          |              |          +-----------+
          v              v          |           |
       .jar           npm pkg  desktopNativeMain  iosNativeMain
     (Maven)        (TypeScript  (Curl engine)   (Darwin engine)
                    definitions)     |
                                    v
                              .so / .dylib
                               (C header)
                                    |
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

## Kotlin/Native C Interop

The Rust CLI consumes the Kotlin library via C FFI, working around three Kotlin/Native limitations:

1. **No suspend export** - Wrapper functions use `runBlocking`; Rust calls them via `spawn_blocking`
2. **Opaque pointers** - Complex types are serialized as JSON across the FFI boundary
3. **Vtable nesting** - A C shim ([`krfiles_shim.c`](cli/native/krfiles_shim.c)) flattens the deeply nested symbol table into simple function names

See the [full API documentation](https://rolandh15.github.io/krfiles/) for detailed design decisions and code examples.

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
