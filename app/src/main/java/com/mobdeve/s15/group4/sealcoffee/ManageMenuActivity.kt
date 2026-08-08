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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.math.RoundingMode

class ManageMenuActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var emptyText: TextView

    private val categories = listOf("Coffee", "Non-Coffee", "Pastries", "Others")

    private val adapter = ManageMenuAdapter(
        onEdit = ::showEditor,
        onRemove = ::confirmArchive,
        onToggle = ::toggleAvailability
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthNavigation.requireRole(this, "employee") { isAuthorized ->
            if (!isAuthorized) return@requireRole

            setContentView(R.layout.activity_manage_menu)

            db = FirebaseFirestore.getInstance()
            emptyText = findViewById(R.id.manageMenuEmptyText)

            findViewById<RecyclerView>(R.id.manageMenuRecyclerView).apply {
                layoutManager = LinearLayoutManager(this@ManageMenuActivity)
                adapter = this@ManageMenuActivity.adapter
            }

            findViewById<Button>(R.id.addMenuItemButton).setOnClickListener { showEditor(null) }

            fetchMenu()
        }
    }

    private fun fetchMenu() {
        db.collection("menu")
            .whereEqualTo("archived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load menu", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreMenuItem::class.java)?.copy(id = doc.id)
                    }.sortedBy { it.name }

                    adapter.submitList(items)
                    emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun showEditor(item: FirestoreMenuItem?) {
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
            categories
        )

        if (item != null) {
            nameInput.setText(item.name)
            categorySpinner.setSelection(categories.indexOf(item.category).coerceAtLeast(0))
            descriptionInput.setText(item.description)
            ingredientsInput.setText(item.ingredientsCsv)
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
                        .intValueExact()
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
                if (ingredientsInput.text.toString().split(",").none { it.isNotBlank() }) {
                    ingredientsInput.error = getString(R.string.enter_ingredients)
                    valid = false
                }
                if (cents == null || cents <= 0) {
                    priceInput.error = getString(R.string.enter_valid_price)
                    valid = false
                }

                if (!valid) return@setOnClickListener

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

                val ingredientsList = ingredientsInput.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                val newData = mapOf(
                    "name" to nameInput.text.toString().trim(),
                    "category" to categorySpinner.selectedItem.toString(),
                    "description" to descriptionInput.text.toString().trim(),
                    "ingredients" to ingredientsList,
                    "basePriceCentavos" to cents!!,
                    "available" to availabilityInput.isChecked,
                    "archived" to false
                )

                val docRef = if (item == null) {
                    db.collection("menu").document()
                } else {
                    db.collection("menu").document(item.id)
                }

                docRef.set(newData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@ManageMenuActivity,
                            if (item == null) R.string.menu_item_added else R.string.menu_item_updated,
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ManageMenuActivity, e.message ?: "Failed", Toast.LENGTH_LONG).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
            }
        }
        dialog.show()
    }

    private fun confirmArchive(item: FirestoreMenuItem) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_menu_item)
            .setMessage(getString(R.string.remove_menu_item_message, item.name))
            .setPositiveButton(R.string.remove) { _, _ ->
                db.collection("menu").document(item.id)
                    .update("archived", true)
                    .addOnSuccessListener {
                        Toast.makeText(this, R.string.menu_item_removed, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, R.string.menu_update_failed, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun toggleAvailability(item: FirestoreMenuItem) {
        db.collection("menu").document(item.id)
            .update("available", !item.available)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.availability_updated, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.menu_update_failed, Toast.LENGTH_SHORT).show()
            }
    }
}