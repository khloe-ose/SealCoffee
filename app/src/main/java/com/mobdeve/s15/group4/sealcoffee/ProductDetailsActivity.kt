package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem

class ProductDetailsActivity : AppCompatActivity() {


    private lateinit var menuItem: MenuItem
    private lateinit var quantityText: TextView
    private lateinit var priceText: TextView
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        CustomerNavigation.bind(this, CustomerDestination.MENU)

        val itemId = intent.getStringExtra(EXTRA_MENU_ITEM_ID)
        menuItem = DummyData.menuItems.firstOrNull { it.id == itemId } ?: DummyData.menuItems.first()

        quantityText = findViewById(R.id.productQuantityText)
        priceText = findViewById(R.id.productPriceText)

        bindDetails()
        bindQuantityControls()
        bindAddToCart()
    }

    private fun bindDetails() {
        val imagePlaceholder = findViewById<ImageView>(R.id.productImagePlaceholder)
        imagePlaceholder.setImageResource(menuItem.imageResId)
        findViewById<TextView>(R.id.productNameText).text = menuItem.name
        findViewById<TextView>(R.id.productCategoryText).text = menuItem.category
        findViewById<TextView>(R.id.productDescriptionText).text = menuItem.description
        findViewById<TextView>(R.id.productIngredientsText).text =
            menuItem.ingredients.joinToString(separator = "\n") { "- $it" }

        val addToCartButton = findViewById<Button>(R.id.addToCartButton)
        if (!menuItem.isAvailable) {
            addToCartButton.isEnabled = false
            addToCartButton.text = "Unavailable"
        }

        updatePrice()
    }

    private fun bindQuantityControls() {
        findViewById<Button>(R.id.productDecreaseButton).setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                updatePrice()
            }
        }

        findViewById<Button>(R.id.productIncreaseButton).setOnClickListener {
            quantity += 1
            updatePrice()
        }
    }

    private fun bindAddToCart() {
        findViewById<Button>(R.id.addToCartButton).setOnClickListener {
            val addOns = listOf(
                findViewById<CheckBox>(R.id.addOnExtraShotCheckBox),
                findViewById<CheckBox>(R.id.addOnOatMilkCheckBox),
                findViewById<CheckBox>(R.id.addOnCaramelCheckBox)
            ).filter { it.isChecked }.joinToString { it.text }

            val addOnText = if (addOns.isBlank()) "no add-ons" else addOns
            Toast.makeText(
                this,
                "Added $quantity ${menuItem.name} with $addOnText",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updatePrice() {
        quantityText.text = quantity.toString()
        priceText.text = (menuItem.price * quantity).formatPrice()
    }

    companion object {
        const val EXTRA_MENU_ITEM_ID = "extra_menu_item_id"
    }
}
