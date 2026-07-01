package com.raf.fieldops.integration

import com.raf.fieldops.data.repo.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class NoteFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private val noteRepo: NoteRepo = mock()

    private val testJobId = "job-001"
    private val engineerUid = "engineer-uid-456"
    private val engineerName = "Jane Smith"
    private val dispatcherUid = "dispatcher-uid-123"
    private val dispatcherName = "John Manager"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addNote_engineerNote_passesCorrectParamsWithInternalFalse() = runTest {

        val noteText = "Arrived on site, customer not home. Left card."

        noteRepo.addNote(
            jobId = testJobId,
            text = noteText,
            authorUid = engineerUid,
            authorName = engineerName,
            isInternal = false
        )
        advanceUntilIdle()

        verify(noteRepo).addNote(
            jobId = eq(testJobId),
            text = eq(noteText),
            authorUid = eq(engineerUid),
            authorName = eq(engineerName),
            isInternal = eq(false)
        )
    }

    @Test
    fun addNote_dispatcherInternalNote_passesIsInternalTrue() = runTest {

        val noteText = "Customer has history of complaints — handle with care"

        noteRepo.addNote(
            jobId = testJobId,
            text = noteText,
            authorUid = dispatcherUid,
            authorName = dispatcherName,
            isInternal = true
        )
        advanceUntilIdle()

        verify(noteRepo).addNote(
            jobId = eq(testJobId),
            text = eq(noteText),
            authorUid = eq(dispatcherUid),
            authorName = eq(dispatcherName),
            isInternal = eq(true)
        )
    }
}
