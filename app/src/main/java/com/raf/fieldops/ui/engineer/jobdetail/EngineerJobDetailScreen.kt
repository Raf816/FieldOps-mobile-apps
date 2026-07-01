package com.raf.fieldops.ui.engineer.jobdetail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.Note
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.data.model.toJobStatus
import com.raf.fieldops.data.model.toPriority
import com.raf.fieldops.ui.components.CustomTextField
import com.raf.fieldops.ui.components.PriorityBadge
import com.raf.fieldops.ui.components.ProgressBar
import com.raf.fieldops.ui.components.StatusChip
import com.raf.fieldops.ui.components.FieldOpsSnackbarHost
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtIndigoLight
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import com.raf.fieldops.ui.theme.StatusColours
import com.raf.fieldops.ui.theme.backgroundColour
import com.raf.fieldops.ui.theme.dotColour
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerJobDetailScreen(
    job: Job?,
    navigateBack: () -> Unit,
    vm: EngineerJobDetailVM = hiltViewModel()
) {

    val currentJob by vm.job.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val isSubmitting by vm.isSubmitting.collectAsStateWithLifecycle()
    val noteText by vm.noteText.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completionNote by remember { mutableStateOf("") }

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
        bottomBar = {

            if (currentJob != null) {
                val status = currentJob!!.status.toJobStatus()

                val isExpired = status == JobStatus.Assigned &&
                    currentJob!!.scheduledEnd?.toDate()?.before(java.util.Date()) == true
                if (status != JobStatus.Completed && status != JobStatus.Cancelled && status != JobStatus.Dismissed) {
                    Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 4.dp
                    ) {
                        StatusActionButtons(
                            status = status,
                            isExpired = isExpired,
                            onAccept = { vm.acceptJob() },
                            onReject = { showRejectDialog = true },
                            onStart = { vm.startJob() },
                            onComplete = { showCompleteDialog = true },
                            onDismiss = { vm.dismissJob() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
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
                CircularProgressIndicator(
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
                        item(key = "hero") {
                            Spacer(modifier = Modifier.height(4.dp))
                            HeroCard(job = displayJob, status = status, priority = priority)
                        }
                        item(key = "stepper") {
                            ProgressStepper(currentStatus = status)
                        }
                        item(key = "details") {
                            DetailSections(job = displayJob, onAddressTap = { vm.openMaps(context) })
                        }
                        item(key = "bottomSpacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(0.45f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "notesHeader") {
                            Spacer(modifier = Modifier.height(4.dp))
                            NotesHeader(noteCount = notes.size)
                        }
                        items(items = notes, key = { it.id }) { note ->
                            NoteBubble(note = note, isEngineerNote = vm.isNoteFromEngineer(note))
                        }
                        item(key = "addNote") {
                            AddNoteSection(
                                noteText = noteText,
                                onNoteTextChange = { vm.noteText.value = it },
                                onSend = { vm.addNote() }
                            )
                        }
                        item(key = "bottomSpacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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

                    item(key = "hero") {
                        Spacer(modifier = Modifier.height(4.dp))
                        HeroCard(
                            job = displayJob,
                            status = status,
                            priority = priority
                        )
                    }

                    item(key = "stepper") {
                        ProgressStepper(currentStatus = status)
                    }

                    item(key = "details") {
                        DetailSections(
                            job = displayJob,
                            onAddressTap = { vm.openMaps(context) }
                        )
                    }

                    item(key = "notesHeader") {
                        NotesHeader(noteCount = notes.size)
                    }

                    items(
                        items = notes,
                        key = { it.id }
                    ) { note ->
                        NoteBubble(
                            note = note,
                            isEngineerNote = vm.isNoteFromEngineer(note)
                        )
                    }

                    item(key = "addNote") {
                        AddNoteSection(
                            noteText = noteText,
                            onNoteTextChange = { vm.noteText.value = it },
                            onSend = { vm.addNote() }
                        )
                    }

                    item(key = "bottomSpacer") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                }
            }
        }

        if (showRejectDialog) {
            RejectDialog(
                reason = rejectReason,
                onReasonChange = { rejectReason = it },
                onConfirm = {
                    vm.rejectJob(rejectReason.ifBlank { "No reason provided" })
                    showRejectDialog = false
                    rejectReason = ""
                },
                onDismiss = {
                    showRejectDialog = false
                    rejectReason = ""
                }
            )
        }

        if (showCompleteDialog) {
            CompleteDialog(
                note = completionNote,
                onNoteChange = { completionNote = it },
                onComplete = {
                    vm.completeJob(completionNote)
                    showCompleteDialog = false
                    completionNote = ""
                },
                onSkip = {
                    vm.completeJob("")
                    showCompleteDialog = false
                    completionNote = ""
                }
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
                        colors = listOf(BtIndigo, BtIndigoLight)
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
                        text = "Assigned by dispatcher",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStepper(currentStatus: JobStatus) {
    val steps = listOf(
        JobStatus.Assigned to "Assigned",
        JobStatus.Accepted to "Accepted",
        JobStatus.InProgress to "In Progress",
        JobStatus.Completed to "Completed"
    )

    val isCancelled = currentStatus == JobStatus.Cancelled
    val currentIndex = if (isCancelled) -1 else steps.indexOfFirst { it.first == currentStatus }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Job progress stepper" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isCancelled) {

                Text(
                    text = "Job Cancelled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, (_, label) ->
                    val isCompleted = !isCancelled && index < currentIndex
                    val isCurrent = !isCancelled && index == currentIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {

                        val circleSize = if (isCurrent) 28.dp else 24.dp
                        val circleColour = when {
                            isCancelled -> MaterialTheme.colorScheme.outlineVariant
                            isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(circleSize)
                                .background(
                                    color = circleColour,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "$label completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isCancelled -> MaterialTheme.colorScheme.onSurfaceVariant
                                isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until steps.lastIndex) {
                    val lineColour = when {
                        isCancelled -> MaterialTheme.colorScheme.outlineVariant
                        index < currentIndex -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(lineColour)
                    )
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
                shape = MaterialTheme.shapes.medium
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
            shape = MaterialTheme.shapes.medium
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

        if (job.address.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddressTap)
                    .semantics { contentDescription = "Job address, tap for directions" },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
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
        }
    }
}

@Composable
private fun StatusActionButtons(
    status: JobStatus,
    isExpired: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status) {
        JobStatus.Assigned -> {
            if (isExpired) {

                Button(
                    onClick = onDismiss,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics { contentDescription = "Dismiss job button" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusColours.inProgressDot
                    )
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics { contentDescription = "Reject job button" },
                        border = BorderStroke(
                            1.dp, MaterialTheme.colorScheme.error
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Reject",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics { contentDescription = "Accept job button" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusColours.completedDot
                        )
                    ) {
                        Text(
                            text = "Accept",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        JobStatus.Accepted -> {

            Button(
                onClick = onStart,
                modifier = modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { contentDescription = "Start job button" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusColours.inProgressDot
                )
            ) {
                Text(
                    text = "Start Job",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        JobStatus.InProgress -> {

            Button(
                onClick = onComplete,
                modifier = modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { contentDescription = "Complete job button" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusColours.completedDot
                )
            ) {
                Text(
                    text = "Complete Job",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        else -> {  }
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
    isEngineerNote: Boolean
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.UK) }

    val alignment = if (isEngineerNote) Alignment.End else Alignment.Start
    val bubbleColour = if (isEngineerNote) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColour = if (isEngineerNote) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val metaColour = if (isEngineerNote) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val shape = if (isEngineerNote) {
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
    onNoteTextChange: (String) -> Unit,
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

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

@Composable
private fun RejectDialog(
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Job",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Optionally provide a reason for rejecting this job.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text("Reason (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Rejection reason input" },
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Reject",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CompleteDialog(
    note: String,
    onNoteChange: (String) -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(
                text = "Complete Job",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Optionally add a completion note.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Completion note (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Completion note input" },
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onComplete) {
                Text(
                    text = "Complete",
                    color = StatusColours.completedDot,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }
    )
}
