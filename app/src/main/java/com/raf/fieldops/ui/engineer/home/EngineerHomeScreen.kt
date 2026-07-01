package com.raf.fieldops.ui.engineer.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.R
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.ui.components.EmptyStateCard
import com.raf.fieldops.ui.components.JobListShimmer
import com.raf.fieldops.ui.components.OfflineBanner
import com.raf.fieldops.ui.components.StaggeredAnimatedItem
import com.raf.fieldops.ui.components.StatusChip
import com.raf.fieldops.ui.components.TimelineItem
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtIndigoLight
import com.raf.fieldops.ui.theme.StatusColours
import com.raf.fieldops.ui.theme.dotColour
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerHomeScreen(
    navigateToJobDetail: (Job) -> Unit,
    modifier: Modifier = Modifier,
    vm: EngineerHomeVM = hiltViewModel()
) {

    val jobsState by vm.jobsState.collectAsStateWithLifecycle()
    val displayName by vm.displayName.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val isOffline by vm.isOffline.collectAsStateWithLifecycle()
    val lastSynced by vm.lastSynced.collectAsStateWithLifecycle()

    var showRejectDialog by remember { mutableStateOf(false) }
    var jobToReject by remember { mutableStateOf<Job?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    when (val state = jobsState) {
        is DatabaseState.Loading -> {

            JobListShimmer(modifier = modifier)
        }

        is DatabaseState.Failure -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = "Error loading jobs",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { vm.refresh() }) {
                        Text("Retry")
                    }
                }
            }
        }

        is DatabaseState.Success -> {

            val allJobs = state.data

            val activeJob = allJobs.firstOrNull {
                it.status.toJobStatus() == JobStatus.InProgress
            }

            val remainingJobs = allJobs.filter { it.id != activeJob?.id }

            val totalCount = allJobs.size
            val activeCount = allJobs.count {
                val s = it.status.toJobStatus()
                s == JobStatus.InProgress || s == JobStatus.Accepted
            }
            val doneCount = allJobs.count {
                it.status.toJobStatus() == JobStatus.Completed
            }

            if (allJobs.isEmpty()) {

                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        icon = Icons.Outlined.WorkOff,
                        title = stringResource(R.string.empty_no_jobs_today),
                        subtitle = stringResource(R.string.empty_no_jobs_today_subtitle)
                    )
                }
            } else {

                Column(modifier = modifier.fillMaxSize()) {

                    OfflineBanner(isOffline = isOffline)

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { vm.refresh() },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {

                        item(key = "greeting") {
                            GreetingSection(displayName = displayName)
                        }

                        item(key = "stats") {
                            StatPills(
                                totalCount = totalCount,
                                activeCount = activeCount,
                                doneCount = doneCount
                            )
                        }

                        if (activeJob != null) {
                            item(key = "activeHero") {
                                ActiveJobHeroCard(
                                    job = activeJob,
                                    onComplete = { vm.completeJob(activeJob) },
                                    onTap = { navigateToJobDetail(activeJob) }
                                )
                            }
                        }

                        if (remainingJobs.isNotEmpty()) {
                            item(key = "upNextHeader") {
                                UpNextHeader(count = remainingJobs.size)
                            }
                        }

                        itemsIndexed(
                            items = remainingJobs,
                            key = { _, job -> job.id }
                        ) { index, job ->
                            val isLast = index == remainingJobs.lastIndex
                            val jobStatus = job.status.toJobStatus()

                            val isExpired = jobStatus == JobStatus.Assigned &&
                                job.scheduledEnd?.toDate()?.before(Date()) == true

                            StaggeredAnimatedItem(index = index) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isExpired)
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                            if (isExpired) {

                                TimelineItem(
                                    job = job,
                                    isLast = isLast,
                                    onTap = { navigateToJobDetail(job) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 68.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    Text(
                                        text = "MISSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )

                                    OutlinedButton(
                                        onClick = { vm.dismissJob(job) },
                                        modifier = Modifier.height(36.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        border = BorderStroke(
                                            1.dp, MaterialTheme.colorScheme.outline
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(
                                            "Dismiss",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else if (jobStatus == JobStatus.Assigned) {

                                SwipeableTimelineItem(
                                    job = job,
                                    isLast = isLast,
                                    onTap = { navigateToJobDetail(job) },
                                    onAccept = { vm.acceptJob(job) },
                                    onReject = {
                                        jobToReject = job
                                        showRejectDialog = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 68.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            jobToReject = job
                                            showRejectDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        border = BorderStroke(
                                            1.dp, MaterialTheme.colorScheme.error
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Reject", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = { vm.acceptJob(job) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BtIndigo
                                        )
                                    ) {
                                        Text("Accept", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {

                                TimelineItem(
                                    job = job,
                                    isLast = isLast,
                                    onTap = { navigateToJobDetail(job) }
                                )
                            }
                            }
                            }
                        }

                        item(key = "bottomSpacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                    LastSyncedIndicator(lastSynced = lastSynced)
                }
            }
        }
    }

    if (showRejectDialog && jobToReject != null) {
        RejectJobDialog(
            jobTitle = jobToReject?.title ?: "",
            reason = rejectReason,
            onReasonChange = { rejectReason = it },
            onConfirm = {
                jobToReject?.let { vm.rejectJob(it, rejectReason) }
                showRejectDialog = false
                jobToReject = null
                rejectReason = ""
            },
            onDismiss = {
                showRejectDialog = false
                jobToReject = null
                rejectReason = ""
            }
        )
    }
}

@Composable
private fun GreetingSection(displayName: String) {
    val dateFormatter = remember {
        SimpleDateFormat("EEEE, dd MMM", Locale.UK)
    }
    val today = remember { dateFormatter.format(Date()) }

    Column {
        Text(
            text = "Hey, $displayName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = today,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatPills(
    totalCount: Int,
    activeCount: Int,
    doneCount: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        StatPill(
            label = "Total",
            count = totalCount,
            backgroundColour = MaterialTheme.colorScheme.surfaceVariant,
            foregroundColour = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        StatPill(
            label = "Active",
            count = activeCount,
            backgroundColour = StatusColours.inProgressBackground,
            foregroundColour = StatusColours.inProgressForeground,
            modifier = Modifier.weight(1f)
        )

        StatPill(
            label = "Done",
            count = doneCount,
            backgroundColour = StatusColours.completedBackground,
            foregroundColour = StatusColours.completedForeground,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    count: Int,
    backgroundColour: Color,
    foregroundColour: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColour,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = foregroundColour
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = foregroundColour.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ActiveJobHeroCard(
    job: Job,
    onComplete: () -> Unit,
    onTap: () -> Unit
) {

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.UK) }
    val startTime = job.scheduledStart?.toDate()?.let { timeFormatter.format(it) } ?: "—"
    val endTime = job.scheduledEnd?.toDate()?.let { timeFormatter.format(it) } ?: "—"

    val infiniteTransition = rememberInfiniteTransition(label = "activePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BtIndigo, BtIndigoLight)
                )
            )
            .clickable(onClick = onTap)
            .padding(20.dp)
            .semantics { contentDescription = "Currently active job: ${job.title}" }
    ) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = StatusColours.inProgressDot.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )
                Text(
                    text = "CURRENTLY ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = job.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (job.address.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$startTime – $endTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BtIndigo
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Complete this job"
                }
            ) {
                Text(
                    text = "Complete",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun UpNextHeader(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Up Next",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SwipeableTimelineItem(
    job: Job,
    isLast: Boolean,
    onTap: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 200),
        label = "swipeOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {

        if (animatedOffset > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = StatusColours.completedBackground,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Accept",
                        tint = StatusColours.completedForeground,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Accept",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusColours.completedForeground
                    )
                }
            }
        }

        if (animatedOffset < 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = StatusColours.cancelledBackground,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reject",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusColours.cancelledForeground
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Reject",
                        tint = StatusColours.cancelledForeground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> {

                                    onAccept()
                                }
                                offsetX < -swipeThreshold -> {

                                    onReject()
                                }
                            }

                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
        ) {
            TimelineItem(
                job = job,
                isLast = isLast,
                onTap = onTap
            )
        }
    }
}

@Composable
private fun RejectJobDialog(
    jobTitle: String,
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Job",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rejecting \"$jobTitle\". You can optionally provide a reason.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    placeholder = {
                        Text(
                            text = "Reason (optional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Rejection reason input" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Reject",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LastSyncedIndicator(lastSynced: Long?) {
    if (lastSynced == null) return

    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            tick++
        }
    }

    val relativeTime = remember(lastSynced, tick) {
        formatRelativeTime(lastSynced)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Last synced: $relativeTime",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

private fun formatRelativeTime(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - epochMillis
    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "$diffMinutes min ago"
        diffHours < 24 -> "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
        else -> "Over a day ago"
    }
}
