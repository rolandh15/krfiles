/// Build script — runs before Rust compilation.
///
/// This does two things:
/// 1. Compiles the C shim (krfiles_shim.c) that wraps the Kotlin vtable
/// 2. Tells the linker where to find libkrfiles.so/.dylib (the Kotlin shared library)
///
/// The Kotlin/Native target is auto-detected from the Rust build target.
/// Override with `KRFILES_NATIVE_LIB_DIR` env var if needed.
fn main() {
    // Skip native compilation when building docs (cargo doc / docs.rs)
    if std::env::var("DOCS_RS").is_ok() {
        return;
    }

    let native_dir = std::path::Path::new("native");

    // Detect the Kotlin/Native target directory from Rust's build target.
    // Cargo sets these env vars during build.rs execution.
    let target_os = std::env::var("CARGO_CFG_TARGET_OS").unwrap();
    let target_arch = std::env::var("CARGO_CFG_TARGET_ARCH").unwrap();

    let kotlin_target = match (target_os.as_str(), target_arch.as_str()) {
        ("linux", "x86_64") => "linuxX64",
        ("linux", "aarch64") => "linuxArm64",
        ("macos", "x86_64") => "macosX64",
        ("macos", "aarch64") => "macosArm64",
        _ => panic!(
            "Unsupported target: os={target_os}, arch={target_arch}. \
             Supported: linux-x86_64, linux-aarch64, macos-x86_64, macos-aarch64"
        ),
    };

    // Allow override via env var (useful in CI or non-standard layouts).
    let default_lib_dir = std::path::Path::new("../lib/build/bin")
        .join(kotlin_target)
        .join("krfilesReleaseShared");

    let krfiles_lib_dir = match std::env::var("KRFILES_NATIVE_LIB_DIR") {
        Ok(dir) => std::path::PathBuf::from(dir),
        Err(_) => default_lib_dir,
    };

    // Step 1: Compile the C shim.
    // The `cc` crate invokes the system C compiler (gcc/clang) to compile
    // krfiles_shim.c into a static library that gets linked into our binary.
    // The shim #includes the Kotlin header, so we add native/ as an include path.
    cc::Build::new()
        .file(native_dir.join("krfiles_shim.c"))
        .include(native_dir) // so #include "libkrfiles_api.h" resolves
        .warnings(false) // Kotlin's generated header has many warnings
        .compile("krfiles_shim"); // produces libkrfiles_shim.a

    // Step 2: Tell the Rust linker where to find the Kotlin shared library.
    // cargo:rustc-link-search adds a directory to the linker search path.
    // cargo:rustc-link-lib=dylib tells it to dynamically link libkrfiles.
    // On Linux this finds libkrfiles.so, on macOS libkrfiles.dylib.
    println!(
        "cargo:rustc-link-search=native={}",
        krfiles_lib_dir.canonicalize().unwrap_or_else(|e| {
            let task_suffix = kotlin_target
                .chars()
                .next()
                .unwrap()
                .to_uppercase()
                .collect::<String>()
                + &kotlin_target[1..];
            panic!(
                "Cannot find Kotlin native library at {}: {e}\n\
                 Build it first: ./gradlew linkKrfilesReleaseShared{task_suffix}",
                krfiles_lib_dir.display()
            )
        })
        .display()
    );
    println!("cargo:rustc-link-lib=dylib=krfiles");

    // Step 3: Rerun this build script if these files change.
    println!("cargo:rerun-if-changed=native/krfiles_shim.c");
    println!("cargo:rerun-if-changed=native/libkrfiles_api.h");
    println!("cargo:rerun-if-env-changed=KRFILES_NATIVE_LIB_DIR");
}
