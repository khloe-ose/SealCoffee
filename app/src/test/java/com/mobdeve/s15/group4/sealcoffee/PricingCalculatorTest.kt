package com.mobdeve.s15.group4.sealcoffee

import com.mobdeve.s15.group4.sealcoffee.domain.PricingCalculator
import com.mobdeve.s15.group4.sealcoffee.domain.ProductOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {
    @Test
    fun drinkPrice_includesSizeAddOnsAndQuantityExactly() {
        val unit = PricingCalculator.unitPriceCentavos(
            basePriceCentavos = 16_500,
            category = "Coffee",
            size = ProductOptions.SIZE_LARGE,
            addOns = listOf(
                ProductOptions.ADD_ON_EXTRA_SHOT,
                ProductOptions.ADD_ON_OAT_MILK,
                ProductOptions.ADD_ON_CARAMEL
            )
        )

        assertEquals(25_500, unit)
        assertEquals(51_000, PricingCalculator.lineTotalCentavos(unit, 2))
        assertEquals(60_500, PricingCalculator.orderTotalCentavos(listOf(51_000, 9_500)))
    }

    @Test
    fun food_ignoresDrinkOnlyConfiguration() {
        assertEquals(
            9_500,
            PricingCalculator.unitPriceCentavos(
                9_500,
                "Snacks",
                ProductOptions.SIZE_LARGE,
                listOf(ProductOptions.ADD_ON_OAT_MILK)
            )
        )
        assertEquals(ProductOptions.SIZE_SINGLE, PricingCalculator.normaliseSize("Snacks", "Large"))
    }

    @Test
    fun nonCoffee_rejectsEspressoShotButAllowsMilk() {
        assertEquals(
            21_000,
            PricingCalculator.unitPriceCentavos(
                18_000,
                "Non-Coffee",
                ProductOptions.SIZE_REGULAR,
                listOf(ProductOptions.ADD_ON_EXTRA_SHOT, ProductOptions.ADD_ON_OAT_MILK)
            )
        )
    }
}
