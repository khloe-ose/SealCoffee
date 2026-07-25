package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

class EmployeeDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.EMPLOYEE)) return
        setContentView(R.layout.activity_employee_dashboard)
        bindNavigation()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeDashboardCounts().collect { counts ->
                    findViewById<TextView>(R.id.totalOrdersCountText).text =
                        getString(R.string.count_value, counts.totalCount)
                    findViewById<TextView>(R.id.activeOrdersCountText).text =
                        getString(R.string.count_value, counts.activeCount)
                    findViewById<TextView>(R.id.completedOrdersCountText).text =
                        getString(R.string.count_value, counts.completedCount)
                    findViewById<TextView>(R.id.delayedOrdersCountText).text =
                        getString(R.string.count_value, counts.delayedCount)
                }
            }
        }
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
        findViewById<Button>(R.id.customerHistoryButton).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            AuthNavigation.logout(this)
        }
    }
}
