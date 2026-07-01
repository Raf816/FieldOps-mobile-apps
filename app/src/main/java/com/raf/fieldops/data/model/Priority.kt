package com.raf.fieldops.data.model

import com.raf.fieldops.util.AppLogger

enum class Priority {
    Low,
    Medium,
    High,
    Urgent
}

private const val PRIORITY_TAG = "Priority"

fun String.toPriority(): Priority =
    runCatching { Priority.valueOf(this) }.getOrElse {
        AppLogger.debug(PRIORITY_TAG, "Unknown Priority value '$this'; falling back to Medium")
        Priority.Medium
    }

fun Priority.displayName(): String = name
