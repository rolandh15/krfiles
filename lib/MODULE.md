# Module krfiles

Kotlin Multiplatform client library for the [Filebrowser](https://github.com/filebrowser/filebrowser) API.

## Overview

krfiles provides a type-safe, coroutine-based API to interact with Filebrowser servers from any Kotlin target:
- **JVM** - Android, backend services, desktop apps
- **JS** - Browser and Node.js applications
- **Native** - iOS, macOS, Linux (including Rust FFI)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.rolandh.krfiles:krfiles:0.1.0")
}
```

### npm

```bash
npm install krfiles
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

| Target | Output | Use Case |
|--------|--------|----------|
| JVM | JAR | Android, Spring Boot, Desktop |
| JS | npm package | React, Vue, Node.js |
| Native | .so/.dylib | iOS, macOS, Rust CLI |

## License

MIT License - see [LICENSE](https://github.com/rolandh15/krfiles/blob/master/LICENSE)

# Package dev.rolandh.krfiles

Core client and data models for the Filebrowser API.
