package com.raf.fieldops.data.model

import com.raf.fieldops.util.AppLogger

enum class JobStatus {
    Assigned,
    Accepted,
    InProgress,
    Completed,
    Cancelled,
    Dismissed
}

private const val JOB_STATUS_TAG = "JobStatus"

fun String.toJobStatus(): JobStatus =
    runCatching { JobStatus.valueOf(this) }.getOrElse {
        AppLogger.debug(JOB_STATUS_TAG, "Unknown JobStatus value '$this'; falling back to Assigned")
        JobStatus.Assigned
    }

fun JobStatus.displayName(): String =
    name.replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
