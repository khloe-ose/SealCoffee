package com.mobdeve.s15.group4.sealcoffee
val FirestoreMenuItem.ingredientsCsv: String
    get() = ingredients.joinToString(", ")
data class FirestoreMenuItem(
    val id: String = "",
    val seedKey: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val basePriceCentavos: Int = 0,
    val imageKey: String = "",
    val featured: Boolean = false,
    val available: Boolean = true,
    val archived: Boolean = false
)