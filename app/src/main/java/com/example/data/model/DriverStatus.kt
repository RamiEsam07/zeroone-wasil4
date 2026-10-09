package com.example.data.model

enum class DriverStatus {
    AVAILABLE,
    BUSY,
    OFFLINE;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                AVAILABLE -> "متاح للطلبات"
                BUSY -> "مشغول بمهمة"
                OFFLINE -> "غير متصل (استراحة)"
            }
        } else {
            when (this) {
                AVAILABLE -> "Available"
                BUSY -> "Busy"
                OFFLINE -> "Offline"
            }
        }
    }
}
