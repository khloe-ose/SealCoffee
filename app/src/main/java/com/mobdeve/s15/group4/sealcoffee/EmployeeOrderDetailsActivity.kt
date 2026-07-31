package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.data.local.OrderWithDetails
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

/**
 * Shared order detail surface. Customers can only load their own orders; employee-only
 * status controls are hidden for customer sessions.
 */
class EmployeeOrderDetailsActivity : AppCompatActivity() {
    private var details: OrderWithDetails? = null
    private lateinit var statusGroup: RadioGroup
    private lateinit var applyButton: Button
    private val isEmployee: Boolean get() = sealApp.session.role == UserRole.EMPLOYEE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireAuthenticated(this)) return
        setContentView(R.layout.activity_employee_order_details)
        statusGroup = findViewById(R.id.employeeStatusRadioGroup)
        applyButton = findViewById(R.id.applyStatusButton)
        statusGroup.visibility = if (isEmployee) View.VISIBLE else View.GONE
        applyButton.visibility = if (isEmployee) View.VISIBLE else View.GONE
        applyButton.setOnClickListener { applyStatus() }
    }

    override fun onResume() {
        super.onResume()
        if (!::statusGroup.isInitialized) return
        loadOrder()
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            val orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0L)
            val loaded = sealApp.repository.getOrder(orderId)
            if (loaded == null) {
                Toast.makeText(this@EmployeeOrderDetailsActivity, R.string.order_not_found, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            if (
                sealApp.session.role == UserRole.CUSTOMER &&
                loaded.order.customerId != sealApp.session.userId
            ) {
                Toast.makeText(this@EmployeeOrderDetailsActivity, R.string.order_access_denied, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            details = loaded
            render(loaded)
        }
    }

    private fun render(details: OrderWithDetails) {
        val order = details.order
        val status = OrderStatus.fromStorage(order.status) ?: OrderStatus.PENDING
        findViewById<TextView>(R.id.employeeDetailsOrderIdText).text = order.orderNumber
        findViewById<TextView>(R.id.employeeDetailsCustomerText).text = getString(
            R.string.customer_contact_summary,
            details.customer.fullName,
            details.customer.contactNumber,
            details.customer.email
        )
        findViewById<TextView>(R.id.employeeDetailsTimeText).text = order.placedAt.formatDateTime()
        findViewById<TextView>(R.id.employeeDetailsPaymentText).text = getString(
            R.string.payment_summary,
            order.orderType,
            order.paymentLabel
        )
        findViewById<TextView>(R.id.employeeDetailsStatusText).apply {
            text = getString(R.string.current_status, status.label)
            setTextColor(getColor(status.statusColor()))
        }
        findViewById<TextView>(R.id.employeeDetailsItemsText).text =
            details.items.joinToString("\n\n") { item ->
                buildString {
                    append(
                        getString(
                            R.string.order_item_quantity_name_size,
                            item.quantity,
                            item.itemNameSnapshot,
                            item.sizeSnapshot
                        )
                    )
                    val addOns = StringListCodec.decode(item.addOnsSnapshotCsv)
                    if (addOns.isNotEmpty()) {
                        append(getString(R.string.order_add_ons_line, addOns.joinToString()))
                    }
                    if (item.notesSnapshot.isNotBlank()) {
                        append(getString(R.string.order_notes_line, item.notesSnapshot))
                    }
                    append(getString(R.string.order_line_amount, item.lineTotalCentavos.formatMoney()))
                }
            }
        findViewById<TextView>(R.id.employeeDetailsSubtotalText).text =
            getString(R.string.subtotal_value, order.subtotalCentavos.formatMoney())
        findViewById<TextView>(R.id.employeeDetailsTotalText).text =
            getString(R.string.total_value, order.totalCentavos.formatMoney())
        statusGroup.check(
            when (status) {
                OrderStatus.PENDING -> R.id.statusPendingRadio
                OrderStatus.PREPARING -> R.id.statusPreparingRadio
                OrderStatus.READY_FOR_PICKUP -> R.id.statusReadyRadio
                OrderStatus.COMPLETED -> R.id.statusCompletedRadio
                OrderStatus.DELAYED -> R.id.statusDelayedRadio
            }
        )
        val editable = isEmployee && status != OrderStatus.COMPLETED
        for (index in 0 until statusGroup.childCount) {
            statusGroup.getChildAt(index).isEnabled = editable
        }
        if (isEmployee) {
            applyButton.isEnabled = editable
            applyButton.text = getString(
                if (editable) R.string.apply_status else R.string.completed_order_final
            )
        }
    }

    private fun applyStatus() {
        val order = details ?: return
        val status = when (statusGroup.checkedRadioButtonId) {
            R.id.statusPreparingRadio -> OrderStatus.PREPARING
            R.id.statusReadyRadio -> OrderStatus.READY_FOR_PICKUP
            R.id.statusCompletedRadio -> OrderStatus.COMPLETED
            R.id.statusDelayedRadio -> OrderStatus.DELAYED
            else -> OrderStatus.PENDING
        }
        applyButton.isEnabled = false
        lifecycleScope.launch {
            if (sealApp.repository.updateOrderStatus(order.order.id, status)) {
                Toast.makeText(
                    this@EmployeeOrderDetailsActivity,
                    getString(R.string.order_marked_status, order.order.orderNumber, status.label),
                    Toast.LENGTH_SHORT
                ).show()
                loadOrder()
            } else {
                Toast.makeText(this@EmployeeOrderDetailsActivity, R.string.status_update_failed, Toast.LENGTH_LONG).show()
            }
            applyButton.isEnabled = true
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}
