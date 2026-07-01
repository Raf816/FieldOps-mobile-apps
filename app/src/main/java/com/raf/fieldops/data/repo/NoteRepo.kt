package com.raf.fieldops.data.repo

import com.raf.fieldops.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepo {

    suspend fun addNote(
        jobId: String,
        text: String,
        authorUid: String,
        authorName: String,
        isInternal: Boolean
    )

    fun getNotesForJob(jobId: String): Flow<List<Note>>
}
