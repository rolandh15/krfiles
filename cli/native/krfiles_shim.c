/**
 * Thin C shim over the Kotlin/Native vtable.
 *
 * Kotlin/Native exports all symbols through a single vtable struct accessed
 * via libkrfiles_symbols(). This shim provides flat C functions that Rust
 * can call directly with extern "C", avoiding the need to replicate the
 * deeply nested vtable layout in Rust.
 *
 * Each function here simply forwards to the corresponding vtable entry.
 */

#include "libkrfiles_api.h"
#include <stdbool.h>
#include <stddef.h>

/* Cached pointer to the vtable — initialized on first use. */
static libkrfiles_ExportedSymbols* sym = NULL;

static void ensure_init(void) {
    if (!sym) {
        sym = libkrfiles_symbols();
    }
}

/* Shorthand for the deeply nested path to our functions. */
#define KR sym->kotlin.root.dev.rolandh.krfiles

/* --- Lifecycle --- */

void krfiles_create_client(const char* base_url) {
    ensure_init();
    KR.nativeCreateClient(base_url);
}

void krfiles_destroy_client(void) {
    ensure_init();
    KR.nativeDestroyClient();
}

const char* krfiles_get_last_error(void) {
    ensure_init();
    return KR.nativeGetLastError();
}

/* --- Auth --- */

const char* krfiles_login(const char* username, const char* password) {
    ensure_init();
    return KR.nativeLogin(username, password);
}

bool krfiles_set_token(const char* token) {
    ensure_init();
    return KR.nativeSetToken(token);
}

bool krfiles_logout(void) {
    ensure_init();
    return KR.nativeLogout();
}

bool krfiles_is_authenticated(void) {
    ensure_init();
    return KR.nativeIsAuthenticated();
}

/* --- Resources (return JSON strings) --- */

const char* krfiles_get_resource(const char* path) {
    ensure_init();
    return KR.nativeGetResource(path);
}

const char* krfiles_list_directory(const char* path) {
    ensure_init();
    return KR.nativeListDirectory(path);
}

const char* krfiles_search(const char* query, const char* path) {
    ensure_init();
    return KR.nativeSearch(query, path);
}

/* --- File operations --- */

bool krfiles_download_to_file(const char* remote_path, const char* local_path) {
    ensure_init();
    return KR.nativeDownloadToFile(remote_path, local_path);
}

bool krfiles_upload_from_file(const char* remote_path, const char* local_path, bool override_) {
    ensure_init();
    return KR.nativeUploadFromFile(remote_path, local_path, override_);
}

bool krfiles_create_directory(const char* path) {
    ensure_init();
    return KR.nativeCreateDirectory(path);
}

bool krfiles_delete(const char* path) {
    ensure_init();
    return KR.nativeDelete(path);
}

bool krfiles_rename(const char* source, const char* destination, bool override_) {
    ensure_init();
    return KR.nativeRename(source, destination, override_);
}

bool krfiles_copy(const char* source, const char* destination, bool override_) {
    ensure_init();
    return KR.nativeCopy(source, destination, override_);
}
