package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class CustomerOrderHistoryActivity : AppCompatActivity() {
    private val adapter = EmployeeCustomerHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_order_history)

        findViewById<RecyclerView>(R.id.customerOrderHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerOrderHistoryActivity)
            adapter = this@CustomerOrderHistoryActivity.adapter
        }

        adapter.submitOrders(DummyData.customerOrders + DummyData.employeeOrders)
    }
}
