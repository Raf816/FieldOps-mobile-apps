package com.raf.fieldops.ui.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.ui.admin.AdminDashboardScreen
import com.raf.fieldops.ui.admin.AdminPendingScreen
import com.raf.fieldops.ui.admin.AdminUsersScreen
import com.raf.fieldops.ui.admin.AdminUserDetailScreen
import com.raf.fieldops.ui.admin.AdminDashboardVM
import com.raf.fieldops.ui.auth.AccountRemovedScreen
import com.raf.fieldops.ui.auth.AccountSuspendedScreen
import com.raf.fieldops.ui.auth.AwaitingApprovalScreen
import com.raf.fieldops.ui.auth.LoginScreen
import com.raf.fieldops.ui.auth.RegisterScreen
import com.raf.fieldops.ui.auth.EmailConfirmationScreen
import com.raf.fieldops.ui.dispatcher.createjob.CreateJobScreen
import com.raf.fieldops.ui.dispatcher.alljobs.AllJobsScreen
import com.raf.fieldops.ui.dispatcher.dashboard.DispatcherDashboardScreen
import com.raf.fieldops.ui.dispatcher.engineers.EngineersListScreen
import com.raf.fieldops.ui.dispatcher.jobdetail.DispatcherJobDetailScreen
import com.raf.fieldops.ui.engineer.home.EngineerHomeScreen
import com.raf.fieldops.ui.engineer.history.HistoryScreen
import com.raf.fieldops.ui.engineer.jobdetail.EngineerJobDetailScreen
import com.raf.fieldops.ui.engineer.upcoming.UpcomingScreen
import com.raf.fieldops.ui.profile.ProfileScreen
import com.raf.fieldops.data.model.DatabaseState
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    vm: NavVM = hiltViewModel()
) {

    var userRole by remember { mutableStateOf<String?>(null) }

    var selectedJob by remember { mutableStateOf<Job?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val animationDuration = 300

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,

        enterTransition = {
            fadeIn(animationSpec = tween(animationDuration)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animationDuration)
                )
        },

        exitTransition = {
            fadeOut(animationSpec = tween(animationDuration)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animationDuration)
                )
        },

        popEnterTransition = {
            fadeIn(animationSpec = tween(animationDuration)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(animationDuration)
                )
        },

        popExitTransition = {
            fadeOut(animationSpec = tween(animationDuration)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(animationDuration)
                )
        }
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                navigateToHomeScreen = {

                    coroutineScope.launch {
                        val uid = vm.currentUserUid ?: return@launch

                        vm.markEmailVerified(uid)

                        val user = vm.getUser(uid)

                        if (user == null) {
                            navController.navigate(Routes.ACCOUNT_REMOVED) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                            return@launch
                        }

                        val role = user.role.ifEmpty { "engineer" }
                        userRole = role

                        val status = user.status.ifEmpty { "active" }

                        val destination = when (status) {
                            "pending" -> Routes.AWAITING_APPROVAL
                            "suspended" -> Routes.ACCOUNT_SUSPENDED
                            "active" -> when (role) {
                                "dispatcher" -> Routes.DISPATCHER_DASHBOARD
                                "admin" -> Routes.ADMIN_DASHBOARD
                                else -> Routes.ENGINEER_HOME
                            }
                            else -> Routes.AWAITING_APPROVAL
                        }

                        navController.navigate(destination) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                navigateToSignUpScreen = {
                    navController.navigate(Routes.REGISTER)
                },
                navigateToEmailConfirmation = { email ->
                    navController.navigate("${Routes.EMAIL_CONFIRMATION}/$email")
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToConfirmation = { email ->
                    navController.navigate("${Routes.EMAIL_CONFIRMATION}/$email") {

                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable("${Routes.EMAIL_CONFIRMATION}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailConfirmationScreen(
                email = email,
                authRepo = vm.authRepo,
                navigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AWAITING_APPROVAL) {
            AwaitingApprovalScreen(
                navigateToHome = { role ->
                    userRole = role
                    val destination = when (role) {
                        "dispatcher" -> Routes.DISPATCHER_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.ENGINEER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.AWAITING_APPROVAL) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToSuspended = {
                    navController.navigate(Routes.ACCOUNT_SUSPENDED) {
                        popUpTo(Routes.AWAITING_APPROVAL) { inclusive = true }
                    }
                },
                navigateToRemoved = {
                    navController.navigate(Routes.ACCOUNT_REMOVED) {
                        popUpTo(Routes.AWAITING_APPROVAL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ACCOUNT_SUSPENDED) {
            AccountSuspendedScreen(
                navigateToHome = { role ->
                    userRole = role
                    val destination = when (role) {
                        "dispatcher" -> Routes.DISPATCHER_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.ENGINEER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.ACCOUNT_SUSPENDED) { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToRemoved = {
                    navController.navigate(Routes.ACCOUNT_REMOVED) {
                        popUpTo(Routes.ACCOUNT_SUSPENDED) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ACCOUNT_REMOVED) {
            AccountRemovedScreen(
                navigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSignOut = { vm.signOut() }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {

            val adminDashboardVM: AdminDashboardVM = hiltViewModel()
            val dashboardState by adminDashboardVM.state.collectAsStateWithLifecycle()
            val pendingCount = when (val s = dashboardState) {
                is DatabaseState.Success -> s.data.pendingUsers
                else -> 0
            }

            AdminDrawer(
                navController = navController,
                currentRoute = Routes.ADMIN_DASHBOARD,
                pendingCount = pendingCount,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                AdminDashboardScreen(
                    navigateToPending = {
                        navController.navigate(Routes.ADMIN_PENDING) {
                            popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateToUsers = {
                        navController.navigate(Routes.ADMIN_USERS) {
                            popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateToUserDetail = { uid ->
                        navController.navigate("${Routes.ADMIN_USER_DETAIL}/$uid")
                    }
                )
            }
        }

        composable(Routes.ADMIN_PENDING) {
            val adminDashboardVM: AdminDashboardVM = hiltViewModel()
            val dashboardState by adminDashboardVM.state.collectAsStateWithLifecycle()
            val pendingCount = when (val s = dashboardState) {
                is DatabaseState.Success -> s.data.pendingUsers
                else -> 0
            }

            AdminDrawer(
                navController = navController,
                currentRoute = Routes.ADMIN_PENDING,
                pendingCount = pendingCount,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                AdminPendingScreen(
                    navigateToUserDetail = { uid ->
                        navController.navigate("${Routes.ADMIN_USER_DETAIL}/$uid")
                    }
                )
            }
        }

        composable(Routes.ADMIN_USERS) {
            val adminDashboardVM: AdminDashboardVM = hiltViewModel()
            val dashboardState by adminDashboardVM.state.collectAsStateWithLifecycle()
            val pendingCount = when (val s = dashboardState) {
                is DatabaseState.Success -> s.data.pendingUsers
                else -> 0
            }

            AdminDrawer(
                navController = navController,
                currentRoute = Routes.ADMIN_USERS,
                pendingCount = pendingCount,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                AdminUsersScreen(
                    navigateToUserDetail = { uid ->
                        navController.navigate("${Routes.ADMIN_USER_DETAIL}/$uid")
                    }
                )
            }
        }

        composable("${Routes.ADMIN_USER_DETAIL}/{uid}") { _ ->

            AdminUserDetailScreen(
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ENGINEER_HOME) {
            EngineerBottomNav(
                navController = navController,
                currentRoute = Routes.ENGINEER_HOME
            ) {
                EngineerHomeScreen(
                    navigateToJobDetail = { job ->
                        selectedJob = job
                        navController.navigate(Routes.ENGINEER_JOB_DETAIL)
                    }
                )
            }
        }

        composable(Routes.ENGINEER_UPCOMING) {
            EngineerBottomNav(
                navController = navController,
                currentRoute = Routes.ENGINEER_UPCOMING
            ) {
                UpcomingScreen(
                    navigateToJobDetail = { job ->
                        selectedJob = job
                        navController.navigate(Routes.ENGINEER_JOB_DETAIL)
                    }
                )
            }
        }

        composable(Routes.ENGINEER_HISTORY) {
            EngineerBottomNav(
                navController = navController,
                currentRoute = Routes.ENGINEER_HISTORY
            ) {
                HistoryScreen(
                    navigateToJobDetail = { job ->
                        selectedJob = job
                        navController.navigate(Routes.ENGINEER_JOB_DETAIL)
                    }
                )
            }
        }

        composable(Routes.ENGINEER_JOB_DETAIL) {

            EngineerJobDetailScreen(
                job = selectedJob,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DISPATCHER_DASHBOARD) {
            DispatcherDrawer(
                navController = navController,
                currentRoute = Routes.DISPATCHER_DASHBOARD,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                DispatcherDashboardScreen(
                    navigateToCreateJob = {
                        navController.navigate(Routes.DISPATCHER_CREATE_JOB)
                    },
                    navigateToJobDetail = { job ->
                        selectedJob = job
                        navController.navigate(Routes.DISPATCHER_JOB_DETAIL)
                    }
                )
            }
        }

        composable(Routes.DISPATCHER_ALL_JOBS) {
            DispatcherDrawer(
                navController = navController,
                currentRoute = Routes.DISPATCHER_ALL_JOBS,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                AllJobsScreen(
                    navigateToJobDetail = { job ->
                        selectedJob = job
                        navController.navigate(Routes.DISPATCHER_JOB_DETAIL)
                    }
                )
            }
        }

        composable(Routes.DISPATCHER_ENGINEERS) {
            DispatcherDrawer(
                navController = navController,
                currentRoute = Routes.DISPATCHER_ENGINEERS,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                EngineersListScreen()
            }
        }

        composable(Routes.DISPATCHER_CREATE_JOB) {
            DispatcherDrawer(
                navController = navController,
                currentRoute = Routes.DISPATCHER_CREATE_JOB,
                onSignOut = {
                    vm.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                CreateJobScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.DISPATCHER_JOB_DETAIL) {

            DispatcherJobDetailScreen(
                job = selectedJob,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {

            when (userRole) {
                "dispatcher" -> DispatcherDrawer(
                    navController = navController,
                    currentRoute = Routes.PROFILE,
                    onSignOut = {
                        vm.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                "admin" -> AdminDrawer(
                    navController = navController,
                    currentRoute = Routes.PROFILE,
                    onSignOut = {
                        vm.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                else -> EngineerBottomNav(
                    navController = navController,
                    currentRoute = Routes.PROFILE
                ) {
                    ProfileScreen(
                        onSignedOut = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
