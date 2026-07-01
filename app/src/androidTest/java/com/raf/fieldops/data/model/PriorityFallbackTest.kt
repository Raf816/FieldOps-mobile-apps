package com.raf.fieldops.data.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PriorityFallbackTest {

    @Test
    fun toPriority_unknownValue_returnsMedium() {

        val input = "Critical"

        val result = input.toPriority()

        assertEquals(Priority.Medium, result)
    }

    @Test
    fun toPriority_emptyString_returnsMedium() {

        val input = ""

        val result = input.toPriority()

        assertEquals(Priority.Medium, result)
    }

    @Test
    fun toPriority_lowercaseValue_returnsMedium() {

        val input = "high"

        val result = input.toPriority()

        assertEquals(Priority.Medium, result)
    }
}
