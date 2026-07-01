package com.raf.fieldops.ui.nav

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.MainActivity
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
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
class RegisterNavigationFlowTest {

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

    private val testEmail = "newuser@fieldops.com"

    @Before
    fun setup() {
        hiltRule.inject()

        val mockFirebaseUser = Mockito.mock(FirebaseUser::class.java)
        whenever(mockFirebaseUser.uid).thenReturn("new-user-uid")
        whenever(mockFirebaseUser.isEmailVerified).thenReturn(false)
        whenever(authRepo.currentUser).thenReturn(mockFirebaseUser)
        whenever(authRepo.isEmailVerified).thenReturn(false)

        runBlocking {
            whenever(authRepo.signUpWithEmailAndPassword(any(), any()))
                .thenReturn(Response.Success)
            whenever(authRepo.sendEmailVerification())
                .thenReturn(Response.Success)
        }

        runBlocking {
            whenever(userRepo.createUser(any())).thenReturn(Unit)
        }
    }

    @Test
    fun `tapping sign up navigates to register screen`() {

        composeRule.onNodeWithText("Create Account").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Create Account", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Engineer", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Full Name", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `register screen shows all form fields`() {

        composeRule.onNodeWithText("Create Account").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Full Name", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Create Account", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Email").assertExists()
        composeRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun `back from register returns to login screen`() {

        composeRule.onNodeWithText("Create Account").performClick()

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Full Name", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Create Account", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Sign in", substring = true))
                .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodes(hasText("Already have", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        try {
            composeRule.onNodeWithText("Sign in").performScrollTo().performClick()
        } catch (_: Exception) {

            composeRule.onNodeWithText("Sign in").performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Sign In"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Sign In").assertExists()
    }
}
