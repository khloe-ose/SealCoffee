package com.mobdeve.s15.group4.sealcoffee.data

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val size: String,
    val temperature: String,
    val addOns: List<String> = emptyList(),
    val notes: String = ""
) {
    val lineTotal: Double
        get() = menuItem.price * quantity
}
