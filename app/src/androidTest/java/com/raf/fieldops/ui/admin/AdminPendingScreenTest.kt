package com.raf.fieldops.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.raf.fieldops.HiltTestActivity
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.di.AppModule
import com.raf.fieldops.di.RepositoryModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.flowOf
import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class AdminPendingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var authRepo: AuthRepo

    @Inject
    lateinit var userRepo: UserRepo

    @Before
    fun setup() {
        hiltRule.inject()

        val mockFirebaseUser = Mockito.mock(FirebaseUser::class.java)
        whenever(mockFirebaseUser.uid).thenReturn("admin-uid")
        whenever(authRepo.currentUser).thenReturn(mockFirebaseUser)

        val pendingUsers = listOf(
            User(uid = "p1", displayName = "Pending One", email = "p1@test.com", role = "engineer", status = "pending"),
            User(uid = "p2", displayName = "Pending Two", email = "p2@test.com", role = "dispatcher", status = "pending")
        )
        whenever(userRepo.getAllUsers()).thenReturn(flowOf(pendingUsers))
    }

    @Test
    fun `pending screen shows pending user names`() {
        composeRule.setContent {
            AdminPendingScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Pending One"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Pending One").assertIsDisplayed()
        composeRule.onNodeWithText("Pending Two").assertIsDisplayed()
    }

    @Test
    fun `pending screen shows approve buttons`() {
        composeRule.setContent {
            AdminPendingScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Approve"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `pending screen shows bulk approve all when multiple pending`() {
        composeRule.setContent {
            AdminPendingScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Approve All"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Approve All").assertIsDisplayed()
    }

    @Test
    fun `pending screen shows search bar`() {
        composeRule.setContent {
            AdminPendingScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Search", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
