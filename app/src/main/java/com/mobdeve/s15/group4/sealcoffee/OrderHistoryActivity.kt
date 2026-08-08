package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var adapter: OrderHistoryAdapter
    private lateinit var db: FirebaseFirestore
    private val allOrders = mutableListOf<FirestoreOrder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthNavigation.requireRole(this, "employee") { isAuthorized ->
            if (!isAuthorized) return@requireRole

            setContentView(R.layout.activity_customer_order_history)

            db = FirebaseFirestore.getInstance()

            findViewById<ImageButton>(R.id.customerOrderHistoryBackButton).setOnClickListener {
                finish()
            }

            val emptyText = findViewById<TextView>(R.id.customerHistoryEmptyText)
            val recyclerView = findViewById<RecyclerView>(R.id.customerOrderHistoryRecyclerView)
            val searchInput = findViewById<EditText>(R.id.customerHistorySearchInput)

            adapter = OrderHistoryAdapter { order ->
                val intent = Intent(this, EmployeeOrderDetailsActivity::class.java).apply {
                    putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, order.id)
                }
                startActivity(intent)
            }

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

            fetchOrders(emptyText)

            searchInput.doAfterTextChanged { text ->
                filterOrders(text?.toString().orEmpty(), emptyText)
            }
        }
    }

    private fun fetchOrders(emptyText: TextView) {
        db.collection("orders")
            .orderBy("placedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                allOrders.clear()
                for (doc in snapshot.documents) {
                    val order = doc.toObject(FirestoreOrder::class.java)?.copy(id = doc.id)
                    if (order != null) {
                        allOrders.add(order)
                    }
                }

                val currentQuery = findViewById<EditText>(R.id.customerHistorySearchInput).text?.toString().orEmpty()
                filterOrders(currentQuery, emptyText)
            }
    }

    private fun filterOrders(query: String, emptyText: TextView) {
        val filtered = if (query.isBlank()) {
            allOrders
        } else {
            allOrders.filter {
                it.orderNumber.contains(query, ignoreCase = true) ||
                        it.customerName.contains(query, ignoreCase = true) ||
                        it.customerEmail.contains(query, ignoreCase = true)
            }
        }

        adapter.submitList(filtered)
        emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}