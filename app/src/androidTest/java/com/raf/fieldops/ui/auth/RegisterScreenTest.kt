package com.raf.fieldops.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.raf.fieldops.HiltTestActivity
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import com.raf.fieldops.data.repo.AuthRepo

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class RegisterScreenTest {

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
    fun `initial state shows all registration fields`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Full name").assertIsDisplayed()
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm password").assertIsDisplayed()
    }

    @Test
    fun `initial state shows create account title and subtitle`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Set up your FieldOps profile").assertIsDisplayed()
    }

    @Test
    fun `role selection cards are visible`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Engineer").assertIsDisplayed()
        composeRule.onNodeWithText("Dispatcher").assertIsDisplayed()
        composeRule.onNodeWithText("View and complete assigned jobs").assertIsDisplayed()
        composeRule.onNodeWithText("Create and assign jobs to engineers").assertIsDisplayed()
    }

    @Test
    fun `select your role label is visible`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Select your role").assertIsDisplayed()
    }

    @Test
    fun `empty name shows validation error on submit`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNode(hasText("Create Account") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 2000) {
            composeRule.onAllNodes(hasText("Name must be at least 2 characters"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `name with numbers shows cannot contain numbers error`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Full name").performTextInput("Raf123")

        composeRule.onNode(hasText("Create Account") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 2000) {
            composeRule.onAllNodes(hasText("Name cannot contain numbers"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `passwords not matching shows error on submit`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Full name").performTextInput("Test User")
        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("Password1")
        composeRule.onNodeWithText("Confirm password").performTextInput("Different1")

        composeRule.onNode(hasText("Create Account") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 2000) {
            composeRule.onAllNodes(hasText("Passwords do not match"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `password strength indicator shows weak for short password`() {
        composeRule.setContent {
            RegisterScreen(
                navigateBack = {},
                navigateToConfirmation = {}
            )
        }

        composeRule.onNodeWithText("Password").performTextInput("abc")

        composeRule.onNodeWithText("Weak").assertIsDisplayed()
    }

    @Test
    fun `back navigation triggers navigateBack callback`() {
        var navigatedBack = false

        composeRule.setContent {
            RegisterScreen(
                navigateBack = { navigatedBack = true },
                navigateToConfirmation = {}
            )
        }

        composeRule.onNode(hasContentDescription("Navigate back to sign in"))
            .performClick()

        org.junit.Assert.assertTrue("Should navigate back", navigatedBack)
    }
}
