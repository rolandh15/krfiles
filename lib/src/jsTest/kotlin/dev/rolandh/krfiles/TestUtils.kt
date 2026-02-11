package dev.rolandh.krfiles

internal actual fun getEnv(name: String): String? = js("process.env[name]") as? String

internal actual val System: SystemTime =
    object : SystemTime {
        override fun currentTimeMillis(): Long =
            kotlin.js.Date
                .now()
                .toLong()
    }
