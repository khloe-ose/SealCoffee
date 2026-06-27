package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.Order

class CustomerOrdersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var orders: List<Order> = ArrayList()

    companion object {
        private const val TYPE_ACTIVE = 1
        private const val TYPE_PAST = 2
    }

    fun submitOrders(newList: List<Order>) {
        this.orders = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val order = orders[position]
        return if (order.status.equals("Completed", ignoreCase = true)) {
            TYPE_PAST
        } else {
            TYPE_ACTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ACTIVE) {
            val view = inflater.inflate(R.layout.item_current_order, parent, false)
            ActiveOrderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_order_history, parent, false)
            PastOrderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = orders[position]

        if (holder is ActiveOrderViewHolder) {
            holder.orderId.text = order.id
            holder.orderType.text = order.orderType
            holder.itemsList.text = order.items.joinToString { "${it.quantity}x ${it.name}" }
            holder.totalText.text = order.total.formatPrice()

            val context = holder.itemView.context
            when (order.status.lowercase()) {
                "pending" -> {
                    holder.statusTitle.text = "Pending"
                    holder.statusDesc.text = "Your order has been received."

                }
                "preparing" -> {
                    holder.statusTitle.text = "Preparing"
                    holder.statusDesc.text = "Our barista is working on your order."

                }
                "ready for pickup" -> {
                    holder.statusTitle.text = "Ready for Pickup"
                    holder.statusDesc.text = "Your drinks and snacks are ready at the counter."

                }
                "delayed" -> {
                    holder.statusTitle.text = "Delayed"
                    holder.statusDesc.text = "We will notify you if preparation takes longer than expected."

                }
            }

        } else if (holder is PastOrderViewHolder) {
            holder.orderId.text = order.id
            holder.statusText.text = order.status
            holder.dateText.text = order.placedAt
            holder.itemsText.text = order.items.joinToString { "${it.quantity}x ${it.name}" }
            holder.totalText.text = order.total.formatPrice()
        }
    }

    override fun getItemCount(): Int = orders.size

    class ActiveOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.currentOrderIdText)
        val orderType: TextView = itemView.findViewById(R.id.currentOrderTypeText)
        val statusTitle: TextView = itemView.findViewById(R.id.currentStatus)
        val statusDesc: TextView = itemView.findViewById(R.id.currentStatusDescriptionText)
        val itemsList: TextView = itemView.findViewById(R.id.currentItemsText)
        val totalText: TextView = itemView.findViewById(R.id.currentTotalText)
    }

    class PastOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.historyOrderIdText)
        val statusText: TextView = itemView.findViewById(R.id.historyStatusText)
        val dateText: TextView = itemView.findViewById(R.id.historyDateText)
        val itemsText: TextView = itemView.findViewById(R.id.historyItemsText)
        val totalText: TextView = itemView.findViewById(R.id.historyTotalText)
    }
}