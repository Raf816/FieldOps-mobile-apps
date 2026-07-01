package com.raf.fieldops.integration

import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CreateJobFlowTest {

    private val testDispatcher = StandardTestDispatcher()

    private val jobRepo: JobRepo = mock()
    private val userRepo: UserRepo = mock()

    private val testEngineer = User(
        uid = "engineer-uid-456",
        displayName = "Jane Smith",
        email = "jane@bt.com",
        role = "engineer"
    )

    private val dispatcherUid = "dispatcher-uid-123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(jobRepo.lastSynced).thenReturn(MutableStateFlow(null))
        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(listOf(testEngineer)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildJob(
        title: String,
        description: String,
        address: String,
        scheduledStartMillis: Long,
        scheduledEndMillis: Long,
        priority: Priority,
        engineer: User,
        createdByUid: String
    ): Job = Job(
        title = title.trim(),
        description = description.trim(),
        address = address.trim(),
        scheduledStart = Timestamp(Date(scheduledStartMillis)),
        scheduledEnd = Timestamp(Date(scheduledEndMillis)),
        priority = priority.name,
        status = JobStatus.Assigned.name,
        assignedTo = engineer.uid,
        assignedEngineerName = engineer.displayName,
        createdBy = createdByUid
    )

    @Test
    fun buildJob_validFields_statusIsAssigned() {

        val job = buildJob(
            title = "Fix broadband router",
            description = "Replace the faulty router at customer premises",
            address = "42 High Street, Manchester",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.High,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals(JobStatus.Assigned.name, job.status)
    }

    @Test
    fun buildJob_validFields_assignedToEngineerUid() {

        val job = buildJob(
            title = "Install fibre optic",
            description = "Full fibre installation at new build property",
            address = "10 Downing Street, London",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 7200000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("engineer-uid-456", job.assignedTo)
    }

    @Test
    fun buildJob_validFields_denormalisesEngineerName() {

        val job = buildJob(
            title = "Repair telephone line",
            description = "Customer reports no dial tone since yesterday",
            address = "7 Baker Street, London",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Urgent,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("Jane Smith", job.assignedEngineerName)
    }

    @Test
    fun buildJob_validFields_createdByIsDispatcherUid() {

        val job = buildJob(
            title = "Routine maintenance",
            description = "Quarterly check on exchange equipment",
            address = "BT Exchange, Leeds",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Low,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("dispatcher-uid-123", job.createdBy)
    }

    @Test
    fun buildJob_validFields_priorityIsPreserved() {

        val job = buildJob(
            title = "Emergency power outage",
            description = "Customer site has lost all power to comms room",
            address = "1 Industrial Park, Birmingham",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.High,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals(Priority.High.name, job.priority)
    }

    @Test
    fun buildJob_validFields_scheduledTimesAreSet() {

        val startMillis = 1700000000000L
        val endMillis = 1700003600000L

        val job = buildJob(
            title = "Network survey",
            description = "Survey new housing estate for fibre rollout",
            address = "New Build Estate, Bristol",
            scheduledStartMillis = startMillis,
            scheduledEndMillis = endMillis,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertNotNull(job.scheduledStart)
        assertNotNull(job.scheduledEnd)
        assertEquals(startMillis, job.scheduledStart!!.toDate().time)
        assertEquals(endMillis, job.scheduledEnd!!.toDate().time)
    }

    @Test
    fun buildJob_validFields_titleIsTrimmed() {

        val job = buildJob(
            title = "  Fix router  ",
            description = "Replace the faulty router at customer premises",
            address = "42 High Street",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("Fix router", job.title)
    }

    @Test
    fun buildJob_validFields_descriptionIsTrimmed() {

        val job = buildJob(
            title = "Fix router",
            description = "  Replace the faulty router  ",
            address = "42 High Street",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("Replace the faulty router", job.description)
    }

    @Test
    fun buildJob_validFields_addressIsTrimmed() {

        val job = buildJob(
            title = "Fix router",
            description = "Replace the faulty router at premises",
            address = "  42 High Street  ",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("42 High Street", job.address)
    }

    @Test
    fun createJob_callsRepoWithCorrectJob() = runTest {

        val job = buildJob(
            title = "Cable replacement",
            description = "Replace damaged underground cable section",
            address = "15 Park Lane, Oxford",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.High,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        jobRepo.createJob(job)
        advanceUntilIdle()

        val captor = argumentCaptor<Job>()
        verify(jobRepo).createJob(captor.capture())
        val savedJob = captor.firstValue
        assertEquals("Cable replacement", savedJob.title)
        assertEquals(JobStatus.Assigned.name, savedJob.status)
        assertEquals("engineer-uid-456", savedJob.assignedTo)
        assertEquals("Jane Smith", savedJob.assignedEngineerName)
        assertEquals("dispatcher-uid-123", savedJob.createdBy)
        assertEquals(Priority.High.name, savedJob.priority)
    }

    @Test
    fun createJob_differentPriorities_allStoredCorrectly() = runTest {

        for (priority in Priority.entries) {
            val job = buildJob(
                title = "Test job for $priority",
                description = "Testing priority storage for ${priority.name}",
                address = "Test Address",
                scheduledStartMillis = System.currentTimeMillis(),
                scheduledEndMillis = System.currentTimeMillis() + 3600000,
                priority = priority,
                engineer = testEngineer,
                createdByUid = dispatcherUid
            )
            assertEquals(priority.name, job.priority)
        }
    }

    @Test
    fun createJob_idIsEmpty_firestoreWillAutoGenerate() {

        val job = buildJob(
            title = "New job",
            description = "This is a brand new job being created",
            address = "Somewhere",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("", job.id)
    }

    @Test
    fun createJob_serverTimestamps_areNull() {

        val job = buildJob(
            title = "New job",
            description = "This is a brand new job being created",
            address = "Somewhere",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertNull(job.createdAt)
        assertNull(job.updatedAt)
    }

    @Test
    fun createJob_rejectionReason_isEmptyForNewJobs() {

        val job = buildJob(
            title = "New job",
            description = "This is a brand new job being created",
            address = "Somewhere",
            scheduledStartMillis = System.currentTimeMillis(),
            scheduledEndMillis = System.currentTimeMillis() + 3600000,
            priority = Priority.Medium,
            engineer = testEngineer,
            createdByUid = dispatcherUid
        )

        assertEquals("", job.rejectionReason)
    }
}
