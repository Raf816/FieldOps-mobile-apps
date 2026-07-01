package com.raf.fieldops.ui.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserDetailVM @Inject constructor(
    private val userRepo: UserRepo,
    private val jobRepo: JobRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userUid: String = savedStateHandle.get<String>("uid") ?: ""

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        AppLogger.error("AdminUserDetailVM", "Error: ${exception.message}")
        _actionResult.value = ActionResult.Error(exception.message ?: "An error occurred")
    }

    private val _actionResult = MutableStateFlow<ActionResult>(ActionResult.Idle)
    val actionResult: StateFlow<ActionResult> = _actionResult.asStateFlow()

    fun clearActionResult() { _actionResult.value = ActionResult.Idle }

    val user: StateFlow<DatabaseState<User?>> = userRepo.observeUser(userUid)
        .map { user ->
            DatabaseState.Success(user) as DatabaseState<User?>
        }
        .catch { e ->
            emit(DatabaseState.Failure(e.message ?: "Failed to load user"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )

    val activeJobCount: StateFlow<Int> = jobRepo.getAllJobs()
        .map { jobs ->
            jobs.count { it.assignedTo == userUid && it.status != "Completed" && it.status != "Cancelled" }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun changeRole(newRole: String) {
        viewModelScope.launch(errorHandler) {
            userRepo.updateUserRole(userUid, newRole)
            _actionResult.value = ActionResult.Success("Role updated to ${newRole.replaceFirstChar { it.uppercase() }}")
        }
    }

    fun suspendUser() {
        viewModelScope.launch(errorHandler) {
            userRepo.updateUserStatus(userUid, "suspended")
            _actionResult.value = ActionResult.Success("User suspended")
        }
    }

    fun unsuspendUser() {
        viewModelScope.launch(errorHandler) {
            userRepo.updateUserStatus(userUid, "active")
            _actionResult.value = ActionResult.Success("User unsuspended")
        }
    }

    fun approveUser() {
        viewModelScope.launch(errorHandler) {
            userRepo.updateUserStatus(userUid, "active")
            _actionResult.value = ActionResult.Success("User approved")
        }
    }

    fun rejectUser(onRejected: () -> Unit) {
        viewModelScope.launch(errorHandler) {
            userRepo.deleteUser(userUid)
            _actionResult.value = ActionResult.Success("User rejected and removed")
            onRejected()
        }
    }

    fun deleteUser(onDeleted: () -> Unit) {
        viewModelScope.launch(errorHandler) {

            val allJobs = jobRepo.getAllJobs().first()
            val userJobs = allJobs.filter {
                it.assignedTo == userUid &&
                    it.status != "Completed" &&
                    it.status != "Cancelled"
            }

            userJobs.forEach { job ->
                val updatedJob = job.copy(
                    assignedTo = "",
                    assignedEngineerName = "",
                    status = "Assigned"
                )
                jobRepo.updateJob(updatedJob)
            }

            userRepo.deleteUser(userUid)

            _actionResult.value = ActionResult.Success(
                "User deleted. ${userJobs.size} job(s) unassigned."
            )
            onDeleted()
        }
    }
}

sealed class ActionResult {
    data object Idle : ActionResult()
    data class Success(val message: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}
