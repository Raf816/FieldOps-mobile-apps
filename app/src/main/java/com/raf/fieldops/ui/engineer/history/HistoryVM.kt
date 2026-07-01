package com.raf.fieldops.ui.engineer.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo
) : ViewModel() {

    val jobsState: StateFlow<DatabaseState<List<Job>>> = run {
        val uid = authRepo.currentUser?.uid ?: ""
        jobRepo.getCompletedJobsForEngineer(uid)
            .map { jobs ->

                val sortedJobs = jobs.sortedByDescending { it.updatedAt }
                DatabaseState.Success(sortedJobs) as DatabaseState<List<Job>>
            }
            .catch { e ->
                emit(DatabaseState.Failure(e.message ?: "Failed to load job history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DatabaseState.Loading
            )
    }
}
