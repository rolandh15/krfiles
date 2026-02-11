package dev.rolandh.krfiles

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for AuthManager - credential storage across platforms.
 *
 * Uses in-memory storage for testing to avoid platform-specific file I/O.
 */
class AuthManagerTest {
    @Test
    fun testSaveAndRetrieveCredentials() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val serverUrl = "https://files.example.com"
            val token = "test-token-12345"

            // Save credentials
            authManager.saveCredentials(serverUrl, token)

            // Retrieve credentials
            val retrieved = authManager.getCredentials(serverUrl)
            assertNotNull(retrieved)
            assertEquals(token, retrieved)
        }

    @Test
    fun testGetCredentialsForUnknownServer() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val retrieved = authManager.getCredentials("https://unknown.example.com")
            assertNull(retrieved)
        }

    @Test
    fun testRemoveCredentials() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val serverUrl = "https://files.example.com"
            val token = "test-token-12345"

            // Save then remove
            authManager.saveCredentials(serverUrl, token)
            authManager.removeCredentials(serverUrl)

            // Should be gone
            val retrieved = authManager.getCredentials(serverUrl)
            assertNull(retrieved)
        }

    @Test
    fun testMultipleServers() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val server1 = "https://files1.example.com"
            val server2 = "https://files2.example.com"
            val token1 = "token-1"
            val token2 = "token-2"

            authManager.saveCredentials(server1, token1)
            authManager.saveCredentials(server2, token2)

            assertEquals(token1, authManager.getCredentials(server1))
            assertEquals(token2, authManager.getCredentials(server2))
        }

    @Test
    fun testListServers() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val server1 = "https://files1.example.com"
            val server2 = "https://files2.example.com"

            authManager.saveCredentials(server1, "token1")
            authManager.saveCredentials(server2, "token2")

            val servers = authManager.listServers()
            assertEquals(2, servers.size)
            assertTrue(servers.contains(server1))
            assertTrue(servers.contains(server2))
        }

    @Test
    fun testSetAndGetDefaultServer() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val serverUrl = "https://files.example.com"
            authManager.saveCredentials(serverUrl, "token")
            authManager.setDefaultServer(serverUrl)

            assertEquals(serverUrl, authManager.getDefaultServer())
        }

    @Test
    fun testGetDefaultCredentials() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            val serverUrl = "https://files.example.com"
            val token = "default-token"

            authManager.saveCredentials(serverUrl, token)
            authManager.setDefaultServer(serverUrl)

            val credentials = authManager.getDefaultCredentials()
            assertNotNull(credentials)
            assertEquals(serverUrl, credentials.serverUrl)
            assertEquals(token, credentials.token)
        }

    @Test
    fun testIsAuthenticated() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            assertFalse(authManager.isAuthenticated("https://files.example.com"))

            authManager.saveCredentials("https://files.example.com", "token")

            assertTrue(authManager.isAuthenticated("https://files.example.com"))
        }

    @Test
    fun testClearAll() =
        runTest {
            val storage = InMemoryAuthStorage()
            val authManager = AuthManager(storage)

            authManager.saveCredentials("https://server1.com", "token1")
            authManager.saveCredentials("https://server2.com", "token2")
            authManager.setDefaultServer("https://server1.com")

            authManager.clearAll()

            assertTrue(authManager.listServers().isEmpty())
            assertNull(authManager.getDefaultServer())
        }
}

/**
 * In-memory implementation of AuthStorage for testing.
 */
class InMemoryAuthStorage : AuthStorage {
    private val credentials = mutableMapOf<String, String>()
    private var defaultServer: String? = null

    override suspend fun saveToken(
        serverUrl: String,
        token: String,
    ) {
        credentials[serverUrl] = token
    }

    override suspend fun getToken(serverUrl: String): String? = credentials[serverUrl]

    override suspend fun removeToken(serverUrl: String) {
        credentials.remove(serverUrl)
    }

    override suspend fun listServerUrls(): List<String> = credentials.keys.toList()

    override suspend fun setDefaultServerUrl(serverUrl: String?) {
        defaultServer = serverUrl
    }

    override suspend fun getDefaultServerUrl(): String? = defaultServer

    override suspend fun clear() {
        credentials.clear()
        defaultServer = null
    }
}
