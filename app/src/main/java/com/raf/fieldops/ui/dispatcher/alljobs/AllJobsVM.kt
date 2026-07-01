package com.raf.fieldops.ui.dispatcher.alljobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.model.toPriority
import com.raf.fieldops.data.repo.JobRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AllJobsVM @Inject constructor(
    private val jobRepo: JobRepo
) : ViewModel() {

    val jobsState: StateFlow<DatabaseState<List<Job>>> = jobRepo.getAllJobs()
        .map<List<Job>, DatabaseState<List<Job>>> { DatabaseState.Success(it) }
        .catch { e -> emit(DatabaseState.Failure(e.message ?: "Failed to load jobs")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseState.Loading)

    val searchQuery = MutableStateFlow("")
    val selectedStatus = MutableStateFlow<JobStatus?>(null)
    val showMissedOnly = MutableStateFlow(false)

    val missedCount: StateFlow<Int> = jobsState
        .map { state ->
            when (state) {
                is DatabaseState.Success -> state.data.count {
                    it.status.toJobStatus() == JobStatus.Dismissed
                }
                else -> 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredJobs: StateFlow<List<Job>> = combine(
        jobsState,
        searchQuery,
        selectedStatus,
        showMissedOnly
    ) { state, query, status, missedOnly ->
        val jobs = when (state) {
            is DatabaseState.Success -> state.data
            else -> emptyList()
        }

        val baseJobs = if (missedOnly) {
            jobs.filter { it.status.toJobStatus() == JobStatus.Dismissed }
        } else {

            if (status == null) jobs
            else jobs.filter { it.status.toJobStatus() == status }
        }

        val searched = if (query.isBlank()) baseJobs
        else {
            val lower = query.lowercase()
            baseJobs.filter {
                it.title.lowercase().contains(lower) ||
                    it.assignedEngineerName.lowercase().contains(lower) ||
                    it.address.lowercase().contains(lower)
            }
        }

        searched.sortedByDescending { it.scheduledStart }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
