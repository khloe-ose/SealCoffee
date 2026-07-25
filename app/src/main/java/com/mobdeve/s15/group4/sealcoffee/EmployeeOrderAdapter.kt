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

class EmployeeOrderAdapter(
    private val onOrderClick: (OrderWithDetails) -> Unit
) : ListAdapter<OrderWithDetails, EmployeeOrderAdapter.EmployeeOrderViewHolder>(DiffCallback) {

    fun itemAt(position: Int): OrderWithDetails? = currentList.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employee_order, parent, false)
        return EmployeeOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeOrderViewHolder, position: Int) {
        holder.bind(getItem(position), onOrderClick)
    }

    class EmployeeOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdText = itemView.findViewById<TextView>(R.id.employeeOrderIdText)
        private val customerText = itemView.findViewById<TextView>(R.id.employeeCustomerNameText)
        private val timeText = itemView.findViewById<TextView>(R.id.employeeOrderTimeText)
        private val summaryText = itemView.findViewById<TextView>(R.id.employeeOrderSummaryText)
        private val totalText = itemView.findViewById<TextView>(R.id.employeeOrderTotalText)
        private val statusText = itemView.findViewById<TextView>(R.id.employeeOrderStatusText)

        fun bind(details: OrderWithDetails, onOrderClick: (OrderWithDetails) -> Unit) {
            val status = OrderStatus.fromStorage(details.order.status) ?: OrderStatus.PENDING
            orderIdText.text = details.order.orderNumber
            customerText.text = itemView.context.getString(
                R.string.employee_customer_line,
                details.customer.fullName,
                details.customer.email
            )
            timeText.text = details.order.placedAt.formatDateTime()
            summaryText.text = details.items.joinToString { "${it.quantity}× ${it.itemNameSnapshot}" }
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
