package com.example.data.repository

import com.example.data.model.CustomerUser
import com.example.data.model.DeliveryAddress
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.DriverStatus
import com.example.data.model.DriverUser
import com.example.data.model.OrderItem
import com.example.data.model.PaymentMethod
import com.example.data.model.Store
import com.example.data.model.TodayDriverStats
import com.example.data.model.VehicleType
import com.example.data.supabase.SupabaseRestClient
import com.example.data.supabase.SupabaseSession
import com.example.data.supabase.SupabaseSessionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Supabase-backed driver repository. The legacy Firebase repository is not used by MainActivity. */
class SupabaseDriverRepository : DriverRepository {
    private val client = SupabaseRestClient()
    private val _driverProfile = MutableStateFlow(
        DriverUser(id = "", name = "", nameAr = "", phone = "", status = DriverStatus.OFFLINE)
    )
    override val driverProfile: StateFlow<DriverUser> = _driverProfile
    private val _driverStatus = MutableStateFlow(DriverStatus.OFFLINE)
    override val driverStatus: StateFlow<DriverStatus> = _driverStatus
    private val _activeDelivery = MutableStateFlow<DeliveryOrder?>(null)
    override val activeDelivery: StateFlow<DeliveryOrder?> = _activeDelivery
    private val _availableOrders = MutableStateFlow<List<DeliveryOrder>>(emptyList())
    override val availableOrders: StateFlow<List<DeliveryOrder>> = _availableOrders
    private val _historyOrders = MutableStateFlow<List<DeliveryOrder>>(emptyList())
    override val historyOrders: StateFlow<List<DeliveryOrder>> = _historyOrders
    private val _todayStats = MutableStateFlow(
        TodayDriverStats(0, 0.0, "SDG", 0.0, 0.0, 0f)
    )
    override val todayStats: StateFlow<TodayDriverStats> = _todayStats
    private val _isArabic = MutableStateFlow(true)
    override val isArabic: StateFlow<Boolean> = _isArabic
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    private var serverAvailability = false

    private suspend fun requireSession(): SupabaseSession =
        SupabaseSessionProvider.validSessionOrNull()
            ?: throw IllegalStateException("سجّل الدخول أولاً.")

    private suspend fun rows(path: String): JSONArray {
        val session = requireSession()
        return JSONArray(client.rest(path, accessToken = session.accessToken).getOrThrow())
    }

    override suspend fun login(phone: String, vehicleType: VehicleType): Result<DriverUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val session = requireSession()
                val uid = session.userId
                val profiles = rows("rest/v1/profiles?select=id,full_name,phone,role,is_active&id=eq.$uid")
                check(profiles.length() > 0) { "لم يتم العثور على ملف حساب المندوب." }
                val profile = profiles.getJSONObject(0)
                check(profile.optString("role") == "driver" && profile.optBoolean("is_active", false)) {
                    "حساب المندوب غير مفعّل."
                }

                val driverRows = rows("rest/v1/driver_profiles?select=*&profile_id=eq.$uid")
                if (driverRows.length() == 0) {
                    client.rest(
                        "rest/v1/driver_profiles",
                        method = "POST",
                        body = JSONObject().put("profile_id", uid)
                            .put("vehicle_type", vehicleType.name.lowercase()).toString(),
                        accessToken = session.accessToken,
                        prefer = "return=representation",
                    ).getOrThrow()
                } else if (phone.isNotBlank() && vehicleType.name.lowercase() != driverRows.getJSONObject(0).optString("vehicle_type")) {
                    client.rest(
                        "rest/v1/driver_profiles?profile_id=eq.$uid",
                        method = "PATCH",
                        body = JSONObject().put("vehicle_type", vehicleType.name.lowercase()).toString(),
                        accessToken = session.accessToken,
                    ).getOrThrow()
                }

                if (phone.isNotBlank() && phone != profile.optString("phone")) {
                    client.rest(
                        "rest/v1/profiles?id=eq.$uid",
                        method = "PATCH",
                        body = JSONObject().put("phone", phone).toString(),
                        accessToken = session.accessToken,
                    ).getOrThrow()
                }

                val refreshedProfile = rows("rest/v1/profiles?select=id,full_name,phone,role,is_active&id=eq.$uid").getJSONObject(0)
                val refreshedDriver = rows("rest/v1/driver_profiles?select=*&profile_id=eq.$uid").getJSONObject(0)
                serverAvailability = refreshedDriver.optBoolean("is_available", false)
                val driver = DriverUser(
                    id = uid,
                    name = refreshedProfile.optString("full_name").ifBlank { session.email.orEmpty().substringBefore("@") },
                    nameAr = refreshedProfile.optString("full_name").ifBlank { "مندوب وصّل" },
                    phone = refreshedProfile.optString("phone"),
                    rating = 0f,
                    totalDeliveries = 0,
                    vehicleType = refreshedDriver.optString("vehicle_type").toVehicleType(),
                    vehiclePlate = "",
                    status = if (serverAvailability) DriverStatus.AVAILABLE else DriverStatus.OFFLINE,
                )
                _driverProfile.value = driver
                _isLoggedIn.value = true
                refreshAll()
                driver
            }
        }

    override suspend fun logout() {
        _isLoggedIn.value = false
        _driverStatus.value = DriverStatus.OFFLINE
        _activeDelivery.value = null
        _availableOrders.value = emptyList()
        _historyOrders.value = emptyList()
        _driverProfile.value = DriverUser(id = "", name = "", nameAr = "", phone = "", status = DriverStatus.OFFLINE)
        serverAvailability = false
    }

    override suspend fun setDriverStatus(status: DriverStatus) = withContext(Dispatchers.IO) {
        val session = requireSession()
        if (status == DriverStatus.BUSY && _activeDelivery.value == null) {
            throw IllegalStateException("لا يمكن اختيار مشغول دون وجود طلب نشط.")
        }
        val available = status == DriverStatus.AVAILABLE
        client.rest(
            "rest/v1/rpc/wasil_set_driver_availability",
            method = "POST",
            body = JSONObject().put("p_is_available", available).toString(),
            accessToken = session.accessToken,
        ).getOrThrow()
        serverAvailability = available
        refreshAll()
    }

    override suspend fun acceptDelivery(orderId: String): Result<DeliveryOrder> = withContext(Dispatchers.IO) {
        runCatching {
            val session = requireSession()
            val response = client.rest(
                "rest/v1/rpc/wasil_accept_order",
                method = "POST",
                body = JSONObject().put("p_order_id", orderId).toString(),
                accessToken = session.accessToken,
            ).getOrThrow().trim()
            check(response == "true") { "سبق مندوب آخر إلى قبول هذا الطلب." }
            refreshAll()
            _activeDelivery.value?.takeIf { it.id == orderId }
                ?: throw IllegalStateException("تم قبول الطلب، لكن تعذر تحديث تفاصيله.")
        }
    }

    override suspend fun updateDeliveryStatus(orderId: String, newStatus: DeliveryStatus): Result<DeliveryOrder> =
        withContext(Dispatchers.IO) {
            runCatching {
                val nextStatus = when (newStatus) {
                    DeliveryStatus.PICKED_UP -> "picked_up"
                    DeliveryStatus.ON_THE_WAY -> "on_the_way"
                    DeliveryStatus.DELIVERED -> "delivered"
                    DeliveryStatus.ASSIGNED -> throw IllegalArgumentException("حالة الإسناد يحددها الخادم.")
                    DeliveryStatus.CANCELLED -> throw IllegalArgumentException("إلغاء الطلب غير متاح للمندوب في هذا الإصدار.")
                }
                val session = requireSession()
                val response = client.rest(
                    "rest/v1/rpc/wasil_driver_update_order_status",
                    method = "POST",
                    body = JSONObject().put("p_order_id", orderId).put("p_next_status", nextStatus).toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                check(response == "true") { "رفض الخادم تحديث حالة التوصيل." }
                refreshAll()
                findOrder(orderId) ?: throw IllegalStateException("تم تحديث الطلب لكن تعذر تحميله.")
            }
        }

    override suspend fun cancelDelivery(orderId: String, reason: String): Result<DeliveryOrder> =
        withContext(Dispatchers.IO) {
            runCatching {
                val session = requireSession()
                val response = client.rest(
                    "rest/v1/rpc/wasil_driver_release_assignment",
                    method = "POST",
                    body = JSONObject().put("p_order_id", orderId).put("p_reason", reason).toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                check(response == "true") { "تعذر إرجاع الطلب لقائمة المندوبين." }
                refreshAll()
                (_availableOrders.value.firstOrNull { it.id == orderId }
                    ?: throw IllegalStateException("أُعيد إسناد الطلب لكن تعذر تحديث القائمة."))
                    .copy(status = DeliveryStatus.CANCELLED)
            }
        }

    override fun getOrderById(orderId: String): DeliveryOrder? = findOrder(orderId)

    override suspend fun updateVehicleType(vehicleType: VehicleType) = withContext(Dispatchers.IO) {
        val session = requireSession()
        val uid = session.userId
        client.rest(
            "rest/v1/driver_profiles?profile_id=eq.$uid",
            method = "PATCH",
            body = JSONObject().put("vehicle_type", vehicleType.name.lowercase()).toString(),
            accessToken = session.accessToken,
        ).getOrThrow()
        _driverProfile.value = _driverProfile.value.copy(vehicleType = vehicleType)
    }

    override fun setLanguage(isArabic: Boolean) { _isArabic.value = isArabic }
    override fun toggleLanguage() { _isArabic.value = !_isArabic.value }
    override fun simulateNewIncomingOrder() { /* disabled in production */ }

    private suspend fun refreshAll() {
        val session = requireSession()
        val uid = session.userId
        val available = JSONArray(
            client.rest(
                "rest/v1/rpc/wasil_list_available_orders",
                method = "POST",
                body = "{}",
                accessToken = session.accessToken,
            ).getOrThrow()
        )
        val active = rows("rest/v1/orders?select=*,store:stores(*),address:addresses(*),order_items(*)&driver_id=eq.$uid&status=in.(driver_assigned,picked_up,on_the_way)&order=placed_at.desc")
        val history = rows("rest/v1/orders?select=*,store:stores(*),address:addresses(*),order_items(*)&driver_id=eq.$uid&status=in.(delivered,cancelled)&order=placed_at.desc")
        _availableOrders.value = (0 until available.length()).map { available.getJSONObject(it).toDeliveryOrder() }
        _activeDelivery.value = (0 until active.length()).map { active.getJSONObject(it).toDeliveryOrder() }.firstOrNull()
        _historyOrders.value = (0 until history.length()).map { history.getJSONObject(it).toDeliveryOrder() }
        _driverStatus.value = when {
            _activeDelivery.value != null -> DriverStatus.BUSY
            serverAvailability -> DriverStatus.AVAILABLE
            else -> DriverStatus.OFFLINE
        }
        _driverProfile.value = _driverProfile.value.copy(status = _driverStatus.value)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val completedToday = _historyOrders.value.filter {
            it.status == DeliveryStatus.DELIVERED && it.deliveredAt.orEmpty().startsWith(today)
        }
        _todayStats.value = TodayDriverStats(completedToday.size, 0.0, "SDG", 0.0, completedToday.sumOf { it.distanceKm }, 0f)
    }

    private fun findOrder(orderId: String): DeliveryOrder? =
        _activeDelivery.value?.takeIf { it.id == orderId }
            ?: _availableOrders.value.firstOrNull { it.id == orderId }
            ?: _historyOrders.value.firstOrNull { it.id == orderId }

    private fun JSONObject.toDeliveryOrder(): DeliveryOrder {
        val storeJson = optJSONObject("store") ?: JSONObject()
        val addressJson = optJSONObject("address") ?: JSONObject()
        val itemRows = optJSONArray("order_items") ?: JSONArray()
        val store = Store(
            id = storeJson.optString("id", optString("store_id")),
            name = storeJson.optString("name_en").ifBlank { storeJson.optString("name_ar") },
            nameAr = storeJson.optString("name_ar"),
            category = storeJson.optString("category_id"),
            categoryAr = storeJson.optString("category_id"),
            phone = storeJson.optString("phone"),
            address = storeJson.optString("address_text"),
            addressAr = storeJson.optString("address_text"),
            latitude = storeJson.optDouble("latitude", 0.0),
            longitude = storeJson.optDouble("longitude", 0.0),
        )
        val address = DeliveryAddress(
            area = addressJson.optString("area"),
            areaAr = addressJson.optString("area"),
            street = addressJson.optString("street"),
            streetAr = addressJson.optString("street"),
            buildingNumber = addressJson.optString("building_number"),
            floorNumber = addressJson.optString("floor_or_apartment"),
            apartmentNumber = addressJson.optString("apartment"),
            landmark = addressJson.optString("landmark"),
            landmarkAr = addressJson.optString("landmark"),
            deliveryNotes = addressJson.optString("delivery_notes"),
            deliveryNotesAr = addressJson.optString("delivery_notes"),
            latitude = addressJson.optDouble("latitude", 0.0),
            longitude = addressJson.optDouble("longitude", 0.0),
        )
        val customer = CustomerUser(
            id = optString("customer_id"),
            name = optString("customer_name_snapshot").ifBlank { addressJson.optString("recipient_name").ifBlank { "عميل وصّل" } },
            phone = optString("customer_phone_snapshot").ifBlank { addressJson.optString("recipient_phone") },
        )
        val items = (0 until itemRows.length()).map { i ->
            val item = itemRows.getJSONObject(i)
            val name = item.optString("product_name_snapshot")
            OrderItem(
                id = item.optString("id"),
                name = name,
                nameAr = name,
                quantity = item.optInt("quantity", 1),
                unitPrice = item.optDouble("unit_price"),
                notes = item.optString("special_instructions"),
                notesAr = item.optString("special_instructions"),
            )
        }
        val status = when (optString("status")) {
            "driver_assigned" -> DeliveryStatus.ASSIGNED
            "picked_up" -> DeliveryStatus.PICKED_UP
            "on_the_way" -> DeliveryStatus.ON_THE_WAY
            "delivered" -> DeliveryStatus.DELIVERED
            "cancelled" -> DeliveryStatus.CANCELLED
            else -> DeliveryStatus.ASSIGNED
        }
        return DeliveryOrder(
            id = optString("id"),
            orderNumber = "WAS-" + optLong("order_number"),
            store = store,
            customer = customer,
            deliveryAddress = address,
            items = items,
            orderTotal = optDouble("total"),
            driverEarnings = 0.0,
            currency = optString("currency").ifBlank { "SDG" },
            status = status,
            paymentMethod = if (optString("payment_method") == "cash_on_delivery") PaymentMethod.CASH_ON_DELIVERY else PaymentMethod.PREPAID,
            distanceKm = 0.0,
            estimatedDurationMin = 30,
            createdAt = optString("placed_at"),
            assignedAt = optString("assigned_at").takeIf { it.isNotBlank() && it != "null" },
            pickedUpAt = optString("picked_up_at").takeIf { it.isNotBlank() && it != "null" },
            deliveredAt = optString("delivered_at").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    private fun String.toVehicleType(): VehicleType = when (lowercase()) {
        "car" -> VehicleType.CAR
        "bicycle" -> VehicleType.BICYCLE
        "van" -> VehicleType.VAN
        else -> VehicleType.MOTORCYCLE
    }
}
