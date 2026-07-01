package com.raf.fieldops.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.raf.fieldops.HiltTestActivity
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
import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, RepositoryModule::class)
class AdminDashboardScreenTest {

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
        whenever(mockFirebaseUser.uid).thenReturn("admin-uid")
        whenever(authRepo.currentUser).thenReturn(mockFirebaseUser)

        val testUsers = listOf(
            User(uid = "u1", displayName = "Active Eng", email = "eng@test.com", role = "engineer", status = "active"),
            User(uid = "u2", displayName = "Active Disp", email = "disp@test.com", role = "dispatcher", status = "active"),
            User(uid = "u3", displayName = "Pending User", email = "pending@test.com", role = "engineer", status = "pending"),
            User(uid = "u4", displayName = "Suspended Eng", email = "susp@test.com", role = "engineer", status = "suspended")
        )
        whenever(userRepo.getAllUsers()).thenReturn(flowOf(testUsers))
        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(testUsers.filter { it.role == "engineer" }))

        runBlocking {
            whenever(userRepo.getUserById("admin-uid"))
                .thenReturn(User(uid = "admin-uid", displayName = "Admin", role = "admin", status = "active"))
        }
    }

    @Test
    fun `dashboard shows welcome greeting`() {
        composeRule.setContent {
            AdminDashboardScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Welcome", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Welcome", substring = true).assertIsDisplayed()
    }

    @Test
    fun `dashboard shows pending action card when pending users exist`() {
        composeRule.setContent {
            AdminDashboardScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Pending Approvals", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Pending Approvals", substring = true).assertIsDisplayed()
    }

    @Test
    fun `dashboard shows recent registrations section`() {
        composeRule.setContent {
            AdminDashboardScreen()
        }

        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText("Recent Registrations"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Recent Registrations").assertIsDisplayed()
    }
}
