package com.raf.fieldops.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority

@Composable
fun JobStatus.backgroundColour(): Color {
    val isDark = LocalIsDarkResolved.current
    return when (this) {
        JobStatus.Assigned -> if (isDark) StatusColoursDark.assignedBackground else StatusColours.assignedBackground
        JobStatus.Accepted -> if (isDark) StatusColoursDark.acceptedBackground else StatusColours.acceptedBackground
        JobStatus.InProgress -> if (isDark) StatusColoursDark.inProgressBackground else StatusColours.inProgressBackground
        JobStatus.Completed -> if (isDark) StatusColoursDark.completedBackground else StatusColours.completedBackground
        JobStatus.Cancelled -> if (isDark) StatusColoursDark.cancelledBackground else StatusColours.cancelledBackground
        JobStatus.Dismissed -> if (isDark) StatusColoursDark.cancelledBackground else StatusColours.cancelledBackground
    }
}

@Composable
fun JobStatus.foregroundColour(): Color {
    val isDark = LocalIsDarkResolved.current
    return when (this) {
        JobStatus.Assigned -> if (isDark) StatusColoursDark.assignedForeground else StatusColours.assignedForeground
        JobStatus.Accepted -> if (isDark) StatusColoursDark.acceptedForeground else StatusColours.acceptedForeground
        JobStatus.InProgress -> if (isDark) StatusColoursDark.inProgressForeground else StatusColours.inProgressForeground
        JobStatus.Completed -> if (isDark) StatusColoursDark.completedForeground else StatusColours.completedForeground
        JobStatus.Cancelled -> if (isDark) StatusColoursDark.cancelledForeground else StatusColours.cancelledForeground
        JobStatus.Dismissed -> if (isDark) StatusColoursDark.cancelledForeground else StatusColours.cancelledForeground
    }
}

@Composable
fun JobStatus.dotColour(): Color {
    val isDark = LocalIsDarkResolved.current
    return when (this) {
        JobStatus.Assigned -> if (isDark) StatusColoursDark.assignedDot else StatusColours.assignedDot
        JobStatus.Accepted -> if (isDark) StatusColoursDark.acceptedDot else StatusColours.acceptedDot
        JobStatus.InProgress -> if (isDark) StatusColoursDark.inProgressDot else StatusColours.inProgressDot
        JobStatus.Completed -> if (isDark) StatusColoursDark.completedDot else StatusColours.completedDot
        JobStatus.Cancelled -> if (isDark) StatusColoursDark.cancelledDot else StatusColours.cancelledDot
        JobStatus.Dismissed -> if (isDark) StatusColoursDark.cancelledDot else StatusColours.cancelledDot
    }
}

@Composable
fun Priority.backgroundColour(): Color {
    val isDark = LocalIsDarkResolved.current
    return when (this) {
        Priority.Low -> if (isDark) PriorityColoursDark.lowBackground else PriorityColours.lowBackground
        Priority.Medium -> if (isDark) PriorityColoursDark.mediumBackground else PriorityColours.mediumBackground
        Priority.High -> if (isDark) PriorityColoursDark.highBackground else PriorityColours.highBackground
        Priority.Urgent -> if (isDark) PriorityColoursDark.urgentBackground else PriorityColours.urgentBackground
    }
}

@Composable
fun Priority.foregroundColour(): Color {
    val isDark = LocalIsDarkResolved.current
    return when (this) {
        Priority.Low -> if (isDark) PriorityColoursDark.lowForeground else PriorityColours.lowForeground
        Priority.Medium -> if (isDark) PriorityColoursDark.mediumForeground else PriorityColours.mediumForeground
        Priority.High -> if (isDark) PriorityColoursDark.highForeground else PriorityColours.highForeground
        Priority.Urgent -> if (isDark) PriorityColoursDark.urgentForeground else PriorityColours.urgentForeground
    }
}
