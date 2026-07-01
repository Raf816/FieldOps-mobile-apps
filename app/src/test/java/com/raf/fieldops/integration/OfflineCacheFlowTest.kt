package com.raf.fieldops.integration

import com.raf.fieldops.data.local.CachedJob
import com.raf.fieldops.data.local.toCachedJob
import com.raf.fieldops.data.local.toJob
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class OfflineCacheFlowTest {

    @Test
    fun toCachedJob_preservesId() {

        val job = createTestJob(id = "job-abc-123")

        val cached = job.toCachedJob()

        assertEquals("job-abc-123", cached.id)
    }

    @Test
    fun toCachedJob_preservesTitle() {

        val job = createTestJob(title = "Fix broadband router")

        val cached = job.toCachedJob()

        assertEquals("Fix broadband router", cached.title)
    }

    @Test
    fun toCachedJob_preservesDescription() {

        val job = createTestJob(description = "Replace faulty router at customer premises")

        val cached = job.toCachedJob()

        assertEquals("Replace faulty router at customer premises", cached.description)
    }

    @Test
    fun toCachedJob_preservesAddress() {

        val job = createTestJob(address = "42 High Street, Manchester")

        val cached = job.toCachedJob()

        assertEquals("42 High Street, Manchester", cached.address)
    }

    @Test
    fun toCachedJob_convertsTimestampToEpochMillis() {

        val startMillis = 1700000000000L
        val endMillis = 1700003600000L
        val job = createTestJob(
            scheduledStart = Timestamp(Date(startMillis)),
            scheduledEnd = Timestamp(Date(endMillis))
        )

        val cached = job.toCachedJob()

        assertEquals(startMillis, cached.scheduledStartMillis)
        assertEquals(endMillis, cached.scheduledEndMillis)
    }

    @Test
    fun toCachedJob_nullTimestamp_remainsNull() {

        val job = createTestJob(scheduledStart = null, scheduledEnd = null)

        val cached = job.toCachedJob()

        assertNull(cached.scheduledStartMillis)
        assertNull(cached.scheduledEndMillis)
    }

    @Test
    fun toCachedJob_preservesPriority() {

        val job = createTestJob(priority = Priority.High.name)

        val cached = job.toCachedJob()

        assertEquals(Priority.High.name, cached.priority)
    }

    @Test
    fun toCachedJob_preservesStatus() {

        val job = createTestJob(status = JobStatus.InProgress.name)

        val cached = job.toCachedJob()

        assertEquals(JobStatus.InProgress.name, cached.status)
    }

    @Test
    fun toCachedJob_preservesAssignment() {

        val job = createTestJob(
            assignedTo = "engineer-uid-456",
            assignedEngineerName = "Jane Smith"
        )

        val cached = job.toCachedJob()

        assertEquals("engineer-uid-456", cached.assignedTo)
        assertEquals("Jane Smith", cached.assignedEngineerName)
    }

    @Test
    fun toCachedJob_preservesRejectionReason() {

        val job = createTestJob(rejectionReason = "Vehicle breakdown")

        val cached = job.toCachedJob()

        assertEquals("Vehicle breakdown", cached.rejectionReason)
    }

    @Test
    fun toCachedJob_recordsCacheTimestamp() {

        val beforeCache = System.currentTimeMillis()
        val job = createTestJob()

        val cached = job.toCachedJob()
        val afterCache = System.currentTimeMillis()

        assertTrue(cached.cachedAt >= beforeCache)
        assertTrue(cached.cachedAt <= afterCache)
    }

    @Test
    fun toJob_roundTrip_preservesAllFields() {

        val startMillis = 1700000000000L
        val endMillis = 1700003600000L
        val original = Job(
            id = "job-round-trip",
            title = "Round trip test",
            description = "Testing round-trip conversion",
            address = "123 Test Street",
            scheduledStart = Timestamp(Date(startMillis)),
            scheduledEnd = Timestamp(Date(endMillis)),
            priority = Priority.Urgent.name,
            status = JobStatus.InProgress.name,
            assignedTo = "engineer-uid",
            assignedEngineerName = "Test Engineer",
            createdBy = "dispatcher-uid",
            rejectionReason = ""
        )

        val cached = original.toCachedJob()
        val restored = cached.toJob()

        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.address, restored.address)
        assertEquals(original.priority, restored.priority)
        assertEquals(original.status, restored.status)
        assertEquals(original.assignedTo, restored.assignedTo)
        assertEquals(original.assignedEngineerName, restored.assignedEngineerName)
        assertEquals(original.createdBy, restored.createdBy)
        assertEquals(original.rejectionReason, restored.rejectionReason)

        assertEquals(startMillis, restored.scheduledStart?.toDate()?.time)
        assertEquals(endMillis, restored.scheduledEnd?.toDate()?.time)
    }

    @Test
    fun toJob_nullTimestamps_remainNull() {

        val cached = createCachedJob(
            scheduledStartMillis = null,
            scheduledEndMillis = null,
            createdAtMillis = null,
            updatedAtMillis = null
        )

        val job = cached.toJob()

        assertNull(job.scheduledStart)
        assertNull(job.scheduledEnd)
        assertNull(job.createdAt)
        assertNull(job.updatedAt)
    }

    @Test
    fun cacheFiltering_todaysJobs_filtersCorrectly() {

        val now = System.currentTimeMillis()
        val todayStart = now - (now % 86400000)
        val todayEnd = todayStart + 86400000 - 1
        val tomorrowStart = todayEnd + 1

        val cachedJobs = listOf(
            createCachedJob(id = "today-1", scheduledStartMillis = todayStart + 3600000),
            createCachedJob(id = "today-2", scheduledStartMillis = todayStart + 7200000),
            createCachedJob(id = "tomorrow-1", scheduledStartMillis = tomorrowStart + 3600000)
        )

        val todaysJobs = cachedJobs.filter { job ->
            val startMillis = job.scheduledStartMillis ?: 0L
            startMillis in todayStart..todayEnd
        }

        assertEquals(2, todaysJobs.size)
        assertTrue(todaysJobs.all { it.id.startsWith("today") })
    }

    @Test
    fun cacheFiltering_sortByScheduledStart_ordersCorrectly() {

        val cachedJobs = listOf(
            createCachedJob(id = "late", scheduledStartMillis = 1700010000000L),
            createCachedJob(id = "early", scheduledStartMillis = 1700000000000L),
            createCachedJob(id = "middle", scheduledStartMillis = 1700005000000L)
        )

        val sorted = cachedJobs.sortedBy { it.scheduledStartMillis }

        assertEquals("early", sorted[0].id)
        assertEquals("middle", sorted[1].id)
        assertEquals("late", sorted[2].id)
    }

    @Test
    fun cacheFiltering_byEngineer_filtersCorrectly() {

        val cachedJobs = listOf(
            createCachedJob(id = "job-1", assignedTo = "engineer-a"),
            createCachedJob(id = "job-2", assignedTo = "engineer-b"),
            createCachedJob(id = "job-3", assignedTo = "engineer-a")
        )

        val engineerAJobs = cachedJobs.filter { it.assignedTo == "engineer-a" }

        assertEquals(2, engineerAJobs.size)
        assertTrue(engineerAJobs.all { it.assignedTo == "engineer-a" })
    }

    @Test
    fun cacheStaleness_recentCache_isNotStale() {

        val oneMinuteAgo = System.currentTimeMillis() - 60_000L
        val cached = createCachedJob(cachedAt = oneMinuteAgo)

        val staleThreshold = 5 * 60 * 1000L
        val isStale = (System.currentTimeMillis() - cached.cachedAt) > staleThreshold

        assertFalse(isStale)
    }

    @Test
    fun cacheStaleness_oldCache_isStale() {

        val tenMinutesAgo = System.currentTimeMillis() - 600_000L
        val cached = createCachedJob(cachedAt = tenMinutesAgo)

        val staleThreshold = 5 * 60 * 1000L
        val isStale = (System.currentTimeMillis() - cached.cachedAt) > staleThreshold

        assertTrue(isStale)
    }

    @Test
    fun toCachedJob_allStatuses_preservedCorrectly() {

        for (status in JobStatus.entries) {
            val job = createTestJob(status = status.name)
            val cached = job.toCachedJob()
            assertEquals(
                "Status ${status.name} should be preserved",
                status.name,
                cached.status
            )
        }
    }

    @Test
    fun toCachedJob_allPriorities_preservedCorrectly() {

        for (priority in Priority.entries) {
            val job = createTestJob(priority = priority.name)
            val cached = job.toCachedJob()
            assertEquals(
                "Priority ${priority.name} should be preserved",
                priority.name,
                cached.priority
            )
        }
    }

    private fun createTestJob(
        id: String = "test-job-id",
        title: String = "Test Job",
        description: String = "Test description",
        address: String = "Test Address",
        scheduledStart: Timestamp? = Timestamp.now(),
        scheduledEnd: Timestamp? = Timestamp.now(),
        priority: String = Priority.Medium.name,
        status: String = JobStatus.Assigned.name,
        assignedTo: String = "engineer-uid",
        assignedEngineerName: String = "Test Engineer",
        createdBy: String = "dispatcher-uid",
        rejectionReason: String = ""
    ): Job = Job(
        id = id,
        title = title,
        description = description,
        address = address,
        scheduledStart = scheduledStart,
        scheduledEnd = scheduledEnd,
        priority = priority,
        status = status,
        assignedTo = assignedTo,
        assignedEngineerName = assignedEngineerName,
        createdBy = createdBy,
        rejectionReason = rejectionReason
    )

    private fun createCachedJob(
        id: String = "cached-job-id",
        title: String = "Cached Job",
        description: String = "Cached description",
        address: String = "Cached Address",
        scheduledStartMillis: Long? = System.currentTimeMillis(),
        scheduledEndMillis: Long? = System.currentTimeMillis() + 3600000,
        priority: String = Priority.Medium.name,
        status: String = JobStatus.Assigned.name,
        assignedTo: String = "engineer-uid",
        assignedEngineerName: String = "Test Engineer",
        createdBy: String = "dispatcher-uid",
        rejectionReason: String = "",
        createdAtMillis: Long? = null,
        updatedAtMillis: Long? = null,
        cachedAt: Long = System.currentTimeMillis()
    ): CachedJob = CachedJob(
        id = id,
        title = title,
        description = description,
        address = address,
        scheduledStartMillis = scheduledStartMillis,
        scheduledEndMillis = scheduledEndMillis,
        priority = priority,
        status = status,
        assignedTo = assignedTo,
        assignedEngineerName = assignedEngineerName,
        createdBy = createdBy,
        rejectionReason = rejectionReason,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        cachedAt = cachedAt
    )
}
