package com.raf.fieldops.ui.engineer.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ExpiredJobDismissTest {

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

    private val testUid = "test-engineer-dismiss"
    private val testEmail = "engineer@fieldops.com"
    private val testPassword = "Password123"

    private val expiredJob = Job(
        id = "expired-job-001",
        title = "Fix broadband router",
        description = "Replace faulty router at customer premises",
        address = "42 High Street, Manchester",
        scheduledStart = Timestamp(Date(System.currentTimeMillis() - 86400000)),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() - 82800000)),
        priority = Priority.Medium.name,
        status = JobStatus.Assigned.name,
        assignedTo = testUid,
        assignedEngineerName = "Test Engineer",
        createdBy = "dispatcher-uid"
    )

    private val futureJob = Job(
        id = "future-job-002",
        title = "Install fibre optic",
        description = "Full fibre installation at new build",
        address = "10 Downing Street, London",
        scheduledStart = Timestamp(Date(System.currentTimeMillis() + 86400000)),
        scheduledEnd = Timestamp(Date(System.currentTimeMillis() + 90000000)),
        priority = Priority.High.name,
        status = JobStatus.Assigned.name,
        assignedTo = testUid,
        assignedEngineerName = "Test Engineer",
        createdBy = "dispatcher-uid"
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
            whenever(userRepo.getUserRole(testUid)).thenReturn("engineer")
            whenever(userRepo.getUserById(testUid))
                .thenReturn(User(uid = testUid, displayName = "Test Engineer", email = testEmail, role = "engineer", status = "active"))
        }

        whenever(jobRepo.getJobsForEngineer(testUid))
            .thenReturn(flowOf(listOf(expiredJob, futureJob)))
    }

    private fun loginAsEngineer() {
        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Hey,", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `expired assigned job shows MISSED badge`() {
        loginAsEngineer()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("MISSED"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("MISSED").assertIsDisplayed()
    }

    @Test
    fun `expired assigned job shows Dismiss button`() {
        loginAsEngineer()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Dismiss"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun `future assigned job shows Accept button`() {
        loginAsEngineer()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Accept"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Accept").assertIsDisplayed()
    }

    @Test
    fun `future assigned job shows Reject button`() {
        loginAsEngineer()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Reject"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Reject").assertIsDisplayed()
    }

    @Test
    fun `expired job title is displayed`() {
        loginAsEngineer()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Fix broadband router"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Fix broadband router").assertIsDisplayed()
    }
}
