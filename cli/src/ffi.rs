/// FFI bridge to the Kotlin/Native shared library.
///
/// The C shim (native/krfiles_shim.c) wraps the Kotlin vtable and exposes
/// flat function names. This module declares those functions and provides
/// a safe Rust API around them.
///
/// ## How it works
///
/// ```text
/// Rust (this module)
///   → calls extern "C" krfiles_login(...)
///     → C shim forwards to symbols->kotlin.root...nativeLogin(...)
///       → Kotlin/Native uses runBlocking to call suspend FilebrowserClient.login()
///         → Ktor makes the HTTP request
/// ```
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

// ---------------------------------------------------------------------------
// extern "C" declarations — these match the functions in krfiles_shim.c
// ---------------------------------------------------------------------------
//
// `extern "C"` tells Rust: "these functions exist somewhere else, compiled
// with C calling conventions." The linker connects them at build time.
//
// Every call is `unsafe` because Rust can't verify what C code does —
// it could dereference null, corrupt memory, etc. Our safe wrapper below
// handles the unsafe boundary.

unsafe extern "C" {
    fn krfiles_create_client(base_url: *const c_char);
    fn krfiles_destroy_client();
    fn krfiles_get_last_error() -> *const c_char;

    fn krfiles_login(username: *const c_char, password: *const c_char) -> *const c_char;
    fn krfiles_set_token(token: *const c_char) -> bool;
    #[allow(dead_code)]
    fn krfiles_logout() -> bool;
    #[allow(dead_code)]
    fn krfiles_is_authenticated() -> bool;

    fn krfiles_get_resource(path: *const c_char) -> *const c_char;
    fn krfiles_list_directory(path: *const c_char) -> *const c_char;
    fn krfiles_search(query: *const c_char, path: *const c_char) -> *const c_char;

    fn krfiles_download_to_file(remote_path: *const c_char, local_path: *const c_char) -> bool;
    fn krfiles_upload_from_file(
        remote_path: *const c_char,
        local_path: *const c_char,
        override_: bool,
    ) -> bool;
    fn krfiles_create_directory(path: *const c_char) -> bool;
    fn krfiles_delete(path: *const c_char) -> bool;
    fn krfiles_rename(source: *const c_char, dest: *const c_char, override_: bool) -> bool;
    fn krfiles_copy(source: *const c_char, dest: *const c_char, override_: bool) -> bool;
}

// ---------------------------------------------------------------------------
// Safe public API
// ---------------------------------------------------------------------------
//
// These functions handle:
// 1. Converting Rust &str → C strings (CString adds a null terminator)
// 2. Calling the unsafe extern functions
// 3. Converting C string results back to Rust Strings
// 4. Null checks → Result errors with the last Kotlin error message

/// Initialize the Kotlin client for a server URL.
pub fn create_client(base_url: &str) {
    let url = CString::new(base_url).unwrap();
    unsafe { krfiles_create_client(url.as_ptr()) }
}

/// Clean up the Kotlin client.
pub fn destroy_client() {
    unsafe { krfiles_destroy_client() }
}

/// Get the last error from the Kotlin side.
pub fn last_error() -> String {
    unsafe {
        let ptr = krfiles_get_last_error();
        if ptr.is_null() {
            "Unknown error".to_string()
        } else {
            // CStr::from_ptr reads bytes until it hits a null terminator.
            // to_string_lossy handles any non-UTF8 bytes gracefully.
            CStr::from_ptr(ptr).to_string_lossy().into_owned()
        }
    }
}

/// Authenticate with username/password. Returns the auth token.
pub fn login(username: &str, password: &str) -> Result<String, String> {
    let u = CString::new(username).unwrap();
    let p = CString::new(password).unwrap();
    unsafe { nullable_str_to_result(krfiles_login(u.as_ptr(), p.as_ptr())) }
}

/// Set auth token directly (e.g. from stored credentials).
pub fn set_token(token: &str) -> bool {
    let t = CString::new(token).unwrap();
    unsafe { krfiles_set_token(t.as_ptr()) }
}

/// Log out and clear the token.
#[allow(dead_code)]
pub fn logout() -> bool {
    unsafe { krfiles_logout() }
}

/// Check if the client has a valid auth token.
#[allow(dead_code)]
pub fn is_authenticated() -> bool {
    unsafe { krfiles_is_authenticated() }
}

/// Get resource info as a JSON string.
pub fn get_resource(path: &str) -> Result<String, String> {
    let p = CString::new(path).unwrap();
    unsafe { nullable_str_to_result(krfiles_get_resource(p.as_ptr())) }
}

/// List directory contents as a JSON string.
pub fn list_directory(path: &str) -> Result<String, String> {
    let p = CString::new(path).unwrap();
    unsafe { nullable_str_to_result(krfiles_list_directory(p.as_ptr())) }
}

/// Search for files. Returns JSON array of results.
pub fn search(query: &str, path: &str) -> Result<String, String> {
    let q = CString::new(query).unwrap();
    let p = CString::new(path).unwrap();
    unsafe { nullable_str_to_result(krfiles_search(q.as_ptr(), p.as_ptr())) }
}

/// Download a remote file to a local path.
pub fn download_to_file(remote_path: &str, local_path: &str) -> Result<(), String> {
    let r = CString::new(remote_path).unwrap();
    let l = CString::new(local_path).unwrap();
    bool_to_result(unsafe { krfiles_download_to_file(r.as_ptr(), l.as_ptr()) })
}

/// Upload a local file to a remote path.
pub fn upload_from_file(
    remote_path: &str,
    local_path: &str,
    overwrite: bool,
) -> Result<(), String> {
    let r = CString::new(remote_path).unwrap();
    let l = CString::new(local_path).unwrap();
    bool_to_result(unsafe { krfiles_upload_from_file(r.as_ptr(), l.as_ptr(), overwrite) })
}

/// Create a remote directory.
pub fn create_directory(path: &str) -> Result<(), String> {
    let p = CString::new(path).unwrap();
    bool_to_result(unsafe { krfiles_create_directory(p.as_ptr()) })
}

/// Delete a remote file or directory.
pub fn delete(path: &str) -> Result<(), String> {
    let p = CString::new(path).unwrap();
    bool_to_result(unsafe { krfiles_delete(p.as_ptr()) })
}

/// Rename/move a remote file or directory.
pub fn rename(source: &str, dest: &str, overwrite: bool) -> Result<(), String> {
    let s = CString::new(source).unwrap();
    let d = CString::new(dest).unwrap();
    bool_to_result(unsafe { krfiles_rename(s.as_ptr(), d.as_ptr(), overwrite) })
}

/// Copy a remote file or directory.
pub fn copy(source: &str, dest: &str, overwrite: bool) -> Result<(), String> {
    let s = CString::new(source).unwrap();
    let d = CString::new(dest).unwrap();
    bool_to_result(unsafe { krfiles_copy(s.as_ptr(), d.as_ptr(), overwrite) })
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/// Convert a nullable C string to Result. Null means error → check last_error().
unsafe fn nullable_str_to_result(ptr: *const c_char) -> Result<String, String> {
    if ptr.is_null() {
        Err(last_error())
    } else {
        // unsafe block required even inside unsafe fn (Rust 2024 edition)
        Ok(unsafe { CStr::from_ptr(ptr) }
            .to_string_lossy()
            .into_owned())
    }
}

/// Convert a bool return to Result. False means error → check last_error().
fn bool_to_result(ok: bool) -> Result<(), String> {
    if ok { Ok(()) } else { Err(last_error()) }
}
