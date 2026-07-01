package com.raf.fieldops.ui.engineer.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.util.JobActions
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
import java.util.Calendar
import javax.inject.Inject

enum class UpcomingRange {
    ThisWeek,
    ThisMonth,
    All
}

@HiltViewModel
class UpcomingVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val selectedRange = MutableStateFlow(UpcomingRange.ThisWeek)

    private val rawJobsFlow = run {
        val uid = authRepo.currentUser?.uid ?: ""
        jobRepo.getJobsForEngineer(uid)
    }

    val jobsState: StateFlow<DatabaseState<List<Job>>> = combine(
        rawJobsFlow,
        selectedRange
    ) { jobs, range ->
        val upcomingJobs = jobs
            .filter { job ->
                job.status.toJobStatus() != JobStatus.Dismissed &&
                    isInRange(job.scheduledStart, range)
            }
            .sortedBy { it.scheduledStart }
        DatabaseState.Success(upcomingJobs) as DatabaseState<List<Job>>
    }
        .catch { e ->
            emit(DatabaseState.Failure(e.message ?: "Failed to load upcoming jobs"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )

    fun onRangeChange(range: UpcomingRange) {
        selectedRange.value = range
    }

    fun acceptJob(job: Job) {
        viewModelScope.launch { JobActions.accept(job, jobRepo) }
    }

    fun rejectJob(job: Job, reason: String) {
        viewModelScope.launch { JobActions.reject(job, reason, jobRepo) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
        }
    }

    private fun isInRange(timestamp: Timestamp?, range: UpcomingRange): Boolean {
        if (timestamp == null) return false

        val cal = Calendar.getInstance()

        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfTomorrow = cal.time

        val jobDate = timestamp.toDate()

        if (jobDate < startOfTomorrow) return false

        return when (range) {
            UpcomingRange.ThisWeek -> {
                val endCal = Calendar.getInstance()
                endCal.add(Calendar.DAY_OF_YEAR, 8)
                endCal.set(Calendar.HOUR_OF_DAY, 0)
                endCal.set(Calendar.MINUTE, 0)
                endCal.set(Calendar.SECOND, 0)
                endCal.set(Calendar.MILLISECOND, 0)
                jobDate < endCal.time
            }
            UpcomingRange.ThisMonth -> {
                val endCal = Calendar.getInstance()
                endCal.add(Calendar.DAY_OF_YEAR, 31)
                endCal.set(Calendar.HOUR_OF_DAY, 0)
                endCal.set(Calendar.MINUTE, 0)
                endCal.set(Calendar.SECOND, 0)
                endCal.set(Calendar.MILLISECOND, 0)
                jobDate < endCal.time
            }
            UpcomingRange.All -> true
        }
    }
}
