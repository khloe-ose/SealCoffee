package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat

class ProductDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var menuItemId: String
    private var cartItemId: String? = null

    private lateinit var quantityText: TextView
    private lateinit var priceText: TextView
    private lateinit var actionButton: Button

    private var quantity = 1
    private var basePriceCentavos = 0
    private var category = ""
    private var isAvailable = true
    private var itemName = ""
    private var imageKey = ""

    companion object {
        private const val MAX_ITEM_QUANTITY = 10
        private const val MAX_TOTAL_CART_ITEMS = 15

        const val EXTRA_MENU_ITEM_ID = "extra_menu_item_id"
        const val EXTRA_CART_ITEM_ID = "extra_cart_item_id"
        const val EXTRA_QUANTITY = "extra_quantity"
        const val EXTRA_SIZE = "extra_size"
        const val EXTRA_ADD_ONS = "extra_addons"
        const val EXTRA_NOTES = "extra_notes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        menuItemId = intent.getStringExtra(EXTRA_MENU_ITEM_ID) ?: ""
        cartItemId = intent.getStringExtra(EXTRA_CART_ITEM_ID)
        quantity = intent.getIntExtra(EXTRA_QUANTITY, 1).coerceAtLeast(1)

        quantityText = findViewById(R.id.productQuantityText)
        priceText = findViewById(R.id.productPriceText)
        actionButton = findViewById(R.id.addToCartButton)
        actionButton.isEnabled = false

        setupQuantityControls()
        setupListeners()
        fetchProductDetails()
    }

    private fun fetchProductDetails() {
        if (menuItemId.isEmpty()) {
            Toast.makeText(this, R.string.product_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val doc = db.collection("menu").document(menuItemId).get().await()
                if (!doc.exists()) {
                    Toast.makeText(this@ProductDetailsActivity, R.string.product_not_found, Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }

                itemName = doc.getString("name") ?: ""
                category = doc.getString("category") ?: ""
                val description = doc.getString("description") ?: ""

                val rawIngredients = doc.get("ingredients") as? List<*>
                val ingredientsList = rawIngredients?.filterIsInstance<String>() ?: emptyList()

                basePriceCentavos = when (val priceVal = doc.get("basePriceCentavos")) {
                    is Number -> priceVal.toInt()
                    else -> 0
                }

                isAvailable = doc.getBoolean("available") ?: true

                val rawImageKey = doc.getString("imageKey") ?: ""
                imageKey = rawImageKey.substringBeforeLast(".")

                bindDetails(description, ingredientsList)
                restoreConfigurationFromIntent()
                updatePrice()
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Permission or Load Error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun bindDetails(description: String, ingredients: List<String>) {
        findViewById<TextView>(R.id.productDetailsTitleText).text = getString(R.string.product_details)
        findViewById<TextView>(R.id.productNameText).text = itemName
        findViewById<TextView>(R.id.productCategoryText).text = category
        findViewById<TextView>(R.id.productDescriptionText).text = description
        findViewById<TextView>(R.id.productIngredientsText).text = ingredients.joinToString("\n") {
            getString(R.string.bullet_item, it)
        }

        findViewById<ImageView>(R.id.productImagePlaceholder).apply {
            val cleanKey = imageKey.substringBeforeLast(".").removePrefix("img_")
            load(ImageCatalog.urlFor(cleanKey)) {
                crossfade(true)
                placeholder(R.drawable.bg_image_placeholder)
                error(R.drawable.bg_image_placeholder)
            }
            contentDescription = itemName
        }

        val isDrink = category.equals("Coffee", true) || category.equals("Non-Coffee", true) || category.equals("Drinks", true)
        val isCoffee = category.equals("Coffee", true)

        findViewById<View>(R.id.productSizeContainer).visibility = if (isDrink) View.VISIBLE else View.GONE
        findViewById<View>(R.id.productAddOnsContainer).visibility = if (isDrink) View.VISIBLE else View.GONE
        findViewById<View>(R.id.coffeeAddOnsGroup).visibility = if (isCoffee) View.VISIBLE else View.GONE
        findViewById<View>(R.id.nonCoffeeAddOnsGroup).visibility = if (isDrink && !isCoffee) View.VISIBLE else View.GONE

        actionButton.isEnabled = isAvailable
        actionButton.text = when {
            !isAvailable -> getString(R.string.unavailable)
            cartItemId != null -> getString(R.string.update_cart)
            else -> getString(R.string.add_to_cart)
        }
    }

    private fun restoreConfigurationFromIntent() {
        quantityText.text = NumberFormat.getIntegerInstance().format(quantity)

        val size = intent.getStringExtra(EXTRA_SIZE) ?: "Regular"
        if (size.equals("Large", true)) {
            findViewById<RadioButton>(R.id.productSizeLarge).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.productSizeRegular).isChecked = true
        }

        val addOns = intent.getStringArrayListExtra(EXTRA_ADD_ONS) ?: emptyList()
        findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked = addOns.contains("Extra Shot")
        findViewById<CheckBox>(R.id.addOnOatMilkCoffeeCheckBox).isChecked = addOns.contains("Oat Milk")
        findViewById<CheckBox>(R.id.addOnOatMilkNonCoffeeCheckBox).isChecked = addOns.contains("Oat Milk")
        findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked = addOns.contains("Caramel Drizzle")
        findViewById<CheckBox>(R.id.addOnExtraMatchaCheckBox).isChecked = addOns.contains("Extra Matcha")

        findViewById<EditText>(R.id.productNotesInput).setText(intent.getStringExtra(EXTRA_NOTES) ?: "")
    }

    private fun setupQuantityControls() {
        findViewById<ImageButton>(R.id.productDecreaseButton).setOnClickListener {
            if (quantity > 1) {
                quantity--
                updatePrice()
            }
        }
        findViewById<ImageButton>(R.id.productIncreaseButton).setOnClickListener {
            if (quantity < MAX_ITEM_QUANTITY) {
                quantity++
                updatePrice()
            } else {
                Toast.makeText(this, "Maximum of $MAX_ITEM_QUANTITY units allowed per item.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        findViewById<RadioGroup>(R.id.productSizeGroup).setOnCheckedChangeListener { _, _ -> updatePrice() }
        val checkboxes = listOf(
            R.id.addOnExtraShotCheckBox,
            R.id.addOnOatMilkCoffeeCheckBox,
            R.id.addOnOatMilkNonCoffeeCheckBox,
            R.id.addOnCaramelCheckBox,
            R.id.addOnExtraMatchaCheckBox
        )
        checkboxes.forEach { id ->
            findViewById<CheckBox>(id).setOnCheckedChangeListener { _, _ -> updatePrice() }
        }

        actionButton.setOnClickListener {
            saveCartItem()
        }

        findViewById<ImageButton>(R.id.productBackButton).setOnClickListener {
            finish()
        }
    }

    private fun updatePrice() {
        quantityText.text = NumberFormat.getIntegerInstance().format(quantity)
        val unitPriceCentavos = calculateUnitPriceCentavos()
        val totalPriceCentavos = unitPriceCentavos * quantity
        priceText.text = totalPriceCentavos.formatMoney()
    }

    private fun calculateUnitPriceCentavos(): Int {
        var price = basePriceCentavos
        val isDrink = category.equals("Coffee", true) || category.equals("Non-Coffee", true) || category.equals("Drinks", true)

        if (isDrink && findViewById<RadioButton>(R.id.productSizeLarge).isChecked) {
            price += 2500 // ₱25.00
        }

        if (findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isVisible && findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked) price += 3500
        if (findViewById<CheckBox>(R.id.addOnOatMilkCoffeeCheckBox).isVisible && findViewById<CheckBox>(R.id.addOnOatMilkCoffeeCheckBox).isChecked) price += 4500
        if (findViewById<CheckBox>(R.id.addOnOatMilkNonCoffeeCheckBox).isVisible && findViewById<CheckBox>(R.id.addOnOatMilkNonCoffeeCheckBox).isChecked) price += 4500
        if (findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isVisible && findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked) price += 2000
        if (findViewById<CheckBox>(R.id.addOnExtraMatchaCheckBox).isVisible && findViewById<CheckBox>(R.id.addOnExtraMatchaCheckBox).isChecked) price += 3000

        return price
    }

    private fun saveCartItem() {
        AuthNavigation.requireAuthenticated(this) { isAuthenticated ->
            if (!isAuthenticated) return@requireAuthenticated

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show()
                return@requireAuthenticated
            }

            actionButton.isEnabled = false
            val isDrink = category.equals("Coffee", true) || category.equals("Non-Coffee", true) || category.equals("Drinks", true)
            val size = if (isDrink && findViewById<RadioButton>(R.id.productSizeLarge).isChecked) "Large" else "Regular"

            val addOns = mutableListOf<String>()
            if (isDrink) {
                if (findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked) addOns.add("Extra Shot")
                if (findViewById<CheckBox>(R.id.addOnOatMilkCoffeeCheckBox).isChecked || findViewById<CheckBox>(R.id.addOnOatMilkNonCoffeeCheckBox).isChecked) addOns.add("Oat Milk")
                if (findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked) addOns.add("Caramel Drizzle")
                if (findViewById<CheckBox>(R.id.addOnExtraMatchaCheckBox).isChecked) addOns.add("Extra Matcha")
            }

            val notes = findViewById<EditText>(R.id.productNotesInput).text.toString().trim()
            val unitPriceCentavos = calculateUnitPriceCentavos()

            lifecycleScope.launch {
                try {
                    val cartRef = db.collection("users").document(userId).collection("cart")
                    val existingCartSnapshot = cartRef.get().await()

                    var currentTotalCartCount = 0
                    for (doc in existingCartSnapshot.documents) {
                        if (cartItemId != null && doc.id == cartItemId) continue
                        val qty = (doc.getLong("quantity") ?: 1).toInt()
                        currentTotalCartCount += qty
                    }

                    if (currentTotalCartCount + quantity > MAX_TOTAL_CART_ITEMS) {
                        Toast.makeText(
                            this@ProductDetailsActivity,
                            "Cart limit reached! Standard orders max out at $MAX_TOTAL_CART_ITEMS items total.",
                            Toast.LENGTH_LONG
                        ).show()
                        actionButton.isEnabled = isAvailable
                        return@launch
                    }

                    val cartData = hashMapOf(
                        "userId" to userId,
                        "menuItemId" to menuItemId,
                        "name" to itemName,
                        "category" to category,
                        "imageKey" to imageKey,
                        "quantity" to quantity,
                        "size" to size,
                        "addOns" to addOns,
                        "notes" to notes,
                        "unitPriceCentavos" to unitPriceCentavos,
                        "totalPriceCentavos" to (unitPriceCentavos * quantity),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )

                    if (cartItemId != null) {
                        cartRef.document(cartItemId!!).set(cartData).await()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        cartRef.add(cartData).await()
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.added_to_cart, quantity, itemName),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.view_cart) {
                            val intent = Intent(this@ProductDetailsActivity, CartActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.show()
                        actionButton.isEnabled = isAvailable
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProductDetailsActivity, "Failed to save to cart: ${e.message}", Toast.LENGTH_LONG).show()
                    actionButton.isEnabled = isAvailable
                }
            }
        }
    }
}