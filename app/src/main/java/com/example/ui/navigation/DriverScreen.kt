package com.example.ui.navigation

sealed class DriverScreen(val route: String) {
    data object Login : DriverScreen("login")
    data object Home : DriverScreen("home")
    data object AvailableDeliveries : DriverScreen("available_deliveries")
    data object DeliveryDetails : DriverScreen("delivery_details/{orderId}") {
        fun createRoute(orderId: String) = "delivery_details/$orderId"
    }
    data object ActiveDelivery : DriverScreen("active_delivery")
    data object DeliveryHistory : DriverScreen("delivery_history")
    data object DriverProfile : DriverScreen("driver_profile")
}
