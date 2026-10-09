package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API interface for driver interactions with the shared delivery platform backend.
 */
interface DriverApiService {

    /**
     * Fetch list of delivery orders.
     * Can be filtered by status: "AVAILABLE", "ASSIGNED", "ACTIVE", "HISTORY", etc.
     */
    @GET("api/driver/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null
    ): Response<List<OrderDto>>

    /**
     * Fetch single order details by id.
     */
    @GET("api/driver/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): Response<OrderDto>

    /**
     * Accept an available delivery offer.
     */
    @PUT("api/driver/orders/{id}/accept")
    suspend fun acceptOrder(
        @Path("id") id: String
    ): Response<OrderDto>

    /**
     * Transition the delivery status (e.g. ASSIGNED -> PICKED_UP -> ON_THE_WAY -> DELIVERED).
     */
    @PUT("api/driver/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderDto>

    /**
     * Update driver availability (AVAILABLE, BUSY, OFFLINE).
     */
    @PUT("api/driver/availability")
    suspend fun updateAvailability(
        @Body request: UpdateAvailabilityRequest
    ): Response<DriverProfileDto>

    /**
     * Retrieve driver profile.
     */
    @GET("api/driver/profile")
    suspend fun getDriverProfile(): Response<DriverProfileDto>
}
