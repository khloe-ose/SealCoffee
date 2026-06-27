package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class CustomerOrdersActivity : AppCompatActivity() {

    private lateinit var adapter: EmployeeCustomerHistoryAdapter
    private lateinit var btnActive: Button
    private lateinit var btnPast: Button
    private var showActiveOrders: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_orders)

        btnActive = findViewById(R.id.customerActiveOrders)
        btnPast = findViewById(R.id.customerPastOrders)
        val recyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)

        adapter = EmployeeCustomerHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnActive.setOnClickListener {
            if (!showActiveOrders) {
                showActiveOrders = true
                updateFilterUI()
                displayOrders()
            }
        }

        btnPast.setOnClickListener {
            if (showActiveOrders) {
                showActiveOrders = false
                updateFilterUI()
                displayOrders()
            }
        }

        updateFilterUI()
        displayOrders()

        CustomerNavigation.bind(this, CustomerDestination.ORDERS)
    }

    private fun displayOrders() {

        val currentUserEmail = DummyData.profileData.email

        val allOrders = DummyData.customerOrders + DummyData.employeeOrders
        val userSpecificOrders = allOrders.filter { it.customerEmail.equals(currentUserEmail) }


        val filteredList = if (showActiveOrders) {
            userSpecificOrders.filter { !it.status.equals("Completed") }
        } else {
            userSpecificOrders.filter { it.status.equals("Completed") }
        }

        adapter.submitOrders(filteredList)
    }

    private fun updateButtonState(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_chip_selected)
            button.setTextColor(getColor(R.color.seal_navy))
        } else {
            button.setBackgroundResource(R.drawable.bg_chip)
            button.setTextColor(getColor(R.color.white))
        }
    }
    private fun updateFilterUI() {
        updateButtonState(btnActive, showActiveOrders)
        updateButtonState(btnPast, !showActiveOrders)
    }
}
