package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem

class ManageMenuActivity : AppCompatActivity() {
    private val menuItems = DummyData.menuItems.toMutableList()
    private val adapter = ManageMenuAdapter(
        onEdit = ::showEditDialog,
        onRemove = ::removeItem,
        onToggle = ::toggleAvailability
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_menu)

        findViewById<RecyclerView>(R.id.manageMenuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@ManageMenuActivity)
            adapter = this@ManageMenuActivity.adapter
        }

        findViewById<Button>(R.id.addMenuItemButton).setOnClickListener {
            showEditDialog(null)
        }

        refreshMenu()
    }

    private fun showEditDialog(item: MenuItem?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_menu_item_editor, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.menuEditorNameInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.menuEditorCategoryInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.menuEditorPriceInput)

        if (item != null) {
            nameInput.setText(item.name)
            categoryInput.setText(item.category)
            priceInput.setText(item.price.toInt().toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (item == null) "Add Menu Item" else "Edit Menu Item")
            .setView(dialogView)
            .setPositiveButton(if (item == null) "Add" else "Save") { _, _ ->
                if (item == null) {
                    menuItems.add(
                        MenuItem(
                            id = "menu_${System.currentTimeMillis()}",
                            name = nameInput.text.toString().ifBlank { "New Drink" },
                            category = categoryInput.text.toString().ifBlank { "Coffee" },
                            description = "Prototype menu item.",
                            price = priceInput.text.toString().toDoubleOrNull() ?: 120.0,
                            imageResId = R.drawable.img_custom
                        )
                    )
                    Toast.makeText(this, "Menu item added", Toast.LENGTH_SHORT).show()
                } else {
                    val index = menuItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        menuItems[index] = item.copy(
                            name = nameInput.text.toString().ifBlank { item.name },
                            category = categoryInput.text.toString().ifBlank { item.category },
                            price = priceInput.text.toString().toDoubleOrNull() ?: item.price
                        )
                        Toast.makeText(this, "Menu item updated", Toast.LENGTH_SHORT).show()
                    }
                }
                refreshMenu()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeItem(item: MenuItem) {
        menuItems.removeAll { it.id == item.id }
        Toast.makeText(this, "${item.name} removed", Toast.LENGTH_SHORT).show()
        refreshMenu()
    }

    private fun toggleAvailability(item: MenuItem) {
        val index = menuItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            menuItems[index] = item.copy(isAvailable = !item.isAvailable)
            Toast.makeText(this, "${item.name} availability changed", Toast.LENGTH_SHORT).show()
            refreshMenu()
        }
    }

    private fun refreshMenu() {
        adapter.submitItems(menuItems)
    }
}
