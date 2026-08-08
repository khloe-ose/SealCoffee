package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class FirestoreOrderModel(
    val id: String = "",
    val orderNumber: String = "",
    val orderType: String = "",
    val status: String = "PENDING",
    val placedAt: Long = 0L,
    val totalCentavos: Long = 0L,
    val items: List<FirestoreOrderItem> = emptyList()
) {
    companion object {
        fun formatMoney(centavos: Long): String {
            val locale = Locale.Builder().setLanguage("en").setRegion("PH").build()
            return NumberFormat.getCurrencyInstance(locale)
                .format(centavos / 100.0)
        }

        fun formatDate(placedAt: Any?): String {
            when (placedAt) {
                is Timestamp -> {
                    return SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        .format(placedAt.toDate())
                }
                is Number -> {
                    return SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        .format(java.util.Date(placedAt.toLong()))
                }
                else -> return ""
            }
        }
    }
}

data class FirestoreOrderItem(
    val name: String = "",
    val quantity: Int = 1,
    val size: String? = null,
    val addOns: List<String> = emptyList(),
    val notes: String? = null,
    val totalPriceCentavos: Long = 0L
)

class CustomerOrdersAdapter(
    private val onOrderClick: (FirestoreOrderModel) -> Unit
) : ListAdapter<FirestoreOrderModel, RecyclerView.ViewHolder>(DiffCallback) {

    private companion object {
        const val TYPE_ACTIVE = 1
        const val TYPE_PAST = 2
    }

    override fun getItemViewType(position: Int): Int {
        val status = getItem(position).status.uppercase()
        return if (status == "COMPLETED") TYPE_PAST else TYPE_ACTIVE
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

        fun bind(order: FirestoreOrderModel) {
            val statusUpper = order.status.uppercase()
            orderId.text = order.orderNumber
            orderType.text = order.orderType
            statusTitle.text = order.status

            val (descRes, colorRes) = when (statusUpper) {
                "PENDING" -> Pair(R.string.status_pending_description, R.color.seal_navy)
                "PREPARING" -> Pair(R.string.status_preparing_description, R.color.seal_navy)
                "READY_FOR_PICKUP" -> Pair(R.string.status_ready_description, R.color.seal_success)
                "DELAYED" -> Pair(R.string.status_delayed_description, R.color.seal_error)
                else -> Pair(R.string.status_completed_description, R.color.seal_success)
            }

            statusTitle.setTextColor(itemView.context.getColor(colorRes))
            statusDesc.text = itemView.context.getString(descRes)

            itemsList.text = order.items.joinToString(separator = "\n") { item ->
                val sizeText = if (!item.size.isNullOrBlank()) " (${item.size})" else ""
                "${item.quantity}× ${item.name}$sizeText"
            }
            totalText.text = FirestoreOrderModel.formatMoney(order.totalCentavos)
        }
    }

    class PastOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.historyOrderIdText)
        private val statusText: TextView = itemView.findViewById(R.id.historyStatusText)
        private val dateText: TextView = itemView.findViewById(R.id.historyDateText)
        private val itemsText: TextView = itemView.findViewById(R.id.historyItemsText)
        private val totalText: TextView = itemView.findViewById(R.id.historyTotalText)

        fun bind(order: FirestoreOrderModel) {
            orderId.text = order.orderNumber
            statusText.text = order.status
            dateText.text = FirestoreOrderModel.formatDate(order.placedAt)
            itemsText.text = order.items.joinToString(separator = "\n") { item ->
                val sizeText = if (!item.size.isNullOrBlank()) " (${item.size})" else ""
                "${item.quantity}× ${item.name}$sizeText"
            }
            totalText.text = FirestoreOrderModel.formatMoney(order.totalCentavos)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FirestoreOrderModel>() {
        override fun areItemsTheSame(oldItem: FirestoreOrderModel, newItem: FirestoreOrderModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FirestoreOrderModel, newItem: FirestoreOrderModel) =
            oldItem == newItem
    }
}