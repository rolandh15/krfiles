//! Serde models for JSON responses from the Kotlin/Native FFI bridge.
//!
//! These structs mirror the Kotlin `Resource` and search result types, but only
//! include the fields needed for CLI display. Fields absent from the JSON are
//! handled via `#[serde(default)]` — serde silently ignores unknown keys.
//!
//! ## Why `f64` for sizes?
//!
//! Kotlin's `Long` (64-bit integer) has no lossless equivalent in JavaScript's
//! `Number` type (which is IEEE 754 `f64`). Since the Kotlin library targets
//! JS as well, `Resource.size` uses `Double` on the Kotlin side for cross-platform
//! consistency, and we match that here with `f64`.

use serde::Deserialize;

/// A file or directory from the Filebrowser API.
///
/// Returned by [`crate::ffi::get_resource`] and [`crate::ffi::list_directory`].
/// For directory listings, `items` contains child resources and `num_files`/`num_dirs`
/// provide summary counts.
#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct Resource {
    /// Filename or directory name (without path).
    pub name: String,
    /// File size in bytes. Uses `f64` for JS compatibility (see module docs).
    pub size: f64,
    /// File extension (e.g. `"txt"`, `"png"`), empty string for directories.
    #[allow(dead_code)]
    pub extension: String,
    /// Whether this resource is a directory.
    #[serde(default)]
    pub is_dir: bool,
    /// Full path on the Filebrowser server (e.g. `"/documents/report.pdf"`).
    pub path: String,
    /// ISO 8601 timestamp of last modification.
    pub modified: String,
    /// Child resources, present only when listing a directory's contents.
    #[serde(default)]
    pub items: Option<Vec<Resource>>,
    /// Number of files in this directory (0 for files).
    #[serde(default)]
    pub num_dirs: i32,
    /// Number of subdirectories in this directory (0 for files).
    #[serde(default)]
    pub num_files: i32,
}

/// A single result from the Filebrowser search API.
///
/// Returned as a `Vec<SearchResult>` by [`crate::ffi::search`].
#[derive(Deserialize, Debug)]
pub struct SearchResult {
    /// Full path of the matching file or directory.
    pub path: String,
    /// Whether this result is a directory.
    #[serde(default)]
    pub dir: bool,
}
