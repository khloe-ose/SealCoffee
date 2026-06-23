package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class CompletedOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_order_list)

        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Completed Orders",
            subtitle = "Orders already claimed or served",
            orders = DummyData.employeeOrders.filter { it.status == "Completed" }
        )
    }
}
