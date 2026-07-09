package com.raf.fieldops.data.repo

import com.raf.fieldops.data.model.Note
import com.raf.fieldops.data.remote.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// notes are append-only
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepo {

    override suspend fun addNote(
        jobId: String,
        text: String,
        authorUid: String,
        authorName: String,
        isInternal: Boolean
    ) = noteDao.addNote(jobId, text, authorUid, authorName, isInternal)

    override fun getNotesForJob(jobId: String): Flow<List<Note>> =
        noteDao.getNotesForJob(jobId)
}
