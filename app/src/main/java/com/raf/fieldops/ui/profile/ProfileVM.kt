package com.raf.fieldops.ui.profile

import com.raf.fieldops.util.AppLogger
import com.raf.fieldops.util.InputSanitiser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.local.CachedJobDao
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.ThemeDataStore
import com.raf.fieldops.util.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileVM @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val cachedJobDao: CachedJobDao,
    private val themeDataStore: ThemeDataStore
) : ViewModel() {

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        AppLogger.error("ProfileVM", "Error: ${exception.message}")
    }

    private val _user = MutableStateFlow<User?>(null)

    val user: StateFlow<User?> = _user.asStateFlow()

    val themePreference: StateFlow<ThemePreference> = themeDataStore.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.System
        )

    private val _showSignOutDialog = MutableStateFlow(false)

    val showSignOutDialog: StateFlow<Boolean> = _showSignOutDialog.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)

    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

    private val _showEditNameDialog = MutableStateFlow(false)

    val showEditNameDialog: StateFlow<Boolean> = _showEditNameDialog.asStateFlow()

    private val _editNameText = MutableStateFlow("")

    val editNameText: StateFlow<String> = _editNameText.asStateFlow()

    private val _nameUpdateSuccess = MutableStateFlow(false)

    val nameUpdateSuccess: StateFlow<Boolean> = _nameUpdateSuccess.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)

    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _currentPassword = MutableStateFlow("")

    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")

    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmNewPassword = MutableStateFlow("")

    val confirmNewPassword: StateFlow<String> = _confirmNewPassword.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)

    val passwordChangeError: StateFlow<String?> = _passwordChangeError.asStateFlow()

    private val _passwordChangeSuccess = MutableStateFlow(false)

    val passwordChangeSuccess: StateFlow<Boolean> = _passwordChangeSuccess.asStateFlow()

    private val _isChangingPassword = MutableStateFlow(false)

    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch(errorHandler) {
            val uid = authRepo.currentUser?.uid ?: return@launch
            val fetchedUser = userRepo.getUserById(uid)
            _user.value = fetchedUser
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch(errorHandler) {
            themeDataStore.setThemePreference(preference)
        }
    }

    fun showSignOutConfirmation() {
        _showSignOutDialog.value = true
    }

    fun dismissSignOutConfirmation() {
        _showSignOutDialog.value = false
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch(errorHandler) {
            _isSigningOut.value = true
            _showSignOutDialog.value = false

            authRepo.signOut()

            cachedJobDao.clearAll()

            _isSigningOut.value = false
            onSignedOut()
        }
    }

    fun showEditName() {
        _editNameText.value = _user.value?.displayName ?: ""
        _showEditNameDialog.value = true
    }

    fun dismissEditName() {
        _showEditNameDialog.value = false
    }

    fun onEditNameTextChange(text: String) {
        _editNameText.value = text
    }

    fun updateDisplayName() {
        val trimmedName = InputSanitiser.sanitiseShort(_editNameText.value)
        if (trimmedName.length < 2) return

        viewModelScope.launch(errorHandler) {
            val uid = authRepo.currentUser?.uid ?: return@launch
            userRepo.updateDisplayName(uid, trimmedName)

            loadUser()

            _showEditNameDialog.value = false
            _nameUpdateSuccess.value = true
        }
    }

    fun clearNameUpdateSuccess() {
        _nameUpdateSuccess.value = false
    }

    fun showChangePassword() {
        _currentPassword.value = ""
        _newPassword.value = ""
        _confirmNewPassword.value = ""
        _passwordChangeError.value = null
        _showChangePasswordDialog.value = true
    }

    fun dismissChangePassword() {
        _showChangePasswordDialog.value = false
        _passwordChangeError.value = null
    }

    fun onCurrentPasswordChange(text: String) {
        _currentPassword.value = text
        _passwordChangeError.value = null
    }

    fun onNewPasswordChange(text: String) {
        _newPassword.value = text
        _passwordChangeError.value = null
    }

    fun onConfirmNewPasswordChange(text: String) {
        _confirmNewPassword.value = text
        _passwordChangeError.value = null
    }

    fun changePassword() {
        val current = _currentPassword.value
        val new = _newPassword.value
        val confirm = _confirmNewPassword.value

        when {
            current.isBlank() -> {
                _passwordChangeError.value = "Current password is required"
                return
            }
            new.length < 6 -> {
                _passwordChangeError.value = "New password must be at least 6 characters"
                return
            }
            new != confirm -> {
                _passwordChangeError.value = "Passwords do not match"
                return
            }
            new == current -> {
                _passwordChangeError.value = "New password must be different from current password"
                return
            }
        }

        viewModelScope.launch(errorHandler) {
            _isChangingPassword.value = true
            _passwordChangeError.value = null

            val result = authRepo.changePassword(current, new)

            when (result) {
                is Response.Success -> {
                    _showChangePasswordDialog.value = false
                    _passwordChangeSuccess.value = true
                    _isChangingPassword.value = false
                }
                is Response.Failure -> {

                    val message = when {
                        result.e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ->
                            "Current password is incorrect"
                        result.e.message?.contains("invalid", ignoreCase = true) == true ->
                            "Current password is incorrect"
                        result.e.message?.contains("weak-password", ignoreCase = true) == true ->
                            "New password is too weak"
                        else -> result.e.message ?: "Failed to change password"
                    }
                    _passwordChangeError.value = message
                    _isChangingPassword.value = false
                }
                else -> {
                    _isChangingPassword.value = false
                }
            }
        }
    }

    fun clearPasswordChangeSuccess() {
        _passwordChangeSuccess.value = false
    }
}
