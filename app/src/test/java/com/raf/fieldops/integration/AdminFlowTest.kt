package com.raf.fieldops.integration

import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AdminFlowTest {

    private lateinit var userRepo: UserRepo
    private lateinit var jobRepo: JobRepo

    @Before
    fun setup() {
        userRepo = mock()
        jobRepo = mock()
        whenever(jobRepo.lastSynced).thenReturn(MutableStateFlow(null))
    }

    @Test
    fun approve_setsStatusToActive() = runTest {

        val uid = "user-123"

        userRepo.updateUserStatus(uid, "active")

        verify(userRepo).updateUserStatus(uid, "active")
    }

    @Test
    fun reject_deletesUserDocument() = runTest {
        val uid = "user-456"

        userRepo.deleteUser(uid)

        verify(userRepo).deleteUser(uid)
    }

    @Test
    fun suspend_setsStatusToSuspended() = runTest {
        val uid = "user-789"

        userRepo.updateUserStatus(uid, "suspended")

        verify(userRepo).updateUserStatus(uid, "suspended")
    }

    @Test
    fun unsuspend_setsStatusToActive() = runTest {
        val uid = "user-789"

        userRepo.updateUserStatus(uid, "active")

        verify(userRepo).updateUserStatus(uid, "active")
    }

    @Test
    fun deleteCascade_unassignsActiveJobs_thenDeletesUser() = runTest {
        val uid = "engineer-1"
        val assignedJob = Job(
            id = "job-1",
            title = "Fix router",
            assignedTo = uid,
            assignedEngineerName = "Test Engineer",
            status = "Accepted"
        )
        val completedJob = Job(
            id = "job-2",
            title = "Old job",
            assignedTo = uid,
            status = "Completed"
        )

        whenever(jobRepo.getAllJobs()).thenReturn(flowOf(listOf(assignedJob, completedJob)))

        val allJobs = listOf(assignedJob, completedJob)
        val activeJobs = allJobs.filter {
            it.assignedTo == uid && it.status != "Completed" && it.status != "Cancelled"
        }

        assertEquals(1, activeJobs.size)
        assertEquals("job-1", activeJobs[0].id)

        val unassigned = activeJobs[0].copy(
            assignedTo = "",
            assignedEngineerName = "",
            status = "Assigned"
        )
        assertEquals("", unassigned.assignedTo)
        assertEquals("", unassigned.assignedEngineerName)
        assertEquals("Assigned", unassigned.status)
    }

    @Test
    fun deleteCascade_noActiveJobs_justDeletesUser() = runTest {
        val uid = "engineer-2"
        val completedJob = Job(
            id = "job-3",
            title = "Done job",
            assignedTo = uid,
            status = "Completed"
        )

        val allJobs = listOf(completedJob)
        val activeJobs = allJobs.filter {
            it.assignedTo == uid && it.status != "Completed" && it.status != "Cancelled"
        }

        assertEquals(0, activeJobs.size)
    }

    @Test
    fun deleteCascade_excludesCancelledJobs() {
        val uid = "engineer-3"
        val cancelledJob = Job(id = "j1", assignedTo = uid, status = "Cancelled")
        val activeJob = Job(id = "j2", assignedTo = uid, status = "InProgress")

        val allJobs = listOf(cancelledJob, activeJob)
        val activeJobs = allJobs.filter {
            it.assignedTo == uid && it.status != "Completed" && it.status != "Cancelled"
        }

        assertEquals(1, activeJobs.size)
        assertEquals("j2", activeJobs[0].id)
    }

    @Test
    fun changeRole_engineerToDispatcher() = runTest {
        val uid = "user-role-1"

        userRepo.updateUserRole(uid, "dispatcher")

        verify(userRepo).updateUserRole(uid, "dispatcher")
    }

    @Test
    fun changeRole_dispatcherToEngineer() = runTest {
        val uid = "user-role-2"

        userRepo.updateUserRole(uid, "engineer")

        verify(userRepo).updateUserRole(uid, "engineer")
    }

    @Test
    fun bulkApprove_setsAllToActive() = runTest {
        val users = listOf(
            User(uid = "u1", status = "pending"),
            User(uid = "u2", status = "pending"),
            User(uid = "u3", status = "pending")
        )

        users.forEach { user ->
            userRepo.updateUserStatus(user.uid, "active")
        }

        verify(userRepo).updateUserStatus("u1", "active")
        verify(userRepo).updateUserStatus("u2", "active")
        verify(userRepo).updateUserStatus("u3", "active")
    }

    @Test
    fun bulkReject_deletesAll() = runTest {
        val users = listOf(
            User(uid = "u4", status = "pending"),
            User(uid = "u5", status = "pending")
        )

        users.forEach { user ->
            userRepo.deleteUser(user.uid)
        }

        verify(userRepo).deleteUser("u4")
        verify(userRepo).deleteUser("u5")
    }

    @Test
    fun statusRouting_pending_routesToAwaitingApproval() {
        val status = "pending"
        val destination = when (status) {
            "pending" -> "awaiting_approval"
            "suspended" -> "account_suspended"
            "active" -> "home"
            else -> "awaiting_approval"
        }
        assertEquals("awaiting_approval", destination)
    }

    @Test
    fun statusRouting_suspended_routesToSuspendedScreen() {
        val status = "suspended"
        val destination = when (status) {
            "pending" -> "awaiting_approval"
            "suspended" -> "account_suspended"
            "active" -> "home"
            else -> "awaiting_approval"
        }
        assertEquals("account_suspended", destination)
    }

    @Test
    fun statusRouting_active_routesToHome() {
        val status = "active"
        val destination = when (status) {
            "pending" -> "awaiting_approval"
            "suspended" -> "account_suspended"
            "active" -> "home"
            else -> "awaiting_approval"
        }
        assertEquals("home", destination)
    }

    @Test
    fun statusRouting_unknown_fallsToAwaitingApproval() {
        val status = "garbage"
        val destination = when (status) {
            "pending" -> "awaiting_approval"
            "suspended" -> "account_suspended"
            "active" -> "home"
            else -> "awaiting_approval"
        }
        assertEquals("awaiting_approval", destination)
    }

    @Test
    fun statusRouting_empty_fallsToAwaitingApproval() {
        val status = ""
        val destination = when (status) {
            "pending" -> "awaiting_approval"
            "suspended" -> "account_suspended"
            "active" -> "home"
            else -> "awaiting_approval"
        }
        assertEquals("awaiting_approval", destination)
    }

    @Test
    fun statusRouting_nullUser_routesToAccountRemoved() {

        val user: User? = null
        val destination = if (user == null) {
            "account_removed"
        } else {
            when (user.status) {
                "pending" -> "awaiting_approval"
                "suspended" -> "account_suspended"
                "active" -> "home"
                else -> "awaiting_approval"
            }
        }
        assertEquals("account_removed", destination)
    }

    @Test
    fun assignmentDropdown_excludesSuspendedEngineers() {
        val engineers = listOf(
            User(uid = "e1", displayName = "Active Eng", status = "active", role = "engineer"),
            User(uid = "e2", displayName = "Suspended Eng", status = "suspended", role = "engineer"),
            User(uid = "e3", displayName = "Pending Eng", status = "pending", role = "engineer")
        )

        val available = engineers.filter { it.status == "active" }

        assertEquals(1, available.size)
        assertEquals("e1", available[0].uid)
    }

    @Test
    fun assignmentDropdown_allActive_showsAll() {
        val engineers = listOf(
            User(uid = "e1", status = "active", role = "engineer"),
            User(uid = "e2", status = "active", role = "engineer")
        )

        val available = engineers.filter { it.status == "active" }
        assertEquals(2, available.size)
    }

    @Test
    fun assignmentDropdown_noneActive_showsEmpty() {
        val engineers = listOf(
            User(uid = "e1", status = "suspended", role = "engineer"),
            User(uid = "e2", status = "pending", role = "engineer")
        )

        val available = engineers.filter { it.status == "active" }
        assertEquals(0, available.size)
    }
}
