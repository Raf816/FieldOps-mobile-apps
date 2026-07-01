package com.raf.fieldops.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raf.fieldops.R
import com.raf.fieldops.data.model.Response
import com.raf.fieldops.ui.components.CustomTextField
import com.raf.fieldops.ui.components.FieldOpsSnackbarHost
import com.raf.fieldops.ui.components.ProgressBar
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtMagenta

@Composable
fun LoginScreen(
    navigateToHomeScreen: () -> Unit,
    navigateToSignUpScreen: () -> Unit,
    navigateToEmailConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: LoginVM = hiltViewModel()
) {

    val loginUiState = vm.loginUiState
    val response = vm.signInResponse
    val snackbarHostState = remember { SnackbarHostState() }
    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (response is Response.Success) {
        LaunchedEffect(response) {
            if (vm.isEmailVerified) {
                navigateToHomeScreen()
            } else {

                navigateToEmailConfirmation(loginUiState.email)
            }
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
                            BtIndigo.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
            ) {

                Spacer(modifier = Modifier.height(80.dp))

                BrandSection()

                Spacer(modifier = Modifier.height(48.dp))

                FormSection(
                    loginUiState = loginUiState,
                    hasAttemptedSubmit = hasAttemptedSubmit,
                    onEmailChange = { vm.onChange(email = it) },
                    onPasswordChange = { vm.onChange(password = it) },
                    onSignIn = {
                        hasAttemptedSubmit = true
                        if (loginUiState.isValid()) {
                            vm.signInWithEmailAndPassword()
                        }
                    },
                    onForgotPassword = { vm.forgotPassword() },
                    onCreateAccount = navigateToSignUpScreen
                )
            }

            if (response is Response.Loading) {
                ProgressBar()
            }
        }
    }
}

@Composable
private fun BrandSection() {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bt_logo),
                contentDescription = "BT Logo",
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    val gradientBrush = Brush.linearGradient(
        colors = listOf(BtIndigo, BtMagenta)
    )

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append("Field Engineer.\n")
            }
            withStyle(SpanStyle(brush = gradientBrush)) {
                append("Dispatch.")
            }
        },
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 40.sp,
            letterSpacing = (-1.2).sp
        ),
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun FormSection(
    loginUiState: LoginUiState,
    hasAttemptedSubmit: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        CustomTextField(
            label = stringResource(R.string.auth_email),
            value = loginUiState.email,
            onValueChange = onEmailChange,
            error = if (hasAttemptedSubmit && loginUiState.emailIsInvalid()) {
                stringResource(R.string.error_email_invalid)
            } else null
        )

        Spacer(modifier = Modifier.height(14.dp))

        CustomTextField(
            label = stringResource(R.string.auth_password),
            value = loginUiState.password,
            onValueChange = onPasswordChange,
            isPasswordField = true,
            error = if (hasAttemptedSubmit && loginUiState.passwordIsInvalid()) {
                stringResource(R.string.error_password_too_short)
            } else null
        )

        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = stringResource(R.string.auth_forgot_password),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
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
                    text = stringResource(R.string.auth_sign_in),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.auth_no_account),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.material3.OutlinedButton(
            onClick = onCreateAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.primary
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.auth_create_account),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
