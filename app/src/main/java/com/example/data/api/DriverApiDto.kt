package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "name_ar") val nameAr: String? = null,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "notes_ar") val notesAr: String? = null
)

@JsonClass(generateAdapter = true)
data class StoreDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "name_ar") val nameAr: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "category_ar") val categoryAr: String? = null,
    @Json(name = "phone") val phone: String,
    @Json(name = "address") val address: String,
    @Json(name = "address_ar") val addressAr: String? = null,
    @Json(name = "latitude") val latitude: Double? = 0.0,
    @Json(name = "longitude") val longitude: Double? = 0.0,
    @Json(name = "pickup_instructions") val pickupInstructions: String? = null,
    @Json(name = "pickup_instructions_ar") val pickupInstructionsAr: String? = null
)

@JsonClass(generateAdapter = true)
data class CustomerDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class AddressDto(
    @Json(name = "area") val area: String,
    @Json(name = "area_ar") val areaAr: String? = null,
    @Json(name = "street") val street: String,
    @Json(name = "street_ar") val streetAr: String? = null,
    @Json(name = "building_number") val buildingNumber: String? = null,
    @Json(name = "floor_number") val floorNumber: String? = null,
    @Json(name = "apartment_number") val apartmentNumber: String? = null,
    @Json(name = "landmark") val landmark: String? = null,
    @Json(name = "landmark_ar") val landmarkAr: String? = null,
    @Json(name = "delivery_notes") val deliveryNotes: String? = null,
    @Json(name = "delivery_notes_ar") val deliveryNotesAr: String? = null,
    @Json(name = "latitude") val latitude: Double? = 0.0,
    @Json(name = "longitude") val longitude: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "store") val store: StoreDto,
    @Json(name = "customer") val customer: CustomerDto,
    @Json(name = "delivery_address") val deliveryAddress: AddressDto,
    @Json(name = "items") val items: List<OrderItemDto>,
    @Json(name = "order_total") val orderTotal: Double,
    @Json(name = "driver_earnings") val driverEarnings: Double,
    @Json(name = "currency") val currency: String? = "SAR",
    @Json(name = "status") val status: String,
    @Json(name = "payment_method") val paymentMethod: String? = "PREPAID",
    @Json(name = "distance_km") val distanceKm: Double? = 0.0,
    @Json(name = "estimated_duration_min") val estimatedDurationMin: Int? = 15,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "assigned_at") val assignedAt: String? = null,
    @Json(name = "picked_up_at") val pickedUpAt: String? = null,
    @Json(name = "delivered_at") val deliveredAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateOrderStatusRequest(
    @Json(name = "status") val status: String,
    @Json(name = "note") val note: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateAvailabilityRequest(
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class DriverProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "name_ar") val nameAr: String? = null,
    @Json(name = "phone") val phone: String,
    @Json(name = "rating") val rating: Float? = 4.9f,
    @Json(name = "total_deliveries") val totalDeliveries: Int? = 0,
    @Json(name = "vehicle_type") val vehicleType: String? = "MOTORCYCLE",
    @Json(name = "vehicle_plate") val vehiclePlate: String? = null,
    @Json(name = "status") val status: String? = "AVAILABLE"
)
