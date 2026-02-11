@file:OptIn(ExperimentalForeignApi::class)

package dev.rolandh.krfiles

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite

/**
 * C-exported wrapper functions for FFI consumers (Rust CLI).
 *
 * These top-level functions bridge the async [FilebrowserClient] to synchronous
 * C-callable functions using [runBlocking]. They appear in the generated C header
 * as part of the `libkrfiles_ExportedSymbols` vtable.
 *
 * ## Why JSON instead of C structs?
 *
 * Kotlin/Native's C interop exposes Kotlin objects as **opaque pointers** — not as
 * real C structs with accessible fields. The generated header gives you handles like:
 *
 * ```c
 * typedef struct { void* pinned; } libkrfiles_kref_dev_rolandh_krfiles_Resource;
 * ```
 *
 * To read a single field, you must call a getter function through the vtable:
 *
 * ```c
 * const char* name = symbols->kotlin.root...Resource.get_name(resource);
 * double size      = symbols->kotlin.root...Resource.get_size(resource);
 * ```
 *
 * This is because Kotlin objects live on the GC-managed heap and cannot be laid out
 * as plain C structs. For a directory listing with N items, accessing all fields
 * requires O(N × fields) individual FFI calls, plus Kotlin `List` iteration through
 * the vtable (no direct index access from C).
 *
 * The JSON bridge solves this: **one FFI call** returns all data as a string that
 * the caller deserializes natively (e.g. serde in Rust, json.Unmarshal in Go,
 * cJSON in C). This is a common pattern in cross-language FFI — React Native used
 * a similar JSON bridge for years.
 *
 * Additionally, Kotlin `suspend` functions cannot be exported through C interop at
 * all. These wrappers use [runBlocking] to call them synchronously. The Rust side
 * compensates by calling these blocking functions from [tokio::task::spawn_blocking].
 *
 * ## File transfers
 *
 * Rather than passing raw byte arrays across the FFI boundary (which would require
 * manual buffer management), file transfers go through the filesystem:
 * - [nativeDownloadToFile]: Kotlin downloads and writes to a local path
 * - [nativeUploadFromFile]: Kotlin reads from a local path and uploads
 */

private var globalClient: FilebrowserClient? = null
private var lastError: String? = null

private val exportJson =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

// --- Lifecycle ---

/** Create a new client for the given server URL. Closes any previous client. */
public fun nativeCreateClient(baseUrl: String) {
    globalClient?.close()
    globalClient = FilebrowserClient(baseUrl)
    lastError = null
}

/** Destroy the client and free resources. */
public fun nativeDestroyClient() {
    globalClient?.close()
    globalClient = null
    lastError = null
}

/** Get the last error message, or null if no error. */
public fun nativeGetLastError(): String? = lastError

// --- Auth ---

/** Login and return the auth token, or null on failure (check [nativeGetLastError]). */
public fun nativeLogin(
    username: String,
    password: String,
): String? =
    runBlocking {
        val client = requireClient() ?: return@runBlocking null
        client.login(username, password).fold(
            onSuccess = { token ->
                lastError = null
                token
            },
            onFailure = { e ->
                lastError = e.message
                null
            },
        )
    }

/** Set the auth token directly (e.g. restored from storage). */
public fun nativeSetToken(token: String): Boolean {
    val client = requireClient() ?: return false
    client.setToken(token)
    return true
}

/** Log out and clear the auth token. */
public fun nativeLogout(): Boolean {
    val client = requireClient() ?: return false
    client.logout()
    return true
}

/** Check if the client is authenticated. */
public fun nativeIsAuthenticated(): Boolean = globalClient?.isAuthenticated ?: false

// --- Resources (return JSON) ---

/** Get resource info as JSON, or null on failure. */
public fun nativeGetResource(path: String): String? =
    runBlocking {
        val client = requireClient() ?: return@runBlocking null
        client.getResource(path).fold(
            onSuccess = { resource ->
                lastError = null
                exportJson.encodeToString(resource)
            },
            onFailure = { e ->
                lastError = e.message
                null
            },
        )
    }

/** List directory contents as JSON, or null on failure. */
public fun nativeListDirectory(path: String): String? =
    runBlocking {
        val client = requireClient() ?: return@runBlocking null
        client.listDirectory(path).fold(
            onSuccess = { resource ->
                lastError = null
                exportJson.encodeToString(resource)
            },
            onFailure = { e ->
                lastError = e.message
                null
            },
        )
    }

/** Search for files, returns JSON array of results, or null on failure. */
public fun nativeSearch(
    query: String,
    path: String,
): String? =
    runBlocking {
        val client = requireClient() ?: return@runBlocking null
        client.search(query, path).fold(
            onSuccess = { results ->
                lastError = null
                exportJson.encodeToString(results)
            },
            onFailure = { e ->
                lastError = e.message
                null
            },
        )
    }

// --- File Operations ---

/** Download a remote file and save to a local path. Returns true on success. */
public fun nativeDownloadToFile(
    remotePath: String,
    localPath: String,
): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        client.download(remotePath).fold(
            onSuccess = { bytes ->
                writeLocalFile(localPath, bytes)
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

/** Upload a local file to a remote path. Returns true on success. */
public fun nativeUploadFromFile(
    remotePath: String,
    localPath: String,
    override_: Boolean,
): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        val bytes = readLocalFile(localPath)
        if (bytes == null) {
            lastError = "Failed to read local file: $localPath"
            return@runBlocking false
        }
        client.upload(remotePath, bytes, override_).fold(
            onSuccess = {
                lastError = null
                true
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

/** Create a directory. Returns true on success. */
public fun nativeCreateDirectory(path: String): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        client.createDirectory(path).fold(
            onSuccess = {
                lastError = null
                true
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

/** Delete a file or directory. Returns true on success. */
public fun nativeDelete(path: String): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        client.delete(path).fold(
            onSuccess = {
                lastError = null
                true
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

/** Rename/move a file or directory. Returns true on success. */
public fun nativeRename(
    source: String,
    destination: String,
    override_: Boolean,
): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        client.rename(source, destination, override_).fold(
            onSuccess = {
                lastError = null
                true
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

/** Copy a file or directory. Returns true on success. */
public fun nativeCopy(
    source: String,
    destination: String,
    override_: Boolean,
): Boolean =
    runBlocking {
        val client = requireClient() ?: return@runBlocking false
        client.copy(source, destination, override_).fold(
            onSuccess = {
                lastError = null
                true
            },
            onFailure = { e ->
                lastError = e.message
                false
            },
        )
    }

// --- Internal helpers ---

private fun requireClient(): FilebrowserClient? {
    val client = globalClient
    if (client == null) {
        lastError = "Client not initialized. Call nativeCreateClient() first."
    }
    return client
}

private fun readLocalFile(path: String): ByteArray? {
    val file: CPointer<FILE>? = fopen(path, "rb")
    if (file == null) return null
    return try {
        fseek(file, 0, platform.posix.SEEK_END)
        val size = ftell(file).toInt()
        fseek(file, 0, platform.posix.SEEK_SET)
        if (size <= 0) return ByteArray(0)
        memScoped {
            val buffer = allocArray<ByteVar>(size)
            val read = fread(buffer, 1u, size.toULong(), file)
            buffer.readBytes(read.toInt())
        }
    } finally {
        fclose(file)
    }
}

private fun writeLocalFile(
    path: String,
    data: ByteArray,
): Boolean {
    val file: CPointer<FILE>? = fopen(path, "wb")
    if (file == null) {
        lastError = "Failed to open file for writing: $path"
        return false
    }
    return try {
        // usePinned pins the ByteArray in memory so the GC won't move it
        // while the C function (fwrite) is reading from it
        data.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, data.size.toULong(), file)
        }
        lastError = null
        true
    } finally {
        fclose(file)
    }
}
