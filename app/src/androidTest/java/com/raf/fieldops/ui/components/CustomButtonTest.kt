package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CustomButtonTest {

    @get:Rule
    val rule = createComposeRule()

    private val buttonText = "Sign In"

    private val buttonMatcher = hasContentDescription("$buttonText button")

    @Test
    fun `button exists and is displayed with correct text`() {

        rule.setContent {
            CustomButton(text = buttonText, onClick = {})
        }

        rule.onNode(buttonMatcher).assertIsDisplayed()
        rule.onNode(hasText(buttonText)).assertIsDisplayed()
    }

    @Test
    fun `button is clickable and invokes onClick lambda`() {

        var wasClicked = false
        rule.setContent {
            CustomButton(text = buttonText, onClick = { wasClicked = true })
        }

        rule.onNode(buttonMatcher).performClick()

        assertTrue("onClick lambda should have been called", wasClicked)
    }

    @Test
    fun `button click action exists`() {

        rule.setContent {
            CustomButton(text = buttonText, onClick = {})
        }

        rule.onNode(buttonMatcher).assertHasClickAction()
    }

    @Test
    fun `button is enabled by default`() {

        rule.setContent {
            CustomButton(text = buttonText, onClick = {})
        }

        rule.onNode(buttonMatcher).assertIsEnabled()
    }

    @Test
    fun `button is disabled when enabled is false`() {

        rule.setContent {
            CustomButton(text = buttonText, onClick = {}, enabled = false)
        }

        rule.onNode(buttonMatcher).assertIsNotEnabled()
    }
}
