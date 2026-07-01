package com.raf.fieldops.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.dotColour
import com.raf.fieldops.util.ThemePreference
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineItem(
    job: Job,
    isLast: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobStatus = job.status.toJobStatus()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.UK) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.UK) }
    val startTime = job.scheduledStart?.toDate()?.let { timeFormatter.format(it) } ?: "—"
    val endTime = job.scheduledEnd?.toDate()?.let { timeFormatter.format(it) } ?: "—"
    val dateLabel = job.scheduledStart?.toDate()?.let { dateFormatter.format(it) } ?: ""
    val dotColour = jobStatus.dotColour()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .semantics { contentDescription = "${job.title} at $startTime" }
    ) {

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = endTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (dateLabel.isNotEmpty()) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color = dotColour, shape = CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(color = MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (job.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(status = jobStatus)

                val isOverdue = job.scheduledEnd?.toDate()?.let { endTime ->
                    val isActive = jobStatus == JobStatus.Assigned ||
                        jobStatus == JobStatus.Accepted ||
                        jobStatus == JobStatus.InProgress
                    isActive && endTime.before(Date())
                } ?: false
                if (isOverdue) {
                    Text(
                        text = "NEEDS ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "TimelineItem — Assigned (with connector)",
    showBackground = true
)
@Composable
private fun TimelineItemPreviewAssigned() {
    FieldOpsTheme {
        Column {
            TimelineItem(
                job = Job(
                    id = "preview-1",
                    title = "Fibre Installation",
                    address = "42 Park Lane, Newcastle NE1 4PL",
                    priority = Priority.High.name,
                    status = JobStatus.Assigned.name,
                    scheduledStart = Timestamp(Date(System.currentTimeMillis() + 3600_000)),
                    scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 7200_000))
                ),
                isLast = false,
                onTap = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TimelineItem(
                job = Job(
                    id = "preview-2",
                    title = "Router Replacement",
                    address = "8 High Street, Gateshead NE8 2BQ",
                    priority = Priority.Medium.name,
                    status = JobStatus.Accepted.name,
                    scheduledStart = Timestamp(Date(System.currentTimeMillis() + 10800_000)),
                    scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 14400_000))
                ),
                isLast = true,
                onTap = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(
    name = "TimelineItem — InProgress (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TimelineItemPreviewInProgressDark() {
    FieldOpsTheme(
        themePreference = ThemePreference.Dark
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            TimelineItem(
                job = Job(
                    id = "preview-3",
                    title = "Line Fault Investigation",
                    address = "15 Station Road, Durham DH1 3FG",
                    priority = Priority.Urgent.name,
                    status = JobStatus.InProgress.name,
                    scheduledStart = Timestamp(Date(System.currentTimeMillis() - 3600_000)),
                    scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 3600_000))
                ),
                isLast = true,
                onTap = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(
    name = "TimelineItem — Overdue (Needs Action)",
    showBackground = true
)
@Composable
private fun TimelineItemPreviewOverdue() {
    FieldOpsTheme {
        TimelineItem(
            job = Job(
                id = "preview-4",
                title = "Cabinet Maintenance",
                address = "27 Elm Grove, Sunderland SR2 7HN",
                priority = Priority.High.name,
                status = JobStatus.Assigned.name,
                scheduledStart = Timestamp(Date(System.currentTimeMillis() - 7200_000)),
                scheduledEnd = Timestamp(Date(System.currentTimeMillis() - 3600_000))
            ),
            isLast = true,
            onTap = {},
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
