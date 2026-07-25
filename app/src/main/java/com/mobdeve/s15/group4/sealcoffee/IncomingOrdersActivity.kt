package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole

class IncomingOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.EMPLOYEE)) return
        setContentView(R.layout.activity_employee_order_list)

        EmployeeOrderListBinder.bind(
            activity = this,
            title = "Incoming Orders",
            subtitle = "Newly placed orders awaiting preparation",
            statuses = setOf(OrderStatus.PENDING)
        )
    }
}
