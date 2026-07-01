package com.raf.fieldops.ui.engineer.upcoming

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
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class UpcomingRangeNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var authRepo: AuthRepo
    @Inject lateinit var userRepo: UserRepo
    @Inject lateinit var jobRepo: JobRepo

    private val testUid = "test-engineer-upcoming"
    private val testEmail = "engineer@fieldops.com"

    private val thisWeekJob = Job(
        id = "job-week",
        title = "Fix broadband this week",
        description = "Broadband repair",
        address = "10 High Street",
        scheduledStart = Timestamp(getDateDaysFromNow(3)),
        scheduledEnd = Timestamp(getDateDaysFromNow(3, addHours = 1)),
        priority = Priority.High.name,
        status = JobStatus.Accepted.name,
        assignedTo = testUid,
        assignedEngineerName = "Test Engineer",
        createdBy = "disp-1"
    )

    private val thisMonthJob = Job(
        id = "job-month",
        title = "Install router next fortnight",
        description = "New installation",
        address = "5 Low Road",
        scheduledStart = Timestamp(getDateDaysFromNow(15)),
        scheduledEnd = Timestamp(getDateDaysFromNow(15, addHours = 2)),
        priority = Priority.Medium.name,
        status = JobStatus.Assigned.name,
        assignedTo = testUid,
        assignedEngineerName = "Test Engineer",
        createdBy = "disp-1"
    )

    @Before
    fun setup() {
        hiltRule.inject()

        val mockUser = Mockito.mock(FirebaseUser::class.java)
        whenever(mockUser.uid).thenReturn(testUid)
        whenever(mockUser.isEmailVerified).thenReturn(true)
        whenever(authRepo.currentUser).thenReturn(mockUser)
        whenever(authRepo.isEmailVerified).thenReturn(true)

        runBlocking {
            whenever(authRepo.signInWithEmailAndPassword(any(), any()))
                .thenReturn(Response.Success)
            whenever(userRepo.getUserRole(testUid)).thenReturn("engineer")
            whenever(userRepo.getUserById(testUid))
                .thenReturn(User(uid = testUid, displayName = "Test Engineer", email = testEmail, role = "engineer", status = "active"))
        }

        whenever(jobRepo.getJobsForEngineer(testUid))
            .thenReturn(flowOf(listOf(thisWeekJob, thisMonthJob)))
    }

    private fun loginAndNavigateToUpcoming() {

        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput("Password123")
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("My Jobs", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Hey,", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Upcoming").performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("This Week", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `upcoming screen shows filter chips after login`() {
        loginAndNavigateToUpcoming()

        composeRule.onNodeWithText("This Week").assertExists()
        composeRule.onNodeWithText("This Month").assertExists()
        composeRule.onNodeWithText("All").assertExists()
    }

    @Test
    fun `this week filter shows only jobs within 7 days`() {
        loginAndNavigateToUpcoming()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Fix broadband this week"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Fix broadband this week").assertExists()

        composeRule.onNodeWithText("Install router next fortnight").assertDoesNotExist()
    }

    @Test
    fun `this month filter shows jobs within 30 days`() {
        loginAndNavigateToUpcoming()

        composeRule.onNodeWithText("This Month").performClick()
        Thread.sleep(500)

        composeRule.onNodeWithText("Fix broadband this week").assertExists()
        composeRule.onNodeWithText("Install router next fortnight").assertExists()
    }

    private fun getDateDaysFromNow(days: Int, addHours: Int = 0): java.util.Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        cal.set(Calendar.HOUR_OF_DAY, 9 + addHours)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.time
    }
}
