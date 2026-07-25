package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.MenuEditorInput
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.domain.MenuCategory
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class ManageMenuActivity : AppCompatActivity() {
    private val adapter = ManageMenuAdapter(
        onEdit = ::showEditor,
        onRemove = ::confirmArchive,
        onToggle = ::toggleAvailability
    )
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.EMPLOYEE)) return
        setContentView(R.layout.activity_manage_menu)
        emptyText = findViewById(R.id.manageMenuEmptyText)
        findViewById<RecyclerView>(R.id.manageMenuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@ManageMenuActivity)
            adapter = this@ManageMenuActivity.adapter
        }
        findViewById<Button>(R.id.addMenuItemButton).setOnClickListener { showEditor(null) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeEmployeeMenu().collect {
                    adapter.submitList(it)
                    emptyText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showEditor(item: MenuItemEntity?) {
        val view = layoutInflater.inflate(R.layout.dialog_menu_item_editor, null)
        val nameInput = view.findViewById<EditText>(R.id.menuEditorNameInput)
        val categorySpinner = view.findViewById<Spinner>(R.id.menuEditorCategorySpinner)
        val descriptionInput = view.findViewById<EditText>(R.id.menuEditorDescriptionInput)
        val ingredientsInput = view.findViewById<EditText>(R.id.menuEditorIngredientsInput)
        val priceInput = view.findViewById<EditText>(R.id.menuEditorPriceInput)
        val availabilityInput = view.findViewById<CheckBox>(R.id.menuEditorAvailableCheckBox)
        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            MenuCategory.labels
        )
        if (item != null) {
            nameInput.setText(item.name)
            categorySpinner.setSelection(MenuCategory.labels.indexOf(item.category).coerceAtLeast(0))
            descriptionInput.setText(item.description)
            ingredientsInput.setText(StringListCodec.decode(item.ingredientsCsv).joinToString(", "))
            priceInput.setText(BigDecimal(item.basePriceCentavos).movePointLeft(2).toPlainString())
            availabilityInput.isChecked = item.available
        } else {
            availabilityInput.isChecked = true
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (item == null) R.string.add_menu_item else R.string.edit_menu_item)
            .setView(view)
            .setPositiveButton(if (item == null) R.string.add else R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                listOf(nameInput, descriptionInput, ingredientsInput, priceInput).forEach { input -> input.error = null }
                val cents = runCatching {
                    BigDecimal(priceInput.text.toString().trim())
                        .setScale(2, RoundingMode.UNNECESSARY)
                        .movePointRight(2)
                        .longValueExact()
                }.getOrNull()
                var valid = true
                if (nameInput.text.toString().trim().length < 2) {
                    nameInput.error = getString(R.string.enter_product_name)
                    valid = false
                }
                if (descriptionInput.text.toString().trim().length < 5) {
                    descriptionInput.error = getString(R.string.enter_product_description)
                    valid = false
                }
                if (ingredientsInput.text.toString().split(",").none { ingredient -> ingredient.isNotBlank() }) {
                    ingredientsInput.error = getString(R.string.enter_ingredients)
                    valid = false
                }
                if (cents == null || cents <= 0) {
                    priceInput.error = getString(R.string.enter_valid_price)
                    valid = false
                }
                if (!valid) return@setOnClickListener

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                lifecycleScope.launch {
                    sealApp.repository.saveMenuItem(
                        MenuEditorInput(
                            id = item?.id ?: 0,
                            name = nameInput.text.toString(),
                            category = categorySpinner.selectedItem.toString(),
                            description = descriptionInput.text.toString(),
                            ingredients = ingredientsInput.text.toString().split(",").map(String::trim),
                            basePriceCentavos = cents!!,
                            available = availabilityInput.isChecked
                        )
                    ).onSuccess {
                        Toast.makeText(
                            this@ManageMenuActivity,
                            if (item == null) R.string.menu_item_added else R.string.menu_item_updated,
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }.onFailure {
                        Toast.makeText(this@ManageMenuActivity, it.message, Toast.LENGTH_LONG).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        }
        dialog.show()
    }

    private fun confirmArchive(item: MenuItemEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_menu_item)
            .setMessage(getString(R.string.remove_menu_item_message, item.name))
            .setPositiveButton(R.string.remove) { _, _ ->
                lifecycleScope.launch {
                    val success = sealApp.repository.archiveMenuItem(item.id)
                    Toast.makeText(
                        this@ManageMenuActivity,
                        if (success) R.string.menu_item_removed else R.string.menu_update_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun toggleAvailability(item: MenuItemEntity) {
        lifecycleScope.launch {
            val success = sealApp.repository.setAvailability(item.id, !item.available)
            Toast.makeText(
                this@ManageMenuActivity,
                if (success) R.string.availability_updated else R.string.menu_update_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
