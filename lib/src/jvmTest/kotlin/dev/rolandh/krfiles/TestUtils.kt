package dev.rolandh.krfiles

internal actual fun getEnv(name: String): String? = java.lang.System.getenv(name)

internal actual val System: SystemTime =
    object : SystemTime {
        override fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()
    }
