package com.raf.fieldops.ui.dispatcher.createjob

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.R
import com.raf.fieldops.data.model.Priority
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.model.displayName
import com.raf.fieldops.ui.components.CustomButton
import com.raf.fieldops.ui.components.CustomDropDownMenu
import com.raf.fieldops.ui.components.CustomTextField
import com.raf.fieldops.ui.components.DatePickerField
import com.raf.fieldops.ui.components.FieldOpsSnackbarHost
import com.raf.fieldops.ui.components.ProgressBar
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtMagenta
import com.raf.fieldops.ui.components.DatePickerField
import com.raf.fieldops.ui.components.TimePickerField

@Composable
fun CreateJobScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: CreateJobVM = hiltViewModel()
) {

    val title by vm.title.collectAsStateWithLifecycle()
    val description by vm.description.collectAsStateWithLifecycle()
    val address by vm.address.collectAsStateWithLifecycle()
    val addressSuggestions by vm.addressSuggestions.collectAsStateWithLifecycle()
    val isLookingUp by vm.isLookingUp.collectAsStateWithLifecycle()
    val noResultsFound by vm.noResultsFound.collectAsStateWithLifecycle()
    val scheduledStartMillis by vm.scheduledStartMillis.collectAsStateWithLifecycle()
    val scheduledEndMillis by vm.scheduledEndMillis.collectAsStateWithLifecycle()
    val selectedPriority by vm.selectedPriority.collectAsStateWithLifecycle()
    val selectedEngineer by vm.selectedEngineer.collectAsStateWithLifecycle()
    val engineers by vm.engineers.collectAsStateWithLifecycle()
    val isSubmitting by vm.isSubmitting.collectAsStateWithLifecycle()
    val hasAttemptedSubmit by vm.hasAttemptedSubmit.collectAsStateWithLifecycle()
    val freeSlots by vm.freeSlots.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        vm.navigateBack.collect { navigateBack() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { FieldOpsSnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                FormSectionCard(title = "Job Details") {
                    CustomTextField(
                        label = "Title",
                        value = title,
                        onValueChange = { vm.title.value = it },
                        error = if (hasAttemptedSubmit) vm.titleError() else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField(
                        label = "Description",
                        value = description,
                        onValueChange = { vm.description.value = it },
                        error = if (hasAttemptedSubmit) vm.descriptionError() else null,
                        minimumNumberOfLines = 3,
                        maximumNumberOfLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField(
                        label = "Address",
                        value = address,
                        onValueChange = { vm.onAddressChange(it) },
                        error = if (hasAttemptedSubmit) vm.addressError() else null
                    )

                    if (isLookingUp) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Searching...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (noResultsFound && !isLookingUp) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No results found — try a different search",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

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
                                            .clickable { vm.selectAddress(suggestion) }
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (suggestion != addressSuggestions.last()) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatePickerField(
                            label = "Start Date",
                            selectedMillis = scheduledStartMillis,
                            onSelected = { millis ->
                                vm.scheduledStartMillis.value = mergeDateAndTime(
                                    dateMillis = millis,
                                    timeMillis = scheduledStartMillis
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        TimePickerField(
                            label = "Start Time",
                            selectedMillis = scheduledStartMillis,
                            onSelected = { vm.scheduledStartMillis.value = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatePickerField(
                            label = "End Date",
                            selectedMillis = scheduledEndMillis,
                            onSelected = { millis ->
                                vm.scheduledEndMillis.value = mergeDateAndTime(
                                    dateMillis = millis,
                                    timeMillis = scheduledEndMillis
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        TimePickerField(
                            label = "End Time",
                            selectedMillis = scheduledEndMillis,
                            onSelected = { vm.scheduledEndMillis.value = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (hasAttemptedSubmit) {
                        val scheduleErr = vm.scheduleError()
                        if (scheduleErr != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scheduleErr,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    if (freeSlots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        onClick = { vm.selectFreeSlot(slot) },
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
                        options = Priority.entries.toList(),
                        selectedValue = selectedPriority,
                        onOptionSelected = { vm.selectedPriority.value = it },
                        displayText = { it.displayName() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (engineers.isEmpty()) {
                        CustomDropDownMenu(
                            label = "Assign Engineer",
                            options = emptyList<User>(),
                            selectedValue = null,
                            onOptionSelected = {},
                            displayText = { "" },
                            error = if (hasAttemptedSubmit) vm.engineerError() else null
                        )
                    } else {
                        CustomDropDownMenu(
                            label = "Assign Engineer",
                            options = engineers,
                            selectedValue = selectedEngineer,
                            onOptionSelected = { vm.selectedEngineer.value = it },
                            displayText = { it.displayName },
                            error = if (hasAttemptedSubmit) vm.engineerError() else null
                        )
                    }
                }

                Button(
                    onClick = { vm.createJob() },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics { contentDescription = "Create Job button" },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(BtIndigo, BtIndigo.copy(alpha = 0.8f), BtMagenta)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.btn_create_job),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = navigateBack,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = stringResource(R.string.btn_cancel))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (isSubmitting) {
            ProgressBar()
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

private fun mergeDateAndTime(dateMillis: Long, timeMillis: Long?): Long {
    val dateCal = java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }
    val timeCal = java.util.Calendar.getInstance().apply {
        if (timeMillis != null) timeInMillis = timeMillis
        else {
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
        }
    }

    dateCal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY))
    dateCal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE))
    dateCal.set(java.util.Calendar.SECOND, 0)
    dateCal.set(java.util.Calendar.MILLISECOND, 0)
    return dateCal.timeInMillis
}
