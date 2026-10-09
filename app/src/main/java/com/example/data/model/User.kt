package com.example.data.model

enum class VehicleType {
    MOTORCYCLE,
    CAR,
    BICYCLE,
    VAN;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                MOTORCYCLE -> "دراجة نارية 🛵"
                CAR -> "سيارة 🚗"
                BICYCLE -> "دراجة هوائية 🚲"
                VAN -> "شاحنة توصيل 🚐"
            }
        } else {
            when (this) {
                MOTORCYCLE -> "Motorcycle 🛵"
                CAR -> "Car 🚗"
                BICYCLE -> "Bicycle 🚲"
                VAN -> "Delivery Van 🚐"
            }
        }
    }
}

data class DriverUser(
    val id: String,
    val name: String,
    val nameAr: String,
    val phone: String,
    val rating: Float = 4.9f,
    val totalDeliveries: Int = 1248,
    val vehicleType: VehicleType = VehicleType.MOTORCYCLE,
    val vehiclePlate: String = "أ ب ج 4 5 8 2",
    val status: DriverStatus = DriverStatus.AVAILABLE,
    val shiftStartTime: String = "08:00 AM"
) {
    fun getDisplayName(isArabic: Boolean): String = if (isArabic) nameAr else name
}

data class CustomerUser(
    val id: String,
    val name: String,
    val phone: String
)
