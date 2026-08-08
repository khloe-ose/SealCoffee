package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class DelayedOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthNavigation.requireRole(this, "employee") { isAuthorized ->
            if (!isAuthorized) return@requireRole

            setContentView(R.layout.activity_employee_order_list)

            findViewById<ImageButton>(R.id.employeeOrderBackButton).setOnClickListener {
                finish()
            }

            EmployeeOrderListBinder.bind(
                activity = this,
                title = "Delayed Orders",
                subtitle = "Orders that need attention",
                statuses = setOf("DELAYED")
            )
        }
    }
}