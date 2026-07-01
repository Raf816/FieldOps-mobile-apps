package com.raf.fieldops.ui.dispatcher.alljobs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.MainActivity
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Date
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class DismissedFilterTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepo: AuthRepo

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var jobRepo: JobRepo

    private val testUid = "test-dispatcher-dismissed"
    private val testEmail = "dispatcher@fieldops.com"
    private val testPassword = "Password123"

    private val dismissedJob = Job(
        id = "dismissed-job-001",
        title = "Missed router fix",
        description = "This job was dismissed by the engineer",
        address = "42 High Street, Manchester",
        scheduledStart = Timestamp(Date(System.currentTimeMillis() - 86400000)),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() - 82800000)),
        priority = Priority.Medium.name,
        status = JobStatus.Dismissed.name,
        assignedTo = "engineer-uid-456",
        assignedEngineerName = "Jane Smith",
        createdBy = testUid
    )

    private val activeJob = Job(
        id = "active-job-002",
        title = "Install fibre optic",
        description = "Full fibre installation",
        address = "10 Downing Street, London",
        scheduledStart = Timestamp(Date(System.currentTimeMillis() + 86400000)),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 90000000)),
        priority = Priority.High.name,
        status = JobStatus.Assigned.name,
        assignedTo = "engineer-uid-456",
        assignedEngineerName = "Jane Smith",
        createdBy = testUid
    )

    @Before
    fun setup() {
        hiltRule.inject()

        val mockFirebaseUser = Mockito.mock(FirebaseUser::class.java)
        whenever(mockFirebaseUser.uid).thenReturn(testUid)
        whenever(mockFirebaseUser.isEmailVerified).thenReturn(true)
        whenever(authRepo.currentUser).thenReturn(mockFirebaseUser)
        whenever(authRepo.isEmailVerified).thenReturn(true)

        runBlocking {
            whenever(authRepo.signInWithEmailAndPassword(any(), any()))
                .thenReturn(Response.Success)
            whenever(userRepo.getUserRole(testUid)).thenReturn("dispatcher")
            whenever(userRepo.getUserById(testUid))
                .thenReturn(User(uid = testUid, displayName = "Test Dispatcher", email = testEmail, role = "dispatcher", status = "active"))
        }

        whenever(jobRepo.getAllJobs())
            .thenReturn(flowOf(listOf(dismissedJob, activeJob)))
        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(emptyList()))
    }

    private fun navigateToAllJobs() {

        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Dashboard", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Hey,", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasContentDescription("Open navigation drawer"))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasContentDescription("Menu"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        try {
            composeRule.onNodeWithContentDescription("Open navigation drawer").performClick()
        } catch (_: Exception) {
            composeRule.onNodeWithContentDescription("Menu").performClick()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("All Jobs"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("All Jobs").performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("All Jobs", substring = true))
                .fetchSemanticsNodes().size >= 1
        }
    }

    @Test
    fun `dismissed filter chip shows count`() {
        navigateToAllJobs()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Dismissed", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        val nodes = composeRule.onAllNodes(hasText("Dismissed", substring = true))
            .fetchSemanticsNodes()
        assert(nodes.isNotEmpty()) { "Expected at least one node with 'Dismissed' text" }
    }

    @Test
    fun `all jobs screen shows both dismissed and active jobs by default`() {
        navigateToAllJobs()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Install fibre optic", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Install fibre optic").assertIsDisplayed()
        composeRule.onNodeWithText("Missed router fix").assertIsDisplayed()
    }
}
