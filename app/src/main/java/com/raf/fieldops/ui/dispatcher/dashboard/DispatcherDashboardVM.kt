package com.raf.fieldops.ui.dispatcher.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.model.toPriority
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.isToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatusFilter {
    All, Active, Done
}

enum class SortOption {
    ByTime, ByPriority, ByStatus
}

@HiltViewModel
class DispatcherDashboardVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    val jobsState: StateFlow<DatabaseState<List<Job>>> = jobRepo.getAllJobs()
        .map<List<Job>, DatabaseState<List<Job>>> { DatabaseState.Success(it) }
        .catch { e -> emit(DatabaseState.Failure(e.message ?: "Failed to load jobs")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )

    val searchQuery = MutableStateFlow("")

    val selectedFilter = MutableStateFlow(StatusFilter.All)

    val selectedSort = MutableStateFlow(SortOption.ByTime)

    private val _displayName = MutableStateFlow("Dispatcher")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val filteredJobs: StateFlow<List<Job>> = combine(
        jobsState,
        searchQuery,
        selectedFilter,
        selectedSort
    ) { state, query, filter, sort ->

        val jobs = when (state) {
            is DatabaseState.Success -> state.data
            else -> emptyList()
        }

        val todayJobs = jobs.filter { job ->
            job.scheduledStart.isToday()
        }

        val filtered = when (filter) {
            StatusFilter.All -> todayJobs
            StatusFilter.Active -> todayJobs.filter { job ->
                val status = job.status.toJobStatus()
                status == JobStatus.Assigned ||
                    status == JobStatus.Accepted ||
                    status == JobStatus.InProgress
            }
            StatusFilter.Done -> todayJobs.filter { job ->
                val status = job.status.toJobStatus()
                status == JobStatus.Completed || status == JobStatus.Cancelled
            }
        }

        val searched = if (query.isBlank()) {
            filtered
        } else {
            val lowerQuery = query.lowercase()
            filtered.filter { job ->
                job.title.lowercase().contains(lowerQuery) ||
                    job.assignedEngineerName.lowercase().contains(lowerQuery)
            }
        }

        when (sort) {
            SortOption.ByTime -> searched.sortedBy { it.scheduledStart }
            SortOption.ByPriority -> searched.sortedByDescending {
                it.priority.toPriority().ordinal
            }
            SortOption.ByStatus -> searched.sortedBy {
                it.status.toJobStatus().ordinal
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {

        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch
            val user = userRepo.getUserById(uid)
            _displayName.value = user?.displayName ?: "Dispatcher"
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterChange(filter: StatusFilter) {
        selectedFilter.value = filter
    }

    fun onSortChange(sort: SortOption) {
        selectedSort.value = sort
    }
}
