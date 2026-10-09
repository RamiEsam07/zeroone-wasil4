package com.example.data.model

data class TodayDriverStats(
    val completedDeliveriesCount: Int = 8,
    val totalEarnings: Double = 192.50,
    val currency: String = "SAR",
    val activeHours: Double = 4.5,
    val totalDistanceKm: Double = 34.2,
    val customerRating: Float = 4.95f
)
