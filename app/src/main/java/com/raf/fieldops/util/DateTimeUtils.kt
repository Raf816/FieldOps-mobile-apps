package com.raf.fieldops.util

import com.google.firebase.Timestamp
import java.util.Calendar

fun Timestamp?.isToday(): Boolean {
    if (this == null) return false
    val cal = Calendar.getInstance()
    val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val todayYear = cal.get(Calendar.YEAR)
    cal.time = this.toDate()
    return cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear &&
        cal.get(Calendar.YEAR) == todayYear
}
