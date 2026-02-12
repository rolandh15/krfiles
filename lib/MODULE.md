# Module krfiles

Kotlin Multiplatform client library for the [Filebrowser](https://github.com/filebrowser/filebrowser) API.

## Overview

krfiles provides a type-safe, coroutine-based API to interact with Filebrowser servers from any Kotlin target:
- **JVM** - Android, backend services, desktop apps
- **JS** - Browser and Node.js applications
- **Native** - iOS, macOS, Linux (including Rust FFI)

## Installation

### Gradle (Kotlin DSL)

Add the GitHub Packages repository and dependency:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/rolandh15/krfiles")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("dev.rolandh.krfiles:krfiles:0.1.0")
}
```

### npm

```bash
echo "@rolandh15:registry=https://npm.pkg.github.com" >> .npmrc
npm install @rolandh15/krfiles
```

## Quick Start

```kotlin
import dev.rolandh.krfiles.FilebrowserClient

suspend fun main() {
    val client = FilebrowserClient("https://files.example.com")

    // Authenticate
    client.login("username", "password").getOrThrow()

    // List files
    val listing = client.listDirectory("/documents").getOrThrow()
    listing.items?.forEach { file ->
        println("${file.name} - ${if (file.isDir) "DIR" else file.size}")
    }

    // Upload a file
    client.upload("/documents/hello.txt", "Hello, World!".encodeToByteArray())

    // Download a file
    val content = client.download("/documents/hello.txt").getOrThrow()
    println(content.decodeToString())

    // Search for files
    val results = client.search("report").getOrThrow()
    results.forEach { println(it.path) }

    // Tab completion support
    val completions = client.getCompletions("/doc").getOrThrow()
    completions.forEach { println(it) }

    client.close()
}
```

## Features

- **Authentication** - Login/logout with token management
- **File Operations** - Upload, download, delete, rename, copy
- **Directory Operations** - List, create, navigate
- **Search** - Find files by name
- **Tab Completion** - CLI-friendly path completion
- **User Management** - Admin operations for user CRUD

## Targets

| Target | Output | HTTP Engine | Use Case |
|--------|--------|-------------|----------|
| JVM | `.jar` (Maven) | Ktor CIO | Android, Spring, server-side JVM |
| JS/Node | npm package | Ktor JS (fetch) | Node.js scripts, TypeScript projects |
| Native (desktop) | `.so` / `.dylib` | Ktor Curl | C, Rust, Go, or any FFI consumer |
| Native (iOS) | Framework | Ktor Darwin | iOS / iPadOS apps |

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

**Workaround:** A thin C shim ([`cli/native/krfiles_shim.c`](https://github.com/rolandh15/krfiles/blob/master/cli/native/krfiles_shim.c)) includes the Kotlin header and provides flat function names:
```c
const char* krfiles_login(const char* u, const char* p) {
    return KR.nativeLogin(u, p);
}
```

The Rust side then declares these as simple `extern "C"` functions and wraps them with safe Rust APIs in the `ffi` module.

## License

Apache License 2.0 - see [LICENSE](https://github.com/rolandh15/krfiles/blob/master/LICENSE)

# Package dev.rolandh.krfiles

Core client and data models for the Filebrowser API.
