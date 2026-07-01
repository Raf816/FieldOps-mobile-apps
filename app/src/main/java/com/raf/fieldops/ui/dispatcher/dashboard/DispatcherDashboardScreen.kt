package com.raf.fieldops.ui.dispatcher.dashboard

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.R
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.ui.components.EmptyStateCard
import com.raf.fieldops.ui.components.JobCard
import com.raf.fieldops.ui.components.JobListShimmer
import com.raf.fieldops.ui.components.StaggeredAnimatedItem
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtIndigoLight
import com.raf.fieldops.ui.theme.BtMagenta
import com.raf.fieldops.ui.theme.BtMagentaLight
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import com.raf.fieldops.ui.theme.StatusColours
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatcherDashboardScreen(
    navigateToCreateJob: () -> Unit,
    navigateToJobDetail: (Job) -> Unit,
    modifier: Modifier = Modifier,
    vm: DispatcherDashboardVM = hiltViewModel()
) {

    val jobsState by vm.jobsState.collectAsStateWithLifecycle()
    val filteredJobs by vm.filteredJobs.collectAsStateWithLifecycle()
    val displayName by vm.displayName.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by vm.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by vm.selectedSort.collectAsStateWithLifecycle()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {

            Button(
                onClick = navigateToCreateJob,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .semantics { contentDescription = "Create new job" }
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BtIndigo, BtMagenta)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = stringResource(R.string.btn_new_job),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->

        when (val state = jobsState) {
            is DatabaseState.Loading -> {

                JobListShimmer(modifier = Modifier.padding(innerPadding))
            }

            is DatabaseState.Failure -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
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
                            Text(stringResource(R.string.btn_retry))
                        }
                    }
                }
            }

            is DatabaseState.Success -> {

                val allJobs = state.data

                val todayJobs = allJobs.filter { job ->
                    val timestamp = job.scheduledStart ?: return@filter false
                    val cal = java.util.Calendar.getInstance()
                    val todayDayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
                    val todayYear = cal.get(java.util.Calendar.YEAR)
                    cal.time = timestamp.toDate()
                    cal.get(java.util.Calendar.DAY_OF_YEAR) == todayDayOfYear &&
                        cal.get(java.util.Calendar.YEAR) == todayYear
                }

                val assignedCount = todayJobs.count {
                    it.status.toJobStatus() == JobStatus.Assigned
                }
                val inProgressCount = todayJobs.count {
                    it.status.toJobStatus() == JobStatus.InProgress
                }
                val completedCount = todayJobs.count {
                    it.status.toJobStatus() == JobStatus.Completed
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    val widthClass = LocalWindowWidthClass.current
                    val useGrid = widthClass == WindowWidthSizeClass.Expanded

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        item(key = "greeting") {
                            GreetingSection(displayName = displayName)
                        }

                        item(key = "stats") {
                            BentoGrid(
                                totalCount = todayJobs.size,
                                assignedCount = assignedCount,
                                inProgressCount = inProgressCount,
                                completedCount = completedCount
                            )
                        }

                        item(key = "search") {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = vm::onSearchQueryChange
                            )
                        }

                        item(key = "filters") {
                            FilterSortRow(
                                selectedFilter = selectedFilter,
                                onFilterChange = vm::onFilterChange,
                                selectedSort = selectedSort,
                                onSortChange = vm::onSortChange
                            )
                        }

                        if (filteredJobs.isEmpty()) {
                            item(key = "empty") {
                                EmptyState()
                            }
                        } else {
                            if (useGrid) {

                                val chunkedJobs = filteredJobs.chunked(2)
                                chunkedJobs.forEachIndexed { chunkIndex, pair ->
                                    item(key = "row_$chunkIndex") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            pair.forEach { job ->
                                                Box(modifier = Modifier.weight(1f)) {
                                                    StaggeredAnimatedItem(index = chunkIndex) {
                                                        JobCard(
                                                            job = job,
                                                            onClick = { navigateToJobDetail(job) },
                                                            showEngineerName = true
                                                        )
                                                    }
                                                }
                                            }

                                            if (pair.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {

                                itemsIndexed(
                                    items = filteredJobs,
                                    key = { _, job -> job.id }
                                ) { index, job ->
                                    StaggeredAnimatedItem(index = index) {
                                        JobCard(
                                            job = job,
                                            onClick = { navigateToJobDetail(job) },
                                            showEngineerName = true
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "bottomSpacer") {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
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
private fun BentoGrid(
    totalCount: Int,
    assignedCount: Int,
    inProgressCount: Int,
    completedCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.animateContentSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BtIndigo, BtIndigoLight)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_todays_jobs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                label = "Assigned",
                count = assignedCount,
                backgroundColor = StatusColours.assignedBackground,
                foregroundColor = StatusColours.assignedForeground,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "In Progress",
                count = inProgressCount,
                backgroundColor = StatusColours.inProgressBackground,
                foregroundColor = StatusColours.inProgressForeground,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Completed",
                count = completedCount,
                backgroundColor = StatusColours.completedBackground,
                foregroundColor = StatusColours.completedForeground,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    backgroundColor: Color,
    foregroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = foregroundColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = foregroundColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search jobs or engineers...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search icon",
                tint = if (query.isNotEmpty()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            androidx.compose.animation.AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.semantics {
                        contentDescription = "Clear search"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (query.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
            unfocusedContainerColor = if (query.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Search jobs or engineers" }
    )
}

@Composable
private fun FilterSortRow(
    selectedFilter: StatusFilter,
    onFilterChange: (StatusFilter) -> Unit,
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            StatusFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            text = filter.name,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "${filter.name} filter"
                    }
                )
            }
        }

        SortDropdown(
            selectedSort = selectedSort,
            onSortChange = onSortChange
        )
    }
}

@Composable
private fun SortDropdown(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics {
                contentDescription = "Sort options"
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                val isSelected = selectedSort == option
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (option) {
                                SortOption.ByTime -> "By Time"
                                SortOption.ByPriority -> "By Priority"
                                SortOption.ByStatus -> "By Status"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    modifier = Modifier.semantics {
                        contentDescription = "Sort by ${option.name}"
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    EmptyStateCard(
        icon = Icons.Filled.WorkOff,
        title = stringResource(R.string.empty_no_jobs_today),
        subtitle = stringResource(R.string.empty_create_job_to_start)
    )
}
