package dev.rolandh.krfiles

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.json.Json

/**
 * Kotlin Multiplatform client for the Filebrowser API.
 *
 * This client provides a type-safe interface to interact with a
 * [Filebrowser](https://github.com/filebrowser/filebrowser) server.
 *
 * ## Usage
 *
 * ```kotlin
 * val client = FilebrowserClient("https://files.example.com")
 *
 * // Login
 * client.login("username", "password").getOrThrow()
 *
 * // List files
 * val files = client.listDirectory("/documents").getOrThrow()
 * files.items?.forEach { println(it.name) }
 *
 * // Upload a file
 * client.upload("/documents/hello.txt", "Hello, World!".encodeToByteArray())
 *
 * // Download a file
 * val content = client.download("/documents/hello.txt").getOrThrow()
 *
 * // Don't forget to close
 * client.close()
 * ```
 *
 * @property baseUrl The base URL of the Filebrowser server (e.g., "https://files.example.com")
 * @param httpClient Optional custom HTTP client. If not provided, a default client will be created.
 */
public class FilebrowserClient(
    private val baseUrl: String,
    httpClient: HttpClient? = null,
) : Closeable {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

    private val client: HttpClient =
        httpClient ?: HttpClient {
            install(ContentNegotiation) {
                json(this@FilebrowserClient.json)
            }
        }

    private var authToken: String? = null

    /**
     * Whether the client is currently authenticated.
     */
    public val isAuthenticated: Boolean
        get() = authToken != null

    /**
     * Authenticate with the Filebrowser server.
     *
     * @param username The username
     * @param password The password
     * @return Result containing the auth token on success, or an error on failure
     */
    public suspend fun login(
        username: String,
        password: String,
    ): Result<String> =
        runCatching {
            val response =
                client.post("$baseUrl/api/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username = username, password = password))
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, "Authentication failed")
            }

            val token = response.bodyAsText()
            authToken = token
            token
        }

    /**
     * Set the authentication token directly, without calling [login].
     *
     * Use this to restore a previously stored token (e.g., from [AuthManager]).
     *
     * ```kotlin
     * val creds = authManager.getDefaultCredentials()!!
     * val client = FilebrowserClient(creds.serverUrl)
     * client.setToken(creds.token)
     * ```
     *
     * @param token The authentication token
     */
    public fun setToken(token: String) {
        authToken = token
    }

    /**
     * Log out and clear the authentication token.
     */
    public fun logout() {
        authToken = null
    }

    /**
     * Get information about a resource (file or directory).
     *
     * @param path Path to the resource
     * @return Result containing the resource information
     */
    public suspend fun getResource(path: String): Result<Resource> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.get("$baseUrl/api/resources$encodedPath") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }

            response.body<Resource>()
        }

    /**
     * List contents of a directory.
     *
     * @param path Path to the directory
     * @return Result containing the directory resource with items
     */
    public suspend fun listDirectory(path: String): Result<Resource> = getResource(path)

    /**
     * Search for files and directories by name.
     *
     * @param query Search query (matches file/directory names)
     * @param path Optional path to search within (defaults to root)
     * @return Result containing list of matching paths
     */
    public suspend fun search(
        query: String,
        path: String = "/",
    ): Result<List<SearchResult>> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.get("$baseUrl/api/search$encodedPath") {
                    authHeader()
                    parameter("query", query)
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }

            response.body<List<SearchResult>>()
        }

    /**
     * Get path completions for CLI tab-completion.
     *
     * Given a partial path like "/doc", returns matching entries
     * like ["/documents", "/docs"].
     *
     * @param partialPath Partial path to complete
     * @return Result containing list of matching paths
     */
    public suspend fun getCompletions(partialPath: String): Result<List<String>> =
        runCatching {
            requireAuth()

            // Split into parent directory and prefix
            val normalized = if (partialPath.startsWith("/")) partialPath else "/$partialPath"
            val lastSlash = normalized.lastIndexOf('/')
            val parentPath = if (lastSlash == 0) "/" else normalized.substring(0, lastSlash)
            val prefix = normalized.substring(lastSlash + 1).lowercase()

            // List parent directory and filter by prefix
            val listing = listDirectory(parentPath).getOrThrow()
            val items = listing.items ?: emptyList()

            items
                .filter { it.name.lowercase().startsWith(prefix) }
                .map {
                    val path = if (parentPath == "/") "/${it.name}" else "$parentPath/${it.name}"
                    if (it.isDir) "$path/" else path
                }.sorted()
        }

    /**
     * Download a file.
     *
     * @param path Path to the file
     * @return Result containing the file contents as bytes
     */
    public suspend fun download(path: String): Result<ByteArray> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.get("$baseUrl/api/raw$encodedPath") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }

            response.body<ByteArray>()
        }

    /**
     * Upload a file.
     *
     * @param path Destination path for the file
     * @param content File contents as bytes
     * @param override Whether to override existing file (default: true)
     * @return Result indicating success or failure
     */
    public suspend fun upload(
        path: String,
        content: ByteArray,
        override: Boolean = true,
    ): Result<Unit> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.post("$baseUrl/api/resources$encodedPath") {
                    authHeader()
                    parameter("override", override)
                    contentType(ContentType.Application.OctetStream)
                    setBody(content)
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Create a directory.
     *
     * @param path Path for the new directory
     * @return Result indicating success or failure
     */
    public suspend fun createDirectory(path: String): Result<Unit> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.post("$baseUrl/api/resources$encodedPath/") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Delete a file or directory.
     *
     * @param path Path to delete
     * @return Result indicating success or failure
     */
    public suspend fun delete(path: String): Result<Unit> =
        runCatching {
            requireAuth()
            val encodedPath = path.encodeURLPath()
            val response =
                client.delete("$baseUrl/api/resources$encodedPath") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Rename or move a file/directory.
     *
     * @param source Current path
     * @param destination New path
     * @param override Whether to override if destination exists
     * @return Result indicating success or failure
     */
    public suspend fun rename(
        source: String,
        destination: String,
        override: Boolean = false,
    ): Result<Unit> =
        runCatching {
            requireAuth()
            val encodedSource = source.encodeURLPath()
            val response =
                client.patch("$baseUrl/api/resources$encodedSource") {
                    authHeader()
                    parameter("action", "rename")
                    parameter("destination", destination)
                    parameter("override", override)
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Copy a file or directory.
     *
     * @param source Source path
     * @param destination Destination path
     * @param override Whether to override if destination exists
     * @return Result indicating success or failure
     */
    public suspend fun copy(
        source: String,
        destination: String,
        override: Boolean = false,
    ): Result<Unit> =
        runCatching {
            requireAuth()
            val encodedSource = source.encodeURLPath()
            val response =
                client.patch("$baseUrl/api/resources$encodedSource") {
                    authHeader()
                    parameter("action", "copy")
                    parameter("destination", destination)
                    parameter("override", override)
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * List all users (admin only).
     *
     * @return Result containing list of users
     */
    public suspend fun listUsers(): Result<List<User>> =
        runCatching {
            requireAuth()
            val response =
                client.get("$baseUrl/api/users") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }

            response.body<List<User>>()
        }

    /**
     * Get a user by ID (admin only).
     *
     * @param id User ID
     * @return Result containing the user
     */
    public suspend fun getUser(id: Int): Result<User> =
        runCatching {
            requireAuth()
            val response =
                client.get("$baseUrl/api/users/$id") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }

            response.body<User>()
        }

    /**
     * Create a new user (admin only).
     *
     * @param userData User information
     * @return Result indicating success or failure
     */
    public suspend fun createUser(userData: UserData): Result<Unit> =
        runCatching {
            requireAuth()
            val response =
                client.post("$baseUrl/api/users") {
                    authHeader()
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(data = userData))
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Update an existing user (admin only).
     *
     * @param id User ID
     * @param userData Updated user information
     * @return Result indicating success or failure
     */
    public suspend fun updateUser(
        id: Int,
        userData: UserData,
    ): Result<Unit> =
        runCatching {
            requireAuth()
            val response =
                client.put("$baseUrl/api/users/$id") {
                    authHeader()
                    contentType(ContentType.Application.Json)
                    setBody(userData)
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Delete a user (admin only).
     *
     * @param id User ID to delete
     * @return Result indicating success or failure
     */
    public suspend fun deleteUser(id: Int): Result<Unit> =
        runCatching {
            requireAuth()
            val response =
                client.delete("$baseUrl/api/users/$id") {
                    authHeader()
                }

            if (!response.status.isSuccess()) {
                throw FilebrowserException(response.status.value, response.bodyAsText())
            }
        }

    /**
     * Close the HTTP client and release resources.
     */
    override fun close() {
        client.close()
    }

    private fun requireAuth() {
        checkNotNull(authToken) { "Not authenticated. Call login() first." }
    }

    private fun HttpRequestBuilder.authHeader() {
        header("X-Auth", authToken)
    }

    private fun String.encodeURLPath(): String {
        // Ensure path starts with / and encode special characters
        val normalized = if (startsWith("/")) this else "/$this"
        return normalized.split("/").joinToString("/") { segment ->
            segment.encodeURLParameter()
        }
    }
}
