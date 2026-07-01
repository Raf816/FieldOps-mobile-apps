package com.raf.fieldops.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CustomTextFieldTest {

    @get:Rule
    val rule = createComposeRule()

    private val label = "Email"

    private val fieldMatcher = hasContentDescription("$label text field")

    @Test
    fun `text field displays label`() {

        rule.setContent {
            CustomTextField(label = label, value = "", onValueChange = {})
        }

        rule.onNode(hasText(label)).assertIsDisplayed()
    }

    @Test
    fun `text field accepts and displays input`() {

        var currentValue by mutableStateOf("")
        rule.setContent {
            CustomTextField(
                label = label,
                value = currentValue,
                onValueChange = { currentValue = it }
            )
        }

        rule.onNode(fieldMatcher).performTextInput("test@example.com")

        rule.onNode(hasText("test@example.com")).assertIsDisplayed()
    }

    @Test
    fun `error text is not displayed when error is null`() {

        val errorText = "Enter a valid email address"
        rule.setContent {
            CustomTextField(label = label, value = "", onValueChange = {}, error = null)
        }

        rule.onNode(hasText(errorText)).assertDoesNotExist()
    }

    @Test
    fun `error text is displayed when error is provided`() {

        val errorText = "Enter a valid email address"
        rule.setContent {
            CustomTextField(
                label = label,
                value = "bad",
                onValueChange = {},
                error = errorText
            )
        }

        rule.onNode(hasText(errorText)).assertIsDisplayed()
    }

    @Test
    fun `error semantic property is set when error is provided`() {

        val errorText = "Enter a valid email address"
        rule.setContent {
            CustomTextField(
                label = label,
                value = "bad",
                onValueChange = {},
                error = errorText
            )
        }

        rule.onNode(
            fieldMatcher and SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
        ).assertExists()
    }

    @Test
    fun `password field masks input with asterisks`() {

        val passwordText = "secret"
        rule.setContent {
            CustomTextField(
                label = "Password",
                value = passwordText,
                onValueChange = {},
                isPasswordField = true
            )
        }

        val expectedMask = "*".repeat(passwordText.length)
        rule.onNode(hasText(expectedMask)).assertIsDisplayed()
    }

    @Test
    fun `non-password field shows plain text`() {

        val plainText = "hello@test.com"
        rule.setContent {
            CustomTextField(
                label = label,
                value = plainText,
                onValueChange = {}
            )
        }

        rule.onNode(hasText(plainText)).assertIsDisplayed()
    }

    @Test
    fun `password field shows visibility toggle icon`() {

        rule.setContent {
            CustomTextField(
                label = "Password",
                value = "secret",
                onValueChange = {},
                isPasswordField = true
            )
        }

        rule.onNodeWithContentDescription("Show password").assertIsDisplayed()
    }

    @Test
    fun `non-password field does not show visibility toggle`() {

        rule.setContent {
            CustomTextField(
                label = label,
                value = "hello",
                onValueChange = {},
                isPasswordField = false
            )
        }

        rule.onNodeWithContentDescription("Show password").assertDoesNotExist()
        rule.onNodeWithContentDescription("Hide password").assertDoesNotExist()
    }

    @Test
    fun `tapping visibility toggle reveals password text`() {

        val passwordText = "secret"
        rule.setContent {
            CustomTextField(
                label = "Password",
                value = passwordText,
                onValueChange = {},
                isPasswordField = true
            )
        }

        rule.onNodeWithContentDescription("Show password").performClick()

        rule.onNode(hasText(passwordText)).assertIsDisplayed()
        rule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun `tapping visibility toggle twice re-hides password`() {

        val passwordText = "secret"
        rule.setContent {
            CustomTextField(
                label = "Password",
                value = passwordText,
                onValueChange = {},
                isPasswordField = true
            )
        }

        rule.onNodeWithContentDescription("Show password").performClick()
        rule.onNodeWithContentDescription("Hide password").performClick()

        val expectedMask = "*".repeat(passwordText.length)
        rule.onNode(hasText(expectedMask)).assertIsDisplayed()
        rule.onNodeWithContentDescription("Show password").assertIsDisplayed()
    }
}
