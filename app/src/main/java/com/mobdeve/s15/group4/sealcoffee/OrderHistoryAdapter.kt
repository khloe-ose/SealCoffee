package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirestoreOrder(
    val id: String = "",
    val orderNumber: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val itemsSummary: String = "",
    val totalCentavos: Long = 0L,
    val status: String = "PENDING",
    val placedAt: Long = 0L
)

class OrderHistoryAdapter(
    private val onOrderClick: (FirestoreOrder) -> Unit
) : ListAdapter<FirestoreOrder, OrderHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_customer_order_history_employee, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onOrderClick)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idText = itemView.findViewById<TextView>(R.id.employeeHistoryOrderText)
        private val customerText = itemView.findViewById<TextView>(R.id.employeeHistoryCustomerText)
        private val itemsText = itemView.findViewById<TextView>(R.id.employeeHistoryItemsText)
        private val totalText = itemView.findViewById<TextView>(R.id.employeeHistoryTotalText)
        private val statusText = itemView.findViewById<TextView>(R.id.employeeHistoryStatusText)

        fun bind(order: FirestoreOrder, onOrderClick: (FirestoreOrder) -> Unit) {
            val context = itemView.context
            idText.text = order.orderNumber

            val dateStr = runCatching {
                SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.placedAt))
            }.getOrDefault("")

            customerText.text = context.getString(
                R.string.customer_history_line,
                order.customerName,
                order.customerEmail,
                dateStr
            )

            itemsText.text = order.itemsSummary

            val formattedMoney = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                .format(order.totalCentavos / 100.0)
            totalText.text = formattedMoney

            statusText.text = order.status
            itemView.setOnClickListener { onOrderClick(order) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FirestoreOrder>() {
        override fun areItemsTheSame(oldItem: FirestoreOrder, newItem: FirestoreOrder) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FirestoreOrder, newItem: FirestoreOrder) =
            oldItem == newItem
    }
}