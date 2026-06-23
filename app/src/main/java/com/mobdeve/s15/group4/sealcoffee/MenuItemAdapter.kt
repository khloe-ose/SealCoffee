package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem

class MenuItemAdapter(
    private val onItemClick: (MenuItem) -> Unit,
    private val onItemLongClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {
    private val items = mutableListOf<MenuItem>()

    fun submitItems(newItems: List<MenuItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu_product, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePlaceholder = itemView.findViewById<TextView>(R.id.menuItemImagePlaceholder)
        private val nameText = itemView.findViewById<TextView>(R.id.menuItemNameText)
        private val categoryText = itemView.findViewById<TextView>(R.id.menuItemCategoryText)
        private val priceText = itemView.findViewById<TextView>(R.id.menuItemPriceText)
        private val availabilityText = itemView.findViewById<TextView>(R.id.menuItemAvailabilityText)

        fun bind(item: MenuItem) {
            imagePlaceholder.text = item.name.initials()
            nameText.text = item.name
            categoryText.text = item.category
            priceText.text = item.price.formatPrice()
            availabilityText.text = if (item.isAvailable) "Available" else "Unavailable"
            availabilityText.setTextColor(
                itemView.context.getColor(
                    if (item.isAvailable) R.color.seal_success else R.color.seal_error
                )
            )
            itemView.alpha = if (item.isAvailable) 1.0f else 0.62f
            itemView.setOnClickListener { onItemClick(item) }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}
