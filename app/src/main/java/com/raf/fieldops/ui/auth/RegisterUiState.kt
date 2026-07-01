package com.raf.fieldops.ui.auth

import android.util.Patterns

enum class PasswordStrength {

    WEAK,

    MEDIUM,

    STRONG
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    val hasMinLength = password.length >= 6
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }

    return when {
        !hasMinLength -> PasswordStrength.WEAK
        hasUppercase && hasDigit -> PasswordStrength.STRONG
        else -> PasswordStrength.MEDIUM
    }
}

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: String = "engineer"
) {

    fun nameIsInvalid(): Boolean = displayName.trim().length < 2 || displayName.any { it.isDigit() }

    fun nameContainsNumbers(): Boolean = displayName.any { it.isDigit() }

    fun emailIsInvalid(): Boolean =
        !Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun passwordIsInvalid(): Boolean = password.length < 6

    fun passwordsDoNotMatch(): Boolean = password != confirmPassword

    fun isValid(): Boolean =
        !nameIsInvalid() &&
            !emailIsInvalid() &&
            !passwordIsInvalid() &&
            !passwordsDoNotMatch()
}
