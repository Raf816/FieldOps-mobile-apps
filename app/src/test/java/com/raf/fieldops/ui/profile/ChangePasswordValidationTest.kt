package com.raf.fieldops.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChangePasswordValidationTest {

    private fun validatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): String? {
        return when {
            currentPassword.isBlank() -> "Current password is required"
            newPassword.length < 6 -> "New password must be at least 6 characters"
            newPassword != confirmNewPassword -> "Passwords do not match"
            newPassword == currentPassword -> "New password must be different from current password"
            else -> null
        }
    }

    @Test
    fun validatePasswordChange_emptyCurrentPassword_returnsRequired() {
        val result = validatePasswordChange(
            currentPassword = "",
            newPassword = "NewPass123",
            confirmNewPassword = "NewPass123"
        )
        assertEquals("Current password is required", result)
    }

    @Test
    fun validatePasswordChange_blankCurrentPassword_returnsRequired() {
        val result = validatePasswordChange(
            currentPassword = "   ",
            newPassword = "NewPass123",
            confirmNewPassword = "NewPass123"
        )
        assertEquals("Current password is required", result)
    }

    @Test
    fun validatePasswordChange_newPasswordTooShort_returnsMinLength() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "abc",
            confirmNewPassword = "abc"
        )
        assertEquals("New password must be at least 6 characters", result)
    }

    @Test
    fun validatePasswordChange_newPasswordExactly5Chars_returnsMinLength() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "abcde",
            confirmNewPassword = "abcde"
        )
        assertEquals("New password must be at least 6 characters", result)
    }

    @Test
    fun validatePasswordChange_newPasswordExactly6Chars_passes() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "abcdef",
            confirmNewPassword = "abcdef"
        )
        assertNull(result)
    }

    @Test
    fun validatePasswordChange_passwordsDoNotMatch_returnsMismatch() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "NewPass123",
            confirmNewPassword = "DifferentPass"
        )
        assertEquals("Passwords do not match", result)
    }

    @Test
    fun validatePasswordChange_passwordsMatch_passes() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "NewPass123",
            confirmNewPassword = "NewPass123"
        )
        assertNull(result)
    }

    @Test
    fun validatePasswordChange_newSameAsCurrent_returnsMustBeDifferent() {
        val result = validatePasswordChange(
            currentPassword = "SamePass123",
            newPassword = "SamePass123",
            confirmNewPassword = "SamePass123"
        )
        assertEquals("New password must be different from current password", result)
    }

    @Test
    fun validatePasswordChange_allValid_returnsNull() {
        val result = validatePasswordChange(
            currentPassword = "OldPassword1",
            newPassword = "NewPassword2",
            confirmNewPassword = "NewPassword2"
        )
        assertNull(result)
    }

    @Test
    fun validatePasswordChange_newPasswordWithSpaces_passes() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "pass with spaces",
            confirmNewPassword = "pass with spaces"
        )
        assertNull(result)
    }

    @Test
    fun validatePasswordChange_emptyNewPassword_returnsMinLength() {
        val result = validatePasswordChange(
            currentPassword = "OldPass123",
            newPassword = "",
            confirmNewPassword = ""
        )
        assertEquals("New password must be at least 6 characters", result)
    }
}
