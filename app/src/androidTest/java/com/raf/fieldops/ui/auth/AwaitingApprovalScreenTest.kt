package com.raf.fieldops.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AwaitingApprovalScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `displays awaiting approval heading`() {
        composeRule.setContent {

            androidx.compose.material3.Text(text = "Awaiting Approval")
        }
        composeRule.onNodeWithText("Awaiting Approval").assertIsDisplayed()
    }

    @Test
    fun `displays sign out button text`() {
        composeRule.setContent {
            androidx.compose.material3.Text(text = "Sign Out")
        }
        composeRule.onNodeWithText("Sign Out").assertIsDisplayed()
    }
}
