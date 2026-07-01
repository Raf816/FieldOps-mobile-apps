package com.raf.fieldops.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.R
import com.raf.fieldops.ui.components.CustomButton
import com.raf.fieldops.ui.components.CustomTextField
import com.raf.fieldops.ui.components.FieldOpsSnackbarHost
import com.raf.fieldops.ui.components.ProgressBar
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtMagenta

@Composable
fun RegisterScreen(
    navigateBack: () -> Unit,
    navigateToConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: RegisterVM = hiltViewModel()
) {

    val registerUiState = vm.registerUiState
    val response = vm.signUpResponse

    val snackbarHostState = remember { SnackbarHostState() }

    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val passwordStrength = calculatePasswordStrength(registerUiState.password)

    LaunchedEffect(Unit) {
        vm.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (response is Response.Success) {
        LaunchedEffect(response) {
            navigateToConfirmation(registerUiState.email)
        }
    }

    Scaffold(
        snackbarHost = { FieldOpsSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)

                .background(MaterialTheme.colorScheme.background)

                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BtMagenta.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                BackNavigationRow(onBackClick = navigateBack)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.auth_create_account),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.auth_set_up_profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomTextField(
                    label = stringResource(R.string.auth_full_name),
                    value = registerUiState.displayName,
                    onValueChange = { vm.onChange(displayName = it) },
                    error = if (hasAttemptedSubmit && registerUiState.nameIsInvalid()) {
                        if (registerUiState.nameContainsNumbers()) {
                            stringResource(R.string.error_name_contains_numbers)
                        } else {
                            stringResource(R.string.error_name_too_short)
                        }
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = stringResource(R.string.auth_email),
                    value = registerUiState.email,
                    onValueChange = { vm.onChange(email = it) },
                    error = if (hasAttemptedSubmit && registerUiState.emailIsInvalid()) {
                        stringResource(R.string.error_email_invalid)
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = stringResource(R.string.auth_password),
                    value = registerUiState.password,
                    onValueChange = { vm.onChange(password = it) },
                    isPasswordField = true,
                    error = if (hasAttemptedSubmit && registerUiState.passwordIsInvalid()) {
                        stringResource(R.string.error_password_too_short)
                    } else {
                        null
                    }
                )

                if (registerUiState.password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthIndicator(
                        password = registerUiState.password,
                        strength = passwordStrength
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = stringResource(R.string.auth_confirm_password),
                    value = registerUiState.confirmPassword,
                    onValueChange = { vm.onChange(confirmPassword = it) },
                    isPasswordField = true,
                    error = if (hasAttemptedSubmit && registerUiState.passwordsDoNotMatch()) {
                        stringResource(R.string.error_passwords_mismatch)
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.auth_select_role),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                RoleSelectionRow(
                    selectedRole = registerUiState.role,
                    onRoleSelected = { vm.onChange(role = it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        hasAttemptedSubmit = true
                        if (registerUiState.isValid()) {
                            vm.signUpWithEmailAndPassword()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
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
                            text = stringResource(R.string.auth_create_account),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.auth_already_have_account),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = navigateBack) {
                        Text(
                            text = stringResource(R.string.auth_sign_in_link),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (response is Response.Loading) {
                ProgressBar()
            }
        }
    }
}

@Composable
private fun BackNavigationRow(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.semantics {
                contentDescription = "Navigate back to sign in"
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back arrow",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(R.string.auth_back_to_sign_in),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(
    password: String,
    strength: PasswordStrength
) {

    val filledBars = when (strength) {
        PasswordStrength.WEAK -> 1
        PasswordStrength.MEDIUM -> 2
        PasswordStrength.STRONG -> 3
    }

    val strengthColour = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
    }

    val strengthLabel = when (strength) {
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

    val hasMinLength = password.length >= 6
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Password strength: $strengthLabel" }
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            repeat(3) { index ->
                val isFilled = index < filledBars

                val barColour by animateColorAsState(
                    targetValue = if (isFilled) strengthColour
                    else MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(durationMillis = 300),
                    label = "strength bar colour"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColour)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = strengthLabel,
            style = MaterialTheme.typography.labelSmall,
            color = strengthColour
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CriteriaHint(text = "6+ chars", isMet = hasMinLength)
            CriteriaHint(text = "Uppercase", isMet = hasUppercase)
            CriteriaHint(text = "Number", isMet = hasDigit)
        }
    }
}

@Composable
private fun CriteriaHint(text: String, isMet: Boolean) {
    val colour by animateColorAsState(
        targetValue = if (isMet) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300),
        label = "criteria hint colour"
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = colour
    )
}

@Composable
private fun RoleSelectionRow(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoleCard(
            role = "engineer",
            title = "Engineer",
            description = "View and complete assigned jobs",
            icon = Icons.Filled.Build,
            isSelected = selectedRole == "engineer",
            onSelect = { onRoleSelected("engineer") },
            modifier = Modifier.weight(1f)
        )
        RoleCard(
            role = "dispatcher",
            title = "Dispatcher",
            description = "Create and assign jobs to engineers",
            icon = Icons.Filled.Person,
            isSelected = selectedRole == "dispatcher",
            onSelect = { onRoleSelected("dispatcher") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleCard(
    role: String,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {

    val borderColour by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 200),
        label = "role card border"
    )
    val backgroundColour by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 200),
        label = "role card background"
    )

    Card(
        modifier = modifier
            .clickable(onClick = onSelect)
            .semantics { contentDescription = "$title role card" },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColour
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColour)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "$title selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = "$title icon",
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
