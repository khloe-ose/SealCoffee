package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ManageMenuAdapter(
    private val onEdit: (FirestoreMenuItem) -> Unit,
    private val onRemove: (FirestoreMenuItem) -> Unit,
    private val onToggle: (FirestoreMenuItem) -> Unit
) : ListAdapter<FirestoreMenuItem, ManageMenuAdapter.ManageMenuViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageMenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_menu, parent, false)
        return ManageMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageMenuViewHolder, position: Int) {
        holder.bind(getItem(position), onEdit, onRemove, onToggle)
    }

    class ManageMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.manageMenuNameText)
        private val categoryText = itemView.findViewById<TextView>(R.id.manageMenuCategoryText)
        private val priceText = itemView.findViewById<TextView>(R.id.manageMenuPriceText)
        private val availabilityText = itemView.findViewById<TextView>(R.id.manageMenuAvailabilityText)
        private val editButton = itemView.findViewById<TextView>(R.id.manageMenuEditButton)
        private val removeButton = itemView.findViewById<TextView>(R.id.manageMenuRemoveButton)
        private val toggleButton = itemView.findViewById<TextView>(R.id.manageMenuToggleButton)

        fun bind(
            item: FirestoreMenuItem,
            onEdit: (FirestoreMenuItem) -> Unit,
            onRemove: (FirestoreMenuItem) -> Unit,
            onToggle: (FirestoreMenuItem) -> Unit
        ) {
            nameText.text = item.name
            categoryText.text = item.category

            val formattedMoney = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                .format(item.basePriceCentavos / 100.0)
            priceText.text = formattedMoney

            availabilityText.text = itemView.context.getString(
                if (item.available) R.string.available else R.string.unavailable
            )
            availabilityText.setTextColor(
                itemView.context.getColor(if (item.available) R.color.seal_success else R.color.seal_error)
            )
            toggleButton.text = itemView.context.getString(
                if (item.available) R.string.mark_unavailable else R.string.mark_available
            )
            editButton.setOnClickListener { onEdit(item) }
            removeButton.setOnClickListener { onRemove(item) }
            toggleButton.setOnClickListener { onToggle(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FirestoreMenuItem>() {
        override fun areItemsTheSame(oldItem: FirestoreMenuItem, newItem: FirestoreMenuItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FirestoreMenuItem, newItem: FirestoreMenuItem) =
            oldItem == newItem
    }
}