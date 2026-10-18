package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.DriverStatus
import com.example.data.model.DriverUser
import com.example.data.model.TodayDriverStats
import com.example.data.model.VehicleType
import com.example.data.repository.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class NavigateTo(val route: String) : UiEvent
}

class DriverViewModel(
    private val repository: DriverRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val isArabic: StateFlow<Boolean> = repository.isArabic
    val driverProfile: StateFlow<DriverUser> = repository.driverProfile
    val driverStatus: StateFlow<DriverStatus> = repository.driverStatus
    val activeDelivery: StateFlow<DeliveryOrder?> = repository.activeDelivery
    val availableOrders: StateFlow<List<DeliveryOrder>> = repository.availableOrders
    val historyOrders: StateFlow<List<DeliveryOrder>> = repository.historyOrders
    val todayStats: StateFlow<TodayDriverStats> = repository.todayStats

    private val _selectedOrder = MutableStateFlow<DeliveryOrder?>(null)
    val selectedOrder: StateFlow<DeliveryOrder?> = _selectedOrder.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isAvailabilityDialogVisible = MutableStateFlow(false)
    val isAvailabilityDialogVisible: StateFlow<Boolean> = _isAvailabilityDialogVisible.asStateFlow()

    private val _isConfirmDeliverDialogVisible = MutableStateFlow(false)
    val isConfirmDeliverDialogVisible: StateFlow<Boolean> = _isConfirmDeliverDialogVisible.asStateFlow()

    private val _isCancelIssueDialogVisible = MutableStateFlow(false)
    val isCancelIssueDialogVisible: StateFlow<Boolean> = _isCancelIssueDialogVisible.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setAvailabilityDialogVisible(visible: Boolean) {
        _isAvailabilityDialogVisible.value = visible
    }

    fun setConfirmDeliverDialogVisible(visible: Boolean) {
        _isConfirmDeliverDialogVisible.value = visible
    }

    fun setCancelIssueDialogVisible(visible: Boolean) {
        _isCancelIssueDialogVisible.value = visible
    }

    fun setSelectedOrder(order: DeliveryOrder?) {
        _selectedOrder.value = order
    }

    fun setSelectedOrderById(orderId: String) {
        _selectedOrder.value = repository.getOrderById(orderId)
    }

    fun toggleLanguage() {
        repository.toggleLanguage()
    }

    fun setDriverStatus(status: DriverStatus) {
        viewModelScope.launch {
            repository.setDriverStatus(status)
            _isAvailabilityDialogVisible.value = false
            val ar = repository.isArabic.value
            val msg = if (ar) {
                "تم تحديث حالتك إلى: ${status.getDisplayName(true)}"
            } else {
                "Availability updated to: ${status.getDisplayName(false)}"
            }
            showToast(msg)
        }
    }

    suspend fun loginWithSupabase(): Result<DriverUser> =
        repository.login("", VehicleType.MOTORCYCLE)

    suspend fun refreshSupabaseData() {
        (repository as? SupabaseDriverRepository)?.refreshLiveData()
    }

    fun login(phone: String, vehicleType: VehicleType) {
        viewModelScope.launch {
            repository.login(phone, vehicleType)
            val ar = repository.isArabic.value
            showToast(if (ar) "مرحباً بك! بدأت ورديتك بنجاح" else "Shift started successfully!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            val ar = repository.isArabic.value
            showToast(if (ar) "تم تسجيل الخروج بنجاح" else "Signed out successfully")
        }
    }

    fun acceptDelivery(orderId: String, onAccepted: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.acceptDelivery(orderId)
            val ar = repository.isArabic.value
            if (result.isSuccess) {
                val order = result.getOrNull()
                _selectedOrder.value = order
                showToast(
                    if (ar) "تم قبول الطلب #${order?.orderNumber}، توجه للمتجر للاستلام"
                    else "Accepted order #${order?.orderNumber}, go to store"
                )
                onAccepted?.invoke()
            } else {
                showToast(result.exceptionOrNull()?.message ?: if (ar) "تعذر قبول الطلب" else "Could not accept order")
            }
        }
    }

    /**
     * Steps through the strict sequential flow:
     * ASSIGNED -> PICKED_UP ("استلمت الطلب")
     * PICKED_UP -> ON_THE_WAY ("بدأت التوصيل")
     * ON_THE_WAY -> DELIVERED ("تم التسليم")
     */
    fun advanceActiveDeliveryStep() {
        val current = activeDelivery.value ?: return
        val next = current.status.nextState() ?: return

        if (next == DeliveryStatus.DELIVERED) {
            // Require explicit confirmation dialog before closing delivery
            _isConfirmDeliverDialogVisible.value = true
        } else {
            performStatusUpdate(current.id, next)
        }
    }

    fun confirmDeliveryCompleted() {
        val current = activeDelivery.value ?: return
        _isConfirmDeliverDialogVisible.value = false
        performStatusUpdate(current.id, DeliveryStatus.DELIVERED)
    }

    private fun performStatusUpdate(orderId: String, newStatus: DeliveryStatus) {
        viewModelScope.launch {
            val result = repository.updateDeliveryStatus(orderId, newStatus)
            val ar = repository.isArabic.value
            if (result.isSuccess) {
                val order = result.getOrNull()
                val msg = if (ar) {
                    when (newStatus) {
                        DeliveryStatus.PICKED_UP -> "تم تأكيد استلام الطلب من المتجر 📦"
                        DeliveryStatus.ON_THE_WAY -> "بدأت التوصيل! في الطريق للعميل 🛵"
                        DeliveryStatus.DELIVERED -> "مبروك! تم التسليم بنجاح وأضيفت عمولتك 💰"
                        DeliveryStatus.CANCELLED -> "تم إلغاء الطلب"
                        DeliveryStatus.ASSIGNED -> "تم تعيين الطلب"
                    }
                } else {
                    when (newStatus) {
                        DeliveryStatus.PICKED_UP -> "Order picked up from store 📦"
                        DeliveryStatus.ON_THE_WAY -> "Started delivery to customer 🛵"
                        DeliveryStatus.DELIVERED -> "Delivered! Earnings added 💰"
                        DeliveryStatus.CANCELLED -> "Delivery cancelled"
                        DeliveryStatus.ASSIGNED -> "Delivery assigned"
                    }
                }
                showToast(msg)
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Error updating delivery status")
            }
        }
    }

    fun cancelActiveDelivery(reason: String) {
        val current = activeDelivery.value ?: return
        _isCancelIssueDialogVisible.value = false
        viewModelScope.launch {
            repository.cancelDelivery(current.id, reason)
            val ar = repository.isArabic.value
            showToast(if (ar) "تم تسجيل تعذر التوصيل وإعادة الطلب" else "Order reported as cancelled")
        }
    }

    fun simulateNewOrder() {
        repository.simulateNewIncomingOrder()
        val ar = repository.isArabic.value
        showToast(if (ar) "وصلك عرض طلب توصيل جديد بالقرب منك! 🔔" else "New delivery offer arrived nearby! 🔔")
    }
}
