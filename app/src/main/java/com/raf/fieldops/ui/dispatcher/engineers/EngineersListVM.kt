package com.raf.fieldops.ui.dispatcher.engineers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.isToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class WorkloadStatus {
    Free, Busy, Overloaded
}

data class EngineerWithWorkload(
    val user: User,
    val activeJobCount: Int,
    val todayJobCount: Int
) {

    val workloadStatus: WorkloadStatus
        get() = when {
            activeJobCount == 0 -> WorkloadStatus.Free
            activeJobCount in 1..3 -> WorkloadStatus.Busy
            else -> WorkloadStatus.Overloaded
        }
}

@HiltViewModel
class EngineersListVM @Inject constructor(
    private val userRepo: UserRepo,
    private val jobRepo: JobRepo
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val _selectedEngineer = MutableStateFlow<User?>(null)
    val selectedEngineer: StateFlow<User?> = _selectedEngineer.asStateFlow()

    private val engineersFlow = userRepo.getAllEngineers()
    private val jobsFlow = jobRepo.getAllJobs()

    val engineersState: StateFlow<DatabaseState<List<EngineerWithWorkload>>> = combine(
        engineersFlow,
        jobsFlow,
        searchQuery
    ) { engineers, jobs, query ->

        val engineersWithWorkload = engineers.map { engineer ->
            val engineerJobs = jobs.filter { it.assignedTo == engineer.uid }

            val activeCount = engineerJobs.count { job ->
                val status = job.status.toJobStatus()
                status != JobStatus.Completed && status != JobStatus.Cancelled &&
                    status != JobStatus.Dismissed
            }

            val todayCount = engineerJobs.count { job ->
                job.scheduledStart.isToday()
            }

            EngineerWithWorkload(
                user = engineer,
                activeJobCount = activeCount,
                todayJobCount = todayCount
            )
        }

        val filtered = if (query.isBlank()) {
            engineersWithWorkload
        } else {
            val lowerQuery = query.lowercase()
            engineersWithWorkload.filter { ewl ->
                ewl.user.displayName.lowercase().contains(lowerQuery) ||
                    ewl.user.email.lowercase().contains(lowerQuery)
            }
        }

        DatabaseState.Success(filtered) as DatabaseState<List<EngineerWithWorkload>>
    }
        .catch { e -> emit(DatabaseState.Failure(e.message ?: "Failed to load engineers")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )

    val selectedEngineerJobs: StateFlow<List<Job>> = combine(
        jobsFlow,
        _selectedEngineer
    ) { jobs, engineer ->
        if (engineer == null) {
            emptyList()
        } else {

            jobs.filter { job ->
                val status = job.status.toJobStatus()
                job.assignedTo == engineer.uid &&
                    status != JobStatus.Completed &&
                    status != JobStatus.Cancelled &&
                    status != JobStatus.Dismissed
            }.sortedBy { it.scheduledStart }
        }
    }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun selectEngineer(user: User) {
        _selectedEngineer.value = user
    }

    fun clearSelectedEngineer() {
        _selectedEngineer.value = null
    }

}
