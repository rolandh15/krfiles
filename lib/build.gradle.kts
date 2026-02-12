plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

group = "dev.rolandh.krfiles"
version = rootProject.file("VERSION").readText().trim()

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvmToolchain(17)

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        optIn.add("kotlin.js.ExperimentalJsExport")
    }

    jvm {
        // Unit tests (default)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            // Exclude integration tests from default test run
            exclude("**/*IntegrationTest*")
        }

        // Integration tests (separate task)
        testRuns.create("integrationTest") {
            executionTask.configure {
                useJUnitPlatform()
                // Only run integration tests
                include("**/*IntegrationTest*")
                // Pass environment variables
                environment("FILEBROWSER_URL", System.getenv("FILEBROWSER_URL") ?: "")
                environment("FILEBROWSER_USERNAME", System.getenv("FILEBROWSER_USERNAME") ?: "")
                environment("FILEBROWSER_PASSWORD", System.getenv("FILEBROWSER_PASSWORD") ?: "")
            }
        }
    }

    js {
        outputModuleName.set("krfiles")
        useCommonJs()
        // Node.js only - no browser target
        nodejs {
            testTask {
                useMocha {
                    timeout = "30s"
                }
                filter.excludeTestsMatching("*IntegrationTest*")
            }
        }
        binaries.library()
        generateTypeScriptDefinitions()

        compilations["main"].packageJson {
            customField("description", "Kotlin Multiplatform client library for Filebrowser API")
            customField("license", "Apache-2.0")
            customField(
                "repository",
                mapOf(
                    "type" to "git",
                    "url" to "https://github.com/rolandh15/krfiles.git",
                ),
            )
            customField("homepage", "https://github.com/rolandh15/krfiles")
            customField(
                "keywords",
                listOf("filebrowser", "kotlin", "multiplatform", "api-client", "node"),
            )
            customField(
                "author",
                mapOf(
                    "name" to "Roland H",
                    "url" to "https://rolandh.link",
                ),
            )
            customField("engines", mapOf("node" to ">=16.0.0"))
        }
    }

    linuxX64 {
        binaries {
            sharedLib("krfiles") { baseName = "krfiles" }
        }
    }
    linuxArm64 {
        binaries {
            sharedLib("krfiles") { baseName = "krfiles" }
        }
    }
    macosX64 {
        binaries {
            sharedLib("krfiles") { baseName = "krfiles" }
        }
    }
    macosArm64 {
        binaries {
            sharedLib("krfiles") { baseName = "krfiles" }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
        val nativeTest by creating {
            dependsOn(commonTest.get())
        }

        // Desktop native targets use Curl engine (CIO doesn't support TLS on Native)
        val desktopNativeMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.curl)
            }
        }

        // iOS targets use Darwin (URLSession) engine
        val iosNativeMain by creating {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        linuxX64Main { dependsOn(desktopNativeMain) }
        linuxArm64Main { dependsOn(desktopNativeMain) }
        macosX64Main { dependsOn(desktopNativeMain) }
        macosArm64Main { dependsOn(desktopNativeMain) }
        iosX64Main { dependsOn(iosNativeMain) }
        iosArm64Main { dependsOn(iosNativeMain) }
        iosSimulatorArm64Main { dependsOn(iosNativeMain) }

        linuxX64Test { dependsOn(nativeTest) }
        linuxArm64Test { dependsOn(nativeTest) }
        macosX64Test { dependsOn(nativeTest) }
        macosArm64Test { dependsOn(nativeTest) }
        iosX64Test { dependsOn(nativeTest) }
        iosArm64Test { dependsOn(nativeTest) }
        iosSimulatorArm64Test { dependsOn(nativeTest) }
    }
}

// Exclude integration tests from native test tasks
tasks.matching { it.name.contains("linuxX64Test") || it.name.contains("linuxArm64Test") }.configureEach {
    if (this is org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest) {
        filter.excludeTestsMatching("*IntegrationTest*")
    }
}

// Dokka configuration for beautiful documentation
dokka {
    moduleName.set("krfiles")
    dokkaSourceSets.configureEach {
        includes.from("MODULE.md")
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/rolandh15/krfiles/tree/master/lib/src")
            remoteLineSuffix.set("#L")
        }
    }
    pluginsConfiguration.html {
        customStyleSheets.from("dokka/styles.css")
        footerMessage.set("krfiles - Kotlin Multiplatform Filebrowser Client")
    }
}

// Kover test coverage
kover {
    reports {
        total {
            xml { onCheck = true }
            html { onCheck = true }
        }
        filters {
            excludes {
                classes("*Test*", "*_*")
            }
        }
    }
}

// ktlint configuration
ktlint {
    version.set("1.5.0")
    android.set(false)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
}

val npmDistDir = layout.buildDirectory.dir("dist/js/productionLibrary")

tasks.register<Copy>("copyNpmReadme") {
    group = "publishing"
    description = "Copy README.md into npm distribution directory"
    from(rootProject.file("README.md"))
    into(npmDistDir)
    dependsOn("jsNodeProductionLibraryDistribution")
}

tasks.register("buildAll") {
    group = "build"
    description = "Build all distribution artifacts for the current platform"
    dependsOn("jvmJar", "jsNodeProductionLibraryDistribution")

    // Native shared libs can only be built on the matching host OS
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("linux")) {
        dependsOn("linkKrfilesReleaseSharedLinuxX64", "linkKrfilesReleaseSharedLinuxArm64")
    } else if (os.contains("mac")) {
        dependsOn("linkKrfilesReleaseSharedMacosX64", "linkKrfilesReleaseSharedMacosArm64")
    }
}

tasks.register<Exec>("npmPack") {
    group = "publishing"
    description = "Pack npm package into .tgz"
    dependsOn("copyNpmReadme")
    workingDir(npmDistDir)
    commandLine("npm", "pack")
}

tasks.register<Exec>("npmPublish") {
    group = "publishing"
    description = "Publish npm package to registry"
    dependsOn("copyNpmReadme")
    workingDir(npmDistDir)
    commandLine("npm", "publish", "--access", "public")
}

tasks.register("unitTest") {
    group = "verification"
    description = "Run unit tests on all platforms (excludes integration tests)"
    dependsOn("jvmTest", "jsTest", "linuxX64Test")
}

tasks.register("integrationTest") {
    group = "verification"
    description = "Run integration tests (JVM only, requires FILEBROWSER_* env vars)"
    dependsOn("jvmIntegrationTest")
}

tasks.register("check-all") {
    group = "verification"
    description = "Run all checks: unit tests, lint, coverage"
    dependsOn("unitTest", "ktlintCheck", "koverHtmlReport")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("krfiles")
                description.set("Kotlin Multiplatform client library for Filebrowser API")
                url.set("https://github.com/rolandh15/krfiles")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("rolandh15")
                        name.set("Roland H")
                        url.set("https://rolandh.link")
                    }
                }
                scm {
                    url.set("https://github.com/rolandh15/krfiles")
                    connection.set("scm:git:git://github.com/rolandh15/krfiles.git")
                    developerConnection.set("scm:git:ssh://github.com/rolandh15/krfiles.git")
                }
            }
        }
    }
}
