package com.raf.fieldops.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CustomDropDownMenuTest {

    @get:Rule
    val rule = createComposeRule()

    private val testOptions = listOf("Low", "Medium", "High", "Urgent")

    @Test
    fun `dropdown displays label`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Priority",
                options = testOptions,
                selectedValue = null,
                onOptionSelected = {},
                displayText = { it }
            )
        }

        rule.onNode(hasText("Priority")).assertIsDisplayed()
    }

    @Test
    fun `dropdown displays selected value`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Priority",
                options = testOptions,
                selectedValue = "High",
                onOptionSelected = {},
                displayText = { it }
            )
        }

        rule.onNode(hasText("High")).assertIsDisplayed()
    }

    @Test
    fun `dropdown shows empty when no selection`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Priority",
                options = testOptions,
                selectedValue = null,
                onOptionSelected = {},
                displayText = { it }
            )
        }

        testOptions.forEach { option ->
            rule.onNode(hasText(option)).assertDoesNotExist()
        }
    }

    @Test
    fun `dropdown has accessibility content description`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Priority",
                options = testOptions,
                selectedValue = null,
                onOptionSelected = {},
                displayText = { it }
            )
        }

        rule.onNode(hasContentDescription("Priority dropdown menu"))
            .assertIsDisplayed()
    }

    @Test
    fun `dropdown displays error when provided`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Engineer",
                options = testOptions,
                selectedValue = null,
                onOptionSelected = {},
                displayText = { it },
                error = "Please select an engineer"
            )
        }

        rule.onNode(hasText("Please select an engineer")).assertIsDisplayed()
    }

    @Test
    fun `dropdown does not show error when null`() {

        rule.setContent {
            CustomDropDownMenu(
                label = "Priority",
                options = testOptions,
                selectedValue = "Medium",
                onOptionSelected = {},
                displayText = { it },
                error = null
            )
        }

        rule.onNode(hasText("Please select")).assertDoesNotExist()
    }
}
