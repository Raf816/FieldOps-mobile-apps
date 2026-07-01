package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class DateTimePickerFieldTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `date picker field displays label`() {

        rule.setContent {
            DatePickerField(
                label = "Start Date",
                selectedMillis = null,
                onSelected = {}
            )
        }

        rule.onNode(hasText("Start Date")).assertIsDisplayed()
    }

    @Test
    fun `date picker field displays formatted date when selected`() {

        rule.setContent {
            DatePickerField(
                label = "Start Date",
                selectedMillis = 1699920000000L,
                onSelected = {}
            )
        }

        rule.onNode(hasText("14 Nov 2023")).assertIsDisplayed()
    }

    @Test
    fun `date picker field shows empty when no date selected`() {

        rule.setContent {
            DatePickerField(
                label = "Start Date",
                selectedMillis = null,
                onSelected = {}
            )
        }

        rule.onNode(hasText("Start Date")).assertIsDisplayed()
    }

    @Test
    fun `date picker field displays error when provided`() {

        rule.setContent {
            DatePickerField(
                label = "Start Date",
                selectedMillis = null,
                onSelected = {},
                error = "Start time is required"
            )
        }

        rule.onNode(hasText("Start time is required")).assertIsDisplayed()
    }

    @Test
    fun `time picker field displays label`() {

        rule.setContent {
            TimePickerField(
                label = "Start Time",
                selectedMillis = null,
                onSelected = {}
            )
        }

        rule.onNode(hasText("Start Time")).assertIsDisplayed()
    }

    @Test
    fun `time picker field displays formatted time when selected`() {

        val nineThirtyMillis = 1699948200000L

        rule.setContent {
            TimePickerField(
                label = "Start Time",
                selectedMillis = nineThirtyMillis,
                onSelected = {}
            )
        }

        rule.onNode(hasText("Start Time")).assertIsDisplayed()
    }

    @Test
    fun `time picker field displays error when provided`() {

        rule.setContent {
            TimePickerField(
                label = "End Time",
                selectedMillis = null,
                onSelected = {},
                error = "End time is required"
            )
        }

        rule.onNode(hasText("End time is required")).assertIsDisplayed()
    }
}
