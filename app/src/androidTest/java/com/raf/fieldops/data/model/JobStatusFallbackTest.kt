package com.raf.fieldops.data.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobStatusFallbackTest {

    @Test
    fun toJobStatus_unknownValue_returnsAssigned() {

        val input = "InvalidStatus"

        val result = input.toJobStatus()

        assertEquals(JobStatus.Assigned, result)
    }

    @Test
    fun toJobStatus_emptyString_returnsAssigned() {

        val input = ""

        val result = input.toJobStatus()

        assertEquals(JobStatus.Assigned, result)
    }

    @Test
    fun toJobStatus_lowercaseValue_returnsAssigned() {

        val input = "assigned"

        val result = input.toJobStatus()

        assertEquals(JobStatus.Assigned, result)
    }
}
