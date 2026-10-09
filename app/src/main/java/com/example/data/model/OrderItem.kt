package com.example.data.model

data class OrderItem(
    val id: String,
    val name: String,
    val nameAr: String,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String = "",
    val notesAr: String = ""
) {
    fun getDisplayName(isArabic: Boolean): String = if (isArabic) nameAr else name
    fun getDisplayNotes(isArabic: Boolean): String = if (isArabic) notesAr else notes
    val totalPrice: Double get() = quantity * unitPrice
}
