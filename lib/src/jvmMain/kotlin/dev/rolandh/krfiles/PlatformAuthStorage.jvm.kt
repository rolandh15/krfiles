package dev.rolandh.krfiles

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * JVM implementation of AuthStorage using file system.
 *
 * Stores credentials in `~/.config/krfiles/auth.json`.
 */
public actual fun createPlatformAuthStorage(): AuthStorage = FileAuthStorage()

internal class FileAuthStorage : AuthStorage {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    private val configDir: File by lazy {
        val userHome = System.getProperty("user.home")
        File(userHome, ".config/krfiles").also { it.mkdirs() }
    }

    private val authFile: File by lazy {
        File(configDir, "auth.json")
    }

    private fun readData(): AuthData =
        if (authFile.exists()) {
            try {
                json.decodeFromString<AuthData>(authFile.readText())
            } catch (e: Exception) {
                AuthData()
            }
        } else {
            AuthData()
        }

    private fun writeData(data: AuthData) {
        authFile.writeText(json.encodeToString(data))
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
        if (authFile.exists()) {
            authFile.delete()
        }
    }
}

@Serializable
internal data class AuthData(
    val tokens: Map<String, String> = emptyMap(),
    val defaultServer: String? = null,
)
