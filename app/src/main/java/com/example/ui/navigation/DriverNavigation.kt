package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.components.DriverAvailabilityDialog
import com.example.ui.localization.DriverStrings
import com.example.ui.screens.ActiveDeliveryScreen
import com.example.ui.screens.AvailableDeliveriesScreen
import com.example.ui.screens.DeliveryDetailsScreen
import com.example.ui.screens.DeliveryHistoryScreen
import com.example.ui.screens.DriverProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange
import com.example.ui.viewmodel.DriverViewModel

@Composable
fun DriverAppNavigation(
    navController: NavHostController,
    viewModel: DriverViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val driverProfile by viewModel.driverProfile.collectAsState()
    val driverStatus by viewModel.driverStatus.collectAsState()
    val activeDelivery by viewModel.activeDelivery.collectAsState()
    val availableOrders by viewModel.availableOrders.collectAsState()
    val historyOrders by viewModel.historyOrders.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val isAvailabilityDialogOpen by viewModel.isAvailabilityDialogVisible.collectAsState()
    val isConfirmDeliverDialogOpen by viewModel.isConfirmDeliverDialogVisible.collectAsState()
    val isCancelIssueDialogOpen by viewModel.isCancelIssueDialogVisible.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = isLoggedIn &&
            currentRoute != DriverScreen.Login.route &&
            currentRoute?.startsWith("delivery_details") != true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (shouldShowBottomBar) {
                DriverBottomNavigationBar(
                    currentRoute = currentRoute,
                    hasActiveDelivery = activeDelivery != null,
                    availableOrdersCount = availableOrders.size,
                    isArabic = isArabic,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) DriverScreen.Home.route else DriverScreen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DriverScreen.Login.route) {
                LoginScreen(
                    isArabic = isArabic,
                    onLoginSuccess = { phone, vehicleType ->
                        viewModel.login(phone, vehicleType)
                        navController.navigate(DriverScreen.Home.route) {
                            popUpTo(DriverScreen.Login.route) { inclusive = true }
                        }
                    },
                    onToggleLanguage = { viewModel.toggleLanguage() }
                )
            }

            composable(DriverScreen.Home.route) {
                HomeScreen(
                    driver = driverProfile,
                    driverStatus = driverStatus,
                    activeDelivery = activeDelivery,
                    availableOrders = availableOrders,
                    todayStats = todayStats,
                    isArabic = isArabic,
                    onStatusClick = { viewModel.setAvailabilityDialogVisible(true) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSimulateOrder = { viewModel.simulateNewOrder() },
                    onNavigateToActiveDelivery = {
                        navController.navigate(DriverScreen.ActiveDelivery.route)
                    },
                    onNavigateToAvailableDeliveries = {
                        navController.navigate(DriverScreen.AvailableDeliveries.route)
                    },
                    onOrderClick = { order ->
                        viewModel.setSelectedOrder(order)
                        navController.navigate(DriverScreen.DeliveryDetails.createRoute(order.id))
                    },
                    onAcceptDelivery = { orderId ->
                        viewModel.acceptDelivery(orderId) {
                            navController.navigate(DriverScreen.ActiveDelivery.route)
                        }
                    }
                )
            }

            composable(DriverScreen.AvailableDeliveries.route) {
                AvailableDeliveriesScreen(
                    availableOrders = availableOrders,
                    driverStatus = driverStatus,
                    isArabic = isArabic,
                    onAcceptDelivery = { orderId ->
                        viewModel.acceptDelivery(orderId) {
                            navController.navigate(DriverScreen.ActiveDelivery.route)
                        }
                    },
                    onOrderClick = { order ->
                        viewModel.setSelectedOrder(order)
                        navController.navigate(DriverScreen.DeliveryDetails.createRoute(order.id))
                    },
                    onSimulateNewOrder = { viewModel.simulateNewOrder() }
                )
            }

            composable(DriverScreen.ActiveDelivery.route) {
                ActiveDeliveryScreen(
                    order = activeDelivery,
                    isArabic = isArabic,
                    onAdvanceDeliveryStep = { viewModel.advanceActiveDeliveryStep() },
                    onConfirmDelivered = { viewModel.confirmDeliveryCompleted() },
                    onCancelDelivery = { reason -> viewModel.cancelActiveDelivery(reason) },
                    isConfirmDialogVisible = isConfirmDeliverDialogOpen,
                    isCancelDialogVisible = isCancelIssueDialogOpen,
                    onDismissConfirmDialog = { viewModel.setConfirmDeliverDialogVisible(false) },
                    onDismissCancelDialog = { viewModel.setCancelIssueDialogVisible(false) },
                    onOpenCancelDialog = { viewModel.setCancelIssueDialogVisible(true) },
                    onNavigateBack = { navController.navigate(DriverScreen.Home.route) }
                )
            }

            composable(DriverScreen.DeliveryDetails.route) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val orderToDisplay = if (selectedOrder?.id == orderId) {
                    selectedOrder
                } else {
                    availableOrders.find { it.id == orderId }
                        ?: (if (activeDelivery?.id == orderId) activeDelivery else null)
                        ?: historyOrders.find { it.id == orderId }
                }

                DeliveryDetailsScreen(
                    order = orderToDisplay,
                    isArabic = isArabic,
                    onAcceptDelivery = { id ->
                        viewModel.acceptDelivery(id) {
                            navController.navigate(DriverScreen.ActiveDelivery.route)
                        }
                    },
                    onNavigateToActiveDelivery = {
                        navController.navigate(DriverScreen.ActiveDelivery.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(DriverScreen.DeliveryHistory.route) {
                DeliveryHistoryScreen(
                    historyOrders = historyOrders,
                    todayStats = todayStats,
                    isArabic = isArabic,
                    onOrderClick = { order ->
                        viewModel.setSelectedOrder(order)
                        navController.navigate(DriverScreen.DeliveryDetails.createRoute(order.id))
                    }
                )
            }

            composable(DriverScreen.DriverProfile.route) {
                DriverProfileScreen(
                    driver = driverProfile,
                    todayStats = todayStats,
                    isArabic = isArabic,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(DriverScreen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Availability Dialog controller
    if (isAvailabilityDialogOpen) {
        DriverAvailabilityDialog(
            currentStatus = driverStatus,
            isArabic = isArabic,
            onStatusSelected = { newStatus ->
                viewModel.setDriverStatus(newStatus)
            },
            onDismiss = { viewModel.setAvailabilityDialogVisible(false) }
        )
    }
}

@Composable
private fun DriverBottomNavigationBar(
    currentRoute: String?,
    hasActiveDelivery: Boolean,
    availableOrdersCount: Int,
    isArabic: Boolean,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("driver_bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Home
        NavigationBarItem(
            selected = currentRoute == DriverScreen.Home.route,
            onClick = { onNavigate(DriverScreen.Home.route) },
            icon = {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
            },
            label = {
                Text(text = DriverStrings.navHome(isArabic), fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DriverOrange,
                selectedTextColor = DriverOrange,
                indicatorColor = DriverOrange.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // Available
        NavigationBarItem(
            selected = currentRoute == DriverScreen.AvailableDeliveries.route,
            onClick = { onNavigate(DriverScreen.AvailableDeliveries.route) },
            icon = {
                BadgedBox(
                    badge = {
                        if (availableOrdersCount > 0) {
                            Badge(containerColor = DriverOrange) {
                                Text("$availableOrdersCount")
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = "Available")
                }
            },
            label = {
                Text(text = DriverStrings.navAvailable(isArabic), fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DriverOrange,
                selectedTextColor = DriverOrange,
                indicatorColor = DriverOrange.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_available")
        )

        // Active Delivery (Highlighted)
        NavigationBarItem(
            selected = currentRoute == DriverScreen.ActiveDelivery.route,
            onClick = { onNavigate(DriverScreen.ActiveDelivery.route) },
            icon = {
                BadgedBox(
                    badge = {
                        if (hasActiveDelivery) {
                            Badge(containerColor = DriverEmerald) {
                                Text("●")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Active Delivery",
                        tint = if (hasActiveDelivery) DriverEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            label = {
                Text(
                    text = DriverStrings.navActive(isArabic),
                    fontWeight = FontWeight.Bold,
                    color = if (hasActiveDelivery) DriverEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DriverEmerald,
                selectedTextColor = DriverEmerald,
                indicatorColor = DriverEmerald.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_active")
        )

        // History
        NavigationBarItem(
            selected = currentRoute == DriverScreen.DeliveryHistory.route,
            onClick = { onNavigate(DriverScreen.DeliveryHistory.route) },
            icon = {
                Icon(imageVector = Icons.Default.History, contentDescription = "History")
            },
            label = {
                Text(text = DriverStrings.navHistory(isArabic), fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DriverOrange,
                selectedTextColor = DriverOrange,
                indicatorColor = DriverOrange.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_history")
        )

        // Profile
        NavigationBarItem(
            selected = currentRoute == DriverScreen.DriverProfile.route,
            onClick = { onNavigate(DriverScreen.DriverProfile.route) },
            icon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
            },
            label = {
                Text(text = DriverStrings.navProfile(isArabic), fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DriverOrange,
                selectedTextColor = DriverOrange,
                indicatorColor = DriverOrange.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_profile")
        )
    }
}
