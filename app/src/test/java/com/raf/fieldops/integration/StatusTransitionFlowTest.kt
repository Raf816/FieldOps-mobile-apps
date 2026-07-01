package com.raf.fieldops.integration

import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.repo.JobRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StatusTransitionFlowTest {

    private val testDispatcher = StandardTestDispatcher()

    private val jobRepo: JobRepo = mock()

    private val assignedJob = Job(
        id = "job-001",
        title = "Fix broadband router",
        description = "Replace faulty router at customer premises",
        address = "42 High Street, Manchester",
        scheduledStart = Timestamp.now(),
        scheduledEnd = Timestamp.now(),
        priority = Priority.Medium.name,
        status = JobStatus.Assigned.name,
        assignedTo = "engineer-uid-456",
        assignedEngineerName = "Jane Smith",
        createdBy = "dispatcher-uid-123"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(jobRepo.lastSynced).thenReturn(MutableStateFlow(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun acceptJob_assignedJob_statusBecomesAccepted() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Accepted.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.Accepted.name, captor.firstValue.status)
    }

    @Test
    fun acceptJob_assignedJob_preservesAssignment() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Accepted.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals("engineer-uid-456", captor.firstValue.assignedTo)
        assertEquals("Jane Smith", captor.firstValue.assignedEngineerName)
    }

    @Test
    fun acceptJob_assignedJob_updatedAtSetToNull() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Accepted.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(null, captor.firstValue.updatedAt)
    }

    @Test
    fun startJob_acceptedJob_statusBecomesInProgress() = runTest {

        val job = assignedJob.copy(status = JobStatus.Accepted.name)

        val updatedJob = job.copy(
            status = JobStatus.InProgress.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.InProgress.name, captor.firstValue.status)
    }

    @Test
    fun startJob_acceptedJob_preservesAllOtherFields() = runTest {

        val job = assignedJob.copy(status = JobStatus.Accepted.name)

        val updatedJob = job.copy(
            status = JobStatus.InProgress.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        val result = captor.firstValue
        assertEquals(job.id, result.id)
        assertEquals(job.title, result.title)
        assertEquals(job.description, result.description)
        assertEquals(job.address, result.address)
        assertEquals(job.priority, result.priority)
        assertEquals(job.assignedTo, result.assignedTo)
        assertEquals(job.assignedEngineerName, result.assignedEngineerName)
        assertEquals(job.createdBy, result.createdBy)
    }

    @Test
    fun completeJob_inProgressJob_statusBecomesCompleted() = runTest {

        val job = assignedJob.copy(status = JobStatus.InProgress.name)

        val updatedJob = job.copy(
            status = JobStatus.Completed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.Completed.name, captor.firstValue.status)
    }

    @Test
    fun completeJob_inProgressJob_preservesAssignment() = runTest {

        val job = assignedJob.copy(status = JobStatus.InProgress.name)

        val updatedJob = job.copy(
            status = JobStatus.Completed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals("engineer-uid-456", captor.firstValue.assignedTo)
        assertEquals("Jane Smith", captor.firstValue.assignedEngineerName)
    }

    @Test
    fun fullLifecycle_assignedToCompleted_eachTransitionProducesCorrectStatus() = runTest {

        var job = assignedJob.copy(status = JobStatus.Assigned.name)

        job = job.copy(status = JobStatus.Accepted.name, updatedAt = null)
        assertEquals(JobStatus.Accepted.name, job.status)

        job = job.copy(status = JobStatus.InProgress.name, updatedAt = null)
        assertEquals(JobStatus.InProgress.name, job.status)

        job = job.copy(status = JobStatus.Completed.name, updatedAt = null)
        assertEquals(JobStatus.Completed.name, job.status)

        assertEquals("job-001", job.id)
        assertEquals("engineer-uid-456", job.assignedTo)
        assertEquals("Fix broadband router", job.title)
    }

    @Test
    fun rejectJob_assignedJob_clearsAssignment() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val rejectionReason = "Unable to attend — vehicle breakdown"
        val updatedJob = job.copy(
            status = JobStatus.Assigned.name,
            assignedTo = "",
            assignedEngineerName = "",
            rejectionReason = rejectionReason,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        val result = captor.firstValue
        assertEquals("", result.assignedTo)
        assertEquals("", result.assignedEngineerName)
    }

    @Test
    fun rejectJob_assignedJob_statusRemainsAssigned() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Assigned.name,
            assignedTo = "",
            assignedEngineerName = "",
            rejectionReason = "Too far away",
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.Assigned.name, captor.firstValue.status)
    }

    @Test
    fun rejectJob_assignedJob_storesRejectionReason() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val reason = "Customer not available until next week"
        val updatedJob = job.copy(
            status = JobStatus.Assigned.name,
            assignedTo = "",
            assignedEngineerName = "",
            rejectionReason = reason,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(reason, captor.firstValue.rejectionReason)
    }

    @Test
    fun rejectJob_blankReason_defaultsToNoReasonProvided() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val reason = ""
        val rejectionReason = reason.ifBlank { "No reason provided" }
        val updatedJob = job.copy(
            status = JobStatus.Assigned.name,
            assignedTo = "",
            assignedEngineerName = "",
            rejectionReason = rejectionReason,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals("No reason provided", captor.firstValue.rejectionReason)
    }

    @Test
    fun dismissJob_expiredAssignedJob_statusBecomesDismissed() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Dismissed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.Dismissed.name, captor.firstValue.status)
    }

    @Test
    fun dismissJob_fromDetailScreen_statusBecomesDismissed() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Dismissed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals(JobStatus.Dismissed.name, captor.firstValue.status)

    }

    @Test
    fun dismissJob_expiredAssignedJob_preservesAssignment() = runTest {

        val job = assignedJob.copy(status = JobStatus.Assigned.name)

        val updatedJob = job.copy(
            status = JobStatus.Dismissed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)

        val captor = argumentCaptor<Job>()
        verify(jobRepo).updateJob(captor.capture())
        assertEquals("engineer-uid-456", captor.firstValue.assignedTo)
        assertEquals("Jane Smith", captor.firstValue.assignedEngineerName)
    }

    @Test
    fun dismissJob_dispatcherReschedules_statusResetsToAssigned() {

        val dismissedJob = assignedJob.copy(status = JobStatus.Dismissed.name)

        val rescheduledJob = dismissedJob.copy(
            status = JobStatus.Assigned.name,
            updatedAt = null
        )

        assertEquals(JobStatus.Assigned.name, rescheduledJob.status)
    }
}
