package com.raf.fieldops.ui.engineer.jobdetail

import android.content.Context
import com.raf.fieldops.util.AppLogger
import com.raf.fieldops.util.MapsLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Note
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.NoteRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.InputSanitiser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EngineerJobDetailVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val noteRepo: NoteRepo,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _job = MutableStateFlow<Job?>(null)
    val job: StateFlow<Job?> = _job.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    val noteText = MutableStateFlow("")

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _uiEvents = Channel<String>()
    val uiEvents = _uiEvents.receiveAsFlow()

    private val _navigateBack = Channel<Unit>()
    val navigateBack = _navigateBack.receiveAsFlow()

    private var currentUserName: String = "Engineer"
    private var currentUserUid: String = ""

    init {

        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch
            currentUserUid = uid
            val user = userRepo.getUserById(uid)
            currentUserName = user?.displayName ?: "Engineer"
        }
    }

    fun isNoteFromEngineer(note: Note): Boolean {
        return note.authorUid == currentUserUid
    }

    fun loadJob(initialJob: Job) {
        _job.value = initialJob
        observeJob(initialJob.id)
        observeNotes(initialJob.id)
    }

    fun acceptJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val updatedJob = currentJob.copy(
                    status = JobStatus.Accepted.name,
                    updatedAt = null
                )
                jobRepo.updateJob(updatedJob)
                _uiEvents.send("Job accepted")
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to accept job: ${e.message}")
                _uiEvents.send("Failed to accept job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun dismissJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val updatedJob = currentJob.copy(
                    status = JobStatus.Dismissed.name,
                    updatedAt = null
                )
                jobRepo.updateJob(updatedJob)
                _uiEvents.send("Job dismissed")
                _navigateBack.send(Unit)
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to dismiss job: ${e.message}")
                _uiEvents.send("Failed to dismiss job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun rejectJob(reason: String) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val updatedJob = currentJob.copy(
                    status = JobStatus.Assigned.name,
                    assignedTo = "",
                    assignedEngineerName = "",
                    rejectionReason = InputSanitiser.sanitiseLong(reason).ifBlank { "No reason provided" },
                    updatedAt = null
                )
                jobRepo.updateJob(updatedJob)
                _uiEvents.send("Job rejected")
                _navigateBack.send(Unit)
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to reject job: ${e.message}")
                _uiEvents.send("Failed to reject job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun startJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val updatedJob = currentJob.copy(
                    status = JobStatus.InProgress.name,
                    updatedAt = null
                )
                jobRepo.updateJob(updatedJob)
                _uiEvents.send("Job started")
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to start job: ${e.message}")
                _uiEvents.send("Failed to start job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun completeJob(completionNote: String) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {

                if (completionNote.isNotBlank()) {
                    noteRepo.addNote(
                        jobId = currentJob.id,
                        text = completionNote,
                        authorUid = currentUserUid,
                        authorName = currentUserName,
                        isInternal = false
                    )
                }

                val updatedJob = currentJob.copy(
                    status = JobStatus.Completed.name,
                    updatedAt = null
                )
                jobRepo.updateJob(updatedJob)
                _uiEvents.send("Job completed")
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to complete job: ${e.message}")
                _uiEvents.send("Failed to complete job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun addNote() {
        val currentJob = _job.value ?: return
        val text = InputSanitiser.sanitiseLong(noteText.value)

        if (text.isBlank()) {
            viewModelScope.launch { _uiEvents.send("Note cannot be empty") }
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                noteRepo.addNote(
                    jobId = currentJob.id,
                    text = text,
                    authorUid = currentUserUid,
                    authorName = currentUserName,
                    isInternal = false
                )

                noteText.value = ""
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to add note: ${e.message}")
                _uiEvents.send("Failed to add note")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun openMaps(context: Context) {
        val address = _job.value?.address ?: return
        val errorMessage = MapsLauncher.openDirections(context, address)
        if (errorMessage != null) {
            viewModelScope.launch { _uiEvents.send(errorMessage) }
        }
    }

    private fun observeJob(jobId: String) {
        viewModelScope.launch {
            firestore.collection("jobs").document(jobId)
                .snapshots()
                .map { snapshot -> snapshot.toObject(Job::class.java) }
                .catch { e ->
                    AppLogger.error(TAG, "Error observing job: ${e.message}")
                }
                .collect { _job.value = it }
        }
    }

    private fun observeNotes(jobId: String) {
        viewModelScope.launch {
            noteRepo.getNotesForJob(jobId)
                .map { notes -> notes.filter { !it.isInternal } }
                .catch { e ->
                    AppLogger.error(TAG, "Error observing notes: ${e.message}")
                    emit(emptyList())
                }
                .collect { _notes.value = it }
        }
    }

    companion object {
        private const val TAG = "EngineerJobDetailVM"
    }
}
