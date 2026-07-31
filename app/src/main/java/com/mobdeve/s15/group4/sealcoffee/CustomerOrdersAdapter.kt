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

class CustomerOrdersAdapter(
    private val onOrderClick: (OrderWithDetails) -> Unit
) : ListAdapter<OrderWithDetails, RecyclerView.ViewHolder>(DiffCallback) {
    private companion object {
        const val TYPE_ACTIVE = 1
        const val TYPE_PAST = 2
    }

    override fun getItemViewType(position: Int): Int =
        if (OrderStatus.fromStorage(getItem(position).order.status) == OrderStatus.COMPLETED) {
            TYPE_PAST
        } else {
            TYPE_ACTIVE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ACTIVE) {
            ActiveOrderViewHolder(inflater.inflate(R.layout.item_current_order, parent, false))
        } else {
            PastOrderViewHolder(inflater.inflate(R.layout.item_order_history, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = getItem(position)
        when (holder) {
            is ActiveOrderViewHolder -> holder.bind(order)
            is PastOrderViewHolder -> holder.bind(order)
        }
        holder.itemView.setOnClickListener { onOrderClick(order) }
    }

    class ActiveOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.currentOrderIdText)
        private val orderType: TextView = itemView.findViewById(R.id.currentOrderTypeText)
        private val statusTitle: TextView = itemView.findViewById(R.id.currentStatus)
        private val statusDesc: TextView = itemView.findViewById(R.id.currentStatusDescriptionText)
        private val itemsList: TextView = itemView.findViewById(R.id.currentItemsText)
        private val totalText: TextView = itemView.findViewById(R.id.currentTotalText)

        fun bind(details: OrderWithDetails) {
            val order = details.order
            val status = OrderStatus.fromStorage(order.status) ?: OrderStatus.PENDING
            orderId.text = order.orderNumber
            orderType.text = order.orderType
            statusTitle.text = status.label
            statusTitle.setTextColor(itemView.context.getColor(status.statusColor()))
            statusDesc.text = itemView.context.getString(
                when (status) {
                    OrderStatus.PENDING -> R.string.status_pending_description
                    OrderStatus.PREPARING -> R.string.status_preparing_description
                    OrderStatus.READY_FOR_PICKUP -> R.string.status_ready_description
                    OrderStatus.DELAYED -> R.string.status_delayed_description
                    OrderStatus.COMPLETED -> R.string.status_completed_description
                }
            )
            itemsList.text = details.items.joinToString { "${it.quantity}× ${it.itemNameSnapshot}" }
            totalText.text = order.totalCentavos.formatMoney()
        }
    }

    class PastOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.historyOrderIdText)
        private val statusText: TextView = itemView.findViewById(R.id.historyStatusText)
        private val dateText: TextView = itemView.findViewById(R.id.historyDateText)
        private val itemsText: TextView = itemView.findViewById(R.id.historyItemsText)
        private val totalText: TextView = itemView.findViewById(R.id.historyTotalText)

        fun bind(details: OrderWithDetails) {
            orderId.text = details.order.orderNumber
            statusText.text = OrderStatus.fromStorage(details.order.status)?.label ?: details.order.status
            dateText.text = details.order.placedAt.formatDateTime()
            itemsText.text = details.items.joinToString { "${it.quantity}× ${it.itemNameSnapshot}" }
            totalText.text = details.order.totalCentavos.formatMoney()
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OrderWithDetails>() {
        override fun areItemsTheSame(oldItem: OrderWithDetails, newItem: OrderWithDetails) =
            oldItem.order.id == newItem.order.id

        override fun areContentsTheSame(oldItem: OrderWithDetails, newItem: OrderWithDetails) =
            oldItem == newItem
    }
}
