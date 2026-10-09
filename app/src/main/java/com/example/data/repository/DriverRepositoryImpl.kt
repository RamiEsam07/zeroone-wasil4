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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriverRepositoryImpl : DriverRepository {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    private val _isLoggedIn = MutableStateFlow(true)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isArabic = MutableStateFlow(true) // Default Arabic as highlighted in prompt
    override val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _driverProfile = MutableStateFlow(
        DriverUser(
            id = "DRV-8942",
            name = "Captain Ahmed Al-Ghamdi",
            nameAr = "كابتن أحمد الغامدي",
            phone = "+966 55 123 4567",
            rating = 4.92f,
            totalDeliveries = 1420,
            vehicleType = VehicleType.MOTORCYCLE,
            vehiclePlate = "أ ب ج ٤ ٥ ٨ ٢",
            status = DriverStatus.BUSY,
            shiftStartTime = "11:30 AM"
        )
    )
    override val driverProfile: StateFlow<DriverUser> = _driverProfile.asStateFlow()

    private val _driverStatus = MutableStateFlow(DriverStatus.BUSY)
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

    // 1 Active order initially assigned to driver so they can test immediately
    private val _activeDelivery = MutableStateFlow<DeliveryOrder?>(
        createInitialActiveOrder()
    )
    override val activeDelivery: StateFlow<DeliveryOrder?> = _activeDelivery.asStateFlow()

    // Available orders pool
    private val _availableOrders = MutableStateFlow<List<DeliveryOrder>>(
        createInitialAvailableOrders()
    )
    override val availableOrders: StateFlow<List<DeliveryOrder>> = _availableOrders.asStateFlow()

    // History orders pool
    private val _historyOrders = MutableStateFlow<List<DeliveryOrder>>(
        createInitialHistoryOrders()
    )
    override val historyOrders: StateFlow<List<DeliveryOrder>> = _historyOrders.asStateFlow()

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
        return Result.success(updated)
    }

    override suspend fun logout() {
        _isLoggedIn.value = false
        _driverStatus.value = DriverStatus.OFFLINE
    }

    override suspend fun setDriverStatus(status: DriverStatus) {
        // If driver has an ongoing active delivery and tries to go offline,
        // we keep them aware or allow change
        _driverStatus.value = status
        _driverProfile.value = _driverProfile.value.copy(status = status)
    }

    override suspend fun acceptDelivery(orderId: String): Result<DeliveryOrder> {
        val currentOrders = _availableOrders.value.toMutableList()
        val targetOrderIndex = currentOrders.indexOfFirst { it.id == orderId }

        if (targetOrderIndex == -1) {
            return Result.failure(IllegalArgumentException("Order not found or no longer available"))
        }

        if (_activeDelivery.value != null) {
            return Result.failure(IllegalStateException("You already have an active delivery in progress"))
        }

        val targetOrder = currentOrders.removeAt(targetOrderIndex)
        val acceptedOrder = targetOrder.copy(
            status = DeliveryStatus.ASSIGNED,
            assignedAt = timeFormat.format(Date())
        )

        _availableOrders.value = currentOrders
        _activeDelivery.value = acceptedOrder
        _driverStatus.value = DriverStatus.BUSY
        _driverProfile.value = _driverProfile.value.copy(status = DriverStatus.BUSY)

        return Result.success(acceptedOrder)
    }

    override suspend fun updateDeliveryStatus(
        orderId: String,
        newStatus: DeliveryStatus
    ): Result<DeliveryOrder> {
        val currentActive = _activeDelivery.value

        if (currentActive == null || currentActive.id != orderId) {
            return Result.failure(IllegalStateException("No matching active delivery found"))
        }

        val now = timeFormat.format(Date())
        val updatedOrder = when (newStatus) {
            DeliveryStatus.PICKED_UP -> currentActive.copy(
                status = DeliveryStatus.PICKED_UP,
                pickedUpAt = now
            )
            DeliveryStatus.ON_THE_WAY -> currentActive.copy(
                status = DeliveryStatus.ON_THE_WAY
            )
            DeliveryStatus.DELIVERED -> currentActive.copy(
                status = DeliveryStatus.DELIVERED,
                deliveredAt = now
            )
            DeliveryStatus.CANCELLED -> currentActive.copy(
                status = DeliveryStatus.CANCELLED
            )
            DeliveryStatus.ASSIGNED -> currentActive
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            // Completed! Archive to history, award earnings, reset driver to AVAILABLE
            _activeDelivery.value = null
            _historyOrders.value = listOf(updatedOrder) + _historyOrders.value

            val currentStats = _todayStats.value
            _todayStats.value = currentStats.copy(
                completedDeliveriesCount = currentStats.completedDeliveriesCount + 1,
                totalEarnings = currentStats.totalEarnings + updatedOrder.driverEarnings,
                totalDistanceKm = currentStats.totalDistanceKm + updatedOrder.distanceKm
            )

            _driverStatus.value = DriverStatus.AVAILABLE
            _driverProfile.value = _driverProfile.value.copy(
                status = DriverStatus.AVAILABLE,
                totalDeliveries = _driverProfile.value.totalDeliveries + 1
            )
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            _activeDelivery.value = null
            _historyOrders.value = listOf(updatedOrder) + _historyOrders.value
            _driverStatus.value = DriverStatus.AVAILABLE
            _driverProfile.value = _driverProfile.value.copy(status = DriverStatus.AVAILABLE)
        } else {
            _activeDelivery.value = updatedOrder
            _driverStatus.value = DriverStatus.BUSY
        }

        return Result.success(updatedOrder)
    }

    override suspend fun cancelDelivery(orderId: String, reason: String): Result<DeliveryOrder> {
        return updateDeliveryStatus(orderId, DeliveryStatus.CANCELLED)
    }

    override fun getOrderById(orderId: String): DeliveryOrder? {
        if (_activeDelivery.value?.id == orderId) return _activeDelivery.value
        val fromAvailable = _availableOrders.value.find { it.id == orderId }
        if (fromAvailable != null) return fromAvailable
        return _historyOrders.value.find { it.id == orderId }
    }

    override suspend fun updateVehicleType(vehicleType: VehicleType) {
        _driverProfile.value = _driverProfile.value.copy(vehicleType = vehicleType)
    }

    override fun setLanguage(isArabic: Boolean) {
        _isArabic.value = isArabic
    }

    override fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    override fun simulateNewIncomingOrder() {
        val newOrderNumber = "ORD-${(1000..9999).random()}"
        val sampleStores = listOf(
            Store(
                id = "store_5",
                name = "Al-Romansiah Restaurant",
                nameAr = "مطاعم الرومنسية",
                category = "Traditional",
                categoryAr = "مأكولات شعبية",
                phone = "+966 11 444 8888",
                address = "King Fahd Road, Al Sahafa",
                addressAr = "طريق الملك فهد، حي الصحافة",
                latitude = 24.7892,
                longitude = 46.6543,
                pickupInstructions = "Pickup counter near side entrance",
                pickupInstructionsAr = "شباك استلام الطلبات بجوار المدخل الجانبي"
            ),
            Store(
                id = "store_6",
                name = "Half Million Coffee",
                nameAr = "مقهى هاف ميلين",
                category = "Cafe",
                categoryAr = "مقهى ومخبوزات",
                phone = "+966 11 222 7777",
                address = "Tahlia Street, Al Olaya",
                addressAr = "شارع التحلية، حي العليا",
                latitude = 24.7001,
                longitude = 46.6800,
                pickupInstructions = "Barista pickup stand 2",
                pickupInstructionsAr = "منصة استلام الباريستا رقم ٢"
            )
        )

        val store = sampleStores.random()
        val generated = DeliveryOrder(
            id = "order_${System.currentTimeMillis()}",
            orderNumber = newOrderNumber,
            store = store,
            customer = CustomerUser(
                id = "cust_${System.currentTimeMillis()}",
                name = "سلطان الشمري",
                phone = "+966 50 ${(1000000..9999999).random()}"
            ),
            deliveryAddress = DeliveryAddress(
                area = "Al Malqa",
                areaAr = "حي الملقا",
                street = "Anas Bin Malik Road",
                streetAr = "طريق أنس بن مالك",
                buildingNumber = "42",
                floorNumber = "3",
                apartmentNumber = "12",
                landmark = "Opposite Kingdom Hospital",
                landmarkAr = "مقابل مستشفى المملكة",
                deliveryNotes = "Call upon arrival",
                deliveryNotesAr = "الاتصال عند الوصول أمام العمارة"
            ),
            items = listOf(
                OrderItem(
                    id = "item_${System.currentTimeMillis()}",
                    name = "Special Meal Order",
                    nameAr = "وجبة مشكلة خاصة مع مقبلات",
                    quantity = 2,
                    unitPrice = 45.0,
                    notes = "Hot sauce on side",
                    notesAr = "الشطة في علبة منفصلة"
                )
            ),
            orderTotal = 90.0,
            driverEarnings = 26.50,
            currency = "SAR",
            status = DeliveryStatus.ASSIGNED, // incoming available pool
            paymentMethod = PaymentMethod.PREPAID,
            distanceKm = 4.8,
            estimatedDurationMin = 18,
            createdAt = timeFormat.format(Date())
        )

        _availableOrders.value = listOf(generated) + _availableOrders.value
    }

    // Helper seeds
    private fun createInitialActiveOrder(): DeliveryOrder {
        return DeliveryOrder(
            id = "ord_active_101",
            orderNumber = "ORD-8492",
            store = Store(
                id = "store_1",
                name = "Shawarma King",
                nameAr = "شاورما كينج",
                category = "Fast Food",
                categoryAr = "وجبات سريعة",
                phone = "+966 11 465 1234",
                address = "King Fahd Road, Al Olaya",
                addressAr = "طريق الملك فهد، حي العليا (بجوار برج المملكة)",
                latitude = 24.7116,
                longitude = 46.6744,
                pickupInstructions = "Go directly to the online pickup counter, show order #8492",
                pickupInstructionsAr = "توجه مباشرة لشباك استلام تطبيقات التوصيل واذكر رقم #8492"
            ),
            customer = CustomerUser(
                id = "cust_201",
                name = "عبدالله بن فهد",
                phone = "+966 54 887 9921"
            ),
            deliveryAddress = DeliveryAddress(
                area = "Al Sulaimaniyah",
                areaAr = "حي السليمانية",
                street = "Prince Abdulaziz Bin Musaed Jalawi St",
                streetAr = "شارع الأمير عبدالعزيز بن مساعد جلوي",
                buildingNumber = "18",
                floorNumber = "2",
                apartmentNumber = "6",
                landmark = "Near Al Habib Hospital",
                landmarkAr = "قرب مستشفى الحبيب - مجمع الزهور",
                deliveryNotes = "Please do not ring bell, newborn sleeping. Just knock softly.",
                deliveryNotesAr = "الرجاء عدم رن الجرس لطفاً، الطفل نائم. اطرق الباب بهدوء."
            ),
            items = listOf(
                OrderItem(
                    id = "it_1",
                    name = "2x Jumbo Chicken Shawarma",
                    nameAr = "٢ وجبة شاورما دجاج جامبو صاج",
                    quantity = 2,
                    unitPrice = 28.0,
                    notes = "Extra garlic paste & pickles",
                    notesAr = "ثوم زيادة ومخلل خارجي"
                ),
                OrderItem(
                    id = "it_2",
                    name = "Large French Fries",
                    nameAr = "بطاطس مقلية مقرمشة حجم كبير",
                    quantity = 1,
                    unitPrice = 12.0
                ),
                OrderItem(
                    id = "it_3",
                    name = "2x Diet Citrus Drink",
                    nameAr = "٢ مشروب غازي حمضيات دايت بارد",
                    quantity = 2,
                    unitPrice = 5.0
                )
            ),
            orderTotal = 78.0,
            driverEarnings = 24.00,
            currency = "SAR",
            status = DeliveryStatus.ASSIGNED,
            paymentMethod = PaymentMethod.PREPAID,
            distanceKm = 3.6,
            estimatedDurationMin = 14,
            createdAt = "12:10 PM",
            assignedAt = "12:12 PM"
        )
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
                    address = "Al Takhassusi St, Al Nakheel",
                    addressAr = "شارع التخصصي، حي النخيل",
                    latitude = 24.7330,
                    longitude = 46.6420,
                    pickupInstructions = "Order ready on heated rack #3",
                    pickupInstructionsAr = "الطلب جاهز على الرف الساخن رقم ٣"
                ),
                customer = CustomerUser(
                    id = "cust_301",
                    name = "محمد القحطاني",
                    phone = "+966 56 331 4488"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Al Mohammadiyah",
                    areaAr = "حي المحمدية",
                    street = "Takhassusi Branch St",
                    streetAr = "شارع التخصصي الفرعي",
                    buildingNumber = "85",
                    floorNumber = "1",
                    apartmentNumber = "3",
                    landmark = "Behind Nakheel Mall",
                    landmarkAr = "خلف نخيل مول مباشرة",
                    deliveryNotes = "Call when in front of the gate",
                    deliveryNotesAr = "اتصل عند الوصول أمام البوابة الرئيسية"
                ),
                items = listOf(
                    OrderItem(
                        id = "it_21",
                        name = "Double Smokehouse Burger",
                        nameAr = "دبل سموك هاوس برجر مع لحم مقدد",
                        quantity = 1,
                        unitPrice = 42.0
                    ),
                    OrderItem(
                        id = "it_22",
                        name = "Curly Fries & Dip",
                        nameAr = "بطاطس حلزونية مع صوص شيدر",
                        quantity = 1,
                        unitPrice = 16.0
                    )
                ),
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
                    address = "Anas Bin Malik, Al Malqa",
                    addressAr = "طريق أنس بن مالك، حي الملقا",
                    latitude = 24.8020,
                    longitude = 46.6190,
                    pickupInstructions = "Medicines pre-bagged and sealed at cashier",
                    pickupInstructionsAr = "الأدوية مجهزة ومغلقة بشريط أمان لدى الكاشير"
                ),
                customer = CustomerUser(
                    id = "cust_302",
                    name = "سارة العتيبي",
                    phone = "+966 53 772 1199"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Al Malqa",
                    areaAr = "حي الملقا",
                    street = "Al Qairawan Road",
                    streetAr = "طريق القيروان، شارع ٤٠",
                    buildingNumber = "12",
                    floorNumber = "Ground",
                    apartmentNumber = "Villa 2",
                    landmark = "Opposite Grand Mosque",
                    landmarkAr = "مقابل الجامع الكبير في الحي",
                    deliveryNotes = "Leave at delivery parcel box next to gate",
                    deliveryNotesAr = "ضع الطلب في صندوق الطرود بجوار البوابة"
                ),
                items = listOf(
                    OrderItem(
                        id = "it_31",
                        name = "Prescription Package & Vitamins",
                        nameAr = "عبوة أدوية ومكملات فيتامينات مجمعة",
                        quantity = 1,
                        unitPrice = 115.0
                    )
                ),
                orderTotal = 115.0,
                driverEarnings = 29.00,
                currency = "SAR",
                status = DeliveryStatus.ASSIGNED,
                paymentMethod = PaymentMethod.PREPAID,
                distanceKm = 5.2,
                estimatedDurationMin = 19,
                createdAt = "12:24 PM"
            ),
            DeliveryOrder(
                id = "ord_avail_203",
                orderNumber = "ORD-9315",
                store = Store(
                    id = "store_4",
                    name = "Pizza Craft",
                    nameAr = "بيتزا كرافت الإيطالية",
                    category = "Pizza",
                    categoryAr = "بيتزا ومعجنات إيطالية",
                    phone = "+966 11 777 3322",
                    address = "Prince Turki St, Hittin",
                    addressAr = "شارع الأمير تركي الأول، حي حطين",
                    latitude = 24.7640,
                    longitude = 46.6020,
                    pickupInstructions = "Keep pizza box strictly level in insulated thermal bag",
                    pickupInstructionsAr = "يرجى الحفاظ على الصندوق مستوياً في الحقيبة الحرارية"
                ),
                customer = CustomerUser(
                    id = "cust_303",
                    name = "خالد الشهري",
                    phone = "+966 59 110 8844"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Hittin",
                    areaAr = "حي حطين الدبلوماسي",
                    street = "Al Bujairi Walk",
                    streetAr = "ممشى البجيري السكني",
                    buildingNumber = "4",
                    floorNumber = "3",
                    apartmentNumber = "302",
                    landmark = "Next to Riyadh Season Blvd",
                    landmarkAr = "بجوار بوليفارد سيتي",
                    deliveryNotes = "Ring bell on intercom #302",
                    deliveryNotesAr = "اضغط على جرس الإنتركوم رقم ٣٠٢"
                ),
                items = listOf(
                    OrderItem(
                        id = "it_41",
                        name = "Truffle Mushroom Pizza",
                        nameAr = "بيتزا كمأة ومشروم نابولية أصلية",
                        quantity = 1,
                        unitPrice = 64.0
                    ),
                    OrderItem(
                        id = "it_42",
                        name = "Garlic Parmesan Dough Balls",
                        nameAr = "كرات العجين بالثوم والبارميزان",
                        quantity = 1,
                        unitPrice = 22.0
                    )
                ),
                orderTotal = 86.0,
                driverEarnings = 27.00,
                currency = "SAR",
                status = DeliveryStatus.ASSIGNED,
                paymentMethod = PaymentMethod.PREPAID,
                distanceKm = 4.1,
                estimatedDurationMin = 16,
                createdAt = "12:28 PM"
            )
        )
    }

    private fun createInitialHistoryOrders(): List<DeliveryOrder> {
        return listOf(
            DeliveryOrder(
                id = "ord_hist_1",
                orderNumber = "ORD-8390",
                store = Store(
                    id = "store_h1",
                    name = "Starbucks Reserve",
                    nameAr = "ستاربكس ريزيرف",
                    category = "Cafe",
                    categoryAr = "مقهى",
                    phone = "+966 11 412 0000",
                    address = "Kingdom Tower, Al Olaya",
                    addressAr = "برج المملكة، حي العليا",
                    latitude = 24.7110,
                    longitude = 46.6740
                ),
                customer = CustomerUser(
                    id = "cust_h1",
                    name = "فيصل الدوسري",
                    phone = "+966 50 123 9988"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Al Olaya",
                    areaAr = "حي العليا",
                    street = "Olaya Commercial Strip",
                    streetAr = "شارع العليا العام",
                    buildingNumber = "200",
                    floorNumber = "14",
                    apartmentNumber = "Office 1402"
                ),
                items = listOf(
                    OrderItem("hi_1", "Iced Spanish Latte", "سبنش لاتيه مثلج", 2, 24.0)
                ),
                orderTotal = 48.0,
                driverEarnings = 21.00,
                currency = "SAR",
                status = DeliveryStatus.DELIVERED,
                distanceKm = 2.1,
                estimatedDurationMin = 10,
                createdAt = "10:15 AM",
                deliveredAt = "10:35 AM"
            ),
            DeliveryOrder(
                id = "ord_hist_2",
                orderNumber = "ORD-8378",
                store = Store(
                    id = "store_h2",
                    name = "Al Baik",
                    nameAr = "البيك",
                    category = "Fast Food",
                    categoryAr = "وجبات سريعة",
                    phone = "+966 11 888 2222",
                    address = "Makkah Road, Al Rabwah",
                    addressAr = "طريق مكة، حي الربوة",
                    latitude = 24.6900,
                    longitude = 46.7400
                ),
                customer = CustomerUser(
                    id = "cust_h2",
                    name = "تركي العنزي",
                    phone = "+966 55 444 3322"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Al Malaz",
                    areaAr = "حي الملز",
                    street = "Salah Al Din Al Ayyubi St",
                    streetAr = "شارع صلاح الدين الأيوبي",
                    buildingNumber = "45",
                    floorNumber = "2",
                    apartmentNumber = "8"
                ),
                items = listOf(
                    OrderItem("hi_2", "4-Piece Chicken Meal", "وجبة دجاج ٤ قطع حراق", 2, 22.0)
                ),
                orderTotal = 44.0,
                driverEarnings = 25.50,
                currency = "SAR",
                status = DeliveryStatus.DELIVERED,
                distanceKm = 4.9,
                estimatedDurationMin = 20,
                createdAt = "11:00 AM",
                deliveredAt = "11:28 AM"
            ),
            DeliveryOrder(
                id = "ord_hist_3",
                orderNumber = "ORD-8361",
                store = Store(
                    id = "store_h3",
                    name = "Kudu",
                    nameAr = "كودو",
                    category = "Sandwiches",
                    categoryAr = "ساندوتشات",
                    phone = "+966 11 333 1111",
                    address = "King Abdulaziz Road",
                    addressAr = "طريق الملك عبدالعزيز",
                    latitude = 24.7200,
                    longitude = 46.6900
                ),
                customer = CustomerUser(
                    id = "cust_h3",
                    name = "نورة المطيري",
                    phone = "+966 56 999 1122"
                ),
                deliveryAddress = DeliveryAddress(
                    area = "Al Waha",
                    areaAr = "حي الواحة",
                    street = "Abu Bakr Al Siddiq Road",
                    streetAr = "طريق أبي بكر الصديق",
                    buildingNumber = "31",
                    floorNumber = "1",
                    apartmentNumber = "2"
                ),
                items = listOf(
                    OrderItem("hi_3", "Chicken Kudu Combo", "كومبو كودو دجاج كبير", 1, 35.0)
                ),
                orderTotal = 35.0,
                driverEarnings = 22.00,
                currency = "SAR",
                status = DeliveryStatus.DELIVERED,
                distanceKm = 3.2,
                estimatedDurationMin = 15,
                createdAt = "11:45 AM",
                deliveredAt = "12:05 PM"
            )
        )
    }
}
