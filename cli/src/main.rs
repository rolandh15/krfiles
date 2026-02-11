//! # krfiles CLI
//!
//! A Filebrowser command-line client that calls into a **Kotlin Multiplatform**
//! shared library via C FFI. This demonstrates a real-world Kotlin/Native
//! integration where the business logic (HTTP client, auth, serialization)
//! lives in Kotlin and is consumed from Rust through a thin C shim.
//!
//! ## Architecture
//!
//! ```text
//! CLI (Rust + clap)
//!   -> ffi.rs (safe Rust wrappers)
//!     -> krfiles_shim.c (flattens Kotlin vtable)
//!       -> libkrfiles.so (Kotlin/Native shared library)
//!         -> Ktor HTTP client -> Filebrowser API
//! ```
//!
//! ## Modules
//!
//! - [`ffi`] — Safe wrappers around the C FFI boundary
//! - [`models`] — Serde models for deserializing JSON from Kotlin

// `mod` declares a module — tells Rust to look for ffi.rs and models.rs
// in the same directory. Like Kotlin's file-per-class but explicit.
mod ffi;
mod models;

use std::process;

use clap::{Parser, Subcommand};
use colored::Colorize;

use crate::models::Resource;

// ---------------------------------------------------------------------------
// CLI definition using clap's derive macros
// ---------------------------------------------------------------------------
//
// `#[derive(Parser)]` generates an argument parser from this struct at
// compile time. Each field becomes a CLI flag, and each enum variant
// in `Commands` becomes a subcommand.
//
// This is similar to Kotlin's kotlinx-cli but resolved at compile time
// rather than runtime — if your CLI definition has a typo, it won't compile.

/// krfiles — Filebrowser CLI powered by Kotlin Multiplatform
#[derive(Parser)]
#[command(name = "krfiles", version, about)]
struct Cli {
    /// Server URL (overrides stored default)
    #[arg(long, global = true)]
    server: Option<String>,

    /// Auth token (from a previous login). Alternatively set KRFILES_TOKEN env var.
    #[arg(long, global = true, env = "KRFILES_TOKEN")]
    token: Option<String>,

    #[command(subcommand)]
    command: Commands,
}

/// Each variant here becomes a subcommand: `krfiles login`, `krfiles ls`, etc.
///
/// In Rust, enums can hold data — unlike Java/Kotlin enums which are just
/// constants. Each variant here is like a different data class.
#[derive(Subcommand)]
enum Commands {
    /// Authenticate with a Filebrowser server
    Login {
        /// Server URL to authenticate with
        #[arg(short, long)]
        server: String,
        /// Username
        #[arg(short, long)]
        username: String,
        /// Password (will prompt if not provided)
        #[arg(short, long)]
        password: Option<String>,
    },

    /// List directory contents
    Ls {
        /// Path to list (defaults to root)
        #[arg(default_value = "/")]
        path: String,
    },

    /// Get info about a file or directory
    Info {
        /// Path to inspect
        path: String,
    },

    /// Download a file
    Get {
        /// Remote file path
        remote_path: String,
        /// Local file path (defaults to filename from remote path)
        local_path: Option<String>,
    },

    /// Upload a file
    Put {
        /// Local file path
        local_path: String,
        /// Remote file path
        remote_path: String,
        /// Overwrite if exists
        #[arg(short = 'f', long)]
        force: bool,
    },

    /// Delete a file or directory
    Rm {
        /// Path to delete
        path: String,
    },

    /// Rename/move a file or directory
    Mv {
        /// Source path
        source: String,
        /// Destination path
        destination: String,
        /// Overwrite if destination exists
        #[arg(short = 'f', long)]
        force: bool,
    },

    /// Copy a file or directory
    Cp {
        /// Source path
        source: String,
        /// Destination path
        destination: String,
        /// Overwrite if destination exists
        #[arg(short = 'f', long)]
        force: bool,
    },

    /// Create a directory
    Mkdir {
        /// Path for the new directory
        path: String,
    },

    /// Search for files
    Search {
        /// Search query
        query: String,
        /// Path to search within
        #[arg(default_value = "/")]
        path: String,
    },
}

// ---------------------------------------------------------------------------
// Main — the #[tokio::main] macro sets up the async runtime
// ---------------------------------------------------------------------------
//
// Tokio is Rust's most popular async runtime. The macro transforms:
//   async fn main() { ... }
// Into:
//   fn main() { tokio::runtime::Runtime::new().block_on(async { ... }) }
//
// We use async here so we CAN use spawn_blocking for the FFI calls,
// though for a simple CLI doing one operation at a time, it's mostly
// for consistency and future-proofing (e.g., progress indicators).

#[tokio::main]
async fn main() {
    // Parse CLI arguments. If invalid, clap prints help and exits.
    let cli = Cli::parse();

    // Run the command, print errors nicely
    if let Err(e) = run(cli).await {
        eprintln!("{} {e}", "error:".red().bold());
        // Clean up Kotlin resources before exiting
        ffi::destroy_client();
        process::exit(1);
    }

    // Always clean up — this closes the Ktor HttpClient inside Kotlin
    ffi::destroy_client();
}

/// Dispatch to the right command handler.
///
/// Returns Result<(), String> — Ok means success, Err holds error message.
/// The `?` operator is Rust's version of Kotlin's `.getOrThrow()`:
/// if the Result is Err, it immediately returns from the function with that error.
async fn run(cli: Cli) -> Result<(), String> {
    // For commands that need auth, we need a server URL.
    // Login provides its own, others use --server flag or could use stored default.
    match cli.command {
        Commands::Login {
            server,
            username,
            password,
        } => cmd_login(&server, &username, password).await,

        // All other commands need an authenticated client
        _ => {
            let server = cli
                .server
                .ok_or("No server specified. Use --server URL or login first.")?;

            // Initialize the Kotlin client
            ffi::create_client(&server);

            // Authenticate with stored token if available
            let token = cli.token.ok_or(
                "No token provided. Use --token, set KRFILES_TOKEN, or run `krfiles login` first.",
            )?;
            ffi::set_token(&token);

            match cli.command {
                Commands::Ls { path } => cmd_ls(&path).await,
                Commands::Info { path } => cmd_info(&path).await,
                Commands::Get {
                    remote_path,
                    local_path,
                } => cmd_get(&remote_path, local_path).await,
                Commands::Put {
                    local_path,
                    remote_path,
                    force,
                } => cmd_put(&local_path, &remote_path, force).await,
                Commands::Rm { path } => cmd_rm(&path).await,
                Commands::Mv {
                    source,
                    destination,
                    force,
                } => cmd_mv(&source, &destination, force).await,
                Commands::Cp {
                    source,
                    destination,
                    force,
                } => cmd_cp(&source, &destination, force).await,
                Commands::Mkdir { path } => cmd_mkdir(&path).await,
                Commands::Search { query, path } => cmd_search(&query, &path).await,
                Commands::Login { .. } => unreachable!(),
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Command implementations
// ---------------------------------------------------------------------------
//
// Each command follows the same pattern:
// 1. Call ffi:: functions (which call Kotlin via the C shim)
// 2. For JSON responses, deserialize with serde into our model structs
// 3. Format and print the output

async fn cmd_login(server: &str, username: &str, password: Option<String>) -> Result<(), String> {
    // If no password provided, prompt for it
    let password = match password {
        Some(p) => p,
        None => {
            // eprint! writes to stderr so it doesn't interfere with piped output
            eprint!("Password: ");
            // Read password without echoing — like sudo does.
            // This is a simple sync call, no need for spawn_blocking.
            rpassword::read_password().map_err(|e| format!("Failed to read password: {e}"))?
        }
    };

    ffi::create_client(server);

    // spawn_blocking moves this closure to a thread pool thread,
    // because ffi::login blocks (Kotlin's runBlocking is running underneath).
    // The .await waits for that thread to finish.
    let token = tokio::task::spawn_blocking({
        // We need to clone these strings because the closure takes ownership.
        // In Rust, each value has exactly one owner — to give the closure its
        // own copy, we clone. (Kotlin doesn't have this; it just reference-counts.)
        let username = username.to_owned();
        let password = password.clone();
        // `move` means: take ownership of username and password into the closure.
        // Without it, the closure would try to borrow them, but the closure
        // outlives this function (it runs on another thread), so borrowing
        // wouldn't be safe — the compiler would reject it.
        move || ffi::login(&username, &password)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;
    // ^^ Two ?? because spawn_blocking returns Result<Result<String, String>, JoinError>
    //    First ? unwraps the JoinError, second ? unwraps our ffi::login Result.

    println!("{} Logged in to {server}", "✓".green().bold());
    println!("  Token: {}...", &token[..20.min(token.len())]);
    Ok(())
}

async fn cmd_ls(path: &str) -> Result<(), String> {
    let json = tokio::task::spawn_blocking({
        let path = path.to_owned();
        move || ffi::list_directory(&path)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    // Deserialize the JSON into our Resource struct.
    // serde_json::from_str parses the string and fills in the struct fields.
    // Fields marked #[serde(default)] get their default if missing from JSON.
    let resource: Resource =
        serde_json::from_str(&json).map_err(|e| format!("Failed to parse response: {e}"))?;

    // Print directory listing
    if let Some(items) = &resource.items {
        if items.is_empty() {
            println!("(empty directory)");
        } else {
            for item in items {
                print_resource_line(item);
            }
            println!(
                "\n{} files, {} directories",
                resource.num_files, resource.num_dirs
            );
        }
    } else {
        println!("(not a directory)");
    }

    Ok(())
}

async fn cmd_info(path: &str) -> Result<(), String> {
    let json = tokio::task::spawn_blocking({
        let path = path.to_owned();
        move || ffi::get_resource(&path)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    let resource: Resource =
        serde_json::from_str(&json).map_err(|e| format!("Failed to parse response: {e}"))?;

    println!("  Name: {}", resource.name);
    println!("  Path: {}", resource.path);
    println!(
        "  Type: {}",
        if resource.is_dir { "directory" } else { "file" }
    );
    println!("  Size: {}", format_size(resource.size));
    println!("  Modified: {}", resource.modified);
    if resource.is_dir {
        println!("  Files: {}", resource.num_files);
        println!("  Dirs:  {}", resource.num_dirs);
    }

    Ok(())
}

async fn cmd_get(remote_path: &str, local_path: Option<String>) -> Result<(), String> {
    // Default local filename: last segment of the remote path.
    // rsplit('/') splits from the right and takes the first piece = filename.
    let local = local_path.unwrap_or_else(|| {
        remote_path
            .rsplit('/')
            .next()
            .unwrap_or("download")
            .to_string()
    });

    tokio::task::spawn_blocking({
        let remote = remote_path.to_owned();
        let local = local.clone();
        move || ffi::download_to_file(&remote, &local)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!("{} Downloaded {remote_path} → {local}", "✓".green().bold());
    Ok(())
}

async fn cmd_put(local_path: &str, remote_path: &str, force: bool) -> Result<(), String> {
    tokio::task::spawn_blocking({
        let remote = remote_path.to_owned();
        let local = local_path.to_owned();
        move || ffi::upload_from_file(&remote, &local, force)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!(
        "{} Uploaded {local_path} → {remote_path}",
        "✓".green().bold()
    );
    Ok(())
}

async fn cmd_rm(path: &str) -> Result<(), String> {
    tokio::task::spawn_blocking({
        let path = path.to_owned();
        move || ffi::delete(&path)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!("{} Deleted {path}", "✓".green().bold());
    Ok(())
}

async fn cmd_mv(source: &str, dest: &str, force: bool) -> Result<(), String> {
    tokio::task::spawn_blocking({
        let s = source.to_owned();
        let d = dest.to_owned();
        move || ffi::rename(&s, &d, force)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!("{} Moved {source} → {dest}", "✓".green().bold());
    Ok(())
}

async fn cmd_cp(source: &str, dest: &str, force: bool) -> Result<(), String> {
    tokio::task::spawn_blocking({
        let s = source.to_owned();
        let d = dest.to_owned();
        move || ffi::copy(&s, &d, force)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!("{} Copied {source} → {dest}", "✓".green().bold());
    Ok(())
}

async fn cmd_mkdir(path: &str) -> Result<(), String> {
    tokio::task::spawn_blocking({
        let path = path.to_owned();
        move || ffi::create_directory(&path)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    println!("{} Created directory {path}", "✓".green().bold());
    Ok(())
}

async fn cmd_search(query: &str, path: &str) -> Result<(), String> {
    let json = tokio::task::spawn_blocking({
        let q = query.to_owned();
        let p = path.to_owned();
        move || ffi::search(&q, &p)
    })
    .await
    .map_err(|e| format!("Task failed: {e}"))??;

    let results: Vec<models::SearchResult> =
        serde_json::from_str(&json).map_err(|e| format!("Failed to parse response: {e}"))?;

    if results.is_empty() {
        println!("No results found for '{query}'");
    } else {
        for result in &results {
            let kind = if result.dir { "dir " } else { "file" };
            println!("  [{kind}] {}", result.path);
        }
        println!("\n{} result(s)", results.len());
    }

    Ok(())
}

// ---------------------------------------------------------------------------
// Display helpers
// ---------------------------------------------------------------------------

/// Print a single resource as a line in a directory listing.
fn print_resource_line(resource: &Resource) {
    if resource.is_dir {
        // Directories shown in blue + bold, like `ls --color`
        println!("  {}/", resource.name.blue().bold());
    } else {
        let size = format_size(resource.size);
        println!("  {:<40} {:>10}", resource.name, size.dimmed());
    }
}

/// Format bytes into human-readable size (like `ls -lh`).
fn format_size(bytes: f64) -> String {
    const KB: f64 = 1024.0;
    const MB: f64 = KB * 1024.0;
    const GB: f64 = MB * 1024.0;

    if bytes < KB {
        format!("{bytes:.0} B")
    } else if bytes < MB {
        format!("{:.1} KB", bytes / KB)
    } else if bytes < GB {
        format!("{:.1} MB", bytes / MB)
    } else {
        format!("{:.2} GB", bytes / GB)
    }
}
