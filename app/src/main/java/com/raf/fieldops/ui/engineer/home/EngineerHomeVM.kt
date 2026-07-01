package com.raf.fieldops.ui.engineer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.JobActions
import com.raf.fieldops.util.NetworkMonitor
import com.raf.fieldops.util.isToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EngineerHomeVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { online -> !online }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val lastSynced: StateFlow<Long?> = jobRepo.lastSynced

    val jobsState: StateFlow<DatabaseState<List<Job>>> = run {
        val uid = authRepo.currentUser?.uid ?: ""
        jobRepo.getJobsForEngineer(uid)
            .map { jobs ->
                val visibleJobs = jobs
                    .filter { job ->

                        val status = job.status.toJobStatus()
                        if (status == JobStatus.Dismissed) return@filter false

                        val isActive = status == JobStatus.Assigned ||
                            status == JobStatus.Accepted ||
                            status == JobStatus.InProgress

                        job.scheduledStart.isToday() || isActive
                    }
                    .sortedBy { it.scheduledStart }
                DatabaseState.Success(visibleJobs) as DatabaseState<List<Job>>
            }
            .catch { e ->
                emit(DatabaseState.Failure(e.message ?: "Failed to load jobs"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DatabaseState.Loading
            )
    }

    init {

        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch
            val user = userRepo.getUserById(uid)
            _displayName.value = user?.displayName ?: "Engineer"
        }
    }

    fun acceptJob(job: Job) {
        viewModelScope.launch { JobActions.accept(job, jobRepo) }
    }

    fun rejectJob(job: Job, reason: String) {
        viewModelScope.launch { JobActions.reject(job, reason, jobRepo) }
    }

    fun completeJob(job: Job) {
        viewModelScope.launch {
            val updatedJob = job.copy(
                status = JobStatus.Completed.name,
                updatedAt = null
            )
            jobRepo.updateJob(updatedJob)
        }
    }

    fun dismissJob(job: Job) {
        viewModelScope.launch { JobActions.dismiss(job, jobRepo) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
        }
    }

}
