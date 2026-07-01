package com.raf.fieldops.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

object FieldOpsMotion {

    const val DurationFast = 150

    const val DurationMedium = 250

    const val DurationSlow = 400

    val EaseStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val EaseEmphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val EaseExit = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    val EaseEnter = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
}
