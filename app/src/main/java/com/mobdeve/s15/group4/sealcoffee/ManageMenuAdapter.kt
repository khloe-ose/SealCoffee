package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem

class ManageMenuAdapter(
    private val onEdit: (MenuItem) -> Unit,
    private val onRemove: (MenuItem) -> Unit,
    private val onToggle: (MenuItem) -> Unit
) : RecyclerView.Adapter<ManageMenuAdapter.ManageMenuViewHolder>() {
    private val items = mutableListOf<MenuItem>()

    fun submitItems(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageMenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_menu, parent, false)
        return ManageMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageMenuViewHolder, position: Int) {
        holder.bind(items[position], onEdit, onRemove, onToggle)
    }

    override fun getItemCount(): Int = items.size

    class ManageMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.manageMenuNameText)
        private val categoryText = itemView.findViewById<TextView>(R.id.manageMenuCategoryText)
        private val priceText = itemView.findViewById<TextView>(R.id.manageMenuPriceText)
        private val availabilityText = itemView.findViewById<TextView>(R.id.manageMenuAvailabilityText)
        private val editButton = itemView.findViewById<TextView>(R.id.manageMenuEditButton)
        private val removeButton = itemView.findViewById<TextView>(R.id.manageMenuRemoveButton)
        private val toggleButton = itemView.findViewById<TextView>(R.id.manageMenuToggleButton)

        fun bind(
            item: MenuItem,
            onEdit: (MenuItem) -> Unit,
            onRemove: (MenuItem) -> Unit,
            onToggle: (MenuItem) -> Unit
        ) {
            nameText.text = item.name
            categoryText.text = item.category
            priceText.text = item.price.formatPrice()
            availabilityText.text = if (item.isAvailable) "Available" else "Unavailable"
            availabilityText.setTextColor(itemView.context.getColor(if (item.isAvailable) R.color.seal_success else R.color.seal_error))
            toggleButton.text = if (item.isAvailable) "Mark Unavailable" else "Mark Available"
            editButton.setOnClickListener { onEdit(item) }
            removeButton.setOnClickListener { onRemove(item) }
            toggleButton.setOnClickListener { onToggle(item) }
        }
    }
}
