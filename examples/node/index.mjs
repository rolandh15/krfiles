/**
 * krfiles Node.js Example
 *
 * Demonstrates using the krfiles Kotlin Multiplatform library from Node.js.
 *
 * Usage:
 *   1. Build the library: npm run build-lib
 *   2. Install deps:      npm install
 *   3. Run:               FILEBROWSER_URL=https://your-server \
 *                          FILEBROWSER_USERNAME=admin \
 *                          FILEBROWSER_PASSWORD=admin \
 *                          npm start
 */

// Import the krfiles library
import krfiles from "krfiles";
const { JsFilebrowserClient, Resource } = krfiles.dev.rolandh.krfiles;

const url = process.env.FILEBROWSER_URL;
const username = process.env.FILEBROWSER_USERNAME;
const password = process.env.FILEBROWSER_PASSWORD;

if (!url || !username || !password) {
  console.error(
    "Please set FILEBROWSER_URL, FILEBROWSER_USERNAME, and FILEBROWSER_PASSWORD"
  );
  process.exit(1);
}

async function main() {
  const client = new JsFilebrowserClient(url);

  try {
    // Login
    console.log(`Connecting to ${url}...`);
    const token = await client.login(username, password);
    console.log(`Authenticated (token: ${token.substring(0, 20)}...)\n`);

    // List root directory
    console.log("Root directory listing:");
    console.log("─".repeat(60));
    const root = await client.listDirectory("/");

    const items = root.items?.asJsReadonlyArrayView() ?? [];
    for (const item of items) {
      const type = item.isDir ? "DIR " : "FILE";
      const size = item.isDir ? "" : ` (${item.size} bytes)`;
      console.log(`  [${type}] ${item.name}${size}`);
    }
    console.log(`\n  ${root.numDirs} directories, ${root.numFiles} files\n`);

    // Upload a test file
    // Note: Kotlin ByteArray maps to Int8Array in JS
    const testPath = "/krfiles-node-example.txt";
    const text = "Hello from krfiles Node.js!";
    const content = new Int8Array(new TextEncoder().encode(text).buffer);
    console.log(`Uploading ${testPath}...`);
    await client.upload(testPath, content);
    console.log("Upload complete.");

    // Download it back
    console.log(`Downloading ${testPath}...`);
    const downloaded = await client.download(testPath);
    const decoded = new TextDecoder().decode(new Uint8Array(downloaded.buffer));
    console.log(`Downloaded: "${decoded}"\n`);

    // Clean up
    console.log(`Cleaning up ${testPath}...`);
    await client.delete(testPath);
    console.log("Done!");
  } catch (err) {
    console.error("Error:", err.message || err);
  } finally {
    client.close();
  }
}

main();
