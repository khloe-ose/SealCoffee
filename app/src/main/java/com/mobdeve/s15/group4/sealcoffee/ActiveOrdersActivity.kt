package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class ActiveOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_order_list)

        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Active Orders",
            subtitle = "Pending, preparing, and ready orders",
            orders = DummyData.employeeOrders.filter { it.status in EmployeeDashboardActivity.activeStatuses }
        )
    }
}
