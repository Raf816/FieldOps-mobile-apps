package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.ui.theme.FieldOpsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PriorityBadgeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `badge displays correct text for Low`() {
        rule.setContent {
            FieldOpsTheme { PriorityBadge(priority = Priority.Low) }
        }

        rule.onNode(hasText("LOW")).assertIsDisplayed()
        rule.onNode(hasContentDescription("Low priority badge")).assertIsDisplayed()
    }

    @Test
    fun `badge displays correct text for Medium`() {
        rule.setContent {
            FieldOpsTheme { PriorityBadge(priority = Priority.Medium) }
        }

        rule.onNode(hasText("MEDIUM")).assertIsDisplayed()
        rule.onNode(hasContentDescription("Medium priority badge")).assertIsDisplayed()
    }

    @Test
    fun `badge displays correct text for High`() {
        rule.setContent {
            FieldOpsTheme { PriorityBadge(priority = Priority.High) }
        }

        rule.onNode(hasText("HIGH")).assertIsDisplayed()
        rule.onNode(hasContentDescription("High priority badge")).assertIsDisplayed()
    }

    @Test
    fun `badge displays correct text for Urgent`() {
        rule.setContent {
            FieldOpsTheme { PriorityBadge(priority = Priority.Urgent) }
        }

        rule.onNode(hasText("URGENT")).assertIsDisplayed()
        rule.onNode(hasContentDescription("Urgent priority badge")).assertIsDisplayed()
    }
}
