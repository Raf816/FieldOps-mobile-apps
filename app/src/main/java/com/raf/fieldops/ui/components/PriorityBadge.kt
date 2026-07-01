package com.raf.fieldops.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.backgroundColour
import com.raf.fieldops.ui.theme.foregroundColour

@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val background = priority.backgroundColour()
    val foreground = priority.foregroundColour()

    Text(
        text = priority.displayName().uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = foreground,
        modifier = modifier
            .background(
                color = background,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .semantics { contentDescription = "${priority.displayName()} priority badge" }
    )
}

@Preview(
    name = "All Priorities — Light",
    showBackground = true
)
@Composable
private fun PriorityBadgePreviewLight() {
    FieldOpsTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Priority.entries.forEach { priority ->
                PriorityBadge(priority = priority)
            }
        }
    }
}

@Preview(
    name = "All Priorities — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PriorityBadgePreviewDark() {
    FieldOpsTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Priority.entries.forEach { priority ->
                PriorityBadge(priority = priority)
            }
        }
    }
}
