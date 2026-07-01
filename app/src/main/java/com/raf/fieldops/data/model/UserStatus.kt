package com.raf.fieldops.data.model

import com.raf.fieldops.util.AppLogger

enum class UserStatus {
    Pending,
    Active,
    Suspended
}

private const val USER_STATUS_TAG = "UserStatus"

fun String.toUserStatus(): UserStatus =
    runCatching {
        UserStatus.valueOf(this.replaceFirstChar { it.uppercase() })
    }.getOrElse {
        AppLogger.debug(USER_STATUS_TAG, "Unknown UserStatus value '$this'; falling back to Pending")
        UserStatus.Pending
    }
