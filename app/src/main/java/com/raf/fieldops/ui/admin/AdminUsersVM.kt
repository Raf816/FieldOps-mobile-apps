package com.raf.fieldops.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminUsersVM @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _roleFilter = MutableStateFlow("all")
    val roleFilter: StateFlow<String> = _roleFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow("all")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onRoleFilterChanged(filter: String) { _roleFilter.value = filter }
    fun onStatusFilterChanged(filter: String) { _statusFilter.value = filter }
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    val users: StateFlow<DatabaseState<List<User>>> = combine(
        userRepo.getAllUsers(),
        _roleFilter,
        _statusFilter,
        _searchQuery
    ) { allUsers, role, status, query ->
        var filtered = allUsers

        if (role != "all") {
            filtered = filtered.filter { it.role.equals(role, ignoreCase = true) }
        }

        if (status != "all") {
            filtered = filtered.filter { it.status.equals(status, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter { user ->
                user.displayName.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
            }
        }

        DatabaseState.Success(filtered) as DatabaseState<List<User>>
    }
        .catch { e ->
            emit(DatabaseState.Failure(e.message ?: "Failed to load users"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseState.Loading
        )
}
