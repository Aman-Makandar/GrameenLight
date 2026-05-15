package com.grameenlight.presentation.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Map
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.*
import androidx.navigation.compose.*
import com.grameenlight.presentation.assistant.AssistantScreen
import com.grameenlight.presentation.auth.AuthViewModel
import com.grameenlight.presentation.auth.LoginScreen
import com.grameenlight.presentation.energy.EnergyDashboardScreen
import com.grameenlight.presentation.map.PoleMapScreen
import com.grameenlight.presentation.report.QuickReportScreen
import com.grameenlight.presentation.tracker.ComplaintDetailScreen
import com.grameenlight.presentation.tracker.ComplaintTrackerScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Login : Screen("auth/login", "Login", null)
    object Map : Screen("main/map", "Map", Icons.Default.Map)
    object Report : Screen("main/report/{poleId}", "Report", null) {
        fun createRoute(poleId: String) = "main/report/$poleId"
    }
    object Tracker : Screen("main/tracker", "Tracker", Icons.AutoMirrored.Filled.List)
    object TrackerDetail : Screen("main/tracker/{complaintId}", "Detail", null) {
        fun createRoute(complaintId: String) = "main/tracker/$complaintId"
    }
    object Energy : Screen("main/energy", "Energy", Icons.Default.Analytics)
    object Assistant : Screen("main/assistant", "Assistant", Icons.Default.Assistant)
}

val bottomNavItems = listOf(
    Screen.Map,
    Screen.Tracker,
    Screen.Energy,
    Screen.Assistant
)

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun MainAppNavigation(
    onThemeToggle: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // FIX: Observe navigateToLogin state
    // When logout() is called this becomes true
    // and we navigate to login clearing the back stack
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    // FIX: React to navigateToLogin state change
    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate(Screen.Login.route) {
                // Clear entire back stack so user cannot
                // press back to return to map
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            // Tell ViewModel navigation was handled
            viewModel.onNavigatedToLogin()
        }
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Screen.Map.route
            else Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToMain = {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Map.route) {
                MainScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    // FIX: Pass logout callback to scaffold
                    // so profile sheet can trigger it
                    onLogout = { viewModel.logout() }
                ) {
                    PoleMapScreen(
                        onNavigateToReport = { poleId ->
                            navController.navigate(Screen.Report.createRoute(poleId))
                        },
                        onThemeToggle = onThemeToggle
                    )
                }
            }

            composable(
                route = Screen.Report.route,
                arguments = listOf(navArgument("poleId") {
                    type = NavType.StringType
                })
            ) {
                QuickReportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTracker = {
                        navController.navigate(Screen.Tracker.route) {
                            popUpTo(Screen.Map.route)
                        }
                    }
                )
            }

            composable(Screen.Tracker.route) {
                MainScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    onLogout = { viewModel.logout() }
                ) {
                    ComplaintTrackerScreen(
                        onNavigateToDetail = { complaintId ->
                            navController.navigate(
                                Screen.TrackerDetail.createRoute(complaintId)
                            )
                        }
                    )
                }
            }

            composable(
                route = Screen.TrackerDetail.route,
                arguments = listOf(navArgument("complaintId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val complaintId = backStackEntry.arguments
                    ?.getString("complaintId") ?: return@composable
                ComplaintDetailScreen(
                    complaintId = complaintId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Energy.route) {
                MainScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    onLogout = { viewModel.logout() }
                ) {
                    EnergyDashboardScreen()
                }
            }

            composable(Screen.Assistant.route) {
                MainScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    onLogout = { viewModel.logout() }
                ) {
                    AssistantScreen()
                }
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    // FIX: Added onLogout callback parameter
    onLogout: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = true }
            override fun onLost(network: Network) { isOnline = false }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                if (!isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "You are offline. Changes saved locally.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(80.dp)
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            screen.icon!!,
                                            contentDescription = screen.title,
                                            tint = if (selected) Color(0xFF1D9E75)
                                            else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        if (selected) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        Color(0xFF1D9E75),
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold
                                        else FontWeight.Normal,
                                        color = if (selected) Color(0xFF1D9E75)
                                        else Color.Gray
                                    )
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D9E75),
                                    unselectedIconColor = Color.Gray,
                                    selectedTextColor = Color(0xFF1D9E75),
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color.Transparent
                                ),
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(
                                            navController.graph.findStartDestination().id
                                        ) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}