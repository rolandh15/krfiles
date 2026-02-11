package dev.rolandh.krfiles

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.JsExport
import kotlin.js.Promise

/**
 * JavaScript/TypeScript-friendly wrapper for [FilebrowserClient].
 *
 * All async methods return [Promise] instead of Kotlin `Result`,
 * making this class natural to use from JavaScript and TypeScript.
 *
 * ## Usage (TypeScript)
 *
 * ```typescript
 * import { dev } from 'krfiles';
 * const { JsFilebrowserClient } = dev.rolandh.krfiles;
 *
 * const client = new JsFilebrowserClient("https://files.example.com");
 * const token = await client.login("admin", "password");
 * const listing = await client.listDirectory("/");
 * const items = listing.items?.asJsReadonlyArrayView() ?? [];
 * items.forEach(item => console.log(item.name));
 * client.close();
 * ```
 *
 * @param baseUrl The base URL of the Filebrowser server
 */
@JsExport
@OptIn(DelicateCoroutinesApi::class)
public class JsFilebrowserClient(
    baseUrl: String,
) {
    private val client = FilebrowserClient(baseUrl)

    /** Whether the client is currently authenticated. */
    public val isAuthenticated: Boolean
        get() = client.isAuthenticated

    /**
     * Authenticate with the Filebrowser server.
     *
     * @param username The username
     * @param password The password
     * @return Promise resolving to the auth token string
     */
    public fun login(
        username: String,
        password: String,
    ): Promise<String> = GlobalScope.promise { client.login(username, password).getOrThrow() }

    /**
     * Set the authentication token directly, without calling [login].
     *
     * Use this to restore a previously stored token.
     *
     * @param token The authentication token
     */
    public fun setToken(token: String) {
        client.setToken(token)
    }

    /** Log out and clear the authentication token. */
    public fun logout() {
        client.logout()
    }

    /**
     * Get information about a resource (file or directory).
     *
     * @param path Path to the resource
     * @return Promise resolving to the Resource
     */
    public fun getResource(path: String): Promise<Resource> =
        GlobalScope.promise { client.getResource(path).getOrThrow() }

    /**
     * List contents of a directory.
     *
     * @param path Path to the directory
     * @return Promise resolving to the directory Resource with items
     */
    public fun listDirectory(path: String): Promise<Resource> =
        GlobalScope.promise { client.listDirectory(path).getOrThrow() }

    /**
     * Search for files and directories by name.
     *
     * @param query Search query
     * @param path Path to search within (defaults to root)
     * @return Promise resolving to an array of SearchResult
     */
    public fun search(
        query: String,
        path: String = "/",
    ): Promise<Array<SearchResult>> =
        GlobalScope.promise {
            client.search(query, path).getOrThrow().toTypedArray()
        }

    /**
     * Get path completions for tab-completion.
     *
     * @param partialPath Partial path to complete
     * @return Promise resolving to an array of matching paths
     */
    public fun getCompletions(partialPath: String): Promise<Array<String>> =
        GlobalScope.promise {
            client.getCompletions(partialPath).getOrThrow().toTypedArray()
        }

    /**
     * Download a file.
     *
     * @param path Path to the file
     * @return Promise resolving to the file contents as Int8Array
     */
    public fun download(path: String): Promise<ByteArray> = GlobalScope.promise { client.download(path).getOrThrow() }

    /**
     * Upload a file.
     *
     * @param path Destination path for the file
     * @param content File contents as Int8Array
     * @param override Whether to override existing file (default: true)
     * @return Promise resolving when upload completes
     */
    public fun upload(
        path: String,
        content: ByteArray,
        override: Boolean = true,
    ): Promise<Unit> = GlobalScope.promise { client.upload(path, content, override).getOrThrow() }

    /**
     * Create a directory.
     *
     * @param path Path for the new directory
     * @return Promise resolving when directory is created
     */
    public fun createDirectory(path: String): Promise<Unit> =
        GlobalScope.promise { client.createDirectory(path).getOrThrow() }

    /**
     * Delete a file or directory.
     *
     * @param path Path to delete
     * @return Promise resolving when deletion completes
     */
    public fun delete(path: String): Promise<Unit> = GlobalScope.promise { client.delete(path).getOrThrow() }

    /**
     * Rename or move a file/directory.
     *
     * @param source Current path
     * @param destination New path
     * @param override Whether to override if destination exists
     * @return Promise resolving when rename completes
     */
    public fun rename(
        source: String,
        destination: String,
        override: Boolean = false,
    ): Promise<Unit> = GlobalScope.promise { client.rename(source, destination, override).getOrThrow() }

    /**
     * Copy a file or directory.
     *
     * @param source Source path
     * @param destination Destination path
     * @param override Whether to override if destination exists
     * @return Promise resolving when copy completes
     */
    public fun copy(
        source: String,
        destination: String,
        override: Boolean = false,
    ): Promise<Unit> = GlobalScope.promise { client.copy(source, destination, override).getOrThrow() }

    /**
     * List all users (admin only).
     *
     * @return Promise resolving to an array of User
     */
    public fun listUsers(): Promise<Array<User>> =
        GlobalScope.promise { client.listUsers().getOrThrow().toTypedArray() }

    /**
     * Get a user by ID (admin only).
     *
     * @param id User ID
     * @return Promise resolving to the User
     */
    public fun getUser(id: Int): Promise<User> = GlobalScope.promise { client.getUser(id).getOrThrow() }

    /**
     * Create a new user (admin only).
     *
     * @param userData User information
     * @return Promise resolving when user is created
     */
    public fun createUser(userData: UserData): Promise<Unit> =
        GlobalScope.promise { client.createUser(userData).getOrThrow() }

    /**
     * Delete a user (admin only).
     *
     * @param id User ID to delete
     * @return Promise resolving when user is deleted
     */
    public fun deleteUser(id: Int): Promise<Unit> = GlobalScope.promise { client.deleteUser(id).getOrThrow() }

    /** Close the HTTP client and release resources. */
    public fun close() {
        client.close()
    }
}
