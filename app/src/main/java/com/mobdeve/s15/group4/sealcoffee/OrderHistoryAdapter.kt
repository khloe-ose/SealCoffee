package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.local.OrderWithDetails
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus

class OrderHistoryAdapter(
    private val onOrderClick: (OrderWithDetails) -> Unit
) : ListAdapter<OrderWithDetails, OrderHistoryAdapter.ViewHolder>(DiffCallback) {
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

        fun bind(details: OrderWithDetails, onOrderClick: (OrderWithDetails) -> Unit) {
            val status = OrderStatus.fromStorage(details.order.status) ?: OrderStatus.PENDING
            idText.text = details.order.orderNumber
            customerText.text = itemView.context.getString(
                R.string.customer_history_line,
                details.customer.fullName,
                details.customer.email,
                details.order.placedAt.formatDateTime()
            )
            itemsText.text = details.items.joinToString { "${it.quantity}× ${it.itemNameSnapshot}" }
            totalText.text = details.order.totalCentavos.formatMoney()
            statusText.text = status.label
            statusText.setTextColor(itemView.context.getColor(status.statusColor()))
            itemView.setOnClickListener { onOrderClick(details) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OrderWithDetails>() {
        override fun areItemsTheSame(oldItem: OrderWithDetails, newItem: OrderWithDetails) =
            oldItem.order.id == newItem.order.id

        override fun areContentsTheSame(oldItem: OrderWithDetails, newItem: OrderWithDetails) =
            oldItem == newItem
    }
}
