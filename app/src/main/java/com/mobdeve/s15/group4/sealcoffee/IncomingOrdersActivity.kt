package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class IncomingOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_order_list)

        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Incoming Orders",
            subtitle = "New and in-progress orders",
            orders = DummyData.employeeOrders.filter { it.status in setOf("Pending", "Preparing") }
        )
    }
}
