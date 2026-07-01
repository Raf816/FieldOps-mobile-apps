package com.raf.fieldops.ui.dispatcher.jobdetail

import android.content.Context
import com.raf.fieldops.util.AppLogger
import com.raf.fieldops.util.MapsLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Note
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.remote.AddressLookupService
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.NoteRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.FreeSlot
import com.raf.fieldops.util.InputSanitiser
import com.raf.fieldops.util.ScheduleValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DispatcherJobDetailVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val noteRepo: NoteRepo,
    private val firestore: FirebaseFirestore,
    private val addressLookupService: AddressLookupService
) : ViewModel() {

    private val _job = MutableStateFlow<Job?>(null)
    val job: StateFlow<Job?> = _job.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val editTitle = MutableStateFlow("")
    val editDescription = MutableStateFlow("")
    val editAddress = MutableStateFlow("")
    val editStartMillis = MutableStateFlow<Long?>(null)
    val editEndMillis = MutableStateFlow<Long?>(null)
    val editPriority = MutableStateFlow(Priority.Medium)
    val editEngineer = MutableStateFlow<User?>(null)

    val noteText = MutableStateFlow("")
    val isNoteInternal = MutableStateFlow(false)

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _showCancelDialog = MutableStateFlow(false)
    val showCancelDialog: StateFlow<Boolean> = _showCancelDialog.asStateFlow()

    val engineers: StateFlow<List<User>> = userRepo.getAllEngineers()
        .map { engineers -> engineers.filter { it.status == "active" } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    private var addressLookupJob: kotlinx.coroutines.Job? = null

    fun onEditAddressChange(query: String) {
        editAddress.value = query
        addressLookupJob?.cancel()
        if (query.length < 3) {
            addressSuggestions.value = emptyList()
            return
        }
        addressLookupJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val results = addressLookupService.findAddresses(query)
            addressSuggestions.value = results
        }
    }

    fun selectEditAddress(address: String) {
        editAddress.value = address
        addressSuggestions.value = emptyList()
    }

    private val _freeSlots = MutableStateFlow<List<FreeSlot>>(emptyList())
    val freeSlots: StateFlow<List<FreeSlot>> = _freeSlots.asStateFlow()

    fun selectFreeSlot(slot: FreeSlot) {
        editStartMillis.value = slot.startMillis
        editEndMillis.value = slot.endMillis
        _freeSlots.value = emptyList()
    }

    private val _uiEvents = Channel<String>()
    val uiEvents = _uiEvents.receiveAsFlow()

    private val _navigateBack = Channel<Unit>()
    val navigateBack = _navigateBack.receiveAsFlow()

    private var currentUserName: String = "Dispatcher"
    private var currentUserUid: String = ""

    init {

        viewModelScope.launch {
            val uid = authRepo.currentUser?.uid ?: return@launch
            currentUserUid = uid
            val user = userRepo.getUserById(uid)
            currentUserName = user?.displayName ?: "Dispatcher"
        }
    }

    fun isNoteFromDispatcher(note: Note): Boolean {
        return note.authorUid == currentUserUid
    }

    fun loadJob(initialJob: Job) {
        _job.value = initialJob
        observeJob(initialJob.id)
        observeNotes(initialJob.id)
    }

    fun startEditing() {
        val currentJob = _job.value ?: return
        editTitle.value = currentJob.title
        editDescription.value = currentJob.description
        editAddress.value = currentJob.address
        editStartMillis.value = currentJob.scheduledStart?.toDate()?.time
        editEndMillis.value = currentJob.scheduledEnd?.toDate()?.time
        editPriority.value = currentJob.priority.let { Priority.valueOf(it) }

        editEngineer.value = engineers.value.find { it.uid == currentJob.assignedTo }
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun saveChanges() {
        val currentJob = _job.value ?: return
        val title = InputSanitiser.sanitiseShort(editTitle.value)
        val description = InputSanitiser.sanitiseLong(editDescription.value)
        val address = InputSanitiser.sanitiseShort(editAddress.value)
        val startMillis = editStartMillis.value
        val endMillis = editEndMillis.value
        val priority = editPriority.value
        val engineer = editEngineer.value

        if (title.length < 5) {
            viewModelScope.launch { _uiEvents.send("Title must be at least 5 characters") }
            return
        }
        if (description.length < 10) {
            viewModelScope.launch { _uiEvents.send("Description must be at least 10 characters") }
            return
        }
        if (address.isBlank()) {
            viewModelScope.launch { _uiEvents.send("Address is required") }
            return
        }
        if (startMillis == null) {
            viewModelScope.launch { _uiEvents.send("Start time is required") }
            return
        }
        if (startMillis < System.currentTimeMillis()) {
            viewModelScope.launch { _uiEvents.send("Start time cannot be in the past") }
            return
        }
        if (endMillis != null && endMillis <= startMillis) {
            viewModelScope.launch { _uiEvents.send("End time must be after start time") }
            return
        }
        if (engineer == null) {
            viewModelScope.launch { _uiEvents.send("Engineer assignment is required") }
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {

                val engineerJobs = jobRepo.getJobsForEngineer(engineer.uid)
                    .first()

                val conflict = ScheduleValidator.findConflict(
                    existingJobs = engineerJobs,
                    proposedStartMillis = startMillis,
                    proposedEndMillis = endMillis ?: (startMillis + 3600000),
                    excludeJobId = currentJob.id
                )

                if (conflict != null) {
                    _uiEvents.send(conflict.toMessage())
                    _freeSlots.value = ScheduleValidator.findFreeSlots(
                        existingJobs = engineerJobs,
                        dayMillis = startMillis,
                        excludeJobId = currentJob.id
                    )
                    return@launch
                }

                _freeSlots.value = emptyList()

                val updatedJob = currentJob.copy(
                    title = title,
                    description = description,
                    address = address,
                    scheduledStart = Timestamp(Date(startMillis)),
                    scheduledEnd = endMillis?.let { Timestamp(Date(it)) },
                    priority = priority.name,
                    status = JobStatus.Assigned.name,
                    assignedTo = engineer.uid,
                    assignedEngineerName = engineer.displayName
                )
                jobRepo.updateJob(updatedJob)
                _isEditing.value = false
                _uiEvents.send("Job updated")
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to update job: ${e.message}")
                _uiEvents.send("Failed to update job")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun showCancelDialog() {
        _showCancelDialog.value = true
    }

    fun dismissCancelDialog() {
        _showCancelDialog.value = false
    }

    fun cancelJob() {
        val currentJob = _job.value ?: return
        _showCancelDialog.value = false

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                jobRepo.deleteJob(currentJob.id)
                _uiEvents.send("Job cancelled")
                _navigateBack.send(Unit)
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to cancel job: ${e.message}")
                _uiEvents.send("Failed to cancel job")
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

        val uid = authRepo.currentUser?.uid ?: return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                noteRepo.addNote(
                    jobId = currentJob.id,
                    text = text,
                    authorUid = uid,
                    authorName = currentUserName,
                    isInternal = isNoteInternal.value
                )

                noteText.value = ""
                isNoteInternal.value = false
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
                .catch { e ->
                    AppLogger.error(TAG, "Error observing notes: ${e.message}")
                    emit(emptyList())
                }
                .collect { _notes.value = it }
        }
    }

    companion object {
        private const val TAG = "DispatcherJobDetailVM"
    }
}
