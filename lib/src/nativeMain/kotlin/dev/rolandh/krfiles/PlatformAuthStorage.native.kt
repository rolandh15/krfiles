package dev.rolandh.krfiles

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getenv
import platform.posix.mkdir
import platform.posix.remove

/**
 * Native implementation of AuthStorage using file system.
 *
 * Stores credentials in `~/.config/krfiles/auth.json`.
 */
public actual fun createPlatformAuthStorage(): AuthStorage = NativeAuthStorage()

@OptIn(ExperimentalForeignApi::class)
internal class NativeAuthStorage : AuthStorage {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    private val configDir: String by lazy {
        val home = getenv("HOME")?.toKString() ?: "/tmp"
        "$home/.config/krfiles"
    }

    private val authFilePath: String by lazy {
        "$configDir/auth.json"
    }

    private fun ensureConfigDir() {
        mkdir(configDir, 493u) // 0755
    }

    private fun readData(): NativeAuthData =
        try {
            val content = readFileContent(authFilePath)
            if (content != null) {
                json.decodeFromString<NativeAuthData>(content)
            } else {
                NativeAuthData()
            }
        } catch (e: Exception) {
            NativeAuthData()
        }

    private fun writeData(data: NativeAuthData) {
        ensureConfigDir()
        writeFileContent(authFilePath, json.encodeToString(data))
    }

    private fun readFileContent(path: String): String? {
        val file: CPointer<platform.posix.FILE>? = fopen(path, "r")
        if (file == null) return null

        return try {
            memScoped {
                val buffer = StringBuilder()
                val lineBuffer = allocArray<ByteVar>(4096)
                while (fgets(lineBuffer, 4096, file) != null) {
                    buffer.append(lineBuffer.toKString())
                }
                buffer.toString().ifEmpty { null }
            }
        } finally {
            fclose(file)
        }
    }

    private fun writeFileContent(
        path: String,
        content: String,
    ) {
        val file: CPointer<platform.posix.FILE>? = fopen(path, "w")
        if (file == null) return

        try {
            fputs(content, file)
        } finally {
            fclose(file)
        }
    }

    override suspend fun saveToken(
        serverUrl: String,
        token: String,
    ) {
        val data = readData()
        val updated =
            data.copy(
                tokens = data.tokens + (serverUrl to token),
            )
        writeData(updated)
    }

    override suspend fun getToken(serverUrl: String): String? = readData().tokens[serverUrl]

    override suspend fun removeToken(serverUrl: String) {
        val data = readData()
        val updated =
            data.copy(
                tokens = data.tokens - serverUrl,
            )
        writeData(updated)
    }

    override suspend fun listServerUrls(): List<String> = readData().tokens.keys.toList()

    override suspend fun setDefaultServerUrl(serverUrl: String?) {
        val data = readData()
        val updated = data.copy(defaultServer = serverUrl)
        writeData(updated)
    }

    override suspend fun getDefaultServerUrl(): String? = readData().defaultServer

    override suspend fun clear() {
        remove(authFilePath)
    }
}

@Serializable
internal data class NativeAuthData(
    val tokens: Map<String, String> = emptyMap(),
    val defaultServer: String? = null,
)
