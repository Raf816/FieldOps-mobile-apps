package com.raf.fieldops.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerVM @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        AppLogger.error("DrawerVM", "Error loading user: ${exception.message}")
    }

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch(errorHandler) {
            val uid = authRepo.currentUser?.uid ?: return@launch
            val user = userRepo.getUserById(uid)
            _displayName.value = user?.displayName ?: ""
            _email.value = user?.email ?: authRepo.currentUser?.email ?: ""
        }
    }
}
