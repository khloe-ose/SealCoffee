package com.mobdeve.s15.group4.sealcoffee.domain

object PricingCalculator {
    const val LARGE_SURCHARGE_CENTAVOS = 2_500L

    private val addOnPrices = mapOf(
        ProductOptions.ADD_ON_EXTRA_SHOT to 2_000L,
        ProductOptions.ADD_ON_OAT_MILK to 3_000L,
        ProductOptions.ADD_ON_CARAMEL to 1_500L
    )

    fun normaliseSize(category: String, size: String): String {
        val isDrink = MenuCategory.fromLabel(category)?.isDrink == true
        if (!isDrink) return ProductOptions.SIZE_SINGLE
        return if (size == ProductOptions.SIZE_LARGE) ProductOptions.SIZE_LARGE else ProductOptions.SIZE_REGULAR
    }

    fun normaliseAddOns(category: String, addOns: Collection<String>): List<String> {
        val parsedCategory = MenuCategory.fromLabel(category) ?: return emptyList()
        if (!parsedCategory.isDrink) return emptyList()
        return addOns.filter {
            it in addOnPrices && (it != ProductOptions.ADD_ON_EXTRA_SHOT || parsedCategory == MenuCategory.COFFEE)
        }.distinct().sorted()
    }

    fun unitPriceCentavos(
        basePriceCentavos: Long,
        category: String,
        size: String,
        addOns: Collection<String>
    ): Long {
        require(basePriceCentavos >= 0) { "Base price cannot be negative" }
        var total = basePriceCentavos
        if (normaliseSize(category, size) == ProductOptions.SIZE_LARGE) total += LARGE_SURCHARGE_CENTAVOS
        normaliseAddOns(category, addOns).forEach { total += addOnPrices.getValue(it) }
        return total
    }

    fun lineTotalCentavos(unitPriceCentavos: Long, quantity: Int): Long {
        require(unitPriceCentavos >= 0) { "Unit price cannot be negative" }
        require(quantity > 0) { "Quantity must be at least one" }
        return Math.multiplyExact(unitPriceCentavos, quantity.toLong())
    }

    fun orderTotalCentavos(lineTotals: Collection<Long>): Long {
        require(lineTotals.all { it >= 0 }) { "Line totals cannot be negative" }
        return lineTotals.fold(0L, Math::addExact)
    }
}
