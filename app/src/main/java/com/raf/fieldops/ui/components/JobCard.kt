package com.raf.fieldops.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.raf.fieldops.data.model.toPriority
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.dotColour
import com.raf.fieldops.util.ThemePreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEngineerName: Boolean = false
) {

    val jobStatus = job.status.toJobStatus()
    val jobPriority = job.priority.toPriority()

    val isOverdue = remember(job.scheduledEnd, job.status) {
        val endTime = job.scheduledEnd?.toDate()
        val isActive = jobStatus == JobStatus.Assigned ||
            jobStatus == JobStatus.Accepted ||
            jobStatus == JobStatus.InProgress
        endTime != null && isActive && endTime.before(Date())
    }

    val borderColour = jobStatus.dotColour()

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "cardPressScale"
    )

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.UK) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.UK) }
    val startTime = job.scheduledStart?.toDate()?.let { timeFormatter.format(it) } ?: "—"
    val endTime = job.scheduledEnd?.toDate()?.let { timeFormatter.format(it) } ?: "—"
    val dateLabel = job.scheduledStart?.toDate()?.let { dateFormatter.format(it) } ?: ""
    val timeSlot = if (dateLabel.isNotEmpty()) "$startTime – $endTime, $dateLabel" else "$startTime – $endTime"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()

            .graphicsLayer(scaleX = scale, scaleY = scale)

            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true

                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .semantics(mergeDescendants = true) { contentDescription = "${job.title} job card" }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {

                    drawRect(
                        color = borderColour,
                        topLeft = Offset.Zero,
                        size = Size(width = 4.dp.toPx(), height = size.height)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = jobPriority)
                }

                if (showEngineerName && job.assignedEngineerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        MiniAvatar(name = job.assignedEngineerName)
                        Text(
                            text = job.assignedEngineerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (job.address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
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

                if (job.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = job.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusChip(status = jobStatus)

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
                    Text(
                        text = timeSlot,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(22.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(
    name = "JobCard — Assigned",
    showBackground = true
)
@Composable
private fun JobCardPreviewAssigned() {
    FieldOpsTheme {
        JobCard(
            job = Job(
                id = "preview-1",
                title = "Fibre Installation",
                description = "Install fibre router at customer premises",
                address = "42 Park Lane, Newcastle NE1 4PL",
                priority = Priority.High.name,
                status = JobStatus.Assigned.name,
                assignedEngineerName = "James Wilson"
            ),
            onClick = {},
            showEngineerName = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "JobCard — InProgress",
    showBackground = true
)
@Composable
private fun JobCardPreviewInProgress() {
    FieldOpsTheme {
        JobCard(
            job = Job(
                id = "preview-2",
                title = "Router Replacement",
                description = "Replace faulty router",
                address = "8 High Street, Gateshead NE8 2BQ",
                priority = Priority.Medium.name,
                status = JobStatus.InProgress.name,
                assignedEngineerName = "Sarah Chen"
            ),
            onClick = {},
            showEngineerName = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "JobCard — Completed (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun JobCardPreviewCompletedDark() {
    FieldOpsTheme(
        themePreference = ThemePreference.Dark
    ) {
        JobCard(
            job = Job(
                id = "preview-3",
                title = "Line Fault Investigation",
                description = "Check exchange connection",
                address = "15 Station Road, Durham DH1 3FG",
                priority = Priority.Urgent.name,
                status = JobStatus.Completed.name
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
