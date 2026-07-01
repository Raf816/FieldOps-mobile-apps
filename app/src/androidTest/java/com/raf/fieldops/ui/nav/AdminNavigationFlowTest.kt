package com.raf.fieldops.ui.nav

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
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
class AdminNavigationFlowTest {

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

    private val testUid = "test-admin-uid"
    private val testEmail = "admin@fieldops.com"
    private val testPassword = "Admin123"

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
            whenever(userRepo.getUserRole(any())).thenReturn("admin")
            whenever(userRepo.getUserById(any()))
                .thenReturn(User(
                    uid = testUid,
                    displayName = "Admin User",
                    email = testEmail,
                    role = "admin",
                    status = "active"
                ))
            whenever(userRepo.markEmailVerified(any())).thenReturn(Unit)
        }

        val testUsers = listOf(
            User(uid = "u1", displayName = "Engineer One", email = "eng1@test.com", role = "engineer", status = "active"),
            User(uid = "u2", displayName = "Pending User", email = "pending@test.com", role = "engineer", status = "pending")
        )
        whenever(userRepo.getAllUsers()).thenReturn(flowOf(testUsers))
        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(testUsers.filter { it.role == "engineer" }))
        whenever(userRepo.observeUser(testUid)).thenReturn(flowOf(User(
            uid = testUid, displayName = "Admin User", email = testEmail, role = "admin", status = "active"
        )))
        whenever(jobRepo.getAllJobs()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `admin login navigates to admin dashboard`() {

        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)

        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Welcome", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `admin dashboard shows bottom navigation bar`() {

        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasContentDescription("Admin bottom navigation bar"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasContentDescription("Admin bottom navigation bar"))
            .assertIsDisplayed()
    }

    @Test
    fun `admin drawer opens when hamburger icon tapped`() {

        composeRule.onNodeWithText("Email").performTextInput(testEmail)
        composeRule.onNodeWithText("Password").performTextInput(testPassword)
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Welcome", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasContentDescription("Open navigation drawer"))
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasContentDescription("Dashboard drawer item"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
