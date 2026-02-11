package dev.rolandh.krfiles

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for FilebrowserClient.
 *
 * These tests run against a real Filebrowser instance.
 * Set environment variables:
 *   - FILEBROWSER_URL: Base URL (e.g., https://files.example.com)
 *   - FILEBROWSER_USERNAME: Username for auth
 *   - FILEBROWSER_PASSWORD: Password for auth
 */
class FilebrowserClientIntegrationTest {
    private val testUrl = getEnv("FILEBROWSER_URL") ?: "http://localhost:8080"
    private val testUsername = getEnv("FILEBROWSER_USERNAME") ?: "admin"
    private val testPassword = getEnv("FILEBROWSER_PASSWORD") ?: "admin"

    @Test
    fun testLoginReturnsToken() =
        runTest {
            val client = FilebrowserClient(testUrl)
            try {
                val result = client.login(testUsername, testPassword)

                assertTrue(result.isSuccess, "Login should succeed")
                val token = result.getOrThrow()
                assertTrue(token.isNotBlank(), "Token should not be blank")
            } finally {
                client.close()
            }
        }

    @Test
    fun testLoginWithInvalidCredentialsFails() =
        runTest {
            val client = FilebrowserClient(testUrl)
            try {
                val result = client.login("invalid", "wrongpassword")

                assertTrue(result.isFailure, "Login with invalid credentials should fail")
            } finally {
                client.close()
            }
        }

    @Test
    fun testListDirectoryRoot() =
        runTest {
            val client = FilebrowserClient(testUrl)
            try {
                client.login(testUsername, testPassword)

                val result = client.listDirectory("/")

                assertTrue(result.isSuccess, "List directory should succeed")
                val listing = result.getOrThrow()
                assertNotNull(listing, "Listing should not be null")
            } finally {
                client.close()
            }
        }

    @Test
    fun testListDirectoryNonExistent() =
        runTest {
            val client = FilebrowserClient(testUrl)
            try {
                client.login(testUsername, testPassword)

                val result = client.listDirectory("/this-path-does-not-exist-12345")

                assertTrue(result.isFailure, "Listing non-existent directory should fail")
            } finally {
                client.close()
            }
        }

    @Test
    fun testGetResourceInfo() =
        runTest {
            val client = FilebrowserClient(testUrl)
            try {
                client.login(testUsername, testPassword)

                val result = client.getResource("/")

                assertTrue(result.isSuccess, "Get resource should succeed")
                val resource = result.getOrThrow()
                assertTrue(resource.isDir, "Root should be a directory")
            } finally {
                client.close()
            }
        }

    @Test
    fun testUploadAndDownloadFile() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val testPath = "/krfiles-test-${System.currentTimeMillis()}.txt"
            try {
                client.login(testUsername, testPassword)

                val testContent = "Hello from krfiles test!"

                // Upload
                val uploadResult = client.upload(testPath, testContent.encodeToByteArray())
                assertTrue(uploadResult.isSuccess, "Upload should succeed")

                // Download and verify
                val downloadResult = client.download(testPath)
                assertTrue(downloadResult.isSuccess, "Download should succeed")
                val downloaded = downloadResult.getOrThrow().decodeToString()
                assertEquals(testContent, downloaded, "Content should match")
            } finally {
                // Always cleanup
                client.delete(testPath)
                client.close()
            }
        }

    @Test
    fun testCreateAndDeleteDirectory() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val testDir = "/krfiles-test-dir-${System.currentTimeMillis()}"
            try {
                client.login(testUsername, testPassword)

                // Create directory
                val createResult = client.createDirectory(testDir)
                assertTrue(createResult.isSuccess, "Create directory should succeed")

                // Verify it exists
                val resourceResult = client.getResource(testDir)
                assertTrue(resourceResult.isSuccess, "Directory should exist")
                assertTrue(resourceResult.getOrThrow().isDir, "Should be a directory")
            } finally {
                // Always cleanup
                client.delete(testDir)
                client.close()
            }
        }

    @Test
    fun testRenameFile() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val timestamp = System.currentTimeMillis()
            val originalPath = "/krfiles-rename-test-$timestamp.txt"
            val newPath = "/krfiles-renamed-$timestamp.txt"
            try {
                client.login(testUsername, testPassword)

                // Create file
                client.upload(originalPath, "test content".encodeToByteArray())

                // Rename
                val renameResult = client.rename(originalPath, newPath)
                assertTrue(renameResult.isSuccess, "Rename should succeed")

                // Verify old path doesn't exist
                val oldResult = client.getResource(originalPath)
                assertTrue(oldResult.isFailure, "Old path should not exist")

                // Verify new path exists
                val newResult = client.getResource(newPath)
                assertTrue(newResult.isSuccess, "New path should exist")
            } finally {
                // Cleanup both paths (one might not exist, that's ok)
                client.delete(originalPath)
                client.delete(newPath)
                client.close()
            }
        }

    @Test
    fun testCopyFile() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val timestamp = System.currentTimeMillis()
            val sourcePath = "/krfiles-copy-source-$timestamp.txt"
            val destPath = "/krfiles-copy-dest-$timestamp.txt"
            try {
                client.login(testUsername, testPassword)

                val content = "copy test content"

                // Create source
                client.upload(sourcePath, content.encodeToByteArray())

                // Copy
                val copyResult = client.copy(sourcePath, destPath)
                assertTrue(copyResult.isSuccess, "Copy should succeed")

                // Verify both exist
                assertTrue(client.getResource(sourcePath).isSuccess, "Source should still exist")
                assertTrue(client.getResource(destPath).isSuccess, "Destination should exist")

                // Verify content
                val destContent = client.download(destPath).getOrThrow().decodeToString()
                assertEquals(content, destContent, "Copied content should match")
            } finally {
                // Cleanup both files
                client.delete(sourcePath)
                client.delete(destPath)
                client.close()
            }
        }

    @Test
    fun testSearchFiles() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val timestamp = System.currentTimeMillis()
            val testPath = "/krfiles-searchable-$timestamp.txt"
            try {
                client.login(testUsername, testPassword)

                // Create a file to search for
                client.upload(testPath, "searchable content".encodeToByteArray())

                // Search for it
                val searchResult = client.search("searchable")
                assertTrue(searchResult.isSuccess, "Search should succeed")

                val results = searchResult.getOrThrow()
                assertTrue(results.any { it.path.contains("searchable") }, "Should find the file")
            } finally {
                client.delete(testPath)
                client.close()
            }
        }

    @Test
    fun testGetCompletions() =
        runTest {
            val client = FilebrowserClient(testUrl)
            val timestamp = System.currentTimeMillis()
            val testDir = "/krfiles-complete-$timestamp"
            val testFile = "$testDir/testfile.txt"
            try {
                client.login(testUsername, testPassword)

                // Create test directory and file
                client.createDirectory(testDir)
                client.upload(testFile, "test".encodeToByteArray())

                // Test completions
                val completions = client.getCompletions("/krfiles-complete")
                assertTrue(completions.isSuccess, "Completions should succeed")

                val paths = completions.getOrThrow()
                assertTrue(paths.any { it.startsWith("/krfiles-complete") }, "Should find completion")
            } finally {
                client.delete(testFile)
                client.delete(testDir)
                client.close()
            }
        }
}

/**
 * Platform-specific environment variable access.
 */
internal expect fun getEnv(name: String): String?

/**
 * Platform-specific current time (for test uniqueness).
 */
internal expect val System: SystemTime

internal interface SystemTime {
    fun currentTimeMillis(): Long
}
