/// Build script — runs before Rust compilation.
///
/// This does two things:
/// 1. Compiles the C shim (krfiles_shim.c) that wraps the Kotlin vtable
/// 2. Tells the linker where to find libkrfiles.so (the Kotlin shared library)
fn main() {
    let native_dir = std::path::Path::new("native");
    let krfiles_lib_dir = std::path::Path::new("../lib/build/bin/linuxX64/krfilesReleaseShared");

    // Step 1: Compile the C shim.
    // The `cc` crate invokes the system C compiler (gcc/clang) to compile
    // krfiles_shim.c into a static library that gets linked into our binary.
    // The shim #includes the Kotlin header, so we add native/ as an include path.
    cc::Build::new()
        .file(native_dir.join("krfiles_shim.c"))
        .include(native_dir) // so #include "libkrfiles_api.h" resolves
        .warnings(false) // Kotlin's generated header has many warnings
        .compile("krfiles_shim"); // produces libkrfiles_shim.a

    // Step 2: Tell the Rust linker where to find libkrfiles.so at link time.
    // cargo:rustc-link-search adds a directory to the linker search path.
    // cargo:rustc-link-lib=dylib tells it to dynamically link libkrfiles.so.
    println!(
        "cargo:rustc-link-search=native={}",
        krfiles_lib_dir.canonicalize().unwrap().display()
    );
    println!("cargo:rustc-link-lib=dylib=krfiles");

    // Step 3: Rerun this build script if these files change.
    println!("cargo:rerun-if-changed=native/krfiles_shim.c");
    println!("cargo:rerun-if-changed=native/libkrfiles_api.h");
}
