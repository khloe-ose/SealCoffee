package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.Order

class EmployeeOrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<EmployeeOrderAdapter.EmployeeOrderViewHolder>() {
    private val orders = mutableListOf<Order>()

    fun submitOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun getOrderAt(position: Int): Order? {
        return orders.getOrNull(position)
    }

    fun updateOrder(position: Int, order: Order) {
        if (position !in orders.indices) return
        orders[position] = order
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employee_order, parent, false)
        return EmployeeOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeOrderViewHolder, position: Int) {
        holder.bind(orders[position], onOrderClick)
    }

    override fun getItemCount(): Int = orders.size

    class EmployeeOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdText = itemView.findViewById<TextView>(R.id.employeeOrderIdText)
        private val customerText = itemView.findViewById<TextView>(R.id.employeeCustomerNameText)
        private val timeText = itemView.findViewById<TextView>(R.id.employeeOrderTimeText)
        private val summaryText = itemView.findViewById<TextView>(R.id.employeeOrderSummaryText)
        private val totalText = itemView.findViewById<TextView>(R.id.employeeOrderTotalText)
        private val statusText = itemView.findViewById<TextView>(R.id.employeeOrderStatusText)

        fun bind(order: Order, onOrderClick: (Order) -> Unit) {
            orderIdText.text = order.id
            customerText.text = order.customerName
            timeText.text = order.placedAt
            summaryText.text = order.items.joinToString { "${it.quantity}x ${it.name}" }
            totalText.text = order.total.formatPrice()
            statusText.text = order.status
            statusText.setTextColor(itemView.context.getColor(order.status.statusColor()))
            itemView.setOnClickListener { onOrderClick(order) }
        }
    }
}

fun String.statusColor(): Int = when (this) {
    "Completed" -> R.color.seal_success
    "Delayed" -> R.color.seal_error
    "Ready for Pickup" -> R.color.seal_success
    "Preparing" -> R.color.seal_warning
    else -> R.color.seal_navy
}
