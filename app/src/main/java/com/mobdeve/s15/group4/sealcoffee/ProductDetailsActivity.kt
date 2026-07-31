package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import com.mobdeve.s15.group4.sealcoffee.data.CartConfiguration
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.domain.MenuCategory
import com.mobdeve.s15.group4.sealcoffee.domain.PricingCalculator
import com.mobdeve.s15.group4.sealcoffee.domain.ProductOptions
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch
import java.text.NumberFormat

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var menuItem: MenuItemEntity
    private lateinit var quantityText: TextView
    private lateinit var priceText: TextView
    private lateinit var actionButton: Button
    private var quantity = 1
    private var editCartItemId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_product_details)
        CustomerNavigation.bind(this, CustomerDestination.MENU)

        quantityText = findViewById(R.id.productQuantityText)
        priceText = findViewById(R.id.productPriceText)
        actionButton = findViewById<Button>(R.id.addToCartButton).apply { isEnabled = false }
        editCartItemId = intent.getLongExtra(EXTRA_CART_ITEM_ID, 0L)
        quantity = intent.getIntExtra(EXTRA_QUANTITY, 1).coerceAtLeast(1)

        bindQuantityControls()
        bindPriceListeners()
        lifecycleScope.launch {
            val itemId = intent.getLongExtra(EXTRA_MENU_ITEM_ID, 0L)
            val found = sealApp.repository.getMenuItem(itemId)
            if (found == null || found.archived) {
                Toast.makeText(this@ProductDetailsActivity, R.string.product_not_found, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            menuItem = found
            bindDetails()
            restoreConfiguration()
            bindAction()
            updatePrice()
        }
    }

    private fun bindDetails() {
        findViewById<ImageView>(R.id.productImagePlaceholder).apply {
            setImageResource(ImageCatalog.resourceFor(menuItem.imageKey))
            contentDescription = menuItem.name
        }
        findViewById<TextView>(R.id.productNameText).text = menuItem.name
        findViewById<TextView>(R.id.productCategoryText).text = menuItem.category
        findViewById<TextView>(R.id.productDescriptionText).text = menuItem.description
        findViewById<TextView>(R.id.productIngredientsText).text =
            StringListCodec.decode(menuItem.ingredientsCsv).joinToString("\n") {
                getString(R.string.bullet_item, it)
            }

        val category = MenuCategory.fromLabel(menuItem.category)
        val isDrink = category?.isDrink == true
        findViewById<View>(R.id.productSizeContainer).visibility = if (isDrink) View.VISIBLE else View.GONE
        findViewById<View>(R.id.productAddOnsContainer).visibility = if (isDrink) View.VISIBLE else View.GONE
        findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).visibility =
            if (category == MenuCategory.COFFEE) View.VISIBLE else View.GONE
        actionButton.isEnabled = menuItem.available
        actionButton.text = when {
            !menuItem.available -> getString(R.string.unavailable)
            editCartItemId > 0 -> getString(R.string.update_cart)
            else -> getString(R.string.add_to_cart)
        }
    }

    private fun restoreConfiguration() {
        quantityText.text = NumberFormat.getIntegerInstance().format(quantity)
        val size = intent.getStringExtra(EXTRA_SIZE) ?: ProductOptions.SIZE_REGULAR
        findViewById<RadioButton>(
            if (size == ProductOptions.SIZE_LARGE) R.id.productSizeLarge else R.id.productSizeRegular
        ).isChecked = true
        val addOns = intent.getStringArrayListExtra(EXTRA_ADD_ONS).orEmpty()
        findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked =
            ProductOptions.ADD_ON_EXTRA_SHOT in addOns
        findViewById<CheckBox>(R.id.addOnOatMilkCheckBox).isChecked =
            ProductOptions.ADD_ON_OAT_MILK in addOns
        findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked =
            ProductOptions.ADD_ON_CARAMEL in addOns
        findViewById<EditText>(R.id.productNotesInput).setText(intent.getStringExtra(EXTRA_NOTES).orEmpty())
    }

    private fun bindQuantityControls() {
        findViewById<ImageButton>(R.id.productDecreaseButton).setOnClickListener {
            if (quantity > 1) {
                quantity--
                updatePrice()
            }
        }
        findViewById<ImageButton>(R.id.productIncreaseButton).setOnClickListener {
            if (quantity < 99) {
                quantity++
                updatePrice()
            }
        }
    }

    private fun bindPriceListeners() {
        findViewById<RadioGroup>(R.id.productSizeGroup).setOnCheckedChangeListener { _, _ -> updatePrice() }
        listOf(
            findViewById<CheckBox>(R.id.addOnExtraShotCheckBox),
            findViewById<CheckBox>(R.id.addOnOatMilkCheckBox),
            findViewById<CheckBox>(R.id.addOnCaramelCheckBox)
        ).forEach { it.setOnCheckedChangeListener { _, _ -> updatePrice() } }
    }

    private fun bindAction() {
        actionButton.setOnClickListener {
            actionButton.isEnabled = false
            lifecycleScope.launch {
                val configuration = selectedConfiguration()
                val result = if (editCartItemId > 0) {
                    sealApp.repository.updateCartConfiguration(
                        sealApp.session.userId,
                        editCartItemId,
                        configuration
                    )
                } else {
                    sealApp.repository.addToCart(sealApp.session.userId, menuItem.id, configuration)
                }
                result.onSuccess {
                    if (editCartItemId > 0) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Snackbar.make(
                            findViewById(R.id.productDetailsRoot),
                            getString(R.string.added_to_cart, quantity, menuItem.name),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.view_cart) {
                            startActivity(Intent(this@ProductDetailsActivity, CartActivity::class.java))
                        }.show()
                        actionButton.isEnabled = menuItem.available
                    }
                }.onFailure {
                    Toast.makeText(this@ProductDetailsActivity, it.message, Toast.LENGTH_LONG).show()
                    actionButton.isEnabled = menuItem.available
                }
            }
        }
    }

    private fun selectedConfiguration(): CartConfiguration {
        val size = if (
            findViewById<View>(R.id.productSizeContainer).isVisible &&
            findViewById<RadioButton>(R.id.productSizeLarge).isChecked
        ) ProductOptions.SIZE_LARGE else ProductOptions.SIZE_REGULAR
        val addOns = buildList {
            if (findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isVisible &&
                findViewById<CheckBox>(R.id.addOnExtraShotCheckBox).isChecked
            ) add(ProductOptions.ADD_ON_EXTRA_SHOT)
            if (findViewById<CheckBox>(R.id.addOnOatMilkCheckBox).isChecked) add(ProductOptions.ADD_ON_OAT_MILK)
            if (findViewById<CheckBox>(R.id.addOnCaramelCheckBox).isChecked) add(ProductOptions.ADD_ON_CARAMEL)
        }
        return CartConfiguration(
            quantity = quantity,
            size = size,
            addOns = addOns,
            notes = findViewById<EditText>(R.id.productNotesInput).text.toString()
        )
    }

    private fun updatePrice() {
        quantityText.text = NumberFormat.getIntegerInstance().format(quantity)
        if (!::menuItem.isInitialized) return
        val configuration = selectedConfiguration()
        val unit = PricingCalculator.unitPriceCentavos(
            menuItem.basePriceCentavos,
            menuItem.category,
            configuration.size,
            configuration.addOns
        )
        priceText.text = PricingCalculator.lineTotalCentavos(unit, quantity).formatMoney()
    }

    companion object {
        const val EXTRA_MENU_ITEM_ID = "extra_menu_item_id"
        const val EXTRA_CART_ITEM_ID = "extra_cart_item_id"
        const val EXTRA_QUANTITY = "extra_quantity"
        const val EXTRA_SIZE = "extra_size"
        const val EXTRA_ADD_ONS = "extra_addons"
        const val EXTRA_NOTES = "extra_notes"
    }
}
