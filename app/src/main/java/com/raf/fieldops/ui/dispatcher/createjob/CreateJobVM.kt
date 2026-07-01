package com.raf.fieldops.ui.dispatcher.createjob

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.remote.AddressLookupService
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateJobVM @Inject constructor(
    private val jobRepo: JobRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val addressLookupService: AddressLookupService
) : ViewModel() {

    private val _addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    val addressSuggestions: StateFlow<List<String>> = _addressSuggestions.asStateFlow()

    private val _isLookingUp = MutableStateFlow(false)
    val isLookingUp: StateFlow<Boolean> = _isLookingUp.asStateFlow()

    private val _noResultsFound = MutableStateFlow(false)
    val noResultsFound: StateFlow<Boolean> = _noResultsFound.asStateFlow()

    private var autocompleteJob: kotlinx.coroutines.Job? = null

    fun onAddressChange(value: String) {
        address.value = value
        _noResultsFound.value = false

        if (value.trim().length < 3) {
            _addressSuggestions.value = emptyList()
            autocompleteJob?.cancel()
            return
        }

        autocompleteJob?.cancel()
        autocompleteJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _isLookingUp.value = true
            val results = addressLookupService.findAddresses(value.trim())
            _addressSuggestions.value = results
            _noResultsFound.value = results.isEmpty()
            _isLookingUp.value = false
        }
    }

    fun selectAddress(selectedAddress: String) {
        address.value = selectedAddress
        _addressSuggestions.value = emptyList()
        _noResultsFound.value = false
        autocompleteJob?.cancel()
    }

    val title = MutableStateFlow("")

    val description = MutableStateFlow("")

    val address = MutableStateFlow("")

    val scheduledStartMillis = MutableStateFlow<Long?>(null)

    val scheduledEndMillis = MutableStateFlow<Long?>(null)

    val selectedPriority = MutableStateFlow(Priority.Medium)

    val selectedEngineer = MutableStateFlow<User?>(null)

    val engineers: StateFlow<List<User>> = userRepo.getAllEngineers()
        .map { engineers -> engineers.filter { it.status == "active" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    val hasAttemptedSubmit = MutableStateFlow(false)

    private val _freeSlots = MutableStateFlow<List<FreeSlot>>(emptyList())
    val freeSlots: StateFlow<List<FreeSlot>> = _freeSlots.asStateFlow()

    fun selectFreeSlot(slot: FreeSlot) {
        scheduledStartMillis.value = slot.startMillis
        scheduledEndMillis.value = slot.endMillis
        _freeSlots.value = emptyList()
    }

    private val _uiEvents = Channel<String>()
    val uiEvents = _uiEvents.receiveAsFlow()

    private val _navigateBack = Channel<Unit>()
    val navigateBack = _navigateBack.receiveAsFlow()

    init {

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        scheduledStartMillis.value = calendar.timeInMillis

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        scheduledEndMillis.value = calendar.timeInMillis
    }

    fun titleError(): String? =
        if (title.value.length >= 5) null
        else "Title must be at least 5 characters"

    fun descriptionError(): String? =
        if (description.value.length >= 10) null
        else "Description must be at least 10 characters"

    fun addressError(): String? =
        if (address.value.isNotBlank()) null
        else "Address is required"

    fun engineerError(): String? =
        if (selectedEngineer.value != null) null
        else "Please select an engineer"

    fun scheduleError(): String? {
        val start = scheduledStartMillis.value ?: return "Start time is required"
        val end = scheduledEndMillis.value ?: return "End time is required"
        if (start < System.currentTimeMillis()) return "Start time cannot be in the past"
        return if (end > start) null else "End time must be after start time"
    }

    fun isValid(): Boolean =
        titleError() == null &&
            descriptionError() == null &&
            addressError() == null &&
            engineerError() == null &&
            scheduleError() == null

    fun createJob() {
        hasAttemptedSubmit.value = true
        if (!isValid()) {
            val schedErr = scheduleError()
            if (schedErr != null) {
                viewModelScope.launch { _uiEvents.send(schedErr) }
            }
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val uid = authRepo.currentUser?.uid ?: run {
                    _uiEvents.send("Not authenticated — please sign in again")
                    return@launch
                }

                val engineer = selectedEngineer.value!!
                val startMillis = scheduledStartMillis.value!!
                val endMillis = scheduledEndMillis.value!!

                val engineerJobs = jobRepo.getJobsForEngineer(engineer.uid).first()

                val conflict = ScheduleValidator.findConflict(
                    existingJobs = engineerJobs,
                    proposedStartMillis = startMillis,
                    proposedEndMillis = endMillis
                )

                if (conflict != null) {
                    _uiEvents.send(conflict.toMessage())
                    _freeSlots.value = ScheduleValidator.findFreeSlots(
                        existingJobs = engineerJobs,
                        dayMillis = startMillis
                    )
                    return@launch
                }

                _freeSlots.value = emptyList()

                val job = Job(
                    title = InputSanitiser.sanitiseShort(title.value),
                    description = InputSanitiser.sanitiseLong(description.value),
                    address = InputSanitiser.sanitiseShort(address.value),
                    scheduledStart = Timestamp(Date(startMillis)),
                    scheduledEnd = Timestamp(Date(endMillis)),
                    priority = selectedPriority.value.name,
                    status = JobStatus.Assigned.name,
                    assignedTo = engineer.uid,
                    assignedEngineerName = engineer.displayName,
                    createdBy = uid
                )

                jobRepo.createJob(job)
                _uiEvents.send("Job created")
                _navigateBack.send(Unit)
            } catch (e: Exception) {
                _uiEvents.send("Failed to create job: ${e.message}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
