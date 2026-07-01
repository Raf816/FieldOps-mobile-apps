package com.raf.fieldops.ui.dispatcher.alljobs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.ui.components.EmptyStateCard
import com.raf.fieldops.ui.components.JobCard
import com.raf.fieldops.ui.components.JobListShimmer
import com.raf.fieldops.ui.components.StaggeredAnimatedItem
import com.raf.fieldops.ui.theme.LocalWindowWidthClass

@Composable
fun AllJobsScreen(
    navigateToJobDetail: (Job) -> Unit,
    modifier: Modifier = Modifier,
    vm: AllJobsVM = hiltViewModel()
) {
    val jobsState by vm.jobsState.collectAsStateWithLifecycle()
    val filteredJobs by vm.filteredJobs.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by vm.selectedStatus.collectAsStateWithLifecycle()
    val showMissedOnly by vm.showMissedOnly.collectAsStateWithLifecycle()
    val missedCount by vm.missedCount.collectAsStateWithLifecycle()

    when (jobsState) {
        is DatabaseState.Loading -> {
            JobListShimmer(modifier = modifier)
        }

        is DatabaseState.Failure -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (jobsState as DatabaseState.Failure).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is DatabaseState.Success -> {
            val totalCount = (jobsState as DatabaseState.Success<List<Job>>).data.size

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.screen_title_all_jobs),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.searchQuery.value = it },
                    placeholder = { Text("Search by title, engineer, or address...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                        ) {
                            IconButton(onClick = { vm.searchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                        unfocusedContainerColor = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                            else Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    item {
                        val isAllSelected = selectedStatus == null && !showMissedOnly
                        FilterChip(
                            selected = isAllSelected,
                            onClick = {
                                vm.selectedStatus.value = null
                                vm.showMissedOnly.value = false
                            },
                            label = { Text("All") },
                            leadingIcon = if (isAllSelected) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    items(JobStatus.entries.filter { it != JobStatus.Dismissed }) { status ->
                        val isStatusSelected = selectedStatus == status && !showMissedOnly
                        FilterChip(
                            selected = isStatusSelected,
                            onClick = {
                                vm.selectedStatus.value = status
                                vm.showMissedOnly.value = false
                            },
                            label = { Text(status.displayName()) },
                            leadingIcon = if (isStatusSelected) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = showMissedOnly,
                            onClick = {
                                vm.showMissedOnly.value = !showMissedOnly
                                if (!showMissedOnly) vm.selectedStatus.value = null
                            },
                            label = {
                                Text(
                                    text = if (missedCount > 0) "Dismissed ($missedCount)" else "Dismissed"
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.WorkOff,
                            title = stringResource(R.string.empty_no_jobs_found),
                            subtitle = stringResource(R.string.empty_adjust_filters)
                        )
                    }
                } else {
                    val widthClass = LocalWindowWidthClass.current
                    val useGrid = widthClass == WindowWidthSizeClass.Expanded

                    if (useGrid) {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                count = filteredJobs.size,
                                key = { filteredJobs[it].id }
                            ) { index ->
                                val job = filteredJobs[index]
                                StaggeredAnimatedItem(index = index) {
                                    JobCard(
                                        job = job,
                                        onClick = { navigateToJobDetail(job) },
                                        showEngineerName = true
                                    )
                                }
                            }
                        }
                    } else {

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
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
                }
            }
        }
    }
}
