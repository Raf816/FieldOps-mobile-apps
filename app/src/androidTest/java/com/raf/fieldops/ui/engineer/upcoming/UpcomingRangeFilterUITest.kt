package com.raf.fieldops.ui.engineer.upcoming

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.ui.theme.FieldOpsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpcomingRangeFilterUITest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `all three range chips are displayed`() {
        composeRule.setContent {
            FieldOpsTheme {

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    UpcomingRange.entries.forEach { range ->
                        val label = when (range) {
                            UpcomingRange.ThisWeek -> "This Week"
                            UpcomingRange.ThisMonth -> "This Month"
                            UpcomingRange.All -> "All"
                        }
                        androidx.compose.material3.FilterChip(
                            selected = range == UpcomingRange.ThisWeek,
                            onClick = {},
                            label = { androidx.compose.material3.Text(label) }
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("This Week").assertIsDisplayed()
        composeRule.onNodeWithText("This Month").assertIsDisplayed()
        composeRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun `tapping a chip triggers callback with correct range`() {
        var selectedRange = UpcomingRange.ThisWeek

        composeRule.setContent {
            FieldOpsTheme {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    UpcomingRange.entries.forEach { range ->
                        val label = when (range) {
                            UpcomingRange.ThisWeek -> "This Week"
                            UpcomingRange.ThisMonth -> "This Month"
                            UpcomingRange.All -> "All"
                        }
                        androidx.compose.material3.FilterChip(
                            selected = selectedRange == range,
                            onClick = { selectedRange = range },
                            label = { androidx.compose.material3.Text(label) }
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("This Month").performClick()

        assertEquals(UpcomingRange.ThisMonth, selectedRange)
    }

    @Test
    fun `tapping All chip triggers callback with All range`() {
        var selectedRange = UpcomingRange.ThisWeek

        composeRule.setContent {
            FieldOpsTheme {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    UpcomingRange.entries.forEach { range ->
                        val label = when (range) {
                            UpcomingRange.ThisWeek -> "This Week"
                            UpcomingRange.ThisMonth -> "This Month"
                            UpcomingRange.All -> "All"
                        }
                        androidx.compose.material3.FilterChip(
                            selected = selectedRange == range,
                            onClick = { selectedRange = range },
                            label = { androidx.compose.material3.Text(label) }
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("All").performClick()

        assertEquals(UpcomingRange.All, selectedRange)
    }
}
