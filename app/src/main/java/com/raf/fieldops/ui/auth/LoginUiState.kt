package com.raf.fieldops.ui.auth

import android.util.Patterns

data class LoginUiState(
    val email: String = "",
    val password: String = ""
) {

    fun emailIsInvalid(): Boolean =
        !Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun passwordIsInvalid(): Boolean = password.length < 6

    fun isValid(): Boolean = !emailIsInvalid() && !passwordIsInvalid()
}
