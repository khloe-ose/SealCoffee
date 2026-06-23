package com.mobdeve.s15.group4.sealcoffee.data

data class OrderItem(
    val id: String,
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val size: String,
    val temperature: String,
    val unitPrice: Double,
    val addOns: List<String> = emptyList(),
    val notes: String = ""
) {
    val lineTotal: Double
        get() = unitPrice * quantity
}
