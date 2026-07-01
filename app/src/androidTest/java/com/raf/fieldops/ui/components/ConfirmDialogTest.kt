package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.ui.theme.FieldOpsTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private val testTitle = "Cancel Job?"
    private val testMessage = "This will delete all notes and cannot be undone."

    @Test
    fun `dialog displays title and message`() {
        rule.setContent {
            FieldOpsTheme {
                ConfirmDialog(
                    title = testTitle,
                    message = testMessage,
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        rule.onNode(hasText(testTitle)).assertIsDisplayed()
        rule.onNode(hasText(testMessage)).assertIsDisplayed()
    }

    @Test
    fun `confirm button invokes onConfirm`() {
        var wasConfirmed = false
        rule.setContent {
            FieldOpsTheme {
                ConfirmDialog(
                    title = testTitle,
                    message = testMessage,
                    onConfirm = { wasConfirmed = true },
                    onDismiss = {}
                )
            }
        }

        rule.onNode(
            hasContentDescription("Confirm confirm button")
        ).performClick()

        assertTrue("onConfirm lambda should have been called", wasConfirmed)
    }

    @Test
    fun `dismiss button invokes onDismiss`() {
        var wasDismissed = false
        rule.setContent {
            FieldOpsTheme {
                ConfirmDialog(
                    title = testTitle,
                    message = testMessage,
                    onConfirm = {},
                    onDismiss = { wasDismissed = true }
                )
            }
        }

        rule.onNode(
            hasContentDescription("Cancel dismiss button")
        ).performClick()

        assertTrue("onDismiss lambda should have been called", wasDismissed)
    }

    @Test
    fun `dialog displays custom button text`() {
        rule.setContent {
            FieldOpsTheme {
                ConfirmDialog(
                    title = testTitle,
                    message = testMessage,
                    confirmText = "Delete Job",
                    dismissText = "Keep It",
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        rule.onNode(hasText("Delete Job")).assertIsDisplayed()
        rule.onNode(hasText("Keep It")).assertIsDisplayed()
    }
}
