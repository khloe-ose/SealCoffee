package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem
import androidx.core.view.isGone
import androidx.core.view.isVisible

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var menuItem: MenuItem
    private lateinit var quantityText: TextView
    private lateinit var priceText: TextView

    private var isEditMode = false
    private var cartItemId: String? = null
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        CustomerNavigation.bind(this, CustomerDestination.MENU)

        // Determine item configuration if triggered from standard menu or via editing layout configurations
        isEditMode = intent.getBooleanExtra("extra_edit_mode", false)

        val itemId = intent.getStringExtra(EXTRA_MENU_ITEM_ID)
        menuItem = DummyData.menuItems.firstOrNull { it.id == itemId } ?: DummyData.menuItems.first()

        quantityText = findViewById(R.id.productQuantityText)
        priceText = findViewById(R.id.productPriceText)

        bindDetails()
        bindQuantityControls()
        bindInteractiveListeners()

        // Setup editing defaults AFTER default layouts are inflated and details are prepared
        checkAndSetupEditMode()

        bindAddToCart()
    }

    private fun checkAndSetupEditMode() {
        if (!isEditMode) return

        cartItemId = intent.getStringExtra("extra_item_id")
        val savedQuantity = intent.getIntExtra("extra_quantity", 1)
        val savedSize = intent.getStringExtra("extra_size") ?: "Regular"
        val savedAddOns = intent.getStringArrayListExtra("extra_addons") ?: arrayListOf()

        quantity = savedQuantity
        quantityText.text = quantity.toString()

        if (savedSize == "Large") {
            findViewById<RadioButton>(R.id.productSizeLarge).isChecked = true
            findViewById<RadioButton>(R.id.productSizeRegular).isChecked = false
        } else {
            findViewById<RadioButton>(R.id.productSizeRegular).isChecked = true
            findViewById<RadioButton>(R.id.productSizeLarge).isChecked = false
        }

        findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked = savedAddOns.contains("Extra espresso shot")
        findViewById<CheckBox>(R.id.addOnOatMilkCheckBox).isChecked = savedAddOns.contains("Oat milk")
        findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked = savedAddOns.contains("Caramel drizzle")

        findViewById<Button>(R.id.addToCartButton).text = "Update Cart"

        updatePrice()
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
        val sizeContainer = findViewById<View>(R.id.productSizeContainer)
        val addOnsContainer = findViewById<View>(R.id.productAddOnsContainer)
        val extraShotBox = findViewById<CheckBox>(R.id.addOnExtraShotCheckBox)
        val category = menuItem.category

        when {
            category.contains("Snacks") || category.contains("Desserts") -> {
                sizeContainer.visibility = View.GONE
                addOnsContainer.visibility = View.GONE
            }
            category.contains("Non-Coffee") -> {
                sizeContainer.visibility = View.VISIBLE
                addOnsContainer.visibility = View.VISIBLE
                extraShotBox.visibility = View.GONE
            }
            else -> {
                sizeContainer.visibility = View.VISIBLE
                addOnsContainer.visibility = View.VISIBLE
                extraShotBox.visibility = View.VISIBLE
            }
        }

        updatePrice()
    }

    private fun bindQuantityControls() {
        findViewById<ImageButton>(R.id.productDecreaseButton).setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                updatePrice()
            }
        }

        findViewById<ImageButton>(R.id.productIncreaseButton).setOnClickListener {
            quantity += 1
            updatePrice()
        }
    }

    private fun bindAddToCart() {
        findViewById<Button>(R.id.addToCartButton).setOnClickListener {
            val sizeContainer = findViewById<View>(R.id.productSizeContainer)

            val selectedSize = if (sizeContainer.isGone) {
                "Regular"
            } else {
                val sizeGroup = findViewById<RadioGroup>(R.id.productSizeGroup)
                val selectedId = sizeGroup.checkedRadioButtonId
                val radioButton = findViewById<RadioButton>(selectedId)
                radioButton?.text?.toString()?.substringBefore("(")?.trim() ?: "Regular"
            }

            val newAddOnsList = mutableListOf<String>()
            if (findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked) newAddOnsList.add("Extra espresso shot")
            if (findViewById<CheckBox>(R.id.addOnOatMilkCheckBox).isChecked) newAddOnsList.add("Oat milk")
            if (findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked) newAddOnsList.add("Caramel drizzle")

            if (isEditMode) {
                val resultIntent = Intent().apply {
                    putExtra("extra_item_id", cartItemId)
                    putExtra("extra_size", selectedSize)
                    putExtra("extra_quantity", quantity)
                    putStringArrayListExtra("extra_addons", ArrayList(newAddOnsList))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                val isDessertOrSnack = menuItem.category == "Desserts" || menuItem.category == "Snacks"
                val addOnsString = newAddOnsList.joinToString { it }

                val addOnText = if (addOnsString.isBlank()) {
                    if (!isDessertOrSnack) "with no add-ons" else ""
                } else {
                    "with $addOnsString"
                }

                Toast.makeText(
                    this,
                    "Added $quantity ${menuItem.name} $addOnText".trim(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindInteractiveListeners() {
        findViewById<RadioGroup>(R.id.productSizeGroup).setOnCheckedChangeListener { _, _ ->
            updatePrice()
        }

        val checkBoxes = listOf(
            findViewById(R.id.addOnExtraShotCheckBox),
            findViewById(R.id.addOnOatMilkCheckBox),
            findViewById<CheckBox>(R.id.addOnCaramelCheckBox)
        )
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ -> updatePrice() }
        }
    }

    private fun updatePrice() {
        var baseUnitPrice = menuItem.price

        val sizeContainer = findViewById<View>(R.id.productSizeContainer)
        if (sizeContainer.isVisible) {
            val largeRadio = findViewById<RadioButton>(R.id.productSizeLarge)
            if (largeRadio.isChecked) {
                baseUnitPrice += 25.0
            }
        }

        val extraShotBox = findViewById<CheckBox>(R.id.addOnExtraShotCheckBox)
        val oatMilkBox = findViewById<CheckBox>(R.id.addOnOatMilkCheckBox)
        val caramelBox = findViewById<CheckBox>(R.id.addOnCaramelCheckBox)

        if (extraShotBox.isVisible && extraShotBox.isChecked) baseUnitPrice += 20.0
        if (oatMilkBox.isVisible && oatMilkBox.isChecked) baseUnitPrice += 30.0
        if (caramelBox.isVisible && caramelBox.isChecked) baseUnitPrice += 15.0

        quantityText.text = quantity.toString()
        priceText.text = (baseUnitPrice * quantity).formatPrice()
    }

    companion object {
        const val EXTRA_MENU_ITEM_ID = "extra_menu_item_id"
    }
}
