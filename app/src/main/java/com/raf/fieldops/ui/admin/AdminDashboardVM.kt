package com.raf.fieldops.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AdminDashboardState(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val pendingUsers: Int = 0,
    val suspendedUsers: Int = 0,
    val recentUsers: List<User> = emptyList(),
    val adminName: String = ""
)

@HiltViewModel
class AdminDashboardVM @Inject constructor(
    private val userRepo: UserRepo,
    private val authRepo: AuthRepo
) : ViewModel() {

    val state: StateFlow<DatabaseState<AdminDashboardState>> = userRepo.getAllUsers()
        .map { users ->

            val adminName = authRepo.currentUser?.uid?.let { uid ->
                userRepo.getUserById(uid)?.displayName ?: ""
            } ?: ""

            val dashboardState = AdminDashboardState(
                totalUsers = users.size,
                activeUsers = users.count { it.status == "active" },
                pendingUsers = users.count { it.status == "pending" },
                suspendedUsers = users.count { it.status == "suspended" },

                recentUsers = users
                    .sortedByDescending { it.createdAt }
                    .take(5),
                adminName = adminName
            )
            DatabaseState.Success(dashboardState) as DatabaseState<AdminDashboardState>
        }
        .catch { e ->
            emit(DatabaseState.Failure(e.message ?: "Failed to load dashboard data"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )
}
