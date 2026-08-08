package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CustomerOrderDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "EXTRA_ORDER_ID"
    }

    private lateinit var orderIdText: TextView
    private lateinit var orderTypeTextView: TextView
    private lateinit var dateText: TextView
    private lateinit var statusText: TextView
    private lateinit var statusDescText: TextView
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var totalText: TextView
    private lateinit var backButton: ImageButton

    private val firestore = FirebaseFirestore.getInstance()
    private var orderId: String? = null
    private val itemsAdapter = OrderItemsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_order_details)

        orderId = intent.getStringExtra(EXTRA_ORDER_ID)

        orderIdText = findViewById(R.id.customerDetailsOrderIdText)
        orderTypeTextView = findViewById(R.id.customerDetailsOrderTypeText)
        dateText = findViewById(R.id.customerDetailsDateText)
        statusText = findViewById(R.id.customerDetailsStatusText)
        statusDescText = findViewById(R.id.customerDetailsStatusDescriptionText)
        itemsRecyclerView = findViewById(R.id.customerDetailsItemsRecyclerView)
        totalText = findViewById(R.id.customerDetailsTotalText)
        backButton = findViewById(R.id.customerDetailsBackButton)

        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        itemsRecyclerView.adapter = itemsAdapter

        backButton.setOnClickListener { finish() }


        if (orderId != null) {
            fetchOrderDetails(orderId!!)
        }
    }

    private fun fetchOrderDetails(id: String) {
        firestore.collection("orders").document(id)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) {
                    return@addSnapshotListener
                }

                val order = document.toObject(FirestoreOrderModel::class.java)?.copy(id = document.id) ?: return@addSnapshotListener

                orderIdText.text = order.orderNumber
                orderTypeTextView.text = order.orderType
                dateText.text = FirestoreOrderModel.formatDate(order.placedAt)
                statusText.text = order.status

                val statusUpper = order.status.uppercase()
                val (descRes, colorRes) = when (statusUpper) {
                    "PENDING" -> Pair(R.string.status_pending_description, R.color.seal_navy)
                    "PREPARING" -> Pair(R.string.status_preparing_description, R.color.seal_navy)
                    "READY_FOR_PICKUP" -> Pair(R.string.status_ready_description, R.color.seal_success)
                    "DELAYED" -> Pair(R.string.status_delayed_description, R.color.seal_error)
                    else -> Pair(R.string.status_completed_description, R.color.seal_success)
                }

                statusText.setTextColor(getColor(colorRes))
                statusDescText.text = getString(descRes)

                itemsAdapter.submitList(order.items)
                totalText.text = FirestoreOrderModel.formatMoney(order.totalCentavos)
            }
    }

    // Standard RecyclerView Adapter optimized for order item lists
    private class OrderItemsAdapter : RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder>() {
        private var items: List<FirestoreOrderItem> = emptyList()

        fun submitList(newItems: List<FirestoreOrderItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(items[position], position == items.size - 1)
        }

        override fun getItemCount(): Int = items.size

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameText: TextView = itemView.findViewById(R.id.itemDetailNameText)
            private val sizeText: TextView = itemView.findViewById(R.id.itemDetailSizeText)
            private val addOnsText: TextView = itemView.findViewById(R.id.itemDetailAddOnsText)
            private val noteText: TextView = itemView.findViewById(R.id.itemDetailNoteText)
            private val priceText: TextView = itemView.findViewById(R.id.itemDetailPriceText)
            private val dividerView: View = itemView.findViewById(R.id.itemDetailDivider)

            fun bind(item: FirestoreOrderItem, isLast: Boolean) {
                // FIXED: using 'item.name' instead of 'itemNameSnapshot'
                val displayName = item.name.ifBlank { "Unknown Item" }
                nameText.text = "• $displayName  (×${item.quantity})"

                // Size
                if (!item.size.isNullOrBlank()) {
                    sizeText.visibility = View.VISIBLE
                    sizeText.text = "Size: ${item.size}"
                } else {
                    sizeText.visibility = View.GONE
                }

                // Add-ons
                if (!item.addOns.isNullOrEmpty()) {
                    addOnsText.visibility = View.VISIBLE
                    addOnsText.text = "Add-ons: ${item.addOns.joinToString(", ")}"
                } else {
                    addOnsText.visibility = View.GONE
                }

                // Notes
                if (!item.notes.isNullOrBlank()) {
                    noteText.visibility = View.VISIBLE
                    noteText.text = "Note: \"${item.notes}\""
                } else {
                    noteText.visibility = View.GONE
                }

                // Price - FIXED: using 'item.totalPriceCentavos' instead of 'lineTotalCentavos'
                priceText.text = "Price: ${FirestoreOrderModel.formatMoney(item.totalPriceCentavos)}"

                // Hide divider on the final element
                dividerView.visibility = if (isLast) View.GONE else View.VISIBLE
            }
        }
    }
}