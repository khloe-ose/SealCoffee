package com.mobdeve.s15.group4.sealcoffee.data

data class Order(
    val id: String,
    val customerName: String,
    val customerEmail: String,
    val items: List<OrderItem>,
    val status: String,
    val orderType: String,
    val paymentMethod: String,
    val placedAt: String,
    val pickupTime: String,
    val serviceFee: Double = 0.0
) {
    val subtotal: Double
        get() = items.sumOf { it.lineTotal }

    val total: Double
        get() = subtotal + serviceFee
}
