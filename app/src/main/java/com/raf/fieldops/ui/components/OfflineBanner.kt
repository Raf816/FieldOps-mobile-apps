package com.raf.fieldops.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.StatusColours

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StatusColours.inProgressBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics {
                    contentDescription = "You are offline. Showing cached data."
                    liveRegion = LiveRegionMode.Assertive
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = StatusColours.inProgressForeground,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "You're offline — showing cached data",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = StatusColours.inProgressForeground
            )
        }
    }
}

@Preview(
    name = "OfflineBanner",
    showBackground = true
)
@Composable
private fun OfflineBannerPreviewVisible() {
    FieldOpsTheme {
        OfflineBanner(isOffline = true)
    }
}
