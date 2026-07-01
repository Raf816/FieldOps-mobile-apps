package com.raf.fieldops.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        val delay = (index * 50).coerceAtMost(250)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delay
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {

            alpha = animationProgress.value

            translationY = (1f - animationProgress.value) * 80f
        }
    ) {
        content()
    }
}
