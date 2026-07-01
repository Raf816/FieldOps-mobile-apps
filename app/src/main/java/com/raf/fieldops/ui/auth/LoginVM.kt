package com.raf.fieldops.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.repo.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    var loginUiState by mutableStateOf(LoginUiState())
        private set

    fun onChange(
        email: String = loginUiState.email,
        password: String = loginUiState.password
    ) {
        loginUiState = loginUiState.copy(email = email, password = password)
    }

    var signInResponse by mutableStateOf<Response>(Response.Startup)
        private set

    val isEmailVerified: Boolean
        get() = authRepo.isEmailVerified

    private val _uiEvents = Channel<String>()

    val uiEvents = _uiEvents.receiveAsFlow()

    fun signInWithEmailAndPassword() {
        viewModelScope.launch {
            signInResponse = Response.Loading
            signInResponse = authRepo.signInWithEmailAndPassword(
                loginUiState.email,
                loginUiState.password
            )

            if (signInResponse is Response.Failure) {
                val errorMessage = (signInResponse as Response.Failure).e.message
                    ?: "Unable to sign in"
                _uiEvents.send("Unable to sign in: $errorMessage")
            }
        }
    }

    fun forgotPassword() {
        viewModelScope.launch {
            val result = authRepo.sendPasswordResetEmail(loginUiState.email)
            val message = when (result) {
                is Response.Success -> "Password reset email has been sent successfully"
                is Response.Failure -> "Unable to send reset email"
                else -> "An unexpected error occurred"
            }
            _uiEvents.send(message)
        }
    }
}
