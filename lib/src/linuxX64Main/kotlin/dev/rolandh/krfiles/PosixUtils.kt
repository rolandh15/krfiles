package dev.rolandh.krfiles

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.mkdir

@OptIn(ExperimentalForeignApi::class)
internal actual fun ensureDirExists(path: String) {
    mkdir(path, 493u) // 0755
}
