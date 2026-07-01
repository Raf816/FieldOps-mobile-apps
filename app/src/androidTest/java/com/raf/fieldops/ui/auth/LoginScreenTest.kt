package com.raf.fieldops.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.raf.fieldops.HiltTestActivity
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepo: AuthRepo

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `initial state shows FieldOps branding in hero section`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("FieldOps").assertIsDisplayed()
        composeRule.onNodeWithText("Field Engineer.", substring = true).assertIsDisplayed()
    }

    @Test
    fun `initial state shows email and password fields`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun `initial state shows sign in button and create account button`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun `initial state shows forgot password link`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Forgot password?").assertIsDisplayed()
    }

    @Test
    fun `empty email shows validation error after submit`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.onNodeWithText("Enter a valid email address").assertIsDisplayed()
    }

    @Test
    fun `short password shows validation error after submit`() {
        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("abc")

        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
    }

    @Test
    fun `failed login shows error snackbar`() {

        runBlocking {
            whenever(authRepo.signInWithEmailAndPassword(any(), any()))
                .thenReturn(Response.Failure(Exception("Invalid credentials")))
        }

        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password123")
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasText("Unable to sign in", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `create account button triggers navigation to sign up`() {
        var navigatedToSignUp = false

        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = { navigatedToSignUp = true },
                navigateToEmailConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Create Account").performClick()

        assertTrue("Should navigate to sign up screen", navigatedToSignUp)
    }

    @Test
    fun `unverified email navigates to email confirmation screen`() {

        var navigatedToConfirmation = false
        runBlocking {
            whenever(authRepo.signInWithEmailAndPassword(any(), any()))
                .thenReturn(Response.Success)
            whenever(authRepo.isEmailVerified).thenReturn(false)
        }

        composeRule.setContent {
            LoginScreen(
                navigateToHomeScreen = {},
                navigateToSignUpScreen = {},
                navigateToEmailConfirmation = { navigatedToConfirmation = true }
            )
        }

        composeRule.onNodeWithText("Email").performTextInput("unverified@test.com")
        composeRule.onNodeWithText("Password").performTextInput("password123")
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.waitUntil(timeoutMillis = 3000) { navigatedToConfirmation }
        assertTrue("Should navigate to email confirmation", navigatedToConfirmation)
    }
}
