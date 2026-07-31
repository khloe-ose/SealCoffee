package com.mobdeve.s15.group4.sealcoffee.domain

enum class UserRole {
    CUSTOMER,
    EMPLOYEE;

    companion object {
        fun fromStorage(value: String?): UserRole? = entries.firstOrNull { it.name == value }
    }
}

enum class MenuCategory(val label: String, val isDrink: Boolean) {
    COFFEE("Coffee", true),
    NON_COFFEE("Non-Coffee", true),
    SNACKS("Snacks", false),
    DESSERTS("Desserts", false);

    companion object {
        val labels: List<String> = entries.map { it.label }

        fun fromLabel(value: String): MenuCategory? =
            entries.firstOrNull { it.label.equals(value.trim(), ignoreCase = true) }
    }
}

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    COMPLETED("Completed"),
    DELAYED("Delayed");

    fun nextPreparationStatus(): OrderStatus = when (this) {
        PENDING -> PREPARING
        PREPARING -> READY_FOR_PICKUP
        READY_FOR_PICKUP -> COMPLETED
        COMPLETED, DELAYED -> this
    }

    companion object {
        val active: Set<OrderStatus> = setOf(PENDING, PREPARING, READY_FOR_PICKUP, DELAYED)
        val preparationFlow: Set<OrderStatus> = setOf(PENDING, PREPARING, READY_FOR_PICKUP)
        val labels: List<String> = entries.map { it.label }

        fun fromStorage(value: String?): OrderStatus? =
            entries.firstOrNull { it.name == value || it.label.equals(value, ignoreCase = true) }
    }
}

object ProductOptions {
    const val SIZE_REGULAR = "Regular"
    const val SIZE_LARGE = "Large"
    const val SIZE_SINGLE = "Single"

    const val ADD_ON_EXTRA_SHOT = "Extra espresso shot"
    const val ADD_ON_OAT_MILK = "Oat milk"
    const val ADD_ON_CARAMEL = "Caramel drizzle"

    val drinkAddOns = listOf(ADD_ON_EXTRA_SHOT, ADD_ON_OAT_MILK, ADD_ON_CARAMEL)
}
