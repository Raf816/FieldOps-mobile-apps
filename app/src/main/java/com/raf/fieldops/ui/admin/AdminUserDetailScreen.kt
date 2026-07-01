package com.raf.fieldops.ui.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raf.fieldops.R
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.User
import com.raf.fieldops.ui.components.ConfirmDialog
import com.raf.fieldops.ui.theme.StatusColours
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    navigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    vm: AdminUserDetailVM = hiltViewModel()
) {
    val userState by vm.user.collectAsStateWithLifecycle()
    val activeJobCount by vm.activeJobCount.collectAsStateWithLifecycle()
    val actionResult by vm.actionResult.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showRoleChangeDialog by remember { mutableStateOf(false) }
    var showSuspendDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionResult) {
        when (val result = actionResult) {
            is ActionResult.Success -> {
                snackbarHostState.showSnackbar(result.message)
                vm.clearActionResult()
            }
            is ActionResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                vm.clearActionResult()
            }
            ActionResult.Idle -> {  }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.admin_user_detail),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val state = userState) {
            is DatabaseState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is DatabaseState.Failure -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is DatabaseState.Success -> {
                val user = state.data
                if (user == null) {

                    LaunchedEffect(Unit) { navigateBack() }
                } else {
                    UserDetailContent(
                        user = user,
                        activeJobCount = activeJobCount,
                        onApprove = { vm.approveUser() },
                        onReject = { showRejectDialog = true },
                        onChangeRole = { showRoleChangeDialog = true },
                        onSuspendToggle = { showSuspendDialog = true },
                        onDelete = { showDeleteDialog = true },
                        modifier = Modifier.padding(innerPadding)
                    )

                    if (showRoleChangeDialog) {
                        val newRole = if (user.role == "engineer") "dispatcher" else "engineer"
                        ConfirmDialog(
                            title = stringResource(R.string.admin_change_role),
                            message = stringResource(R.string.admin_promote_warning),
                            confirmText = stringResource(R.string.admin_change_role),
                            onConfirm = {
                                vm.changeRole(newRole)
                                showRoleChangeDialog = false
                            },
                            onDismiss = { showRoleChangeDialog = false }
                        )
                    }

                    if (showSuspendDialog) {
                        val isSuspended = user.status == "suspended"
                        ConfirmDialog(
                            title = if (isSuspended)
                                stringResource(R.string.admin_unsuspend)
                            else
                                stringResource(R.string.admin_suspend),
                            message = if (isSuspended)
                                stringResource(R.string.admin_unsuspend_warning)
                            else
                                stringResource(R.string.admin_suspend_warning),
                            confirmText = if (isSuspended)
                                stringResource(R.string.admin_unsuspend)
                            else
                                stringResource(R.string.admin_suspend),
                            onConfirm = {
                                if (isSuspended) vm.unsuspendUser() else vm.suspendUser()
                                showSuspendDialog = false
                            },
                            onDismiss = { showSuspendDialog = false }
                        )
                    }

                    if (showDeleteDialog) {
                        ConfirmDialog(
                            title = stringResource(R.string.admin_delete_account),
                            message = stringResource(R.string.admin_delete_warning) +
                                if (activeJobCount > 0)
                                    "\n\n" + stringResource(R.string.admin_active_jobs_warning, activeJobCount)
                                else "",
                            confirmText = stringResource(R.string.admin_delete_account),
                            onConfirm = {
                                vm.deleteUser(onDeleted = navigateBack)
                                showDeleteDialog = false
                            },
                            onDismiss = { showDeleteDialog = false }
                        )
                    }

                    if (showRejectDialog) {
                        ConfirmDialog(
                            title = stringResource(R.string.admin_reject),
                            message = stringResource(R.string.admin_confirm_reject),
                            confirmText = stringResource(R.string.admin_reject),
                            onConfirm = {
                                vm.rejectUser(onRejected = navigateBack)
                                showRejectDialog = false
                            },
                            onDismiss = { showRejectDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserDetailContent(
    user: User,
    activeJobCount: Int,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onChangeRole: () -> Unit,
    onSuspendToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.UK) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "User information card" },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.displayName.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRoleBadge(role = user.role)
                    UserStatusBadge(status = user.status)
                }

                user.createdAt?.let { date ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.admin_registered, dateFormat.format(date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Active jobs: $activeJobCount" },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (activeJobCount > 0)
                    StatusColours.assignedBackground
                else
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Work,
                    contentDescription = null,
                    tint = if (activeJobCount > 0)
                        StatusColours.assignedForeground
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.admin_active_jobs_count, activeJobCount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (activeJobCount > 0)
                            StatusColours.assignedForeground
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (activeJobCount > 0) {
                        Text(
                            text = stringResource(R.string.admin_unassigned_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusColours.assignedForeground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.admin_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (user.status == "pending") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Approve user" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.admin_approve))
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Reject user" },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.admin_reject))
                }
            }
        }

        OutlinedButton(
            onClick = onChangeRole,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Change user role" },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val newRole = if (user.role == "engineer") "Dispatcher" else "Engineer"
            Text(text = "${stringResource(R.string.admin_change_role)} → $newRole")
        }

        val isSuspended = user.status == "suspended"
        OutlinedButton(
            onClick = onSuspendToggle,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = if (isSuspended) "Unsuspend user" else "Suspend user"
                },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isSuspended)
                    StatusColours.completedForeground
                else
                    StatusColours.cancelledForeground
            )
        ) {
            Icon(
                imageVector = if (isSuspended) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSuspended)
                    stringResource(R.string.admin_unsuspend)
                else
                    stringResource(R.string.admin_suspend)
            )
        }

        Button(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Delete user account" },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.admin_delete_account))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
