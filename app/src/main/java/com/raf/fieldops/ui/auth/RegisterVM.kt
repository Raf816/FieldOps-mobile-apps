package com.raf.fieldops.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.InputSanitiser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterVM @Inject constructor(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    var registerUiState by mutableStateOf(RegisterUiState())
        private set

    fun onChange(
        displayName: String = registerUiState.displayName,
        email: String = registerUiState.email,
        password: String = registerUiState.password,
        confirmPassword: String = registerUiState.confirmPassword,
        role: String = registerUiState.role
    ) {
        registerUiState = registerUiState.copy(
            displayName = displayName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            role = role
        )
    }

    var signUpResponse by mutableStateOf<Response>(Response.Startup)
        private set

    var sendEmailVerificationResponse by mutableStateOf<Response>(Response.Startup)
        private set

    private val _uiEvents = Channel<String>()

    val uiEvents = _uiEvents.receiveAsFlow()

    fun signUpWithEmailAndPassword() {
        viewModelScope.launch {
            if (!registerUiState.isValid()) return@launch

            signUpResponse = Response.Loading

            val authResult = authRepo.signUpWithEmailAndPassword(
                registerUiState.email,
                registerUiState.password
            )

            if (authResult is Response.Failure) {
                signUpResponse = authResult
                val errorMessage = authResult.e.message ?: "Unable to create account"
                _uiEvents.send(errorMessage)
                return@launch
            }

            sendEmailVerification()

            createUserDocument()
        }
    }

    private suspend fun sendEmailVerification() {
        sendEmailVerificationResponse = Response.Loading
        sendEmailVerificationResponse = authRepo.sendEmailVerification()

        if (sendEmailVerificationResponse is Response.Failure) {
            _uiEvents.send("Unable to send verification email")
        }
    }

    private suspend fun createUserDocument() {

        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            try {
                val uid = authRepo.currentUser?.uid ?: run {
                    _uiEvents.send("Unable to create user profile")
                    return@withContext
                }

                val user = User(
                    uid = uid,
                    displayName = InputSanitiser.sanitiseShort(registerUiState.displayName),
                    email = registerUiState.email.trim(),
                    role = registerUiState.role,
                    status = "pending"
                )

                userRepo.createUser(user)

                signUpResponse = Response.Success
                _uiEvents.send("Confirm details via email")
            } catch (e: Exception) {
                signUpResponse = Response.Failure(e)
                _uiEvents.send("Unable to create user profile: ${e.message}")
            }
        }
    }
}
