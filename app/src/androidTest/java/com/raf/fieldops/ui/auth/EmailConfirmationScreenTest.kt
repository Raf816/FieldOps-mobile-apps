package com.raf.fieldops.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class EmailConfirmationScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepo: AuthRepo

    private val testEmail = "engineer@fieldops.test"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `displays the provided email address`() {
        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNodeWithText(testEmail).assertIsDisplayed()
    }

    @Test
    fun `displays check your email heading`() {
        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNodeWithText("Check your email").assertIsDisplayed()
    }

    @Test
    fun `displays verification instruction text`() {
        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNodeWithText("We've sent a verification link to").assertIsDisplayed()
        composeRule.onNodeWithText("Please verify your email before signing in.")
            .assertIsDisplayed()
    }

    @Test
    fun `resend verification button is visible`() {
        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNode(hasContentDescription("Resend verification email button"))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Resend Verification Email").assertIsDisplayed()
    }

    @Test
    fun `back to sign in button is visible`() {
        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNodeWithText("Back to Sign In").assertIsDisplayed()
    }

    @Test
    fun `back to sign in button triggers navigation callback`() {
        var navigatedToLogin = false

        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = { navigatedToLogin = true }
            )
        }

        composeRule.onNodeWithText("Back to Sign In").performClick()

        assertTrue("Should navigate to login", navigatedToLogin)
    }

    @Test
    fun `resend button shows snackbar on success`() {

        runBlocking {
            whenever(authRepo.sendEmailVerification())
                .thenReturn(Response.Success)
        }

        composeRule.setContent {
            EmailConfirmationScreen(
                email = testEmail,
                authRepo = authRepo,
                navigateToLogin = {}
            )
        }

        composeRule.onNodeWithText("Resend Verification Email").performClick()

        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasText("Verification email resent"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
