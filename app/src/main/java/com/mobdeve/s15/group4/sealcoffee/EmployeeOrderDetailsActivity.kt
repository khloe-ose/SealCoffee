package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class EmployeeOrderDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_order_details)

        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
        val order = DummyData.employeeOrders.firstOrNull { it.id == orderId } ?: DummyData.employeeOrders.first()

        findViewById<TextView>(R.id.employeeDetailsOrderIdText).text = order.id
        findViewById<TextView>(R.id.employeeDetailsCustomerText).text =
            "${order.customerName}\n${order.customerEmail}"
        findViewById<TextView>(R.id.employeeDetailsTimeText).text = order.placedAt
        findViewById<TextView>(R.id.employeeDetailsPaymentText).text =
            "${order.orderType} / ${order.paymentMethod}"
        findViewById<TextView>(R.id.employeeDetailsItemsText).text = order.items.joinToString("\n\n") {
            buildString {
                append("${it.quantity}x ${it.name} (${it.size}, ${it.temperature})")
                append("\n")
                append(it.lineTotal.formatPrice())
                if (it.addOns.isNotEmpty()) append("\nAdd-ons: ${it.addOns.joinToString()}")
                if (it.notes.isNotBlank()) append("\nNotes: ${it.notes}")
            }
        }
        findViewById<TextView>(R.id.employeeDetailsTotalText).text = order.total.formatPrice()

        val statusGroup = findViewById<RadioGroup>(R.id.employeeStatusRadioGroup)
        statusGroup.check(
            when (order.status) {
                "Preparing" -> R.id.statusPreparingRadio
                "Ready for Pickup" -> R.id.statusReadyRadio
                "Completed" -> R.id.statusCompletedRadio
                "Delayed" -> R.id.statusDelayedRadio
                else -> R.id.statusPendingRadio
            }
        )

        findViewById<Button>(R.id.applyStatusButton).setOnClickListener {
            val status = when (statusGroup.checkedRadioButtonId) {
                R.id.statusPreparingRadio -> "Preparing"
                R.id.statusReadyRadio -> "Ready for Pickup"
                R.id.statusCompletedRadio -> "Completed"
                R.id.statusDelayedRadio -> "Delayed"
                else -> "Pending"
            }
            Toast.makeText(this, "${order.id} marked $status", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}
