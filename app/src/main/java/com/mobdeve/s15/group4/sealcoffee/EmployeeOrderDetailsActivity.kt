package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmployeeOrderDetailsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var statusGroup: RadioGroup
    private lateinit var applyButton: Button
    private var orderDocumentId: String? = null
    private var isEmployeeUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthNavigation.requireRole(this, "employee") { isAuthorized ->
            if (!isAuthorized) return@requireRole

            isEmployeeUser = true

            setContentView(R.layout.activity_employee_order_details)
            findViewById<ImageButton>(R.id.backButton).setOnClickListener {
                finish()
            }
            statusGroup = findViewById(R.id.employeeStatusRadioGroup)
            applyButton = findViewById(R.id.applyStatusButton)

            orderDocumentId = intent.getStringExtra(EXTRA_ORDER_NUMBER) ?: intent.getStringExtra(EXTRA_ORDER_ID)

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                finish()
                return@requireRole
            }

            statusGroup.visibility = View.VISIBLE
            applyButton.visibility = View.VISIBLE
            applyButton.setOnClickListener { applyStatus() }

            loadOrder()
        }
    }

    private fun loadOrder() {
        val targetId = orderDocumentId ?: return

        db.collection("orders").document(targetId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    validateAndRender(doc.id, doc.data)
                } else {
                    db.collection("orders").whereEqualTo("orderNumber", targetId).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val matchDoc = querySnapshot.documents[0]
                                validateAndRender(matchDoc.id, matchDoc.data)
                            } else {
                                Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_LONG).show()
                            finish()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load order", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateAndRender(docId: String, data: Map<String, Any>?) {
        if (data == null) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        orderDocumentId = docId
        render(data)
    }

    private fun render(order: Map<String, Any>) {
        val orderNum = order["orderNumber"] as? String ?: ""
        val customerName = order["customerName"] as? String ?: "Customer"
        val customerContact = order["customerContact"] as? String ?: ""
        val customerEmail = order["customerEmail"] as? String ?: ""
        val status = (order["status"] as? String ?: "PENDING").uppercase()
        val orderType = order["orderType"] as? String ?: "Pickup"
        val paymentLabel = order["paymentLabel"] as? String ?: "Cash"
        val subtotal = (order["subtotalCentavos"] as? Number)?.toLong() ?: 0L
        val total = (order["totalCentavos"] as? Number)?.toLong() ?: 0L

        val placedAtLong = when (val placedAtTime = order["placedAt"]) {
            is com.google.firebase.Timestamp -> placedAtTime.toDate().time
            is Number -> placedAtTime.toLong()
            else -> System.currentTimeMillis()
        }

        findViewById<TextView>(R.id.employeeDetailsOrderIdText).text = orderNum

        val customerSummary = if (customerContact.isNotBlank() || customerEmail.isNotBlank()) {
            getString(R.string.customer_contact_summary, customerName, customerContact, customerEmail)
        } else {
            customerName
        }
        findViewById<TextView>(R.id.employeeDetailsCustomerText).text = customerSummary

        findViewById<TextView>(R.id.employeeDetailsTimeText).text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", placedAtLong)
        findViewById<TextView>(R.id.employeeDetailsPaymentText).text = getString(R.string.payment_summary, orderType, paymentLabel)

        val statusLabel = when (status) {
            "PENDING" -> "Pending"
            "PREPARING" -> "Preparing"
            "READY_FOR_PICKUP" -> "Ready for Pickup"
            "COMPLETED" -> "Completed"
            "DELAYED" -> "Delayed"
            else -> status
        }
        findViewById<TextView>(R.id.employeeDetailsStatusText).text = getString(R.string.current_status, statusLabel)

        @Suppress("UNCHECKED_CAST")
        val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
        val itemsStr = items.joinToString("\n\n") { item ->
            buildString {
                val qty = (item["quantity"] as? Number)?.toInt() ?: 1
                val name = item["name"] as? String ?: item["itemName"] as? String ?: item["itemNameSnapshot"] as? String ?: "Item"
                val size = item["size"] as? String ?: item["sizeSnapshot"] as? String ?: ""
                append("• $name (×$qty)")
                if (size.isNotBlank()) {
                    append("\n  Size: $size")
                }
                @Suppress("UNCHECKED_CAST")
                val addOns = item["addOns"] as? List<String> ?: emptyList()
                if (addOns.isNotEmpty()) {
                    append("\n  Add-ons: ${addOns.joinToString()}")
                }
                val notes = item["notes"] as? String ?: item["notesSnapshot"] as? String ?: ""
                if (notes.isNotBlank()) {
                    append("\n  Note: \"$notes\"")
                }
                val lineTotal = (item["totalPriceCentavos"] as? Number)?.toLong() ?: (item["lineTotalCentavos"] as? Number)?.toLong() ?: 0L
                append("\n  Price: ₱%.2f".format(lineTotal / 100.0))
            }
        }

        findViewById<TextView>(R.id.employeeDetailsItemsText).text = itemsStr
        findViewById<TextView>(R.id.employeeDetailsSubtotalText).text = getString(R.string.subtotal_value, "₱%.2f".format(subtotal / 100.0))
        findViewById<TextView>(R.id.employeeDetailsTotalText).text = getString(R.string.total_value, "₱%.2f".format(total / 100.0))

        when (status) {
            "PENDING" -> statusGroup.check(R.id.statusPendingRadio)
            "PREPARING" -> statusGroup.check(R.id.statusPreparingRadio)
            "READY_FOR_PICKUP" -> statusGroup.check(R.id.statusReadyRadio)
            "COMPLETED" -> statusGroup.check(R.id.statusCompletedRadio)
            "DELAYED" -> statusGroup.check(R.id.statusDelayedRadio)
        }

        if (status == "COMPLETED") {
            statusGroup.visibility = View.GONE
            applyButton.visibility = View.GONE
        } else {
            statusGroup.visibility = View.VISIBLE
            applyButton.visibility = View.VISIBLE

            for (i in 0 until statusGroup.childCount) {
                statusGroup.getChildAt(i).isEnabled = true
            }
            applyButton.isEnabled = true
            applyButton.text = getString(R.string.apply_status)
        }
    }

    private fun applyStatus() {
        val targetId = orderDocumentId ?: return
        val newStatus = when (statusGroup.checkedRadioButtonId) {
            R.id.statusPreparingRadio -> "PREPARING"
            R.id.statusReadyRadio -> "READY_FOR_PICKUP"
            R.id.statusCompletedRadio -> "COMPLETED"
            R.id.statusDelayedRadio -> "DELAYED"
            else -> "PENDING"
        }

        applyButton.isEnabled = false
        db.collection("orders").document(targetId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.order_marked_status, targetId, newStatus), Toast.LENGTH_SHORT).show()
                loadOrder()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.status_update_failed, Toast.LENGTH_LONG).show()
                applyButton.isEnabled = true
            }
    }

    companion object {
        const val EXTRA_ORDER_NUMBER = "extra_order_number"
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}