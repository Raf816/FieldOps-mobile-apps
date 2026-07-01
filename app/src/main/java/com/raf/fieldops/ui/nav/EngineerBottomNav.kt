package com.raf.fieldops.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import com.raf.fieldops.ui.components.AdaptiveContainer

private data class BottomNavItemDef(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val description: String
)

private val engineerNavItemDefs = listOf(
    BottomNavItemDef(
        route = Routes.ENGINEER_HOME,
        labelResId = R.string.nav_my_jobs,
        icon = Icons.Outlined.CalendarToday,
        description = "Navigate to my active jobs"
    ),
    BottomNavItemDef(
        route = Routes.ENGINEER_UPCOMING,
        labelResId = R.string.nav_upcoming,
        icon = Icons.Outlined.DateRange,
        description = "Navigate to upcoming jobs"
    ),
    BottomNavItemDef(
        route = Routes.ENGINEER_HISTORY,
        labelResId = R.string.nav_history,
        icon = Icons.Outlined.History,
        description = "Navigate to completed jobs history"
    ),
    BottomNavItemDef(
        route = Routes.PROFILE,
        labelResId = R.string.nav_profile,
        icon = Icons.Outlined.Person,
        description = "Navigate to profile"
    )
)

@Composable
fun EngineerBottomNav(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                NavigationBar(
                    modifier = Modifier.semantics {
                        contentDescription = "Engineer bottom navigation bar"
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                engineerNavItemDefs.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val label = stringResource(item.labelResId)

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {

                            if (currentRoute == item.route) return@NavigationBarItem

                            navController.navigate(item.route) {

                                popUpTo(Routes.ENGINEER_HOME) {
                                    saveState = true
                                }

                                launchSingleTop = true

                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.description
                            )
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
    ) { innerPadding ->

        AdaptiveContainer(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }
}
