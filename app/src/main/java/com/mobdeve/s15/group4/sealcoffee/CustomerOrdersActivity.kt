package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.local.OrderWithDetails
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerOrdersActivity : AppCompatActivity() {
    private val showActiveOrders = MutableStateFlow(true)
    private lateinit var activeButton: Button
    private lateinit var pastButton: Button
    private lateinit var emptyText: TextView
    private val adapter = CustomerOrdersAdapter(::openOrder)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_customer_orders)
        CustomerNavigation.bind(this, CustomerDestination.ORDERS)

        activeButton = findViewById(R.id.customerActiveOrders)
        pastButton = findViewById(R.id.customerPastOrders)
        emptyText = findViewById(R.id.ordersEmptyText)
        findViewById<RecyclerView>(R.id.menuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerOrdersActivity)
            adapter = this@CustomerOrdersActivity.adapter
        }
        activeButton.setOnClickListener { showActiveOrders.value = true }
        pastButton.setOnClickListener { showActiveOrders.value = false }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                showActiveOrders.flatMapLatest { active ->
                    updateFilterUi(active)
                    val statuses = if (active) OrderStatus.active else setOf(OrderStatus.COMPLETED)
                    sealApp.repository.observeCustomerOrders(sealApp.session.userId, statuses)
                }.collect { orders ->
                    adapter.submitList(orders)
                    emptyText.text = getString(
                        if (showActiveOrders.value) R.string.no_active_orders else R.string.no_past_orders
                    )
                    emptyText.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun openOrder(order: OrderWithDetails) {
        startActivity(
            Intent(this, EmployeeOrderDetailsActivity::class.java)
                .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, order.order.id)
        )
    }

    private fun updateFilterUi(active: Boolean) {
        updateButtonState(activeButton, active)
        updateButtonState(pastButton, !active)
    }

    private fun updateButtonState(button: Button, selected: Boolean) {
        button.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        button.setTextColor(getColor(if (selected) R.color.seal_navy else R.color.white))
    }
}
