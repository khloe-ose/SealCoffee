package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        CustomerNavigation.bind(this, CustomerDestination.ORDERS)

        showCurrentOrders()
        showCompletedOrders()
    }

    private fun showCurrentOrders() {
        val currentOrders = DummyData.employeeOrders.filter {
            it.status != "Completed"
        }

        // TODO: bind to your "Current Orders" UI section
        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Current Orders",
            subtitle = "Orders in progress",
            orders = currentOrders
        )
    }

    private fun showCompletedOrders() {
        val completedOrders = DummyData.employeeOrders.filter {
            it.status == "Completed"
        }

        // TODO: bind to your "Completed Orders" UI section
        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Completed Orders",
            subtitle = "Orders already served or claimed",
            orders = completedOrders
        )
    }
}