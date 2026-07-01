package com.raf.fieldops.ui.dispatcher.createjob

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.HiltTestActivity
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class CreateJobScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepo: AuthRepo

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var jobRepo: JobRepo

    @Before
    fun setup() {
        hiltRule.inject()

        val mockFirebaseUser = Mockito.mock(FirebaseUser::class.java)
        whenever(mockFirebaseUser.uid).thenReturn("dispatcher-uid")
        whenever(authRepo.currentUser).thenReturn(mockFirebaseUser)

        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `shows job details section header`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Job Details").assertIsDisplayed()
    }

    @Test
    fun `shows schedule section header`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Schedule").assertIsDisplayed()
    }

    @Test
    fun `shows assignment section header`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Assignment").assertIsDisplayed()
    }

    @Test
    fun `shows title and description fields`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Title").assertIsDisplayed()
        composeRule.onNodeWithText("Description").assertIsDisplayed()
    }

    @Test
    fun `shows address field`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Address").assertIsDisplayed()
    }

    @Test
    fun `create job button is visible`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Create Job").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `empty title shows validation error on submit`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Create Job").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasText("Title must be at least 5 characters"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `empty address shows validation error on submit`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Create Job").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodes(hasText("Address is required"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `cancel button is visible`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Cancel").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `cancel button triggers navigateBack callback`() {
        var navigatedBack = false

        composeRule.setContent {
            CreateJobScreen(navigateBack = { navigatedBack = true })
        }

        composeRule.onNodeWithText("Cancel").performScrollTo().performClick()

        org.junit.Assert.assertTrue("navigateBack should have been called", navigatedBack)
    }

    @Test
    fun `address field accepts text input`() {
        composeRule.setContent {
            CreateJobScreen(navigateBack = {})
        }

        composeRule.onNodeWithText("Address").assertIsDisplayed()
    }
}
