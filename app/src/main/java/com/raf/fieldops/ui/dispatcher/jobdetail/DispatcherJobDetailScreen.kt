package com.raf.fieldops.ui.dispatcher.jobdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Note
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.model.toPriority
import com.raf.fieldops.ui.components.ConfirmDialog
import com.raf.fieldops.ui.components.CustomButton
import com.raf.fieldops.ui.components.CustomDropDownMenu
import com.raf.fieldops.ui.components.CustomTextField
import com.raf.fieldops.ui.components.DatePickerField
import com.raf.fieldops.ui.components.FieldOpsSnackbarHost
import com.raf.fieldops.ui.components.PriorityBadge
import com.raf.fieldops.ui.components.ProgressBar
import com.raf.fieldops.ui.components.StatusChip
import com.raf.fieldops.ui.components.TimePickerField
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtIndigoLight
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import com.raf.fieldops.ui.theme.backgroundColour
import com.raf.fieldops.util.FreeSlot
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatcherJobDetailScreen(
    job: Job?,
    navigateBack: () -> Unit,
    vm: DispatcherJobDetailVM = hiltViewModel()
) {

    val currentJob by vm.job.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val isEditing by vm.isEditing.collectAsStateWithLifecycle()
    val isSubmitting by vm.isSubmitting.collectAsStateWithLifecycle()
    val showCancelDialog by vm.showCancelDialog.collectAsStateWithLifecycle()
    val engineers by vm.engineers.collectAsStateWithLifecycle()

    val editTitle by vm.editTitle.collectAsStateWithLifecycle()
    val editDescription by vm.editDescription.collectAsStateWithLifecycle()
    val editAddress by vm.editAddress.collectAsStateWithLifecycle()
    val editStartMillis by vm.editStartMillis.collectAsStateWithLifecycle()
    val editEndMillis by vm.editEndMillis.collectAsStateWithLifecycle()
    val editPriority by vm.editPriority.collectAsStateWithLifecycle()
    val editEngineer by vm.editEngineer.collectAsStateWithLifecycle()
    val addressSuggestions by vm.addressSuggestions.collectAsStateWithLifecycle()
    val freeSlots by vm.freeSlots.collectAsStateWithLifecycle()

    val noteText by vm.noteText.collectAsStateWithLifecycle()
    val isNoteInternal by vm.isNoteInternal.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(job) {
        job?.let { vm.loadJob(it) }
    }

    LaunchedEffect(Unit) {
        vm.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        vm.navigateBack.collect {
            navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Job Detail",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { FieldOpsSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (currentJob == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val displayJob = currentJob!!
            val status = displayJob.status.toJobStatus()
            val priority = displayJob.priority.toPriority()
            val listState = rememberLazyListState()

            val widthClass = LocalWindowWidthClass.current
            val isExpanded = widthClass == WindowWidthSizeClass.Expanded

            if (isExpanded) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(0.55f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            HeroCard(job = displayJob, status = status, priority = priority)
                        }
                        if (isEditing) {
                            item {
                                EditModeSection(
                                    editTitle = editTitle, editDescription = editDescription,
                                    editAddress = editAddress, editStartMillis = editStartMillis,
                                    editEndMillis = editEndMillis, editPriority = editPriority,
                                    editEngineer = editEngineer, engineers = engineers,
                                    addressSuggestions = addressSuggestions, freeSlots = freeSlots,
                                    onTitleChange = { vm.editTitle.value = it },
                                    onDescriptionChange = { vm.editDescription.value = it },
                                    onAddressChange = { vm.onEditAddressChange(it) },
                                    onAddressSelect = { vm.selectEditAddress(it) },
                                    onFreeSlotSelect = { vm.selectFreeSlot(it) },
                                    onStartChange = { vm.editStartMillis.value = it },
                                    onEndChange = { vm.editEndMillis.value = it },
                                    onPriorityChange = { vm.editPriority.value = it },
                                    onEngineerChange = { vm.editEngineer.value = it },
                                    onSave = { vm.saveChanges() },
                                    onCancel = { vm.cancelEditing() }
                                )
                            }
                        } else {
                            item { DetailSections(job = displayJob, onAddressTap = { vm.openMaps(context) }) }
                            item { ActionBar(status = status, onEdit = { vm.startEditing() }, onCancel = { vm.showCancelDialog() }) }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(0.45f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            NotesHeader(noteCount = notes.size)
                        }
                        items(items = notes, key = { it.id }) { note ->
                            NoteBubble(note = note, isDispatcherNote = vm.isNoteFromDispatcher(note))
                        }
                        item {
                            AddNoteSection(noteText = noteText, isInternal = isNoteInternal,
                                onNoteTextChange = { vm.noteText.value = it },
                                onInternalChange = { vm.isNoteInternal.value = it },
                                onSend = { vm.addNote() })
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HeroCard(
                        job = displayJob,
                        status = status,
                        priority = priority
                    )
                }

                if (isEditing) {
                    item {
                        EditModeSection(
                            editTitle = editTitle,
                            editDescription = editDescription,
                            editAddress = editAddress,
                            editStartMillis = editStartMillis,
                            editEndMillis = editEndMillis,
                            editPriority = editPriority,
                            editEngineer = editEngineer,
                            engineers = engineers,
                            addressSuggestions = addressSuggestions,
                            freeSlots = freeSlots,
                            onTitleChange = { vm.editTitle.value = it },
                            onDescriptionChange = { vm.editDescription.value = it },
                            onAddressChange = { vm.onEditAddressChange(it) },
                            onAddressSelect = { vm.selectEditAddress(it) },
                            onFreeSlotSelect = { vm.selectFreeSlot(it) },
                            onStartChange = { vm.editStartMillis.value = it },
                            onEndChange = { vm.editEndMillis.value = it },
                            onPriorityChange = { vm.editPriority.value = it },
                            onEngineerChange = { vm.editEngineer.value = it },
                            onSave = { vm.saveChanges() },
                            onCancel = { vm.cancelEditing() }
                        )
                    }
                } else {
                    item {
                        DetailSections(
                            job = displayJob,
                            onAddressTap = { vm.openMaps(context) }
                        )
                    }

                    item {
                        ActionBar(
                            status = status,
                            onEdit = { vm.startEditing() },
                            onCancel = { vm.showCancelDialog() }
                        )
                    }
                }

                item {
                    NotesHeader(noteCount = notes.size)
                }

                items(
                    items = notes,
                    key = { it.id }
                ) { note ->
                    NoteBubble(
                        note = note,
                        isDispatcherNote = vm.isNoteFromDispatcher(note)
                    )
                }

                item {
                    AddNoteSection(
                        noteText = noteText,
                        isInternal = isNoteInternal,
                        onNoteTextChange = { vm.noteText.value = it },
                        onInternalChange = { vm.isNoteInternal.value = it },
                        onSend = { vm.addNote() }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            }
            }
        }

        if (showCancelDialog) {
            ConfirmDialog(
                title = "Cancel Job?",
                message = "This will cancel the job and delete all related notes. This cannot be undone.",
                confirmText = "Cancel Job",
                dismissText = "Keep Job",
                onConfirm = { vm.cancelJob() },
                onDismiss = { vm.dismissCancelDialog() }
            )
        }

        if (isSubmitting) {
            ProgressBar()
        }
    }
}

@Composable
private fun HeroCard(
    job: Job,
    status: JobStatus,
    priority: Priority
) {
    val statusBackground = status.backgroundColour()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Job hero card" },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BtIndigo,
                            BtIndigoLight
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = status)
                    PriorityBadge(priority = priority)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = job.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )

                if (job.assignedEngineerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.assignedEngineerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSections(
    job: Job,
    onAddressTap: () -> Unit
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (job.description.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = job.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                job.scheduledStart?.let { start ->
                    Text(
                        text = "Start: ${dateTimeFormatter.format(start.toDate())}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                job.scheduledEnd?.let { end ->
                    Text(
                        text = "End: ${dateTimeFormatter.format(end.toDate())}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddressTap)
                .semantics { contentDescription = "Job address, tap for directions" },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Tap for directions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        job.createdAt?.let { createdAt ->
            Text(
                text = "Created ${dateTimeFormatter.format(createdAt.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditModeSection(
    editTitle: String,
    editDescription: String,
    editAddress: String,
    editStartMillis: Long?,
    editEndMillis: Long?,
    editPriority: Priority,
    editEngineer: User?,
    engineers: List<User>,
    addressSuggestions: List<String>,
    freeSlots: List<FreeSlot>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onAddressSelect: (String) -> Unit,
    onFreeSlotSelect: (FreeSlot) -> Unit,
    onStartChange: (Long) -> Unit,
    onEndChange: (Long) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onEngineerChange: (User) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        FormSectionCard(title = "Job Details") {
            CustomTextField(
                label = "Title",
                value = editTitle,
                onValueChange = onTitleChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Description",
                value = editDescription,
                onValueChange = onDescriptionChange,
                minimumNumberOfLines = 3,
                maximumNumberOfLines = 5
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Address",
                value = editAddress,
                onValueChange = onAddressChange
            )

            if (addressSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "${addressSuggestions.size} suggestions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        addressSuggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAddressSelect(suggestion) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (suggestion != addressSuggestions.last()) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        FormSectionCard(title = "Schedule") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    label = "Start Date",
                    selectedMillis = editStartMillis,
                    onSelected = onStartChange,
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    label = "Start Time",
                    selectedMillis = editStartMillis,
                    onSelected = onStartChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    label = "End Date",
                    selectedMillis = editEndMillis,
                    onSelected = onEndChange,
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    label = "End Time",
                    selectedMillis = editEndMillis,
                    onSelected = onEndChange,
                    modifier = Modifier.weight(1f)
                )
            }

            if (freeSlots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Available time slots:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        freeSlots.forEach { slot ->
                            androidx.compose.material3.SuggestionChip(
                                onClick = { onFreeSlotSelect(slot) },
                                label = {
                                    Text(
                                        text = slot.toDisplayText(),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        FormSectionCard(title = "Assignment") {
            CustomDropDownMenu(
                label = "Priority",
                options = Priority.entries,
                selectedValue = editPriority,
                onOptionSelected = onPriorityChange,
                displayText = { it.displayName() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomDropDownMenu(
                label = "Assigned Engineer",
                options = engineers,
                selectedValue = editEngineer,
                onOptionSelected = onEngineerChange,
                displayText = { it.displayName }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.onSurface
                ),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Cancel Edit",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            CustomButton(
                text = "Save Changes",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ActionBar(
    status: JobStatus,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {

    val canEdit = status == JobStatus.Assigned || status == JobStatus.Dismissed

    val canCancel = status == JobStatus.Assigned ||
        status == JobStatus.Accepted ||
        status == JobStatus.InProgress ||
        status == JobStatus.Dismissed

    if (!canEdit && !canCancel) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (canEdit) {
            CustomButton(
                text = if (status == JobStatus.Dismissed) "Reschedule" else "Edit",
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
        }
        if (canCancel) {
            CustomButton(
                text = "Delete Job",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                containerColour = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun NotesHeader(noteCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (noteCount > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$noteCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun NoteBubble(
    note: Note,
    isDispatcherNote: Boolean
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.UK) }

    val alignment = if (isDispatcherNote) Alignment.End else Alignment.Start
    val bubbleColour = if (isDispatcherNote) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColour = if (isDispatcherNote) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val metaColour = if (isDispatcherNote) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val shape = if (isDispatcherNote) {
        RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColour)
                .padding(12.dp)
        ) {
            Column {

                if (note.isInternal) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Internal note",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Internal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColour
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${note.authorName} · ${note.createdAt?.let { dateFormatter.format(it.toDate()) } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = metaColour
                )
            }
        }
    }
}

@Composable
private fun AddNoteSection(
    noteText: String,
    isInternal: Boolean,
    onNoteTextChange: (String) -> Unit,
    onInternalChange: (Boolean) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomTextField(
                label = "Add a note...",
                value = noteText,
                onValueChange = onNoteTextChange,
                minimumNumberOfLines = 2,
                maximumNumberOfLines = 4
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isInternal,
                        onCheckedChange = onInternalChange,
                        modifier = Modifier.semantics {
                            contentDescription = "Internal note toggle"
                            stateDescription = if (isInternal) "Enabled — note visible to dispatchers only" else "Disabled — note visible to everyone"
                        }
                    )
                    Text(
                        text = "Internal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Send note button" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send note",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
