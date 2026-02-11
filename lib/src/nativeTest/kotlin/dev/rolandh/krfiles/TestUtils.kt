package dev.rolandh.krfiles

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import kotlin.time.TimeSource

@OptIn(ExperimentalForeignApi::class)
internal actual fun getEnv(name: String): String? = getenv(name)?.toKString()

private val startMark = TimeSource.Monotonic.markNow()

internal actual val System: SystemTime =
    object : SystemTime {
        override fun currentTimeMillis(): Long = startMark.elapsedNow().inWholeMilliseconds
    }
