package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class EmployeeDashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflate the layout immediately so the screen appears instantly
        setContentView(R.layout.activity_employee_dashboard)

        // 2. Perform the role check safely in the background
        AuthNavigation.requireRole(this, "employee") { isAuthorized ->
            if (!isAuthorized) return@requireRole

            bindNavigation()

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    observeDashboardCounts().collect { counts ->
                        findViewById<TextView>(R.id.totalOrdersCountText)?.text =
                            getString(R.string.count_value, counts.totalCount)
                        findViewById<TextView>(R.id.activeOrdersCountText)?.text =
                            getString(R.string.count_value, counts.activeCount)
                        findViewById<TextView>(R.id.completedOrdersCountText)?.text =
                            getString(R.string.count_value, counts.completedCount)
                        findViewById<TextView>(R.id.delayedOrdersCountText)?.text =
                            getString(R.string.count_value, counts.delayedCount)
                    }
                }
            }
        }
    }

    private data class DashboardCounts(
        val totalCount: Int,
        val activeCount: Int,
        val completedCount: Int,
        val delayedCount: Int
    )

    private fun observeDashboardCounts(): Flow<DashboardCounts> = callbackFlow {
        val listener = db.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(DashboardCounts(0, 0, 0, 0))
                return@addSnapshotListener
            }

            var total = 0
            var active = 0
            var completed = 0
            var delayed = 0

            for (doc in snapshot.documents) {
                total++
                val status = doc.getString("status")?.uppercase() ?: ""
                when (status) {
                    "PENDING", "PREPARING", "READY_FOR_PICKUP" -> active++
                    "COMPLETED" -> completed++
                    "DELAYED" -> delayed++
                }
            }

            trySend(DashboardCounts(total, active, completed, delayed))
        }

        awaitClose { listener.remove() }
    }

    private fun bindNavigation() {
        findViewById<Button>(R.id.incomingOrdersButton)?.setOnClickListener {
            startActivity(Intent(this, IncomingOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.activeOrdersButton)?.setOnClickListener {
            startActivity(Intent(this, ActiveOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.completedOrdersButton)?.setOnClickListener {
            startActivity(Intent(this, CompletedOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.delayedOrdersButton)?.setOnClickListener {
            startActivity(Intent(this, DelayedOrdersActivity::class.java))
        }
        findViewById<Button>(R.id.manageMenuButton)?.setOnClickListener {
            startActivity(Intent(this, ManageMenuActivity::class.java))
        }
        findViewById<Button>(R.id.customerHistoryButton)?.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.logoutButton)?.setOnClickListener {
            AuthNavigation.confirmLogout(this)
        }
    }
}