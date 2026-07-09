package com.raf.fieldops.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.raf.fieldops.data.model.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobDao @Inject constructor(private val firestore: FirebaseFirestore) {

    private val jobsCollection = firestore.collection("jobs")

    // reserve ID before writing so the Job carries its own id field
    suspend fun add(job: Job) {
        val newDocRef = jobsCollection.document()
        val jobWithId = job.copy(id = newDocRef.id)
        newDocRef.set(jobWithId).await()
    }

    suspend fun update(job: Job) {
        jobsCollection.document(job.id).set(job).await()
    }

    // delete notes subcollection first, then the job itself
    suspend fun delete(jobId: String) {
        withContext(NonCancellable) {
            val jobRef = jobsCollection.document(jobId)

            val notesSnapshot = jobRef.collection("notes").get().await()
            for (noteDoc in notesSnapshot) {
                noteDoc.reference.delete().await()
            }

            jobRef.delete().await()
        }
    }

    fun getAll(): Flow<List<Job>> {
        return jobsCollection
            .orderBy("scheduledStart", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Job::class.java) }
            .catch { emit(emptyList()) }
    }

    fun getByEngineer(uid: String): Flow<List<Job>> {
        return jobsCollection
            .whereEqualTo("assignedTo", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Job::class.java)
                    .sortedBy { it.scheduledStart }
            }
            .catch { emit(emptyList()) }
    }

    fun getByEngineerToday(uid: String): Flow<List<Job>> {
        val (startOfDay, endOfDay) = getTodayRange()

        return jobsCollection
            .whereEqualTo("assignedTo", uid)
            .whereGreaterThanOrEqualTo("scheduledStart", startOfDay)
            .whereLessThan("scheduledStart", endOfDay)
            .orderBy("scheduledStart", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Job::class.java) }
            .catch { emit(emptyList()) }
    }

    fun getByEngineerUpcoming(uid: String): Flow<List<Job>> {
        val (startOfTomorrow, endOfWeek) = getUpcomingRange()

        return jobsCollection
            .whereEqualTo("assignedTo", uid)
            .whereGreaterThanOrEqualTo("scheduledStart", startOfTomorrow)
            .whereLessThan("scheduledStart", endOfWeek)
            .orderBy("scheduledStart", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Job::class.java) }
            .catch { emit(emptyList()) }
    }

    suspend fun getById(jobId: String): Job? {
        val snapshot = jobsCollection.document(jobId).get().await()
        return snapshot.toObject(Job::class.java)
    }

    fun getCompletedJobsForEngineer(uid: String): Flow<List<Job>> {
        return jobsCollection
            .whereEqualTo("assignedTo", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Job::class.java)
                    .filter { it.status == "Completed" || it.status == "Cancelled" }
            }
            .catch { emit(emptyList()) }
    }

    private fun getTodayRange(): Pair<Timestamp, Timestamp> {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = Timestamp(calendar.time)

        return Pair(startOfDay, endOfDay)
    }

    private fun getUpcomingRange(): Pair<Timestamp, Timestamp> {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val startOfTomorrow = Timestamp(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val endOfWeek = Timestamp(calendar.time)

        return Pair(startOfTomorrow, endOfWeek)
    }
}
