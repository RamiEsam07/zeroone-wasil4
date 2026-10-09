package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseDriverRepository(
    private val db: FirebaseFirestore
) : DriverRepository {

    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isArabic = MutableStateFlow(true)
    override val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _driverProfile = MutableStateFlow(
        DriverUser(
            id = auth.currentUser?.uid ?: "DRV-8942",
            name = auth.currentUser?.displayName ?: "Captain Ahmed Al-Ghamdi",
            nameAr = "كابتن أحمد الغامدي",
            phone = auth.currentUser?.phoneNumber?.ifBlank { null } ?: "+966 55 123 4567",
            rating = 4.94f,
            totalDeliveries = 1420,
            vehicleType = VehicleType.MOTORCYCLE,
            vehiclePlate = "أ ب ج ٤ ٥ ٨ ٢",
            status = DriverStatus.AVAILABLE,
            shiftStartTime = "11:30 AM"
        )
    )
    override val driverProfile: StateFlow<DriverUser> = _driverProfile.asStateFlow()

    private val _driverStatus = MutableStateFlow(DriverStatus.AVAILABLE)
    override val driverStatus: StateFlow<DriverStatus> = _driverStatus.asStateFlow()

    private val _todayStats = MutableStateFlow(
        TodayDriverStats(
            completedDeliveriesCount = 7,
            totalEarnings = 168.50,
            currency = "SAR",
            activeHours = 4.2,
            totalDistanceKm = 31.8,
            customerRating = 4.96f
        )
    )
    override val todayStats: StateFlow<TodayDriverStats> = _todayStats.asStateFlow()

    private val _activeDelivery = MutableStateFlow<DeliveryOrder?>(null)
    override val activeDelivery: StateFlow<DeliveryOrder?> = _activeDelivery.asStateFlow()

    private val _availableOrders = MutableStateFlow<List<DeliveryOrder>>(createInitialAvailableOrders())
    override val availableOrders: StateFlow<List<DeliveryOrder>> = _availableOrders.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<DeliveryOrder>>(createInitialHistoryOrders())
    override val historyOrders: StateFlow<List<DeliveryOrder>> = _historyOrders.asStateFlow()

    private var profileListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isLoggedIn.value = user != null
            if (user != null) {
                attachFirestoreListeners(user.uid)
            } else {
                detachFirestoreListeners()
            }
        }
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun attachFirestoreListeners(uid: String) {
        detachFirestoreListeners()

        // 1. Listen to Driver Profile document
        val profileRef = db.collection("users").document(uid)
        profileListener = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, profileRef.path)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val statusStr = snapshot.getString("status") ?: "AVAILABLE"
                val newStatus = try { DriverStatus.valueOf(statusStr) } catch (e: Exception) { DriverStatus.AVAILABLE }
                _driverStatus.value = newStatus
                val vehicleTypeStr = snapshot.getString("vehicleType") ?: "MOTORCYCLE"
                val vehicleType = try { VehicleType.valueOf(vehicleTypeStr) } catch (e: Exception) { VehicleType.MOTORCYCLE }

                _driverProfile.value = _driverProfile.value.copy(
                    id = uid,
                    name = snapshot.getString("name") ?: _driverProfile.value.name,
                    phone = snapshot.getString("phone") ?: _driverProfile.value.phone,
                    status = newStatus,
                    vehicleType = vehicleType,
                    vehiclePlate = snapshot.getString("vehiclePlate") ?: _driverProfile.value.vehiclePlate,
                    totalDeliveries = (snapshot.getLong("totalDeliveries") ?: 1420L).toInt(),
                    rating = (snapshot.getDouble("rating") ?: 4.9).toFloat()
                )
            } else {
                // Initialize profile in Firestore
                val initialData = mapOf(
                    "userId" to uid,
                    "name" to (_driverProfile.value.name),
                    "phone" to (_driverProfile.value.phone),
                    "status" to "AVAILABLE",
                    "vehicleType" to "MOTORCYCLE",
                    "vehiclePlate" to "أ ب ج ٤ ٥ ٨ ٢",
                    "rating" to 4.95,
                    "totalDeliveries" to 1420,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                profileRef.set(initialData).addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, profileRef.path)
                }
            }
        }

        // 2. Listen to Orders Collection
        val ordersRef = db.collection("orders")
        ordersListener = ordersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, ordersRef.path)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val availableList = mutableListOf<DeliveryOrder>()
                var currentActive: DeliveryOrder? = null
                val historyList = mutableListOf<DeliveryOrder>()

                for (doc in snapshot.documents) {
                    val docDriverId = doc.getString("driverId")
                    val statusStr = doc.getString("status") ?: "ASSIGNED"
                    val status = try { DeliveryStatus.valueOf(statusStr) } catch (e: Exception) { DeliveryStatus.ASSIGNED }

                    val order = mapDocumentToDeliveryOrder(doc.id, doc.data ?: emptyMap(), status)

                    if (docDriverId == uid) {
                        if (status.isActive) {
                            currentActive = order
                        } else if (status == DeliveryStatus.DELIVERED || status == DeliveryStatus.CANCELLED) {
                            historyList.add(order)
                        }
                    } else if (docDriverId == null || docDriverId.isEmpty()) {
                        if (status == DeliveryStatus.ASSIGNED) {
                            availableList.add(order)
                        }
                    }
                }

                if (availableList.isNotEmpty()) {
                    _availableOrders.value = availableList
                }
                _activeDelivery.value = currentActive
                if (currentActive != null) {
                    _driverStatus.value = DriverStatus.BUSY
                }
                if (historyList.isNotEmpty()) {
                    _historyOrders.value = historyList
                }
            }
        }
    }

    private fun detachFirestoreListeners() {
        profileListener?.remove()
        profileListener = null
        ordersListener?.remove()
        ordersListener = null
    }

    private fun mapDocumentToDeliveryOrder(
        docId: String,
        data: Map<String, Any>,
        status: DeliveryStatus
    ): DeliveryOrder {
        val orderNumber = data["orderNumber"] as? String ?: "ORD-${docId.takeLast(4)}"
        val storeName = data["storeName"] as? String ?: "مطعم الشاورما"
        val deliveryAddressStr = data["deliveryAddress"] as? String ?: "حي العليا، الرياض"
        val orderTotal = (data["orderTotal"] as? Number)?.toDouble() ?: 75.0
        val driverEarnings = (data["driverEarnings"] as? Number)?.toDouble() ?: 24.0
        val distanceKm = (data["distanceKm"] as? Number)?.toDouble() ?: 3.5

        return DeliveryOrder(
            id = docId,
            orderNumber = orderNumber,
            store = Store(
                id = "store_${docId.take(4)}",
                name = storeName,
                nameAr = storeName,
                category = "Food",
                categoryAr = "مأكولات",
                phone = "+966 11 465 1234",
                address = "طريق الملك فهد، الرياض",
                addressAr = "طريق الملك فهد، الرياض",
                latitude = 24.7116,
                longitude = 46.6744,
                pickupInstructionsAr = "توجه لشباك الاستلام رقم ٢ واذكر رقم الطلب"
            ),
            customer = CustomerUser(
                id = "cust_${docId.take(4)}",
                name = data["customerName"] as? String ?: "عبدالله بن فهد",
                phone = data["customerPhone"] as? String ?: "+966 54 887 9921"
            ),
            deliveryAddress = DeliveryAddress(
                area = "Riyadh",
                areaAr = deliveryAddressStr,
                street = deliveryAddressStr,
                streetAr = deliveryAddressStr,
                deliveryNotesAr = "الرجاء عدم رن الجرس، اطرق الباب بهدوء"
            ),
            items = listOf(
                OrderItem("it_1", "الوجبة الرئيسية", "الوجبة الرئيسية المميزة", 2, orderTotal / 2)
            ),
            orderTotal = orderTotal,
            driverEarnings = driverEarnings,
            currency = "SAR",
            status = status,
            paymentMethod = PaymentMethod.PREPAID,
            distanceKm = distanceKm,
            estimatedDurationMin = 15,
            createdAt = timeFormat.format(Date())
        )
    }

    override suspend fun login(phone: String, vehicleType: VehicleType): Result<DriverUser> {
        val updated = _driverProfile.value.copy(
            phone = phone.ifBlank { "+966 55 123 4567" },
            vehicleType = vehicleType,
            status = DriverStatus.AVAILABLE,
            shiftStartTime = timeFormat.format(Date())
        )
        _driverProfile.value = updated
        _driverStatus.value = DriverStatus.AVAILABLE
        _isLoggedIn.value = true

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid)
            val updateData = mapOf(
                "phone" to updated.phone,
                "vehicleType" to vehicleType.name,
                "status" to DriverStatus.AVAILABLE.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updateData).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            }
        }

        return Result.success(updated)
    }

    override suspend fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _driverStatus.value = DriverStatus.OFFLINE
    }

    override suspend fun setDriverStatus(status: DriverStatus) {
        _driverStatus.value = status
        _driverProfile.value = _driverProfile.value.copy(status = status)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid)
            docRef.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            }
        }
    }

    override suspend fun acceptDelivery(orderId: String): Result<DeliveryOrder> {
        val currentAvailable = _availableOrders.value.toMutableList()
        val index = currentAvailable.indexOfFirst { it.id == orderId }

        if (index == -1) {
            return Result.failure(IllegalArgumentException("Order no longer available"))
        }

        if (_activeDelivery.value != null) {
            return Result.failure(IllegalStateException("You already have an active delivery"))
        }

        val order = currentAvailable.removeAt(index)
        val accepted = order.copy(
            status = DeliveryStatus.ASSIGNED,
            assignedAt = timeFormat.format(Date())
        )

        _availableOrders.value = currentAvailable
        _activeDelivery.value = accepted
        _driverStatus.value = DriverStatus.BUSY

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val orderRef = db.collection("orders").document(orderId)
            orderRef.update(
                mapOf(
                    "driverId" to uid,
                    "status" to DeliveryStatus.ASSIGNED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
            }
        }

        return Result.success(accepted)
    }

    override suspend fun updateDeliveryStatus(
        orderId: String,
        newStatus: DeliveryStatus
    ): Result<DeliveryOrder> {
        val current = _activeDelivery.value
        if (current == null || current.id != orderId) {
            return Result.failure(IllegalStateException("No matching active delivery found"))
        }

        val now = timeFormat.format(Date())
        val updated = when (newStatus) {
            DeliveryStatus.PICKED_UP -> current.copy(status = DeliveryStatus.PICKED_UP, pickedUpAt = now)
            DeliveryStatus.ON_THE_WAY -> current.copy(status = DeliveryStatus.ON_THE_WAY)
            DeliveryStatus.DELIVERED -> current.copy(status = DeliveryStatus.DELIVERED, deliveredAt = now)
            DeliveryStatus.CANCELLED -> current.copy(status = DeliveryStatus.CANCELLED)
            DeliveryStatus.ASSIGNED -> current
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            _activeDelivery.value = null
            _historyOrders.value = listOf(updated) + _historyOrders.value
            _todayStats.value = _todayStats.value.copy(
                completedDeliveriesCount = _todayStats.value.completedDeliveriesCount + 1,
                totalEarnings = _todayStats.value.totalEarnings + updated.driverEarnings
            )
            _driverStatus.value = DriverStatus.AVAILABLE
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            _activeDelivery.value = null
            _historyOrders.value = listOf(updated) + _historyOrders.value
            _driverStatus.value = DriverStatus.AVAILABLE
        } else {
            _activeDelivery.value = updated
            _driverStatus.value = DriverStatus.BUSY
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val orderRef = db.collection("orders").document(orderId)
            orderRef.update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
            }
        }

        return Result.success(updated)
    }

    override suspend fun cancelDelivery(orderId: String, reason: String): Result<DeliveryOrder> {
        return updateDeliveryStatus(orderId, DeliveryStatus.CANCELLED)
    }

    override fun getOrderById(orderId: String): DeliveryOrder? {
        if (_activeDelivery.value?.id == orderId) return _activeDelivery.value
        return _availableOrders.value.find { it.id == orderId }
            ?: _historyOrders.value.find { it.id == orderId }
    }

    override suspend fun updateVehicleType(vehicleType: VehicleType) {
        _driverProfile.value = _driverProfile.value.copy(vehicleType = vehicleType)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid)
            docRef.update(
                mapOf(
                    "vehicleType" to vehicleType.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            }
        }
    }

    override fun setLanguage(isArabic: Boolean) {
        _isArabic.value = isArabic
    }

    override fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    override fun simulateNewIncomingOrder() {
        val newOrderNumber = "ORD-${(1000..9999).random()}"
        val orderId = "ord_sim_${System.currentTimeMillis()}"
        val newOrder = DeliveryOrder(
            id = orderId,
            orderNumber = newOrderNumber,
            store = Store(
                id = "store_sim",
                name = "Half Million Coffee",
                nameAr = "مقهى هاف ميلين",
                category = "Cafe",
                categoryAr = "مقهى ومخبوزات",
                phone = "+966 11 222 7777",
                address = "شارع التحلية، الرياض",
                addressAr = "شارع التحلية، حي العليا",
                latitude = 24.7001,
                longitude = 46.6800,
                pickupInstructionsAr = "منصة استلام الباريستا رقم ٢"
            ),
            customer = CustomerUser("cust_sim", "سلطان الشمري", "+966 50 123 4567"),
            deliveryAddress = DeliveryAddress(
                area = "Al Malqa",
                areaAr = "حي الملقا",
                street = "طريق أنس بن مالك",
                streetAr = "طريق أنس بن مالك",
                deliveryNotesAr = "الاتصال عند الوصول أمام العمارة"
            ),
            items = listOf(OrderItem("it_sim", "قهوة مميزة ومخبوزات", "قهوة مميزة ومخبوزات", 2, 45.0)),
            orderTotal = 90.0,
            driverEarnings = 26.50,
            currency = "SAR",
            status = DeliveryStatus.ASSIGNED,
            paymentMethod = PaymentMethod.PREPAID,
            distanceKm = 4.8,
            estimatedDurationMin = 18,
            createdAt = timeFormat.format(Date())
        )

        _availableOrders.value = listOf(newOrder) + _availableOrders.value

        // Also push to Firestore if authenticated
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val orderRef = db.collection("orders").document(orderId)
            orderRef.set(
                mapOf(
                    "orderNumber" to newOrderNumber,
                    "storeName" to "مقهى هاف ميلين",
                    "deliveryAddress" to "طريق أنس بن مالك، حي الملقا",
                    "status" to "ASSIGNED",
                    "orderTotal" to 90.0,
                    "driverEarnings" to 26.50,
                    "driverId" to null,
                    "paymentMethod" to "PREPAID",
                    "distanceKm" to 4.8,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.CREATE, orderRef.path)
            }
        }
    }

    private fun createInitialAvailableOrders(): List<DeliveryOrder> {
        return listOf(
            DeliveryOrder(
                id = "ord_avail_201",
                orderNumber = "ORD-9304",
                store = Store(
                    id = "store_2",
                    name = "Burger Factory",
                    nameAr = "برجر فاكتوري",
                    category = "Burgers",
                    categoryAr = "برجر ومشويات",
                    phone = "+966 11 210 5544",
                    address = "شارع التخصصي، حي النخيل",
                    addressAr = "شارع التخصصي، حي النخيل",
                    latitude = 24.7330,
                    longitude = 46.6420,
                    pickupInstructionsAr = "الطلب جاهز على الرف الساخن رقم ٣"
                ),
                customer = CustomerUser("cust_301", "محمد القحطاني", "+966 56 331 4488"),
                deliveryAddress = DeliveryAddress(
                    area = "Al Mohammadiyah",
                    areaAr = "حي المحمدية",
                    street = "شارع التخصصي الفرعي",
                    streetAr = "شارع التخصصي الفرعي",
                    deliveryNotesAr = "اتصل عند الوصول أمام البوابة الرئيسية"
                ),
                items = listOf(OrderItem("it_21", "دبل سموك هاوس برجر", "دبل سموك هاوس برجر", 1, 42.0)),
                orderTotal = 58.0,
                driverEarnings = 22.50,
                currency = "SAR",
                status = DeliveryStatus.ASSIGNED,
                paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
                distanceKm = 2.8,
                estimatedDurationMin = 12,
                createdAt = "12:20 PM"
            ),
            DeliveryOrder(
                id = "ord_avail_202",
                orderNumber = "ORD-9310",
                store = Store(
                    id = "store_3",
                    name = "Al-Dawaa Pharmacy",
                    nameAr = "صيدلية الدواء",
                    category = "Pharmacy",
                    categoryAr = "صيدلية ورعاية صحية",
                    phone = "+966 11 880 9900",
                    address = "طريق أنس بن مالك، حي الملقا",
                    addressAr = "طريق أنس بن مالك، حي الملقا",
                    latitude = 24.8020,
                    longitude = 46.6190,
                    pickupInstructionsAr = "الأدوية مجهزة ومغلقة بشريط أمان لدى الكاشير"
                ),
                customer = CustomerUser("cust_302", "سارة العتيبي", "+966 53 772 1199"),
                deliveryAddress = DeliveryAddress(
                    area = "Al Malqa",
                    areaAr = "حي الملقا",
                    street = "طريق القيروان",
                    streetAr = "طريق القيروان",
                    deliveryNotesAr = "ضع الطلب في صندوق الطرود بجوار البوابة"
                ),
                items = listOf(OrderItem("it_31", "عبوة أدوية ومكملات فيتامينات", "عبوة أدوية ومكملات فيتامينات", 1, 115.0)),
                orderTotal = 115.0,
                driverEarnings = 29.00,
                currency = "SAR",
                status = DeliveryStatus.ASSIGNED,
                paymentMethod = PaymentMethod.PREPAID,
                distanceKm = 5.2,
                estimatedDurationMin = 19,
                createdAt = "12:24 PM"
            )
        )
    }

    private fun createInitialHistoryOrders(): List<DeliveryOrder> {
        return listOf(
            DeliveryOrder(
                id = "ord_hist_1",
                orderNumber = "ORD-8390",
                store = Store("store_h1", "ستاربكس ريزيرف", "ستاربكس ريزيرف", "مقهى", "مقهى", "+966 11 412 0000", "برج المملكة، حي العليا", "برج المملكة، حي العليا", 24.7110, 46.6740),
                customer = CustomerUser("cust_h1", "فيصل الدوسري", "+966 50 123 9988"),
                deliveryAddress = DeliveryAddress("Al Olaya", "حي العليا", "شارع العليا العام", "شارع العليا العام"),
                items = listOf(OrderItem("hi_1", "سبنش لاتيه مثلج", "سبنش لاتيه مثلج", 2, 24.0)),
                orderTotal = 48.0,
                driverEarnings = 21.00,
                currency = "SAR",
                status = DeliveryStatus.DELIVERED,
                distanceKm = 2.1,
                estimatedDurationMin = 10,
                createdAt = "10:15 AM",
                deliveredAt = "10:35 AM"
            )
        )
    }
}
