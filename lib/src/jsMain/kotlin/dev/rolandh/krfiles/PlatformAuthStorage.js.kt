package dev.rolandh.krfiles

import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JS implementation of AuthStorage.
 *
 * Uses localStorage in browser environments.
 * Falls back to in-memory storage in Node.js.
 */
public actual fun createPlatformAuthStorage(): AuthStorage = JsAuthStorage()

internal class JsAuthStorage : AuthStorage {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val storageKey = "krfiles_auth"

    // In-memory fallback for Node.js where localStorage isn't available
    private var inMemoryData: JsAuthData? = null

    private fun isLocalStorageAvailable(): Boolean =
        try {
            js("typeof localStorage !== 'undefined'") as Boolean
        } catch (e: Exception) {
            false
        }

    private fun readData(): JsAuthData =
        if (isLocalStorageAvailable()) {
            try {
                val stored = localStorage.getItem(storageKey)
                if (stored != null) {
                    json.decodeFromString<JsAuthData>(stored)
                } else {
                    JsAuthData()
                }
            } catch (e: Exception) {
                JsAuthData()
            }
        } else {
            inMemoryData ?: JsAuthData()
        }

    private fun writeData(data: JsAuthData) {
        if (isLocalStorageAvailable()) {
            localStorage.setItem(storageKey, json.encodeToString(data))
        } else {
            inMemoryData = data
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
        if (isLocalStorageAvailable()) {
            localStorage.removeItem(storageKey)
        } else {
            inMemoryData = null
        }
    }
}

@Serializable
internal data class JsAuthData(
    val tokens: Map<String, String> = emptyMap(),
    val defaultServer: String? = null,
)
