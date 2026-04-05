package dev.rolandh.krfiles

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Represents a file or directory resource in Filebrowser.
 *
 * @property name The name of the file or directory
 * @property size Size in bytes (0 for directories)
 * @property extension File extension (empty for directories)
 * @property modified Last modification timestamp (Unix epoch)
 * @property mode Unix file mode
 * @property isDir Whether this is a directory
 * @property isSymlink Whether this is a symbolic link
 * @property type MIME type of the file
 * @property path Full path to the resource
 */
@JsExport
@Serializable
public data class Resource(
    val name: String = "",
    val size: Double = 0.0,
    val extension: String = "",
    val modified: String = "",
    val mode: Double = 0.0,
    val isDir: Boolean = false,
    val isSymlink: Boolean = false,
    val type: String = "",
    val path: String = "",
    val items: List<Resource>? = null,
    val numDirs: Int = 0,
    val numFiles: Int = 0,
    val sorting: Sorting? = null,
)

/**
 * Sorting configuration for directory listings.
 */
@JsExport
@Serializable
public data class Sorting(
    val by: String = "name",
    val asc: Boolean = true,
)

/**
 * Search result from Filebrowser search API.
 *
 * @property path Path to the matching file or directory
 * @property dir Whether this is a directory
 */
@JsExport
@Serializable
public data class SearchResult(
    val path: String,
    val dir: Boolean = false,
)

/**
 * User account in Filebrowser.
 *
 * @property id Unique user ID
 * @property username Login username
 * @property scope Root directory scope for this user
 * @property locale User's preferred locale
 * @property perm User permissions
 */
@JsExport
@Serializable
public data class User(
    val id: Int,
    val username: String,
    val scope: String = "/",
    val locale: String = "en",
    val perm: Permissions = Permissions(),
    val lockPassword: Boolean = false,
    val viewMode: String = "list",
    val singleClick: Boolean = false,
    val hideDotfiles: Boolean = false,
    val dateFormat: Boolean = false,
)

/**
 * User permissions in Filebrowser.
 *
 * @property admin Can manage users and settings
 * @property execute Can execute commands
 * @property create Can create files and folders
 * @property rename Can rename files and folders
 * @property modify Can modify files
 * @property delete Can delete files and folders
 * @property share Can create share links
 * @property download Can download files
 */
@JsExport
@Serializable
public data class Permissions(
    val admin: Boolean = false,
    val execute: Boolean = false,
    val create: Boolean = true,
    val rename: Boolean = true,
    val modify: Boolean = true,
    val delete: Boolean = true,
    val share: Boolean = true,
    val download: Boolean = true,
)

/**
 * Login credentials for authentication.
 */
@Serializable
internal data class LoginRequest(
    val username: String,
    val password: String,
    val recaptcha: String = "",
)

/**
 * Request to create a new user.
 */
@Serializable
internal data class CreateUserRequest(
    val what: String = "user",
    val which: List<String> = emptyList(),
    val data: UserData,
)

/**
 * User data for create/update operations.
 */
@JsExport
@Serializable
public data class UserData(
    val username: String,
    val password: String,
    val scope: String = "/",
    val locale: String = "en",
    val perm: Permissions = Permissions(),
)

/**
 * Action request for copy/move operations.
 */
@Serializable
internal data class ActionRequest(
    val action: String,
    val destination: String,
    val override: Boolean = false,
    val rename: Boolean = false,
)

/**
 * A shareable link returned by Filebrowser's `POST /api/share/<path>` endpoint.
 *
 * The fields mirror `share.Link` in the Filebrowser backend. The public share
 * URL is constructed by the caller as `"$baseUrl/share/$hash"` — for
 * password-protected shares, append `?token=$token`.
 *
 * @property hash Randomly generated short identifier used in the share URL.
 * @property path Server-side path the share points at (what was shared).
 * @property userID ID of the user who created the share.
 * @property expire Expiry as a Unix epoch in seconds. `0` means no expiry.
 * @property passwordHash bcrypt hash of the share password, empty if unset.
 * @property token Secondary token required to download password-protected
 *   shares via query arg. Empty when no password is set.
 */
@JsExport
@Serializable
public data class Share(
    val hash: String = "",
    val path: String = "",
    val userID: Int = 0,
    val expire: Long = 0,
    @kotlinx.serialization.SerialName("password_hash")
    val passwordHash: String = "",
    val token: String = "",
)

/**
 * Request body for `POST /api/share/<path>`.
 *
 * Mirrors Filebrowser's `share.CreateBody`. All fields are optional; an empty
 * body creates a permanent, unprotected share link.
 *
 * @property password Optional password. When set, Filebrowser hashes it with
 *   bcrypt and also generates a [Share.token] for query-arg access.
 * @property expires Duration amount as a string (Filebrowser parses it with
 *   strconv.Atoi). Empty means "no expiry".
 * @property unit One of "seconds", "minutes", "hours" (default), "days".
 */
@Serializable
internal data class ShareCreateRequest(
    val password: String = "",
    val expires: String = "",
    val unit: String = "hours",
)

/**
 * Error returned by Filebrowser API.
 */
@JsExport
@Serializable
public data class FilebrowserError(
    val message: String = "Unknown error",
    val status: Int = 0,
)

/**
 * Exception thrown when Filebrowser API returns an error.
 *
 * @property statusCode HTTP status code
 * @property errorMessage Error message from API
 */
public class FilebrowserException(
    public val statusCode: Int,
    public val errorMessage: String,
) : Exception("Filebrowser error ($statusCode): $errorMessage")
