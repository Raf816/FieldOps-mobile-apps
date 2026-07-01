package com.raf.fieldops.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.raf.fieldops.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDao @Inject constructor(private val firestore: FirebaseFirestore) {

    private val jobsCollection = firestore.collection("jobs")

    suspend fun addNote(
        jobId: String,
        text: String,
        authorUid: String,
        authorName: String,
        isInternal: Boolean
    ) {
        val noteData = hashMapOf(
            "text" to text,
            "authorUid" to authorUid,
            "authorName" to authorName,
            "isInternal" to isInternal,
            "createdAt" to FieldValue.serverTimestamp()
        )
        jobsCollection.document(jobId)
            .collection("notes")
            .add(noteData)
            .await()
    }

    fun getNotesForJob(jobId: String): Flow<List<Note>> {
        return jobsCollection.document(jobId)
            .collection("notes")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Note::class.java) }
            .catch { emit(emptyList()) }
    }
}
