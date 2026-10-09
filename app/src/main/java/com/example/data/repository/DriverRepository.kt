package com.example.data.repository

import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.DriverStatus
import com.example.data.model.DriverUser
import com.example.data.model.TodayDriverStats
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.StateFlow

interface DriverRepository {
    val driverProfile: StateFlow<DriverUser>
    val driverStatus: StateFlow<DriverStatus>
    val activeDelivery: StateFlow<DeliveryOrder?>
    val availableOrders: StateFlow<List<DeliveryOrder>>
    val historyOrders: StateFlow<List<DeliveryOrder>>
    val todayStats: StateFlow<TodayDriverStats>
    val isArabic: StateFlow<Boolean>
    val isLoggedIn: StateFlow<Boolean>

    suspend fun login(phone: String, vehicleType: VehicleType): Result<DriverUser>
    suspend fun logout()
    suspend fun setDriverStatus(status: DriverStatus)
    suspend fun acceptDelivery(orderId: String): Result<DeliveryOrder>
    suspend fun updateDeliveryStatus(orderId: String, newStatus: DeliveryStatus): Result<DeliveryOrder>
    suspend fun cancelDelivery(orderId: String, reason: String): Result<DeliveryOrder>
    fun getOrderById(orderId: String): DeliveryOrder?
    suspend fun updateVehicleType(vehicleType: VehicleType)
    fun setLanguage(isArabic: Boolean)
    fun toggleLanguage()
    fun simulateNewIncomingOrder()
}
