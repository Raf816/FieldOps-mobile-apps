package com.raf.fieldops.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raf.fieldops.ui.theme.LocalWindowWidthClass

@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val widthClass = LocalWindowWidthClass.current

    if (widthClass == WindowWidthSizeClass.Compact) {

        Box(modifier = modifier.fillMaxSize(), content = content)
    } else {

        val horizontalPadding = when (widthClass) {
            WindowWidthSizeClass.Medium -> 48.dp
            else -> 80.dp
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            content = content
        )
    }
}
