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
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.raf.fieldops.ui.theme.LocalWindowWidthClass
import com.raf.fieldops.ui.theme.StatusColours
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlinx.coroutines.delay

@Composable
fun AdminDashboardScreen(
    navigateToPending: () -> Unit = {},
    navigateToUsers: () -> Unit = {},
    navigateToUserDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    vm: AdminDashboardVM = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    when (val currentState = state) {
        is DatabaseState.Loading -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
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
            AdminDashboardContent(
                state = currentState.data,
                navigateToPending = navigateToPending,
                navigateToUsers = navigateToUsers,
                navigateToUserDetail = navigateToUserDetail,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AdminDashboardContent(
    state: AdminDashboardState,
    navigateToPending: () -> Unit,
    navigateToUsers: () -> Unit,
    navigateToUserDetail: (String) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -40 }
            ) {
                GreetingCard(adminName = state.adminName)
            }
        }

        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -30 }
            ) {
                StatsRow(
                    totalUsers = state.totalUsers,
                    activeUsers = state.activeUsers,
                    pendingUsers = state.pendingUsers,
                    suspendedUsers = state.suspendedUsers,
                    onTotalClick = navigateToUsers
                )
            }
        }

        if (state.pendingUsers > 0) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    PendingActionCard(
                        pendingCount = state.pendingUsers,
                        onClick = navigateToPending
                    )
                }
            }
        }

        if (state.recentUsers.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.admin_recent_registrations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (isExpanded) {

                val chunkedUsers = state.recentUsers.chunked(2)
                chunkedUsers.forEachIndexed { chunkIndex, pair ->
                    item(key = "recent_row_$chunkIndex") {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically { (chunkIndex + 1) * 20 }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { user ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        RecentUserItem(
                                            user = user,
                                            onClick = { navigateToUserDetail(user.uid) }
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
                    items = state.recentUsers,
                    key = { _, user -> user.uid }
                ) { index, user ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically { (index + 1) * 20 }
                    ) {
                        RecentUserItem(
                            user = user,
                            onClick = { navigateToUserDetail(user.uid) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun GreetingCard(adminName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Welcome greeting card" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "${stringResource(R.string.admin_welcome)}, ${adminName.ifEmpty { "Admin" }}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.admin_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsRow(
    totalUsers: Int,
    activeUsers: Int,
    pendingUsers: Int,
    suspendedUsers: Int,
    onTotalClick: () -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.People,
                label = stringResource(R.string.admin_total_users),
                count = totalUsers,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onTotalClick
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CheckCircle,
                label = stringResource(R.string.admin_active_users),
                count = activeUsers,
                containerColor = StatusColours.completedBackground,
                contentColor = StatusColours.completedForeground
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.HourglassTop,
                label = stringResource(R.string.admin_pending_users),
                count = pendingUsers,
                containerColor = StatusColours.assignedBackground,
                contentColor = StatusColours.assignedForeground
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Block,
                label = stringResource(R.string.admin_suspended_users),
                count = suspendedUsers,
                containerColor = StatusColours.cancelledBackground,
                contentColor = StatusColours.cancelledForeground
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    count: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .semantics { contentDescription = "$label: $count" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PendingActionCard(
    pendingCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$pendingCount pending approvals, tap to review" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = StatusColours.assignedBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(StatusColours.assignedForeground.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    tint = StatusColours.assignedForeground,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$pendingCount ${stringResource(R.string.admin_pending_approvals)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusColours.assignedForeground
                )
                Text(
                    text = stringResource(R.string.admin_tap_to_review),
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusColours.assignedForeground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecentUserItem(user: User, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "User ${user.displayName}, tap for details" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    style = MaterialTheme.typography.bodyMedium,
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

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                UserRoleBadge(role = user.role)
                Spacer(modifier = Modifier.height(4.dp))
                UserStatusBadge(status = user.status)
            }
        }
    }
}

@Composable
fun UserRoleBadge(role: String) {
    val (bgColor, fgColor) = when (role.lowercase()) {
        "engineer" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "dispatcher" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = role.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = fgColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun UserStatusBadge(status: String) {
    val (bgColor, fgColor) = when (status.lowercase()) {
        "active" -> StatusColours.completedBackground to StatusColours.completedForeground
        "pending" -> StatusColours.assignedBackground to StatusColours.assignedForeground
        "suspended" -> StatusColours.cancelledBackground to StatusColours.cancelledForeground
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = fgColor,
            fontWeight = FontWeight.Medium
        )
    }
}
