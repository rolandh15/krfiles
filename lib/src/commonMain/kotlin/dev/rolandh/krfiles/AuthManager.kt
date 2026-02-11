package dev.rolandh.krfiles

/**
 * Manages authentication credentials for multiple Filebrowser servers.
 *
 * Supports storing tokens for multiple servers and designating a default server
 * for CLI convenience (similar to `gh auth login`).
 *
 * ## Usage
 *
 * ```kotlin
 * // Use platform-specific storage
 * val authManager = AuthManager(PlatformAuthStorage())
 *
 * // Or create with default platform storage
 * val authManager = AuthManager.create()
 *
 * // Save credentials after login
 * val client = FilebrowserClient("https://files.example.com")
 * val token = client.login("user", "pass").getOrThrow()
 * authManager.saveCredentials("https://files.example.com", token)
 * authManager.setDefaultServer("https://files.example.com")
 *
 * // Later, retrieve credentials
 * val creds = authManager.getDefaultCredentials()
 * if (creds != null) {
 *     val client = FilebrowserClient(creds.serverUrl)
 *     // Use stored token directly (client would need a method for this)
 * }
 * ```
 *
 * @param storage Platform-specific storage backend
 */
public class AuthManager(
    private val storage: AuthStorage,
) {
    /**
     * Save authentication token for a server.
     *
     * @param serverUrl Base URL of the Filebrowser server
     * @param token Authentication token from login
     */
    public suspend fun saveCredentials(
        serverUrl: String,
        token: String,
    ) {
        storage.saveToken(normalizeUrl(serverUrl), token)
    }

    /**
     * Get stored token for a server.
     *
     * @param serverUrl Base URL of the Filebrowser server
     * @return Token if stored, null otherwise
     */
    public suspend fun getCredentials(serverUrl: String): String? = storage.getToken(normalizeUrl(serverUrl))

    /**
     * Remove stored credentials for a server.
     *
     * @param serverUrl Base URL of the Filebrowser server
     */
    public suspend fun removeCredentials(serverUrl: String) {
        val normalized = normalizeUrl(serverUrl)
        storage.removeToken(normalized)

        // Clear default if it was this server
        if (storage.getDefaultServerUrl() == normalized) {
            storage.setDefaultServerUrl(null)
        }
    }

    /**
     * List all servers with stored credentials.
     *
     * @return List of server URLs
     */
    public suspend fun listServers(): List<String> = storage.listServerUrls()

    /**
     * Set the default server for CLI commands.
     *
     * @param serverUrl Base URL of the Filebrowser server
     */
    public suspend fun setDefaultServer(serverUrl: String) {
        storage.setDefaultServerUrl(normalizeUrl(serverUrl))
    }

    /**
     * Get the default server URL.
     *
     * @return Default server URL if set, null otherwise
     */
    public suspend fun getDefaultServer(): String? = storage.getDefaultServerUrl()

    /**
     * Get credentials for the default server.
     *
     * @return Credentials if default server is set and has stored token, null otherwise
     */
    public suspend fun getDefaultCredentials(): ServerCredentials? {
        val serverUrl = storage.getDefaultServerUrl() ?: return null
        val token = storage.getToken(serverUrl) ?: return null
        return ServerCredentials(serverUrl, token)
    }

    /**
     * Check if credentials are stored for a server.
     *
     * @param serverUrl Base URL of the Filebrowser server
     * @return true if credentials exist
     */
    public suspend fun isAuthenticated(serverUrl: String): Boolean = storage.getToken(normalizeUrl(serverUrl)) != null

    /**
     * Clear all stored credentials and settings.
     */
    public suspend fun clearAll() {
        storage.clear()
    }

    private fun normalizeUrl(url: String): String = url.trimEnd('/')

    public companion object {
        /**
         * Create an AuthManager with the platform-specific default storage.
         */
        public fun create(): AuthManager = AuthManager(createPlatformAuthStorage())
    }
}

/**
 * Stored credentials for a server.
 *
 * @property serverUrl Base URL of the Filebrowser server
 * @property token Authentication token
 */
public data class ServerCredentials(
    val serverUrl: String,
    val token: String,
)

/**
 * Platform-agnostic interface for credential storage.
 *
 * Implementations handle platform-specific storage mechanisms:
 * - JVM/Native: File system (`~/.config/krfiles/auth.json`)
 * - JS Browser: localStorage
 * - JS Node: File system
 */
public interface AuthStorage {
    /** Save a token for a server URL. */
    public suspend fun saveToken(
        serverUrl: String,
        token: String,
    )

    /** Get stored token for a server URL. */
    public suspend fun getToken(serverUrl: String): String?

    /** Remove token for a server URL. */
    public suspend fun removeToken(serverUrl: String)

    /** List all server URLs with stored tokens. */
    public suspend fun listServerUrls(): List<String>

    /** Set the default server URL. */
    public suspend fun setDefaultServerUrl(serverUrl: String?)

    /** Get the default server URL. */
    public suspend fun getDefaultServerUrl(): String?

    /** Clear all stored data. */
    public suspend fun clear()
}

/**
 * Create the platform-specific AuthStorage implementation.
 */
public expect fun createPlatformAuthStorage(): AuthStorage
