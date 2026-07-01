package com.raf.fieldops.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.model.UserStatus
import com.raf.fieldops.data.model.toUserStatus
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatusGatingVM @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    val userEmail: String
        get() = authRepo.currentUser?.email ?: ""

    private val currentUid: String?
        get() = authRepo.currentUser?.uid

    val userStatus: StateFlow<UserStatus?> = currentUid?.let { uid ->
        userRepo.observeUser(uid)
            .map { user -> user?.status?.toUserStatus() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } ?: kotlinx.coroutines.flow.MutableStateFlow<UserStatus?>(null)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userRole: StateFlow<String?> = currentUid?.let { uid ->
        userRepo.observeUser(uid)
            .map { user -> user?.role }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } ?: kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun signOut() {
        authRepo.signOut()
    }
}
