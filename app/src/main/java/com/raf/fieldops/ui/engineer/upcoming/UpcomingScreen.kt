package com.raf.fieldops.ui.engineer.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.raf.fieldops.ui.components.StaggeredAnimatedItem
import com.raf.fieldops.ui.components.TimelineItem
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    navigateToJobDetail: (Job) -> Unit,
    modifier: Modifier = Modifier,
    vm: UpcomingVM = hiltViewModel()
) {

    val jobsState by vm.jobsState.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val selectedRange by vm.selectedRange.collectAsStateWithLifecycle()

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
                        contentDescription = "Error loading upcoming jobs",
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

            if (allJobs.isEmpty()) {

                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = stringResource(R.string.screen_title_upcoming),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .semantics { heading() }
                    )

                    RangeFilterRow(
                        selectedRange = selectedRange,
                        onRangeChange = { vm.onRangeChange(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(
                            icon = Icons.Outlined.WorkOff,
                            title = when (selectedRange) {
                                UpcomingRange.ThisWeek -> "No upcoming jobs this week"
                                UpcomingRange.ThisMonth -> "No upcoming jobs this month"
                                UpcomingRange.All -> "No upcoming jobs"
                            },
                            subtitle = "Check back later"
                        )
                    }
                }
            } else {

                val groupedJobs = remember(allJobs) { groupJobsByDay(allJobs) }

                val widthClass = LocalWindowWidthClass.current
                val isExpanded = widthClass == WindowWidthSizeClass.Expanded

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = modifier.fillMaxSize()
                ) {
                    if (isExpanded) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {

                            Text(
                                text = stringResource(R.string.screen_title_upcoming),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RangeFilterRow(
                                selectedRange = selectedRange,
                                onRangeChange = { vm.onRangeChange(it) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(0.62f)
                                ) {
                                    groupedJobs.forEach { (date, jobs) ->
                                        stickyHeader(key = "header_${date.time}") {
                                            DayHeader(
                                                dayLabel = getDayLabel(date),
                                                jobCount = jobs.size
                                            )
                                        }
                                        itemsIndexed(
                                            items = jobs,
                                            key = { _, job -> job.id }
                                        ) { index, job ->
                                            val isLast = index == jobs.lastIndex
                                            val jobStatus = job.status.toJobStatus()
                                            StaggeredAnimatedItem(index = index) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color = MaterialTheme.colorScheme.surface,
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                        .padding(14.dp)
                                                ) {
                                                    TimelineItem(
                                                        job = job,
                                                        isLast = isLast,
                                                        onTap = { navigateToJobDetail(job) }
                                                    )

                                                    if (jobStatus == JobStatus.Assigned) {
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
                                                                modifier = Modifier.weight(1f).height(40.dp),
                                                                shape = MaterialTheme.shapes.medium,
                                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                                            ) {
                                                                Text("Reject", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                                            }
                                                            Button(
                                                                onClick = { vm.acceptJob(job) },
                                                                modifier = Modifier.weight(1f).height(40.dp),
                                                                shape = MaterialTheme.shapes.medium,
                                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                            ) {
                                                                Text("Accept", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        item(key = "spacer_${date.time}") {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                    item(key = "bottomSpacer") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(0.38f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    androidx.compose.material3.Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = androidx.compose.material3.CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Summary",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "${allJobs.size} jobs",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))

                                            groupedJobs.forEach { (date, jobs) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = getDayLabel(date),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "${jobs.size}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        item(key = "pageTitle") {
                            Text(
                                text = stringResource(R.string.screen_title_upcoming),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        item(key = "rangeFilter") {
                            RangeFilterRow(
                                selectedRange = selectedRange,
                                onRangeChange = { vm.onRangeChange(it) }
                            )
                        }

                        groupedJobs.forEach { (date, jobs) ->

                            stickyHeader(key = "header_${date.time}") {
                                DayHeader(
                                    dayLabel = getDayLabel(date),
                                    jobCount = jobs.size
                                )
                            }

                            itemsIndexed(
                                items = jobs,
                                key = { _, job -> job.id }
                            ) { index, job ->
                                val isLast = index == jobs.lastIndex
                                val jobStatus = job.status.toJobStatus()

                                StaggeredAnimatedItem(index = index) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(14.dp)
                                ) {
                                TimelineItem(
                                    job = job,
                                    isLast = isLast,
                                    onTap = { navigateToJobDetail(job) }
                                )

                                if (jobStatus == JobStatus.Assigned) {
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
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("Accept", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                }
                                }
                            }

                            item(key = "spacer_${date.time}") {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        item(key = "bottomSpacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    }
                }
            }
        }
    }

    if (showRejectDialog && jobToReject != null) {
        AlertDialog(
            onDismissRequest = {
                showRejectDialog = false
                jobToReject = null
                rejectReason = ""
            },
            title = { Text("Reject Job", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Rejecting \"${jobToReject?.title}\". You can optionally provide a reason.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Reason (optional)") },
                        minLines = 2,
                        maxLines = 4,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    jobToReject?.let { vm.rejectJob(it, rejectReason) }
                    showRejectDialog = false
                    jobToReject = null
                    rejectReason = ""
                }) {
                    Text("Reject", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    jobToReject = null
                    rejectReason = ""
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DayHeader(dayLabel: String, jobCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { heading() }
            )
        }

        Text(
            text = "$jobCount ${if (jobCount == 1) "job" else "jobs"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

private fun groupJobsByDay(jobs: List<Job>): Map<Date, List<Job>> {
    return jobs.groupBy { job ->
        val cal = Calendar.getInstance().apply {
            time = job.scheduledStart!!.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.time
    }.toSortedMap()
}

private fun getDayLabel(date: Date): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val tomorrowYear = cal.get(Calendar.YEAR)

    val jobCal = Calendar.getInstance().apply { time = date }
    return if (jobCal.get(Calendar.DAY_OF_YEAR) == tomorrowDayOfYear &&
        jobCal.get(Calendar.YEAR) == tomorrowYear
    ) {
        "Tomorrow"
    } else {
        SimpleDateFormat("EEEE, dd MMM", Locale.UK).format(date)
    }
}

@Composable
private fun RangeFilterRow(
    selectedRange: UpcomingRange,
    onRangeChange: (UpcomingRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UpcomingRange.entries.forEach { range ->
            val isSelected = selectedRange == range
            val label = when (range) {
                UpcomingRange.ThisWeek -> "This Week"
                UpcomingRange.ThisMonth -> "This Month"
                UpcomingRange.All -> "All"
            }
            FilterChip(
                selected = isSelected,
                onClick = { onRangeChange(range) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
