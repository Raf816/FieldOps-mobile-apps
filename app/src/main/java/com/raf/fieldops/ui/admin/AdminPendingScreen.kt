package com.raf.fieldops.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.raf.fieldops.ui.components.EmptyStateCard
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminPendingScreen(
    navigateToUserDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    vm: AdminPendingVM = hiltViewModel()
) {
    val state by vm.pendingUsers.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showApproveAllDialog by remember { mutableStateOf(false) }
    var showRejectAllDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    when (val currentState = state) {
        is DatabaseState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is DatabaseState.Failure -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is DatabaseState.Success -> {
            val allPending = currentState.data

            val filteredUsers = if (searchQuery.isBlank()) {
                allPending
            } else {
                allPending.filter { user ->
                    user.displayName.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true)
                }
            }

            AdminPendingContent(
                users = filteredUsers,
                allPendingCount = allPending.size,
                searchQuery = searchQuery,
                onSearchChanged = vm::onSearchQueryChanged,
                onApprove = { user ->
                    selectedUser = user
                    showApproveDialog = true
                },
                onReject = { user ->
                    selectedUser = user
                    showRejectDialog = true
                },
                onApproveAll = { showApproveAllDialog = true },
                onRejectAll = { showRejectAllDialog = true },
                onUserClick = { user -> navigateToUserDetail(user.uid) },
                modifier = modifier
            )

            if (showApproveDialog && selectedUser != null) {
                ConfirmDialog(
                    title = stringResource(R.string.admin_approve),
                    message = stringResource(R.string.admin_confirm_approve),
                    confirmText = stringResource(R.string.admin_approve),
                    onConfirm = {
                        vm.approveUser(selectedUser!!.uid)
                        showApproveDialog = false
                        selectedUser = null
                    },
                    onDismiss = {
                        showApproveDialog = false
                        selectedUser = null
                    }
                )
            }

            if (showRejectDialog && selectedUser != null) {
                ConfirmDialog(
                    title = stringResource(R.string.admin_reject),
                    message = stringResource(R.string.admin_confirm_reject),
                    confirmText = stringResource(R.string.admin_reject),
                    onConfirm = {
                        vm.rejectUser(selectedUser!!.uid)
                        showRejectDialog = false
                        selectedUser = null
                    },
                    onDismiss = {
                        showRejectDialog = false
                        selectedUser = null
                    }
                )
            }

            if (showApproveAllDialog) {
                ConfirmDialog(
                    title = stringResource(R.string.admin_approve_all),
                    message = stringResource(R.string.admin_confirm_approve_all),
                    confirmText = stringResource(R.string.admin_approve_all),
                    onConfirm = {
                        vm.approveAll(allPending)
                        showApproveAllDialog = false
                    },
                    onDismiss = { showApproveAllDialog = false }
                )
            }

            if (showRejectAllDialog) {
                ConfirmDialog(
                    title = stringResource(R.string.admin_reject_all),
                    message = stringResource(R.string.admin_confirm_reject_all),
                    confirmText = stringResource(R.string.admin_reject_all),
                    onConfirm = {
                        vm.rejectAll(allPending)
                        showRejectAllDialog = false
                    },
                    onDismiss = { showRejectAllDialog = false }
                )
            }
        }
    }
}

@Composable
private fun AdminPendingContent(
    users: List<User>,
    allPendingCount: Int,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onApprove: (User) -> Unit,
    onReject: (User) -> Unit,
    onApproveAll: () -> Unit,
    onRejectAll: () -> Unit,
    onUserClick: (User) -> Unit,
    modifier: Modifier
) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val isExpanded = LocalWindowWidthClass.current == WindowWidthSizeClass.Expanded

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.admin_search_users),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Search pending users" }
            )
        }

        if (allPendingCount > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onApproveAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(R.string.admin_approve_all))
                    }
                    OutlinedButton(
                        onClick = onRejectAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = stringResource(R.string.admin_reject_all))
                    }
                }
            }
        }

        if (users.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.TaskAlt,
                    title = stringResource(R.string.admin_no_pending),
                    subtitle = stringResource(R.string.admin_no_pending_subtitle)
                )
            }
        }

        if (isExpanded) {

            val chunkedUsers = users.chunked(2)
            chunkedUsers.forEachIndexed { chunkIndex, pair ->
                item(key = "pending_row_$chunkIndex") {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically { (chunkIndex + 1) * 30 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { user ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PendingUserCard(
                                        user = user,
                                        onApprove = { onApprove(user) },
                                        onReject = { onReject(user) },
                                        onClick = { onUserClick(user) }
                                    )
                                }
                            }

                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {

            itemsIndexed(
                items = users,
                key = { _, user -> user.uid }
            ) { index, user ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { (index + 1) * 30 }
                ) {
                    PendingUserCard(
                        user = user,
                        onApprove = { onApprove(user) },
                        onReject = { onReject(user) },
                        onClick = { onUserClick(user) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PendingUserCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.UK) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Pending user ${user.displayName}" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName.ifEmpty { "Unknown" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                UserRoleBadge(role = user.role)
            }

            Spacer(modifier = Modifier.height(8.dp))

            user.createdAt?.let { date ->
                Text(
                    text = stringResource(R.string.admin_registered, dateFormat.format(date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.admin_approve))
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.admin_reject))
                }
            }
        }
    }
}
