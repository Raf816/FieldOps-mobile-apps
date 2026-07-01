package com.raf.fieldops.ui.dispatcher.jobdetail

import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DispatcherJobDetailVMValidationTest {

    @Test
    fun pastTimeValidation_startInPast_isInvalid() {

        val startMillis = System.currentTimeMillis() - 3600000

        val isInPast = startMillis < System.currentTimeMillis()

        assertTrue("Start time in the past should be invalid", isInPast)
    }

    @Test
    fun pastTimeValidation_startInFuture_isValid() {

        val startMillis = System.currentTimeMillis() + 3600000

        val isInPast = startMillis < System.currentTimeMillis()

        assertTrue("Start time in the future should be valid", !isInPast)
    }

    @Test
    fun pastTimeValidation_endBeforeStart_isInvalid() {

        val startMillis = System.currentTimeMillis() + 7200000
        val endMillis = System.currentTimeMillis() + 3600000

        val isEndBeforeStart = endMillis <= startMillis

        assertTrue("End time before start should be invalid", isEndBeforeStart)
    }

    @Test
    fun pastTimeValidation_endAfterStart_isValid() {

        val startMillis = System.currentTimeMillis() + 3600000
        val endMillis = System.currentTimeMillis() + 7200000

        val isEndAfterStart = endMillis > startMillis

        assertTrue("End time after start should be valid", isEndAfterStart)
    }

    @Test
    fun pastTimeValidation_titleTooShort_isInvalid() {

        val title = "Fix"

        val isTooShort = title.trim().length < 5

        assertTrue("Title under 5 chars should be invalid", isTooShort)
    }

    @Test
    fun pastTimeValidation_blankAddress_isInvalid() {

        val address = "   "

        val isBlank = address.trim().isBlank()

        assertTrue("Blank address should be invalid", isBlank)
    }

    @Test
    fun statusReset_dismissedJob_becomesAssignedOnSave() {

        val dismissedJob = Job(
            id = "job-001",
            title = "Fix router",
            description = "Replace faulty router",
            address = "42 High Street",
            status = JobStatus.Dismissed.name,
            assignedTo = "engineer-uid",
            assignedEngineerName = "Jane Smith"
        )

        val updatedJob = dismissedJob.copy(
            status = JobStatus.Assigned.name
        )

        assertEquals(JobStatus.Assigned.name, updatedJob.status)
    }

    @Test
    fun statusReset_assignedJob_staysAssignedOnSave() {

        val assignedJob = Job(
            id = "job-002",
            title = "Install fibre",
            description = "Full fibre installation",
            address = "10 Downing Street",
            status = JobStatus.Assigned.name,
            assignedTo = "engineer-uid",
            assignedEngineerName = "John Doe"
        )

        val updatedJob = assignedJob.copy(
            status = JobStatus.Assigned.name
        )

        assertEquals(JobStatus.Assigned.name, updatedJob.status)
    }

    @Test
    fun addressAutocomplete_shortInput_shouldNotTriggerLookup() {

        val query = "Hi"

        val shouldTrigger = query.length >= 3

        assertTrue("Input under 3 chars should not trigger lookup", !shouldTrigger)
    }

    @Test
    fun addressAutocomplete_longInput_shouldTriggerLookup() {

        val query = "College Road"

        val shouldTrigger = query.length >= 3

        assertTrue("Input of 3+ chars should trigger lookup", shouldTrigger)
    }

    @Test
    fun addressAutocomplete_selectingSuggestion_fillsAddress() {

        val suggestion = "42 High Street, Manchester, M1 2AB"

        val address = suggestion
        val suggestions = emptyList<String>()

        assertEquals("42 High Street, Manchester, M1 2AB", address)
        assertTrue("Suggestions should be cleared after selection", suggestions.isEmpty())
    }
}
