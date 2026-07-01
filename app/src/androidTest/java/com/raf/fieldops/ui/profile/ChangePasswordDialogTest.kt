package com.raf.fieldops.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ChangePasswordDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `dialog displays all three password fields`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Current Password").assertIsDisplayed()
        rule.onNodeWithText("New Password").assertIsDisplayed()
        rule.onNodeWithText("Confirm New Password").assertIsDisplayed()
    }

    @Test
    fun `dialog displays title`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Change Password").assertIsDisplayed()
    }

    @Test
    fun `dialog shows error message when provided`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                errorMessage = "Current password is incorrect",
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Current password is incorrect").assertIsDisplayed()
    }

    @Test
    fun `dialog does not show error when null`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNode(hasText("Current password is incorrect")).assertDoesNotExist()
    }

    @Test
    fun `confirm button triggers onConfirm callback`() {
        var confirmCalled = false

        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "old123",
                newPassword = "new123",
                confirmNewPassword = "new123",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = { confirmCalled = true },
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Update Password").performClick()
        assertTrue("onConfirm should have been called", confirmCalled)
    }

    @Test
    fun `cancel button triggers onDismiss callback`() {
        var dismissCalled = false

        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = { dismissCalled = true }
            )
        }

        rule.onNodeWithText("Cancel").performClick()
        assertTrue("onDismiss should have been called", dismissCalled)
    }

    @Test
    fun `confirm button is disabled when loading`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "old123",
                newPassword = "new123",
                confirmNewPassword = "new123",
                errorMessage = null,
                isLoading = true,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Update Password").assertDoesNotExist()
    }

    @Test
    fun `confirm button is enabled when not loading`() {
        rule.setContent {
            ChangePasswordDialog(
                currentPassword = "old123",
                newPassword = "new123",
                confirmNewPassword = "new123",
                errorMessage = null,
                isLoading = false,
                onCurrentPasswordChange = {},
                onNewPasswordChange = {},
                onConfirmNewPasswordChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }

        rule.onNodeWithText("Update Password").assertIsDisplayed()
    }
}
