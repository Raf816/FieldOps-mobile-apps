package com.raf.fieldops.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class FieldOpsElevation(
    val flat: Dp = 0.dp,
    val raised: Dp = 1.dp,
    val floating: Dp = 3.dp,
    val overlay: Dp = 6.dp
)

val LocalElevation = staticCompositionLocalOf { FieldOpsElevation() }
