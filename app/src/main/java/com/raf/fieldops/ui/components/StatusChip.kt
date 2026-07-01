package com.raf.fieldops.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.backgroundColour
import com.raf.fieldops.ui.theme.dotColour
import com.raf.fieldops.ui.theme.foregroundColour

@Composable
fun StatusChip(
    status: JobStatus,
    modifier: Modifier = Modifier
) {

    val background by animateColorAsState(
        targetValue = status.backgroundColour(),
        animationSpec = tween(durationMillis = 300),
        label = "chipBackground"
    )
    val foreground by animateColorAsState(
        targetValue = status.foregroundColour(),
        animationSpec = tween(durationMillis = 300),
        label = "chipForeground"
    )
    val dotColor by animateColorAsState(
        targetValue = status.dotColour(),
        animationSpec = tween(durationMillis = 300),
        label = "chipDot"
    )

    val dotAlpha = if (status == JobStatus.InProgress) {
        val infiniteTransition = rememberInfiniteTransition(label = "inProgressPulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotAlpha"
        )
        alpha
    } else {
        1.0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(
                color = background,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics {
                contentDescription = "${status.displayName()} status chip"
                liveRegion = LiveRegionMode.Polite
            }
    ) {

        Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(dotAlpha)
                .background(color = dotColor, shape = CircleShape)
        )

        Text(
            text = status.displayName(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = foreground
        )
    }
}

@Preview(
    name = "All Statuses — Light",
    showBackground = true
)
@Composable
private fun StatusChipPreviewLight() {
    FieldOpsTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            JobStatus.entries.forEach { status ->
                StatusChip(status = status)
            }
        }
    }
}

@Preview(
    name = "All Statuses — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StatusChipPreviewDark() {
    FieldOpsTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            JobStatus.entries.forEach { status ->
                StatusChip(status = status)
            }
        }
    }
}
