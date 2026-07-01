package com.raf.fieldops.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
class AdminPendingVM @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        AppLogger.error("AdminPendingVM", "Error: ${exception.message}")
        _actionError.value = exception.message ?: "An error occurred"
    }

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun clearError() { _actionError.value = null }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    val pendingUsers: StateFlow<DatabaseState<List<User>>> = userRepo.getAllUsers()
        .map { users ->
            val pending = users.filter { it.status == "pending" }
            DatabaseState.Success(pending) as DatabaseState<List<User>>
        }
        .catch { e ->
            emit(DatabaseState.Failure(e.message ?: "Failed to load pending users"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )

    fun approveUser(uid: String) {
        viewModelScope.launch(errorHandler) {
            userRepo.updateUserStatus(uid, "active")
        }
    }

    fun rejectUser(uid: String) {
        viewModelScope.launch(errorHandler) {
            userRepo.deleteUser(uid)
        }
    }

    fun approveAll(users: List<User>) {
        viewModelScope.launch(errorHandler) {
            users.forEach { user ->
                userRepo.updateUserStatus(user.uid, "active")
            }
        }
    }

    fun rejectAll(users: List<User>) {
        viewModelScope.launch(errorHandler) {
            users.forEach { user ->
                userRepo.deleteUser(user.uid)
            }
        }
    }
}
