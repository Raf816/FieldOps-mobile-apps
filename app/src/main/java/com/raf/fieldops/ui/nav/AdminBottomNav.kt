package com.raf.fieldops.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.raf.fieldops.R

private data class AdminBottomNavItemDef(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val description: String
)

private val adminNavItemDefs = listOf(
    AdminBottomNavItemDef(
        route = Routes.ADMIN_DASHBOARD,
        labelResId = R.string.nav_admin_dashboard,
        icon = Icons.Outlined.Dashboard,
        description = "Navigate to admin dashboard"
    ),
    AdminBottomNavItemDef(
        route = Routes.ADMIN_PENDING,
        labelResId = R.string.nav_admin_pending,
        icon = Icons.Outlined.HourglassTop,
        description = "Navigate to pending approvals"
    ),
    AdminBottomNavItemDef(
        route = Routes.ADMIN_USERS,
        labelResId = R.string.nav_admin_users,
        icon = Icons.Outlined.People,
        description = "Navigate to all users"
    )
)

@Composable
fun AdminBottomNav(
    navController: NavHostController,
    currentRoute: String,
    pendingCount: Int = 0
) {
    Column {

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        NavigationBar(
            modifier = Modifier.semantics {
                contentDescription = "Admin bottom navigation bar"
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            adminNavItemDefs.forEach { item ->
                val isSelected = currentRoute == item.route
                val label = stringResource(item.labelResId)
                val showBadge = item.route == Routes.ADMIN_PENDING && pendingCount > 0

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {

                        if (currentRoute == item.route) return@NavigationBarItem

                        navController.navigate(item.route) {

                            popUpTo(Routes.ADMIN_DASHBOARD) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    },
                    icon = {
                        if (showBadge) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = pendingCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.description
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.description
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
