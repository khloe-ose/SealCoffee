package com.mobdeve.s15.group4.sealcoffee

import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {
    @Test
    fun preparationFlow_isCanonicalAndStopsAtCompleted() {
        assertEquals(OrderStatus.PREPARING, OrderStatus.PENDING.nextPreparationStatus())
        assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.PREPARING.nextPreparationStatus())
        assertEquals(OrderStatus.COMPLETED, OrderStatus.READY_FOR_PICKUP.nextPreparationStatus())
        assertEquals(OrderStatus.COMPLETED, OrderStatus.COMPLETED.nextPreparationStatus())
        assertEquals(OrderStatus.DELAYED, OrderStatus.DELAYED.nextPreparationStatus())
    }

    @Test
    fun storedStatuses_areCaseSafeAndCentralised() {
        assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.fromStorage("READY_FOR_PICKUP"))
        assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.fromStorage("ready for pickup"))
        assertTrue(OrderStatus.PENDING in OrderStatus.preparationFlow)
    }
}
