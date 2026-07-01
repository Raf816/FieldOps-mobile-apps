package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.ui.theme.FieldOpsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatusChipTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `status chip displays correct text for Assigned`() {
        rule.setContent {
            FieldOpsTheme { StatusChip(status = JobStatus.Assigned) }
        }

        rule.onNode(hasText("Assigned")).assertIsDisplayed()
        rule.onNode(hasContentDescription("Assigned status chip")).assertIsDisplayed()
    }

    @Test
    fun `status chip displays correct text for InProgress`() {

        rule.setContent {
            FieldOpsTheme { StatusChip(status = JobStatus.InProgress) }
        }

        rule.onNode(hasText("In Progress")).assertIsDisplayed()
        rule.onNode(hasContentDescription("In Progress status chip")).assertIsDisplayed()
    }

    @Test
    fun `status chip displays correct text for Completed`() {
        rule.setContent {
            FieldOpsTheme { StatusChip(status = JobStatus.Completed) }
        }

        rule.onNode(hasText("Completed")).assertIsDisplayed()
        rule.onNode(hasContentDescription("Completed status chip")).assertIsDisplayed()
    }

    @Test
    fun `status chip exists for each status value`() {

        rule.setContent {
            FieldOpsTheme {
                androidx.compose.foundation.layout.Column {
                    JobStatus.entries.forEach { status ->
                        StatusChip(status = status)
                    }
                }
            }
        }

        JobStatus.entries.forEach { status ->
            rule.onNode(hasText(status.displayName())).assertIsDisplayed()
        }
    }
}
