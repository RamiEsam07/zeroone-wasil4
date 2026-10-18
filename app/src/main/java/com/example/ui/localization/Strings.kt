package com.example.ui.localization

object DriverStrings {

    fun appName(ar: Boolean) = if (ar) "إكسبريس السائق" else "Driver Express"
    fun driverTag(ar: Boolean) = if (ar) "كابتن التوصيل" else "Delivery Captain"

    // Statuses
    fun statusAvailable(ar: Boolean) = if (ar) "متاح للطلبات" else "Available"
    fun statusBusy(ar: Boolean) = if (ar) "مشغول بمهمة" else "Busy on Delivery"
    fun statusOffline(ar: Boolean) = if (ar) "غير متصل (استراحة)" else "Offline"
    fun statusAssigned(ar: Boolean) = if (ar) "مهمة جديدة" else "New Mission"
    fun statusPickedUp(ar: Boolean) = if (ar) "تم الاستلام" else "Picked Up"
    fun statusOnTheWay(ar: Boolean) = if (ar) "في الطريق للعميل" else "On The Way"
    fun statusDelivered(ar: Boolean) = if (ar) "تم التسليم بنجاح" else "Delivered"

    // Primary driver actions requested
    fun actionAcceptDelivery(ar: Boolean) = if (ar) "استلام المهمة" else "Accept Delivery"
    fun actionPickedUp(ar: Boolean) = if (ar) "استلمت الطلب" else "Order Picked Up"
    fun actionStartDelivery(ar: Boolean) = if (ar) "بدأت التوصيل" else "Start Delivery"
    fun actionDelivered(ar: Boolean) = if (ar) "تم التسليم" else "Confirm Delivered"
    fun actionOpenLocation(ar: Boolean) = if (ar) "فتح الموقع" else "Open Location"
    fun actionOpenStoreLocation(ar: Boolean) = if (ar) "موقع المتجر" else "Store Map"
    fun actionOpenCustomerLocation(ar: Boolean) = if (ar) "موقع العميل" else "Customer Map"
    fun actionCallStore(ar: Boolean) = if (ar) "اتصال بالمتجر" else "Call Store"
    fun actionCallCustomer(ar: Boolean) = if (ar) "اتصال بالعميل" else "Call Customer"

    // Dashboard & Metrics
    fun todayEarnings(ar: Boolean) = if (ar) "أرباح اليوم" else "Today's Earnings"
    fun todayCompleted(ar: Boolean) = if (ar) "الطلبات المنجزة" else "Completed Orders"
    fun activeHours(ar: Boolean) = if (ar) "ساعات العمل" else "Active Hours"
    fun totalDistance(ar: Boolean) = if (ar) "المسافة الكلية" else "Total Distance"
    fun quickStatusControl(ar: Boolean) = if (ar) "حالة السائق" else "Driver Availability"
    fun activeDeliveryTitle(ar: Boolean) = if (ar) "الطلب النشط حالياً" else "Active Delivery Mission"
    fun availableOrdersTitle(ar: Boolean) = if (ar) "الطلبات المتاحة بالقرب منك" else "Available Delivery Requests"
    fun viewAll(ar: Boolean) = if (ar) "عرض الكل" else "View All"
    fun noActiveDelivery(ar: Boolean) = if (ar) "لا يوجد طلب نشط حالياً" else "No active delivery right now"
    fun noAvailableOrders(ar: Boolean) = if (ar) "لا توجد طلبات متاحة حالياً" else "No available orders right now"
    fun stayOnlineTip(ar: Boolean) = if (ar) "ابقَ متصلاً لاستلام طلبات جديدة فور توفرها" else "Stay available to receive incoming offers"

    // Delivery details & items
    fun pickupPoint(ar: Boolean) = if (ar) "نقطة الاستلام (المتجر)" else "Pickup (Store)"
    fun dropoffPoint(ar: Boolean) = if (ar) "نقطة التسليم (العميل)" else "Delivery (Customer)"
    fun orderNumberLabel(ar: Boolean) = if (ar) "رقم الطلب" else "Order #"
    fun customerNameLabel(ar: Boolean) = if (ar) "اسم العميل" else "Customer Name"
    fun storeNameLabel(ar: Boolean) = if (ar) "اسم المتجر" else "Store Name"
    fun itemsSummary(ar: Boolean) = if (ar) "محتويات الطلب" else "Order Summary"
    fun orderTotal(ar: Boolean) = if (ar) "إجمالي الفاتورة" else "Order Total"
    fun driverEarnings(ar: Boolean) = if (ar) "عمولتك من التوصيل" else "Your Earnings"
    fun specialNotes(ar: Boolean) = if (ar) "ملاحظات وتوجيهات" else "Special Notes"
    fun distance(ar: Boolean) = if (ar) "المسافة" else "Distance"
    fun estimatedTime(ar: Boolean) = if (ar) "الوقت التقريبي" else "Est. Duration"

    // Navigation & Tabs
    fun navHome(ar: Boolean) = if (ar) "الرئيسية" else "Home"
    fun navAvailable(ar: Boolean) = if (ar) "المتاحة" else "Available"
    fun navActive(ar: Boolean) = if (ar) "النشط" else "Active"
    fun navHistory(ar: Boolean) = if (ar) "السجل" else "History"
    fun navProfile(ar: Boolean) = if (ar) "حسابي" else "Profile"

    // Login screen
    fun loginTitle(ar: Boolean) = if (ar) "دخول الكابتن" else "Driver Sign In"
    fun loginSubtitle(ar: Boolean) = if (ar) "سجل دخولك لبدء استلام وتوصيل الطلبات" else "Log in to start taking delivery tasks"
    fun phoneLabel(ar: Boolean) = if (ar) "رقم الجوال" else "Mobile Number"
    fun vehicleSelectLabel(ar: Boolean) = if (ar) "نوع وسيلة التوصيل" else "Select Vehicle"
    fun startShiftBtn(ar: Boolean) = if (ar) "بدء الوردية والعمل" else "Start Shift & Go Online"
    fun quickDemoLogin(ar: Boolean) = if (ar) "دخول سريع للتجربة" else "Quick Demo Captain"

    // Cash collection notice
    fun cashToCollect(ar: Boolean) = if (ar) "تحصيل نقدي مطلوب من العميل" else "Cash to collect from customer"
    fun prepaidNotice(ar: Boolean) = if (ar) "مدفوع مسبقاً - لا تحصّل أي مبلغ" else "Prepaid online - Do not collect cash"

    // Confirmation & dialogs
    fun confirmDeliveryTitle(ar: Boolean) = if (ar) "تأكيد إتمام التسليم" else "Confirm Delivery Complete"
    fun confirmDeliveryMsg(ar: Boolean) = if (ar) "هل قمت بتسليم الطلب للعميل واستلام المبلغ (إن وجد)؟" else "Have you handed the order to the customer and collected cash if applicable?"
    fun cancelIssueBtn(ar: Boolean) = if (ar) "إرجاع الطلب / إبلاغ عن مشكلة" else "Release assignment / Report issue"
    fun confirmBtn(ar: Boolean) = if (ar) "تأكيد" else "Confirm"
    fun backBtn(ar: Boolean) = if (ar) "رجوع" else "Back"
    fun closeBtn(ar: Boolean) = if (ar) "إغلاق" else "Close"
    fun simulateNewOrder(ar: Boolean) = if (ar) "محاكاة طلب جديد" else "Simulate New Offer"
    fun languageToggle(ar: Boolean) = if (ar) "English" else "العربية"
    fun endShift(ar: Boolean) = if (ar) "إنهاء الوردية (تسجيل خروج)" else "End Shift (Sign Out)"
}
