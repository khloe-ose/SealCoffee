package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.Order

class OrderHistoryAdapter : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {
    private val orders = mutableListOf<Order>()

    fun submitOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer_order_history_employee, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    class OrderHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idText = itemView.findViewById<TextView>(R.id.employeeHistoryOrderText)
        private val customerText = itemView.findViewById<TextView>(R.id.employeeHistoryCustomerText)
        private val itemsText = itemView.findViewById<TextView>(R.id.employeeHistoryItemsText)
        private val totalText = itemView.findViewById<TextView>(R.id.employeeHistoryTotalText)
        private val statusText = itemView.findViewById<TextView>(R.id.employeeHistoryStatusText)

        fun bind(order: Order) {
            idText.text = order.id
            customerText.text = order.customerName
            itemsText.text = order.items.joinToString { "${it.quantity}x ${it.name}" }
            totalText.text = order.total.formatPrice()
            statusText.text = order.status
        }
    }
}
