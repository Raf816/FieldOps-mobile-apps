package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class JobCardTest {

    @get:Rule
    val rule = createComposeRule()

    private val testJob = Job(
        id = "test-job-1",
        title = "Install fibre optic cable",
        description = "Run new fibre from the exchange to the customer premises",
        address = "42 High Street, Manchester",
        scheduledStart = Timestamp(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }.time
        ),
        scheduledEnd = Timestamp(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
        ),
        priority = Priority.High.name,
        status = JobStatus.Assigned.name,
        assignedTo = "engineer-uid-1",
        assignedEngineerName = "James Wilson"
    )

    @Test
    fun `job card displays title`() {
        rule.setContent {
            FieldOpsTheme { JobCard(job = testJob, onClick = {}) }
        }

        rule.onNode(hasText("Install fibre optic cable")).assertIsDisplayed()
    }

    @Test
    fun `job card displays address`() {
        rule.setContent {
            FieldOpsTheme { JobCard(job = testJob, onClick = {}) }
        }

        rule.onNode(hasText("42 High Street, Manchester")).assertIsDisplayed()
    }

    @Test
    fun `job card displays time slot with date`() {
        rule.setContent {
            FieldOpsTheme { JobCard(job = testJob, onClick = {}) }
        }

        rule.onNode(hasText("09:30 – 11:00", substring = true)).assertIsDisplayed()
    }

    @Test
    fun `job card is clickable and invokes onClick`() {
        var wasClicked = false
        rule.setContent {
            FieldOpsTheme {
                JobCard(job = testJob, onClick = { wasClicked = true })
            }
        }

        rule.onNode(
            hasContentDescription("Install fibre optic cable job card")
        ).performClick()

        assertTrue("onClick lambda should have been called", wasClicked)
    }

    @Test
    fun `job card shows engineer name when showEngineerName is true`() {
        rule.setContent {
            FieldOpsTheme {
                JobCard(job = testJob, onClick = {}, showEngineerName = true)
            }
        }

        rule.onNode(hasText("James Wilson")).assertIsDisplayed()
    }

    @Test
    fun `job card hides engineer name when showEngineerName is false`() {
        rule.setContent {
            FieldOpsTheme {
                JobCard(job = testJob, onClick = {}, showEngineerName = false)
            }
        }

        rule.onNode(hasText("James Wilson")).assertDoesNotExist()
    }
}
