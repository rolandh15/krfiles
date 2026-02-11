package dev.rolandh.krfiles

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FilebrowserClientTest {
    private val testJson =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun authenticatedClient(
        handler: (
            url: String,
            method: HttpMethod,
            headers: Map<String, String>,
            params: Map<String, String>,
        ) -> Pair<String, HttpStatusCode>,
    ): FilebrowserClient {
        var requestCount = 0
        val engine =
            MockEngine { request ->
                requestCount++
                if (requestCount == 1) {
                    respond("test-token", HttpStatusCode.OK)
                } else {
                    val headerMap = request.headers.entries().associate { it.key to it.value.first() }
                    val paramMap =
                        request.url.parameters
                            .entries()
                            .associate { it.key to it.value.first() }
                    val (body, status) = handler(request.url.encodedPath, request.method, headerMap, paramMap)
                    if (body.startsWith("[") || body.startsWith("{")) {
                        respond(body, status, jsonHeaders)
                    } else {
                        respond(body, status)
                    }
                }
            }
        val httpClient =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(testJson)
                }
            }
        return FilebrowserClient("http://mock", httpClient)
    }

    private fun unauthenticatedClient(
        responseBody: String = "",
        status: HttpStatusCode = HttpStatusCode.OK,
    ): FilebrowserClient {
        val engine =
            MockEngine { _ ->
                respond(responseBody, status)
            }
        val httpClient =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(testJson)
                }
            }
        return FilebrowserClient("http://mock", httpClient)
    }

    // --- Login Tests ---

    @Test
    fun testLoginSuccess() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals("/api/login", request.url.encodedPath)
                    assertEquals(HttpMethod.Post, request.method)
                    respond("test-token-abc123", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(testJson) }
                }
            val client = FilebrowserClient("http://mock", httpClient)

            val result = client.login("admin", "password")

            assertTrue(result.isSuccess)
            assertEquals("test-token-abc123", result.getOrThrow())
            assertTrue(client.isAuthenticated)
            client.close()
        }

    @Test
    fun testLoginFailureInvalidCredentials() =
        runTest {
            val client = unauthenticatedClient("Forbidden", HttpStatusCode.Forbidden)

            val result = client.login("admin", "wrong")

            assertTrue(result.isFailure)
            assertFalse(client.isAuthenticated)
            client.close()
        }

    @Test
    fun testLogoutClearsAuth() =
        runTest {
            val client = unauthenticatedClient("test-token")

            client.login("admin", "password")
            assertTrue(client.isAuthenticated)

            client.logout()
            assertFalse(client.isAuthenticated)
            client.close()
        }

    // --- Resource / Directory Tests ---

    @Test
    fun testGetResourceSuccess() =
        runTest {
            val client =
                authenticatedClient { url, method, headers, _ ->
                    assertEquals("/api/resources/", url)
                    assertEquals(HttpMethod.Get, method)
                    assertEquals("test-token", headers["X-Auth"])
                    Pair(
                        """{"name":"","size":0,"isDir":true,"path":"/","numDirs":2,"numFiles":3}""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.getResource("/")

            assertTrue(result.isSuccess)
            val resource = result.getOrThrow()
            assertTrue(resource.isDir)
            assertEquals("/", resource.path)
            assertEquals(2, resource.numDirs)
            assertEquals(3, resource.numFiles)
            client.close()
        }

    @Test
    fun testGetResourceNotFound() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair("Not Found", HttpStatusCode.NotFound)
                }

            client.login("admin", "password")
            val result = client.getResource("/nonexistent")

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is FilebrowserException)
            assertEquals(404, exception.statusCode)
            client.close()
        }

    @Test
    fun testListDirectoryWithItems() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair(
                        """{
                    "name":"docs",
                    "size":0,
                    "isDir":true,
                    "path":"/docs",
                    "numDirs":1,
                    "numFiles":2,
                    "items":[
                        {"name":"readme.md","size":1024,"isDir":false,"path":"/docs/readme.md","type":"text"},
                        {"name":"images","size":0,"isDir":true,"path":"/docs/images"},
                        {"name":"notes.txt","size":512,"isDir":false,"path":"/docs/notes.txt","type":"text"}
                    ]
                }""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.listDirectory("/docs")

            assertTrue(result.isSuccess)
            val listing = result.getOrThrow()
            assertNotNull(listing.items)
            assertEquals(3, listing.items!!.size)
            assertEquals("readme.md", listing.items!![0].name)
            assertTrue(listing.items!![1].isDir)
            client.close()
        }

    // --- Download Tests ---

    @Test
    fun testDownloadFileSuccess() =
        runTest {
            val client =
                authenticatedClient { url, _, _, _ ->
                    assertTrue(url.contains("/api/raw/"))
                    Pair("file content here", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.download("/test.txt")

            assertTrue(result.isSuccess)
            assertEquals("file content here", result.getOrThrow().decodeToString())
            client.close()
        }

    @Test
    fun testDownloadNotFound() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair("Not Found", HttpStatusCode.NotFound)
                }

            client.login("admin", "password")
            val result = client.download("/missing.txt")

            assertTrue(result.isFailure)
            client.close()
        }

    // --- Upload Tests ---

    @Test
    fun testUploadFileSuccess() =
        runTest {
            val client =
                authenticatedClient { url, method, _, _ ->
                    assertEquals(HttpMethod.Post, method)
                    assertTrue(url.contains("/api/resources/"))
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.upload("/upload.txt", "hello world".encodeToByteArray())

            assertTrue(result.isSuccess)
            client.close()
        }

    @Test
    fun testUploadFailure() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair("Internal Server Error", HttpStatusCode.InternalServerError)
                }

            client.login("admin", "password")
            val result = client.upload("/upload.txt", "data".encodeToByteArray())

            assertTrue(result.isFailure)
            client.close()
        }

    // --- Delete Tests ---

    @Test
    fun testDeleteSuccess() =
        runTest {
            val client =
                authenticatedClient { _, method, _, _ ->
                    assertEquals(HttpMethod.Delete, method)
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.delete("/file.txt")

            assertTrue(result.isSuccess)
            client.close()
        }

    // --- Rename Tests ---

    @Test
    fun testRenameSuccess() =
        runTest {
            val client =
                authenticatedClient { _, method, _, params ->
                    assertEquals(HttpMethod.Patch, method)
                    assertEquals("rename", params["action"])
                    assertEquals("/new-name.txt", params["destination"])
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.rename("/old-name.txt", "/new-name.txt")

            assertTrue(result.isSuccess)
            client.close()
        }

    // --- Copy Tests ---

    @Test
    fun testCopySuccess() =
        runTest {
            val client =
                authenticatedClient { _, method, _, params ->
                    assertEquals(HttpMethod.Patch, method)
                    assertEquals("copy", params["action"])
                    assertEquals("/dest.txt", params["destination"])
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.copy("/source.txt", "/dest.txt")

            assertTrue(result.isSuccess)
            client.close()
        }

    // --- Create Directory Tests ---

    @Test
    fun testCreateDirectorySuccess() =
        runTest {
            val client =
                authenticatedClient { _, method, _, _ ->
                    assertEquals(HttpMethod.Post, method)
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.createDirectory("/new-dir")

            assertTrue(result.isSuccess)
            client.close()
        }

    // --- Search Tests ---

    @Test
    fun testSearchSuccess() =
        runTest {
            val client =
                authenticatedClient { _, _, _, params ->
                    assertEquals("test-query", params["query"])
                    Pair(
                        """[{"path":"/docs/test-file.txt","dir":false},{"path":"/test-dir","dir":true}]""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.search("test-query")

            assertTrue(result.isSuccess)
            val results = result.getOrThrow()
            assertEquals(2, results.size)
            assertEquals("/docs/test-file.txt", results[0].path)
            assertFalse(results[0].dir)
            assertTrue(results[1].dir)
            client.close()
        }

    // --- Completions Tests ---

    @Test
    fun testGetCompletions() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair(
                        """{
                    "name":"",
                    "size":0,
                    "isDir":true,
                    "path":"/",
                    "items":[
                        {"name":"documents","size":0,"isDir":true,"path":"/documents"},
                        {"name":"downloads","size":0,"isDir":true,"path":"/downloads"},
                        {"name":"readme.md","size":100,"isDir":false,"path":"/readme.md"}
                    ]
                }""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.getCompletions("/do")

            assertTrue(result.isSuccess)
            val completions = result.getOrThrow()
            assertEquals(2, completions.size)
            assertTrue(completions.all { it.startsWith("/do") })
            assertTrue(completions[0].endsWith("/"))
            client.close()
        }

    // --- User Management Tests ---

    @Test
    fun testListUsersSuccess() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair(
                        """[
                    {"id":1,"username":"admin","scope":"/","perm":{"admin":true}},
                    {"id":2,"username":"user1","scope":"/files","perm":{"admin":false}}
                ]""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.listUsers()

            assertTrue(result.isSuccess)
            val users = result.getOrThrow()
            assertEquals(2, users.size)
            assertEquals("admin", users[0].username)
            assertTrue(users[0].perm.admin)
            assertFalse(users[1].perm.admin)
            client.close()
        }

    @Test
    fun testGetUserSuccess() =
        runTest {
            val client =
                authenticatedClient { _, _, _, _ ->
                    Pair(
                        """{"id":1,"username":"admin","scope":"/","perm":{"admin":true}}""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            val result = client.getUser(1)

            assertTrue(result.isSuccess)
            assertEquals("admin", result.getOrThrow().username)
            client.close()
        }

    @Test
    fun testCreateUserSuccess() =
        runTest {
            val client =
                authenticatedClient { _, method, _, _ ->
                    assertEquals(HttpMethod.Post, method)
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.createUser(UserData(username = "newuser", password = "pass123"))

            assertTrue(result.isSuccess)
            client.close()
        }

    @Test
    fun testDeleteUserSuccess() =
        runTest {
            val client =
                authenticatedClient { url, method, _, _ ->
                    assertEquals(HttpMethod.Delete, method)
                    assertTrue(url.contains("/api/users/"))
                    Pair("", HttpStatusCode.OK)
                }

            client.login("admin", "password")
            val result = client.deleteUser(2)

            assertTrue(result.isSuccess)
            client.close()
        }

    // --- Auth Required Tests ---

    @Test
    fun testOperationWithoutLoginFails() =
        runTest {
            val client = unauthenticatedClient()

            val result = client.getResource("/")

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertTrue(error!!.message!!.contains("Not authenticated"))
            client.close()
        }

    // --- Auth Header Tests ---

    @Test
    fun testAuthHeaderSentWithRequests() =
        runTest {
            var capturedAuthHeader: String? = null
            val client =
                authenticatedClient { _, _, headers, _ ->
                    capturedAuthHeader = headers["X-Auth"]
                    Pair(
                        """{"name":"","isDir":true,"path":"/"}""",
                        HttpStatusCode.OK,
                    )
                }

            client.login("admin", "password")
            client.getResource("/")

            assertEquals("test-token", capturedAuthHeader)
            client.close()
        }

    // --- setToken Tests ---

    @Test
    fun testSetTokenAuthenticatesWithoutLogin() =
        runTest {
            var capturedAuthHeader: String? = null
            val engine =
                MockEngine { request ->
                    capturedAuthHeader = request.headers["X-Auth"]
                    respond(
                        """{"name":"","isDir":true,"path":"/"}""",
                        HttpStatusCode.OK,
                        jsonHeaders,
                    )
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(testJson) }
                }
            val client = FilebrowserClient("http://mock", httpClient)

            assertFalse(client.isAuthenticated)

            client.setToken("stored-token-xyz")
            assertTrue(client.isAuthenticated)

            val result = client.getResource("/")
            assertTrue(result.isSuccess)
            assertEquals("stored-token-xyz", capturedAuthHeader)
            client.close()
        }

    @Test
    fun testSetTokenOverridesPreviousLogin() =
        runTest {
            var capturedAuthHeader: String? = null
            var requestCount = 0
            val engine =
                MockEngine { request ->
                    requestCount++
                    if (requestCount == 1) {
                        respond("login-token", HttpStatusCode.OK)
                    } else {
                        capturedAuthHeader = request.headers["X-Auth"]
                        respond(
                            """{"name":"","isDir":true,"path":"/"}""",
                            HttpStatusCode.OK,
                            jsonHeaders,
                        )
                    }
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(testJson) }
                }
            val client = FilebrowserClient("http://mock", httpClient)

            client.login("admin", "password")
            client.setToken("override-token")

            client.getResource("/")
            assertEquals("override-token", capturedAuthHeader)
            client.close()
        }

    // --- Auth Flow (login + store) Tests ---

    @Test
    fun testLoginAndStoreCredentials() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond("fresh-token-123", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(testJson) }
                }
            val client = FilebrowserClient("http://mock", httpClient)
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val token = client.login("admin", "pass").getOrThrow()
            authManager.saveCredentials("http://mock", token)
            authManager.setDefaultServer("http://mock")

            // Verify stored
            assertEquals("fresh-token-123", authManager.getCredentials("http://mock"))
            assertEquals("http://mock", authManager.getDefaultServer())

            // Verify can create new client from stored creds
            val creds = authManager.getDefaultCredentials()!!
            val client2 = FilebrowserClient(creds.serverUrl)
            client2.setToken(creds.token)
            assertTrue(client2.isAuthenticated)

            client.close()
            client2.close()
        }
}
