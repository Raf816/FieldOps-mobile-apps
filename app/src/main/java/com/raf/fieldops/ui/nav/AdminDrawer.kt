package com.raf.fieldops.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.raf.fieldops.R
import com.raf.fieldops.ui.components.AdaptiveContainer
import com.raf.fieldops.ui.components.ConfirmDialog
import com.raf.fieldops.ui.theme.BtIndigo
import com.raf.fieldops.ui.theme.BtIndigoDark
import com.raf.fieldops.ui.theme.BtMagenta
import kotlinx.coroutines.launch

private data class AdminDrawerNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val description: String
)

private val adminNavItems = listOf(
    AdminDrawerNavItem(
        route = Routes.ADMIN_DASHBOARD,
        labelResId = R.string.nav_admin_dashboard,
        icon = Icons.Outlined.Dashboard,
        description = "Navigate to admin dashboard"
    ),
    AdminDrawerNavItem(
        route = Routes.ADMIN_PENDING,
        labelResId = R.string.nav_admin_pending,
        icon = Icons.Outlined.HourglassTop,
        description = "Navigate to pending approvals"
    ),
    AdminDrawerNavItem(
        route = Routes.ADMIN_USERS,
        labelResId = R.string.nav_admin_users,
        icon = Icons.Outlined.People,
        description = "Navigate to all users"
    ),
    AdminDrawerNavItem(
        route = Routes.PROFILE,
        labelResId = R.string.nav_profile,
        icon = Icons.Outlined.Person,
        description = "Navigate to profile"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDrawer(
    navController: NavHostController,
    currentRoute: String,
    pendingCount: Int = 0,
    onSignOut: () -> Unit = {},
    content: @Composable () -> Unit
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val drawerVM: DrawerVM = hiltViewModel()
    val displayName by drawerVM.displayName.collectAsStateWithLifecycle()
    val email by drawerVM.email.collectAsStateWithLifecycle()

    var showSignOutDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {

                AdminDrawerHeader(
                    displayName = displayName,
                    email = email
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.nav_navigation),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {

                        adminNavItems.filter { it.route != Routes.PROFILE }.forEachIndexed { index, item ->
                            val isSelected = currentRoute == item.route
                            val label = stringResource(item.labelResId)

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    if (currentRoute == item.route) return@NavigationDrawerItem
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.description
                                    )
                                },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .semantics {
                                        contentDescription = "$label drawer item"
                                    },
                                shape = MaterialTheme.shapes.small,
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            if (index < adminNavItems.filter { it.route != Routes.PROFILE }.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val profileItem = adminNavItems.first { it.route == Routes.PROFILE }
                val isProfileSelected = currentRoute == Routes.PROFILE
                val profileLabel = stringResource(profileItem.labelResId)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = profileLabel,
                                fontWeight = if (isProfileSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selected = isProfileSelected,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            if (currentRoute == profileItem.route) return@NavigationDrawerItem
                            navController.navigate(profileItem.route) {
                                popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = profileItem.icon,
                                contentDescription = profileItem.description
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = "$profileLabel drawer item"
                            },
                        shape = MaterialTheme.shapes.small,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.nav_sign_out),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        showSignOutDialog = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics { contentDescription = "Sign Out drawer item" },
                    shape = MaterialTheme.shapes.small,
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.error,
                        unselectedTextColor = MaterialTheme.colorScheme.error
                    )
                )

                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    ) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Open navigation drawer"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Menu,
                                    contentDescription = "Menu icon"
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
            bottomBar = {

                AdminBottomNav(
                    navController = navController,
                    currentRoute = currentRoute,
                    pendingCount = pendingCount
                )
            }
        ) { innerPadding ->
            AdaptiveContainer(
                modifier = Modifier.padding(innerPadding)
            ) {
                content()
            }
        }
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = stringResource(R.string.profile_sign_out_confirm_title),
            message = stringResource(R.string.profile_sign_out_confirm_message),
            confirmText = stringResource(R.string.nav_sign_out),
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }
}

@Composable
private fun AdminDrawerHeader(
    displayName: String,
    email: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BtIndigo, BtIndigoDark)
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = displayName.ifEmpty { "Admin" },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(BtMagenta.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.admin_role_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
