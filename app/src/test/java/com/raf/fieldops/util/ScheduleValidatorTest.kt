package com.raf.fieldops.util

import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ScheduleValidatorTest {

    private fun createJob(
        id: String = "job-1",
        startHour: Int,
        endHour: Int,
        status: String = JobStatus.Accepted.name
    ): Job {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, startHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time

        cal.set(Calendar.HOUR_OF_DAY, endHour)
        val end = cal.time

        return Job(
            id = id,
            title = "Test Job $id",
            description = "Test description",
            address = "Test address",
            scheduledStart = Timestamp(start),
            scheduledEnd = Timestamp(end),
            status = status,
            assignedTo = "engineer-1",
            assignedEngineerName = "Jane Smith",
            priority = Priority.Medium.name
        )
    }

    private fun todayAt(hour: Int, minute: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun findConflict_noExistingJobs_returnsNull() {
        val result = ScheduleValidator.findConflict(
            existingJobs = emptyList(),
            proposedStartMillis = todayAt(9),
            proposedEndMillis = todayAt(10)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_noOverlap_returnsNull() {

        val existing = listOf(createJob(startHour = 9, endHour = 10))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(11),
            proposedEndMillis = todayAt(12)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_fullOverlap_returnsConflict() {

        val existing = listOf(createJob(startHour = 9, endHour = 11))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(9, 30),
            proposedEndMillis = todayAt(10, 30)
        )
        assertNotNull(result)
    }

    @Test
    fun findConflict_partialOverlapStart_returnsConflict() {

        val existing = listOf(createJob(startHour = 9, endHour = 11))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(8),
            proposedEndMillis = todayAt(10)
        )
        assertNotNull(result)
    }

    @Test
    fun findConflict_partialOverlapEnd_returnsConflict() {

        val existing = listOf(createJob(startHour = 9, endHour = 11))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(10),
            proposedEndMillis = todayAt(12)
        )
        assertNotNull(result)
    }

    @Test
    fun findConflict_adjacentJobs_noConflict() {

        val existing = listOf(createJob(startHour = 9, endHour = 10))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(10),
            proposedEndMillis = todayAt(11)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_completedJobIgnored_returnsNull() {

        val existing = listOf(createJob(startHour = 9, endHour = 11, status = JobStatus.Completed.name))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(9),
            proposedEndMillis = todayAt(10)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_cancelledJobIgnored_returnsNull() {
        val existing = listOf(createJob(startHour = 9, endHour = 11, status = JobStatus.Cancelled.name))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(9),
            proposedEndMillis = todayAt(10)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_dismissedJobIgnored_returnsNull() {
        val existing = listOf(createJob(startHour = 9, endHour = 11, status = JobStatus.Dismissed.name))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(9),
            proposedEndMillis = todayAt(10)
        )
        assertNull(result)
    }

    @Test
    fun findConflict_excludeJobId_ignoresSelf() {

        val existing = listOf(createJob(id = "job-1", startHour = 9, endHour = 11))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(9, 30),
            proposedEndMillis = todayAt(11, 30),
            excludeJobId = "job-1"
        )
        assertNull(result)
    }

    @Test
    fun findConflict_multipleJobs_findsFirstConflict() {
        val existing = listOf(
            createJob(id = "job-1", startHour = 8, endHour = 9),
            createJob(id = "job-2", startHour = 10, endHour = 12),
            createJob(id = "job-3", startHour = 14, endHour = 16)
        )

        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(11),
            proposedEndMillis = todayAt(13)
        )
        assertNotNull(result)
        assertEquals("job-2", result!!.conflictingJob.id)
    }

    @Test
    fun findConflict_returnsReadableMessage() {
        val existing = listOf(createJob(id = "job-1", startHour = 9, endHour = 11))
        val result = ScheduleValidator.findConflict(
            existingJobs = existing,
            proposedStartMillis = todayAt(10),
            proposedEndMillis = todayAt(12)
        )
        assertNotNull(result)
        assertTrue(result!!.toMessage().contains("Jane Smith"))
        assertTrue(result.toMessage().contains("Test Job job-1"))
    }

    @Test
    fun findFreeSlots_noExistingJobs_returnsHourlySlots() {

        val tomorrowAt7 = run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 7)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val slots = ScheduleValidator.findFreeSlots(
            existingJobs = emptyList(),
            dayMillis = tomorrowAt7
        )

        assertEquals(11, slots.size)
        assertEquals("07:00", slots.first().startFormatted)
        assertEquals("08:00", slots.first().endFormatted)
        assertEquals("17:00", slots.last().startFormatted)
        assertEquals("18:00", slots.last().endFormatted)
    }

    @Test
    fun findFreeSlots_oneJobInMiddle_returnsSlotsBeforeAndAfter() {

        val tomorrowAt9 = run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val existing = listOf(createJobTomorrow(startHour = 10, endHour = 12))
        val slots = ScheduleValidator.findFreeSlots(
            existingJobs = existing,
            dayMillis = tomorrowAt9
        )

        assertEquals(9, slots.size)
        assertEquals("07:00", slots.first().startFormatted)
        assertEquals("12:00", slots[3].startFormatted)
    }

    @Test
    fun findFreeSlots_slotsAreExactlyOneHour() {
        val tomorrowAt9 = run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val slots = ScheduleValidator.findFreeSlots(
            existingJobs = emptyList(),
            dayMillis = tomorrowAt9
        )

        for (slot in slots) {
            assertEquals(3600000L, slot.endMillis - slot.startMillis)
        }
    }

    @Test
    fun findFreeSlots_excludeJobId_ignoresSelf() {
        val tomorrowAt9 = run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val existing = listOf(createJobTomorrow(id = "job-1", startHour = 10, endHour = 12))
        val slots = ScheduleValidator.findFreeSlots(
            existingJobs = existing,
            dayMillis = tomorrowAt9,
            excludeJobId = "job-1"
        )

        assertEquals(11, slots.size)
    }

    @Test
    fun findFreeSlots_completedJobsIgnored() {
        val tomorrowAt9 = run {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val existing = listOf(createJobTomorrow(startHour = 10, endHour = 12, status = JobStatus.Completed.name))
        val slots = ScheduleValidator.findFreeSlots(
            existingJobs = existing,
            dayMillis = tomorrowAt9
        )

        assertEquals(11, slots.size)
    }

    private fun createJobTomorrow(
        id: String = "job-1",
        startHour: Int,
        endHour: Int,
        status: String = JobStatus.Accepted.name
    ): Job {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, startHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time

        cal.set(Calendar.HOUR_OF_DAY, endHour)
        val end = cal.time

        return Job(
            id = id,
            title = "Test Job $id",
            description = "Test description",
            address = "Test address",
            scheduledStart = Timestamp(start),
            scheduledEnd = Timestamp(end),
            status = status,
            assignedTo = "engineer-1",
            assignedEngineerName = "Jane Smith",
            priority = Priority.Medium.name
        )
    }

    @Test
    fun freeSlot_toDisplayText_formatsCorrectly() {
        val slot = FreeSlot(
            startMillis = todayAt(9),
            endMillis = todayAt(10),
            startFormatted = "09:00",
            endFormatted = "10:00"
        )
        assertEquals("09:00 – 10:00", slot.toDisplayText())
    }
}
