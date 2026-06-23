package com.mobdeve.s15.group4.sealcoffee.data

data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val ingredients: List<String> = emptyList(),
    val price: Double,
    val imageName: String,
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true
)
