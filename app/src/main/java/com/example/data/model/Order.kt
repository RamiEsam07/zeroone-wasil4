package com.example.data.model

enum class DeliveryStatus {
    ASSIGNED,
    PICKED_UP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                ASSIGNED -> "تم التعيين (توجه للمتجر)"
                PICKED_UP -> "تم الاستلام من المتجر"
                ON_THE_WAY -> "في الطريق إلى العميل"
                DELIVERED -> "تم التسليم بنجاح"
                CANCELLED -> "ملغي"
            }
        } else {
            when (this) {
                ASSIGNED -> "Assigned (Go to Store)"
                PICKED_UP -> "Picked Up"
                ON_THE_WAY -> "On The Way to Customer"
                DELIVERED -> "Delivered"
                CANCELLED -> "Cancelled"
            }
        }
    }

    /**
     * Returns the next logical delivery state in the flow, or null if final state.
     */
    fun nextState(): DeliveryStatus? {
        return when (this) {
            ASSIGNED -> PICKED_UP
            PICKED_UP -> ON_THE_WAY
            ON_THE_WAY -> DELIVERED
            DELIVERED, CANCELLED -> null
        }
    }

    /**
     * Returns the large primary CTA label for this state.
     * Examples required by user:
     * - ASSIGNED: "استلمت الطلب"
     * - PICKED_UP: "بدأت التوصيل"
     * - ON_THE_WAY: "تم التسليم"
     */
    fun getPrimaryActionLabel(isArabic: Boolean): String? {
        return if (isArabic) {
            when (this) {
                ASSIGNED -> "استلمت الطلب"
                PICKED_UP -> "بدأت التوصيل"
                ON_THE_WAY -> "تم التسليم"
                DELIVERED, CANCELLED -> null
            }
        } else {
            when (this) {
                ASSIGNED -> "Confirm Picked Up"
                PICKED_UP -> "Start Delivery"
                ON_THE_WAY -> "Confirm Delivered"
                DELIVERED, CANCELLED -> null
            }
        }
    }

    /**
     * Subtitle or instruction for driver in active screen
     */
    fun getActionInstruction(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                ASSIGNED -> "توجه إلى المتجر وتأكد من استلام جميع الأصناف"
                PICKED_UP -> "الطلب في حقيبتك الآن، اضغط أدناه عند بدء التحرك"
                ON_THE_WAY -> "توجه إلى موقع العميل وسلم الطلب"
                DELIVERED -> "اكتمل التوصيل وتمت إضافة العمولة لحسابك"
                CANCELLED -> "تم إلغاء هذا الطلب"
            }
        } else {
            when (this) {
                ASSIGNED -> "Go to store and verify all items before pickup"
                PICKED_UP -> "Order is secured. Tap below when you start driving"
                ON_THE_WAY -> "Drive to customer location and hand over order"
                DELIVERED -> "Delivery completed! Payout added to your earnings"
                CANCELLED -> "This order has been cancelled"
            }
        }
    }

    val isActive: Boolean get() = this == ASSIGNED || this == PICKED_UP || this == ON_THE_WAY
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    PREPAID;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                CASH_ON_DELIVERY -> "تحصيل نقدي عند الاستلام"
                PREPAID -> "مدفوع مسبقاً (إلكتروني)"
            }
        } else {
            when (this) {
                CASH_ON_DELIVERY -> "Cash on Delivery"
                PREPAID -> "Prepaid (Online)"
            }
        }
    }
}

data class DeliveryOrder(
    val id: String,
    val orderNumber: String,
    val store: Store,
    val customer: CustomerUser,
    val deliveryAddress: DeliveryAddress,
    val items: List<OrderItem>,
    val orderTotal: Double,
    val driverEarnings: Double,
    val currency: String = "SAR",
    val status: DeliveryStatus,
    val paymentMethod: PaymentMethod = PaymentMethod.PREPAID,
    val distanceKm: Double,
    val estimatedDurationMin: Int,
    val createdAt: String,
    val assignedAt: String? = null,
    val pickedUpAt: String? = null,
    val deliveredAt: String? = null
) {
    val totalItemsCount: Int get() = items.sumOf { it.quantity }
}
