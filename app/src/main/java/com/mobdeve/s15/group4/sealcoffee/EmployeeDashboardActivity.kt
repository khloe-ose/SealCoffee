package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class EmployeeDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_dashboard)

        val orders = DummyData.employeeOrders
        findViewById<TextView>(R.id.totalOrdersCountText).text = orders.size.toString()
        findViewById<TextView>(R.id.activeOrdersCountText).text =
            orders.count { it.status in activeStatuses }.toString()
        findViewById<TextView>(R.id.completedOrdersCountText).text =
            orders.count { it.status == "Completed" }.toString()
        findViewById<TextView>(R.id.delayedOrdersCountText).text =
            orders.count { it.status == "Delayed" }.toString()

        bindNavigation()
    }

    private fun bindNavigation() {
        findViewById<Button>(R.id.incomingOrdersButton).setOnClickListener {
            startActivity(Intent(this, IncomingOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.activeOrdersButton).setOnClickListener {
            startActivity(Intent(this, ActiveOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.completedOrdersButton).setOnClickListener {
            startActivity(Intent(this, CompletedOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.delayedOrdersButton).setOnClickListener {
            startActivity(Intent(this, DelayedOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.manageMenuButton).setOnClickListener {
            startActivity(Intent(this, ManageMenuActivity::class.java))
        }
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    companion object {
        val activeStatuses = setOf("Pending", "Preparing", "Ready for Pickup")
    }
}
