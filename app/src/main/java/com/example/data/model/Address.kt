package com.example.data.model

data class DeliveryAddress(
    val area: String,
    val areaAr: String,
    val street: String,
    val streetAr: String,
    val buildingNumber: String = "",
    val floorNumber: String = "",
    val apartmentNumber: String = "",
    val landmark: String = "",
    val landmarkAr: String = "",
    val deliveryNotes: String = "",
    val deliveryNotesAr: String = "",
    val latitude: Double = 24.7136,
    val longitude: Double = 46.6753
) {
    fun getDisplayArea(isArabic: Boolean): String = if (isArabic) areaAr else area
    
    fun getFullAddress(isArabic: Boolean): String {
        return if (isArabic) {
            val parts = mutableListOf(areaAr, streetAr)
            if (buildingNumber.isNotBlank()) parts.add("مبنى $buildingNumber")
            if (floorNumber.isNotBlank()) parts.add("طابق $floorNumber")
            if (apartmentNumber.isNotBlank()) parts.add("شقة $apartmentNumber")
            parts.joinToString("، ")
        } else {
            val parts = mutableListOf(street, area)
            if (buildingNumber.isNotBlank()) parts.add("Bldg $buildingNumber")
            if (floorNumber.isNotBlank()) parts.add("Floor $floorNumber")
            if (apartmentNumber.isNotBlank()) parts.add("Apt $apartmentNumber")
            parts.joinToString(", ")
        }
    }

    fun getNotes(isArabic: Boolean): String = if (isArabic) deliveryNotesAr else deliveryNotes
}
