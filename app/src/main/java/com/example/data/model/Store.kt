package com.example.data.model

data class Store(
    val id: String,
    val name: String,
    val nameAr: String,
    val category: String,
    val categoryAr: String,
    val phone: String,
    val address: String,
    val addressAr: String,
    val latitude: Double,
    val longitude: Double,
    val pickupInstructions: String = "",
    val pickupInstructionsAr: String = ""
) {
    fun getDisplayName(isArabic: Boolean): String = if (isArabic) nameAr else name
    fun getDisplayAddress(isArabic: Boolean): String = if (isArabic) addressAr else address
    fun getInstructions(isArabic: Boolean): String = if (isArabic) pickupInstructionsAr else pickupInstructions
}
