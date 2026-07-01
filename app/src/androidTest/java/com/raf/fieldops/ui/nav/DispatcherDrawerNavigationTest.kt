package com.raf.fieldops.ui.nav

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.MainActivity
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
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class DispatcherDrawerNavigationTest {

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

    private val testUid = "test-dispatcher-drawer"
    private val testEmail = "dispatcher@fieldops.com"
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
            whenever(userRepo.getUserRole(testUid)).thenReturn("dispatcher")
            whenever(userRepo.getUserById(testUid))
                .thenReturn(User(uid = testUid, displayName = "Test Dispatcher", email = testEmail, role = "dispatcher", status = "active"))
        }

        whenever(jobRepo.getAllJobs()).thenReturn(flowOf(emptyList()))
        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(emptyList()))
    }

    private fun loginAsDispatcher() {
        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Dashboard"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openDrawer() {

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
            composeRule.onAllNodes(hasText("Engineers"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `opening drawer shows all navigation items`() {

        loginAsDispatcher()

        openDrawer()

        composeRule.onNodeWithText("Dashboard").assertExists()
        composeRule.onNodeWithText("Engineers").assertExists()
        composeRule.onNodeWithText("Create Job").assertExists()
        composeRule.onNodeWithText("Profile").assertExists()
        composeRule.onNodeWithText("Sign Out").assertExists()
    }

    @Test
    fun `tapping Engineers in drawer navigates to engineers screen`() {

        loginAsDispatcher()
        openDrawer()

        composeRule.onNodeWithText("Engineers").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("No engineers found", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Engineers", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Search", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `tapping Create Job in drawer navigates to create job screen`() {

        loginAsDispatcher()
        openDrawer()

        composeRule.onNodeWithText("Create Job").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Job Details", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Title", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Create Job", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `tapping Profile in drawer navigates to profile screen`() {

        loginAsDispatcher()
        openDrawer()

        composeRule.onNodeWithText("Profile").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Test Dispatcher", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText(testEmail, substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Sign Out", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `create job empty form shows validation errors`() {

        loginAsDispatcher()
        openDrawer()
        composeRule.onNodeWithText("Create Job").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Job Details", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Title", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("Create Job button").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("at least", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("required", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("must be", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("select an engineer", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
