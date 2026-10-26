package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ClipNexViewModel

sealed class BottomNavItem(val route: String, val titleBengali: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem(Screen.Dashboard.route, "ড্যাশবোর্ড", Icons.Default.Dashboard)
    object Create : BottomNavItem(Screen.CreateProject.route, "নতুন ক্লিপ", Icons.Default.VideoCall)
    object History : BottomNavItem(Screen.History.route, "হিস্ট্রি", Icons.Default.History)
    object Settings : BottomNavItem(Screen.Settings.route, "সেটিংস", Icons.Default.Settings)
}

@Composable
fun ClipNexNavGraph(
    viewModel: ClipNexViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val inputUrl by viewModel.inputUrl.collectAsStateWithLifecycle()
    val selectedDuration by viewModel.selectedDuration.collectAsStateWithLifecycle()
    val selectedClipCount by viewModel.selectedClipCount.collectAsStateWithLifecycle()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsStateWithLifecycle()
    val selectedCaptionLanguage by viewModel.selectedCaptionLanguage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Create,
        BottomNavItem.History,
        BottomNavItem.Settings
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.CreateProject.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NavySurface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleBengali,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.titleBengali,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = BrandPurpleLight,
                                indicatorColor = BrandPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (hasSeenOnboarding) Screen.Dashboard.route else Screen.Landing.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Landing.route) {
                LandingScreen(
                    onGetStarted = {
                        viewModel.setOnboardingSeen(true)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    projects = projects,
                    isApiKeyConfigured = isApiKeyConfigured,
                    isDemoMode = isDemoMode,
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateProject.route)
                    },
                    onNavigateToProject = { projectId ->
                        viewModel.selectProject(projectId)
                        val project = projects.find { it.id == projectId }
                        if (project?.status == com.example.data.model.ProjectStatus.COMPLETED) {
                            navController.navigate(Screen.ResultEditor.createRoute(projectId))
                        } else {
                            navController.navigate(Screen.ProcessingStatus.createRoute(projectId))
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onUsePresetUrl = { presetUrl ->
                        viewModel.fillSampleUrl(presetUrl)
                    }
                )
            }

            composable(Screen.CreateProject.route) {
                CreateProjectScreen(
                    inputUrl = inputUrl,
                    selectedDuration = selectedDuration,
                    selectedClipCount = selectedClipCount,
                    selectedAspectRatio = selectedAspectRatio,
                    selectedCaptionLanguage = selectedCaptionLanguage,
                    isApiKeyConfigured = isApiKeyConfigured,
                    isDemoMode = isDemoMode,
                    errorMessage = errorMessage,
                    onUrlChange = { viewModel.setInputUrl(it) },
                    onDurationChange = { viewModel.setDuration(it) },
                    onClipCountChange = { viewModel.setClipCount(it) },
                    onAspectRatioChange = { viewModel.setAspectRatio(it) },
                    onCaptionLanguageChange = { viewModel.setCaptionLanguage(it) },
                    onSubmit = {
                        viewModel.startClipping(
                            onSuccess = { projectId ->
                                navController.navigate(Screen.ProcessingStatus.createRoute(projectId))
                            }
                        )
                    }
                )
            }

            composable(
                route = Screen.ProcessingStatus.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                LaunchedEffect(projectId) {
                    viewModel.selectProject(projectId)
                }

                ProcessingStatusScreen(
                    project = activeProject,
                    onViewResults = {
                        navController.navigate(Screen.ResultEditor.createRoute(projectId)) {
                            popUpTo(Screen.ProcessingStatus.route) { inclusive = true }
                        }
                    },
                    onRetry = {
                        viewModel.retryProject(projectId)
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.ResultEditor.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                LaunchedEffect(projectId) {
                    viewModel.selectProject(projectId)
                }

                ResultEditorScreen(
                    project = activeProject,
                    onSaveClip = { clipId, title, transcript, postCaption ->
                        viewModel.updateClipContent(clipId, title, transcript, postCaption)
                    },
                    onBack = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    projects = projects,
                    onSelectProject = { projectId ->
                        viewModel.selectProject(projectId)
                        val project = projects.find { it.id == projectId }
                        if (project?.status == com.example.data.model.ProjectStatus.COMPLETED) {
                            navController.navigate(Screen.ResultEditor.createRoute(projectId))
                        } else {
                            navController.navigate(Screen.ProcessingStatus.createRoute(projectId))
                        }
                    },
                    onDeleteProject = { projectId ->
                        viewModel.deleteProject(projectId)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    isApiKeyConfigured = isApiKeyConfigured,
                    isDemoMode = isDemoMode,
                    customApiKey = customApiKey,
                    onSaveApiKey = { viewModel.saveCustomApiKey(it) },
                    onToggleDemoMode = { viewModel.setDemoMode(it) }
                )
            }
        }
    }
}
