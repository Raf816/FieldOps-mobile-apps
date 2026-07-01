package com.raf.fieldops.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AdminBadgeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `role badge shows Engineer for engineer role`() {
        composeRule.setContent { UserRoleBadge(role = "engineer") }
        composeRule.onNodeWithText("Engineer").assertIsDisplayed()
    }

    @Test
    fun `role badge shows Dispatcher for dispatcher role`() {
        composeRule.setContent { UserRoleBadge(role = "dispatcher") }
        composeRule.onNodeWithText("Dispatcher").assertIsDisplayed()
    }

    @Test
    fun `role badge capitalises unknown role`() {
        composeRule.setContent { UserRoleBadge(role = "admin") }
        composeRule.onNodeWithText("Admin").assertIsDisplayed()
    }

    @Test
    fun `status badge shows Active for active status`() {
        composeRule.setContent { UserStatusBadge(status = "active") }
        composeRule.onNodeWithText("Active").assertIsDisplayed()
    }

    @Test
    fun `status badge shows Pending for pending status`() {
        composeRule.setContent { UserStatusBadge(status = "pending") }
        composeRule.onNodeWithText("Pending").assertIsDisplayed()
    }

    @Test
    fun `status badge shows Suspended for suspended status`() {
        composeRule.setContent { UserStatusBadge(status = "suspended") }
        composeRule.onNodeWithText("Suspended").assertIsDisplayed()
    }
}
