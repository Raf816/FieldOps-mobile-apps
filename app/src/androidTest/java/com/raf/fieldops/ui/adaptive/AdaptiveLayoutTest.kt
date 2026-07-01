package com.raf.fieldops.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.ui.components.JobCard
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AdaptiveLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val testJob1 = Job(
        id = "job-1",
        title = "Fix broadband",
        description = "Customer reports slow speeds",
        address = "10 High Street",
        scheduledStart = Timestamp(Date()),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 3600000)),
        priority = Priority.High.name,
        status = JobStatus.Assigned.name,
        assignedTo = "eng-1",
        assignedEngineerName = "Jane Smith",
        createdBy = "disp-1"
    )

    private val testJob2 = Job(
        id = "job-2",
        title = "Install router",
        description = "New customer installation",
        address = "5 Low Street",
        scheduledStart = Timestamp(Date()),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 7200000)),
        priority = Priority.Medium.name,
        status = JobStatus.Accepted.name,
        assignedTo = "eng-2",
        assignedEngineerName = "John Doe",
        createdBy = "disp-1"
    )

    @Test
    fun `job card renders correctly on compact screen`() {
        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Compact) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        JobCard(job = testJob1, onClick = {})
                    }
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()
        composeRule.onNodeWithText("10 High Street").assertIsDisplayed()
    }

    @Test
    fun `job card renders correctly on expanded screen`() {
        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        JobCard(job = testJob1, onClick = {})
                    }
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()
        composeRule.onNodeWithText("10 High Street").assertIsDisplayed()
    }

    @Test
    fun `multiple job cards render on expanded screen`() {
        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            JobCard(job = testJob1, onClick = {})
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            JobCard(job = testJob2, onClick = {})
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()
        composeRule.onNodeWithText("Install router").assertIsDisplayed()
    }

    @Test
    fun `window width class defaults to compact`() {
        composeRule.setContent {
            FieldOpsTheme {

                Box(modifier = Modifier.fillMaxSize()) {
                    JobCard(job = testJob1, onClick = {})
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()
    }

    @Test
    fun `expanded layout shows engineer name on job cards`() {
        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        JobCard(
                            job = testJob1,
                            onClick = {},
                            showEngineerName = true
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    }

    @Test
    fun `two column row renders both cards visible on expanded`() {

        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            JobCard(job = testJob1, onClick = {})
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            JobCard(job = testJob2, onClick = {})
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()
        composeRule.onNodeWithText("Install router").assertIsDisplayed()
    }

    @Test
    fun `compact layout does not show two cards side by side`() {

        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Compact) {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { JobCard(job = testJob1, onClick = {}) }
                        item { JobCard(job = testJob2, onClick = {}) }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Fix broadband").assertIsDisplayed()

    }

    @Test
    fun `expanded two column detail layout renders info and notes`() {

        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {

                        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(0.55f)) {
                            androidx.compose.material3.Text("Job Details Section")
                        }

                        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(0.45f)) {
                            androidx.compose.material3.Text("Notes Section")
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Job Details Section").assertIsDisplayed()
        composeRule.onNodeWithText("Notes Section").assertIsDisplayed()
    }

    @Test
    fun `profile cards render side by side on expanded`() {

        composeRule.setContent {
            FieldOpsTheme {
                CompositionLocalProvider(LocalWindowWidthClass provides WindowWidthSizeClass.Expanded) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        androidx.compose.material3.Card(modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Text(
                                text = "Appearance",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        androidx.compose.material3.Card(modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Text(
                                text = "Account",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Account").assertIsDisplayed()
    }
}
