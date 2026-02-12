package dev.rolandh.krfiles

/** Create a directory if it doesn't exist. Platform-specific because mode_t widths vary. */
internal expect fun ensureDirExists(path: String)
