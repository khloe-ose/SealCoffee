package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class OrderHistoryActivity : AppCompatActivity() {
    private val orderHistoryAdapter = OrderHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_order_history)

        findViewById<RecyclerView>(R.id.customerOrderHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderHistoryAdapter
        }

        orderHistoryAdapter.submitOrders(DummyData.customerOrders.filter { it.status == "Completed" })
    }
}
