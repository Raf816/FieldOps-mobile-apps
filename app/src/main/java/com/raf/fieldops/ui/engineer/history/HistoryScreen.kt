package com.raf.fieldops.ui.engineer.history

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.raf.fieldops.ui.components.EmptyStateCard
import com.raf.fieldops.ui.components.JobCard
import com.raf.fieldops.ui.components.JobListShimmer
import com.raf.fieldops.ui.components.StaggeredAnimatedItem
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun HistoryScreen(
    navigateToJobDetail: (Job) -> Unit,
    modifier: Modifier = Modifier,
    vm: HistoryVM = hiltViewModel()
) {

    val jobsState by vm.jobsState.collectAsStateWithLifecycle()

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
                        contentDescription = "Error loading job history",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {  }) {
                        Text("Retry")
                    }
                }
            }
        }

        is DatabaseState.Success -> {
            val jobs = state.data

            if (jobs.isEmpty()) {

                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        icon = Icons.Outlined.History,
                        title = stringResource(R.string.empty_no_history),
                        subtitle = stringResource(R.string.empty_history_subtitle)
                    )
                }
            } else {

                val widthClass = LocalWindowWidthClass.current
                val useGrid = widthClass == WindowWidthSizeClass.Expanded

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {

                    HistoryHeader(count = jobs.size)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (useGrid) {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                count = jobs.size,
                                key = { jobs[it].id }
                            ) { index ->
                                val job = jobs[index]
                                StaggeredAnimatedItem(index = index) {
                                    JobCard(
                                        job = job,
                                        onClick = { navigateToJobDetail(job) }
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
                                items = jobs,
                                key = { _, job -> job.id }
                            ) { index, job ->
                                StaggeredAnimatedItem(index = index) {
                                    JobCard(
                                        job = job,
                                        onClick = { navigateToJobDetail(job) }
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

@Composable
private fun HistoryHeader(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.screen_title_history),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
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
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
