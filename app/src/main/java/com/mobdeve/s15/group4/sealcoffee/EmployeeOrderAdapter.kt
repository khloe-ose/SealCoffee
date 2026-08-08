package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EmployeeOrderAdapter(
    private val onOrderClick: (Map<String, Any>) -> Unit
) : ListAdapter<Map<String, Any>, EmployeeOrderAdapter.EmployeeOrderViewHolder>(DiffCallback) {

    fun itemAt(position: Int): Map<String, Any>? = currentList.getOrNull(position)

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

        fun bind(order: Map<String, Any>, onOrderClick: (Map<String, Any>) -> Unit) {
            val orderNumber = order["orderNumber"] as? String ?: ""
            val customerName = order["customerName"] as? String ?: "Customer"
            val status = (order["status"] as? String ?: "PENDING").uppercase()
            val totalCentavos = (order["totalCentavos"] as? Number)?.toLong() ?: 0L

            val placedAtTime = order["placedAt"]
            val placedAtLong = when (placedAtTime) {
                is com.google.firebase.Timestamp -> placedAtTime.toDate().time
                is Number -> placedAtTime.toLong()
                else -> System.currentTimeMillis()
            }

            @Suppress("UNCHECKED_CAST")
            val rawItems = order["items"] as? List<Map<String, Any>> ?: emptyList()
            val summaryStr = rawItems.joinToString(separator = "\n") { map ->
                val name = map["name"] as? String ?: map["itemName"] as? String ?: map["itemNameSnapshot"] as? String ?: "Item"
                val qty = (map["quantity"] as? Number)?.toInt() ?: 1
                val size = map["size"] as? String ?: map["sizeSnapshot"] as? String
                val sizeText = if (!size.isNullOrBlank()) " ($size)" else ""
                "$qty× $name$sizeText"
            }

            orderIdText.text = orderNumber
            customerText.text = customerName
            timeText.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", placedAtLong)
            summaryText.text = summaryStr
            totalText.text = "₱%.2f".format(totalCentavos / 100.0)
            statusText.text = status

            when (status) {
                "COMPLETED" -> statusText.setTextColor(itemView.context.getColor(R.color.seal_success))
                "DELAYED" -> statusText.setTextColor(itemView.context.getColor(R.color.seal_error))
                else -> statusText.setTextColor(itemView.context.getColor(R.color.seal_navy))
            }

            itemView.setOnClickListener { onOrderClick(order) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>) =
            oldItem["orderNumber"] == newItem["orderNumber"]

        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>) =
            oldItem == newItem
    }
}