package com.example

import com.example.data.model.DeliveryStatus
import com.example.data.model.DriverStatus
import com.example.data.model.VehicleType
import com.example.data.repository.DriverRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriverWorkflowTest {

    private lateinit var repository: DriverRepositoryImpl

    @Before
    fun setUp() {
        repository = DriverRepositoryImpl()
    }

    @Test
    fun testInitialState() {
        assertNotNull(repository.activeDelivery.value)
        assertEquals(DriverStatus.BUSY, repository.driverStatus.value)
        assertTrue(repository.availableOrders.value.isNotEmpty())
    }

    @Test
    fun testStatusTransitionsSequentialFlow() = runBlocking {
        val initialActive = repository.activeDelivery.value
        assertNotNull(initialActive)
        val orderId = initialActive!!.id

        // 1. Initial status is ASSIGNED
        assertEquals(DeliveryStatus.ASSIGNED, initialActive.status)
        assertEquals("استلمت الطلب", initialActive.status.getPrimaryActionLabel(true))

        // 2. Advance to PICKED_UP
        val pickedUpResult = repository.updateDeliveryStatus(orderId, DeliveryStatus.PICKED_UP)
        assertTrue(pickedUpResult.isSuccess)
        assertEquals(DeliveryStatus.PICKED_UP, repository.activeDelivery.value?.status)
        assertEquals("بدأت التوصيل", repository.activeDelivery.value?.status?.getPrimaryActionLabel(true))

        // 3. Advance to ON_THE_WAY
        val onTheWayResult = repository.updateDeliveryStatus(orderId, DeliveryStatus.ON_THE_WAY)
        assertTrue(onTheWayResult.isSuccess)
        assertEquals(DeliveryStatus.ON_THE_WAY, repository.activeDelivery.value?.status)
        assertEquals("تم التسليم", repository.activeDelivery.value?.status?.getPrimaryActionLabel(true))

        // 4. Advance to DELIVERED
        val initialCompletedCount = repository.todayStats.value.completedDeliveriesCount
        val initialEarnings = repository.todayStats.value.totalEarnings

        val deliveredResult = repository.updateDeliveryStatus(orderId, DeliveryStatus.DELIVERED)
        assertTrue(deliveredResult.isSuccess)

        // Active delivery should now be null
        assertNull(repository.activeDelivery.value)
        // Driver status should return to AVAILABLE
        assertEquals(DriverStatus.AVAILABLE, repository.driverStatus.value)
        // Earnings and count should increase
        assertEquals(initialCompletedCount + 1, repository.todayStats.value.completedDeliveriesCount)
        assertTrue(repository.todayStats.value.totalEarnings > initialEarnings)
        // Order should be in history
        assertTrue(repository.historyOrders.value.any { it.id == orderId })
    }

    @Test
    fun testAcceptAvailableDelivery() = runBlocking {
        // Complete current active delivery first
        val active = repository.activeDelivery.value!!
        repository.updateDeliveryStatus(active.id, DeliveryStatus.DELIVERED)
        assertNull(repository.activeDelivery.value)

        // Now pick first available order
        val available = repository.availableOrders.value.first()
        val acceptResult = repository.acceptDelivery(available.id)

        assertTrue(acceptResult.isSuccess)
        val newActive = repository.activeDelivery.value
        assertNotNull(newActive)
        assertEquals(available.id, newActive!!.id)
        assertEquals(DeliveryStatus.ASSIGNED, newActive.status)
        assertEquals(DriverStatus.BUSY, repository.driverStatus.value)
    }

    @Test
    fun testDriverAvailabilityControl() = runBlocking {
        repository.setDriverStatus(DriverStatus.OFFLINE)
        assertEquals(DriverStatus.OFFLINE, repository.driverStatus.value)

        repository.setDriverStatus(DriverStatus.AVAILABLE)
        assertEquals(DriverStatus.AVAILABLE, repository.driverStatus.value)

        repository.setDriverStatus(DriverStatus.BUSY)
        assertEquals(DriverStatus.BUSY, repository.driverStatus.value)
    }
}
