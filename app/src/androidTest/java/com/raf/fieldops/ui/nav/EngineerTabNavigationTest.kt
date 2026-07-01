package com.raf.fieldops.ui.nav

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.MainActivity
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.NoteRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class EngineerTabNavigationTest {

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

    @Inject
    lateinit var noteRepo: NoteRepo

    private val testUid = "test-engineer-tabs"
    private val testEmail = "engineer@fieldops.com"
    private val testPassword = "Password123"

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
        }

        runBlocking {
            whenever(userRepo.getUserRole(testUid)).thenReturn("engineer")
            whenever(userRepo.getUserById(testUid))
                .thenReturn(User(uid = testUid, displayName = "Tab Test Engineer", email = testEmail, role = "engineer", status = "active"))
        }

        whenever(jobRepo.getJobsForEngineer(testUid)).thenReturn(flowOf(emptyList()))
        whenever(jobRepo.getJobsForEngineerToday(testUid)).thenReturn(flowOf(emptyList()))
        whenever(jobRepo.getJobsForEngineerUpcoming(testUid)).thenReturn(flowOf(emptyList()))
    }

    private fun loginAsEngineer() {
        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("My Jobs", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `tapping Upcoming tab navigates to upcoming screen`() {

        loginAsEngineer()

        composeRule.onNodeWithText("Upcoming").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("No upcoming jobs this week"))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Upcoming", substring = true))
                .fetchSemanticsNodes().size > 1
        }
    }

    @Test
    fun `tapping Profile tab navigates to profile screen`() {

        loginAsEngineer()

        composeRule.onNodeWithText("Profile").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Tab Test Engineer", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText(testEmail, substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Sign Out", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `navigating back to Today tab shows home screen`() {

        loginAsEngineer()

        composeRule.onNodeWithText("Upcoming").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("No upcoming jobs this week"))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Upcoming", substring = true))
                .fetchSemanticsNodes().size > 1
        }

        composeRule.onNodeWithText("My Jobs").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("No jobs scheduled for today"))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Hey,", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `sign out from profile navigates back to login`() {

        loginAsEngineer()

        composeRule.onNodeWithText("Profile").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Sign Out", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Sign Out").performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Sign Out?"))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Are you sure", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodes(hasText("Sign Out"))[1].performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Sign In"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Sign In").assertExists()
    }
}
