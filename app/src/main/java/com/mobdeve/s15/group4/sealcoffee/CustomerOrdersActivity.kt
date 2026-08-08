package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.Timestamp
class CustomerOrdersActivity : AppCompatActivity() {
    private val showActiveOrders = MutableStateFlow(true)
    private lateinit var filterMap: Map<Boolean, CardView>
    private var selectedFilterActive = true
    private lateinit var emptyText: TextView
    private val adapter = CustomerOrdersAdapter(::openOrder)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CustomerOrdersActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_orders)
        CustomerNavigation.bind(this, CustomerDestination.ORDERS)

        emptyText = findViewById(R.id.ordersEmptyText)

        findViewById<RecyclerView>(R.id.menuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerOrdersActivity)
            adapter = this@CustomerOrdersActivity.adapter
        }

        filterMap = mapOf(
            true to findViewById(R.id.cardCustomerActiveOrders),
            false to findViewById(R.id.cardCustomerPastOrders)
        )

        filterMap.forEach { (isActive, cardView) ->
            cardView.setOnClickListener {
                selectedFilterActive = isActive
                showActiveOrders.value = isActive
                updateFilterUI()
                fetchOrders(isActive)
            }
        }

        updateFilterUI()
        checkUserRoleAndLoadOrders()
    }

    private fun checkUserRoleAndLoadOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }

        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    validateRoleAndLoad(document.getString("role"))
                } else {
                    fallbackQueryUserRole(currentUser.email)
                }
            }
            .addOnFailureListener {
                fallbackQueryUserRole(currentUser.email)
            }
    }

    private fun fallbackQueryUserRole(email: String?) {
        if (email == null) {
            updateFilterUI()
            fetchOrders(selectedFilterActive)
            return
        }

        firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    validateRoleAndLoad(querySnapshot.documents[0].getString("role"))
                } else {
                    updateFilterUI()
                    fetchOrders(selectedFilterActive)
                }
            }
            .addOnFailureListener {
                updateFilterUI()
                fetchOrders(selectedFilterActive)
            }
    }

    private fun validateRoleAndLoad(role: String?) {
        val safeRole = role ?: "customer"
        if (safeRole.equals("CUSTOMER", ignoreCase = true) || safeRole.equals("customer", ignoreCase = true)) {
            updateFilterUI()
            fetchOrders(selectedFilterActive)
        } else {
            updateFilterUI()
            fetchOrders(selectedFilterActive)
        }
    }

    private fun fetchOrders(active: Boolean) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val email = currentUser.email

        firestore.collection("orders")
            .whereEqualTo("customerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed for orders by customerId.", error)
                    fetchOrdersFallback(uid, email, active)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreOrderModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                if (orders.isEmpty() && email != null) {
                    fetchOrdersFallback(uid, email, active)
                    return@addSnapshotListener
                }

                processAndDisplayOrders(orders, active)
            }
    }

    private fun fetchOrdersFallback(uid: String, email: String?, active: Boolean) {
        firestore.collection("orders")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val orders = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreOrderModel::class.java)?.copy(id = doc.id)
                }

                if (orders.isEmpty()) {
                    firestore.collection("orders").get().addOnSuccessListener { allSnapshot ->
                        val allOrders = allSnapshot.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreOrderModel::class.java)?.copy(id = doc.id)
                        }
                        processAndDisplayOrders(allOrders, active)
                    }
                } else {
                    processAndDisplayOrders(orders, active)
                }
            }
            .addOnFailureListener {
                adapter.submitList(emptyList())
                emptyText.visibility = View.VISIBLE
            }
    }

    private fun processAndDisplayOrders(allOrders: List<FirestoreOrderModel>, active: Boolean) {
        val filteredOrders = allOrders.filter { order ->
            val statusUpper = order.status.uppercase()
            if (active) {
                statusUpper in listOf("PENDING", "PREPARING", "READY_FOR_PICKUP", "DELAYED")
            } else {
                statusUpper == "COMPLETED"
            }
        }.sortedByDescending { order ->
            when (val time = order.placedAt) {
                is Timestamp -> time.toDate().time
                is Number -> time.toLong()
                else -> 0L
            }
        }

        adapter.submitList(filteredOrders)
        emptyText.text = getString(
            if (active) R.string.no_active_orders else R.string.no_past_orders
        )
        emptyText.visibility = if (filteredOrders.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openOrder(order: FirestoreOrderModel) {
        startActivity(
            Intent(this, CustomerOrderDetailsActivity::class.java)
                .putExtra(CustomerOrderDetailsActivity.EXTRA_ORDER_ID, order.id)
        )
    }

    private fun updateFilterUI() {
        filterMap.forEach { (isActive, cardView) ->
            val textView = cardView.getChildAt(0) as? TextView
            if (isActive == selectedFilterActive) {
                cardView.setCardBackgroundColor(getColor(R.color.seal_navy))
                textView?.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#E5E7EB"))
                textView?.setTextColor(Color.parseColor("#374151"))
            }
        }
    }
}