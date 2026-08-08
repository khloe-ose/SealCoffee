package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.view.LayoutInflater
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.ImageCatalog

class MenuItemAdapter(
    private val onItemClick: (FirestoreMenuItem) -> Unit,
    private val onItemLongClick: (FirestoreMenuItem) -> Unit
) : ListAdapter<FirestoreMenuItem, MenuItemAdapter.MenuItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu_product, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePlaceholder = itemView.findViewById<ImageView>(R.id.menuItemImagePlaceholder)
        private val nameText = itemView.findViewById<TextView>(R.id.menuItemNameText)
        private val categoryText = itemView.findViewById<TextView>(R.id.menuItemCategoryText)
        private val priceText = itemView.findViewById<TextView>(R.id.menuItemPriceText)
        private val availabilityText = itemView.findViewById<TextView>(R.id.menuItemAvailabilityText)

        fun bind(item: FirestoreMenuItem) {
            val context = itemView.context

            imagePlaceholder.setImageResource(ImageCatalog.resourceFor(item.imageKey))
            imagePlaceholder.contentDescription = item.name
            nameText.text = item.name
            categoryText.text = item.category
            priceText.text = item.basePriceCentavos.formatMoney()

            if (item.available) {
                // Normal / Available State
                availabilityText.visibility = View.GONE

                itemView.alpha = 1.0f
                imagePlaceholder.alpha = 1.0f
                nameText.setTextColor(context.getColor(R.color.seal_text_primary))
                categoryText.setTextColor(context.getColor(R.color.seal_text_secondary))
                priceText.setTextColor(context.getColor(R.color.seal_navy))
            } else {
                // Unavailable State
                availabilityText.visibility = View.VISIBLE
                availabilityText.text = context.getString(R.string.unavailable)
                availabilityText.setTextColor(context.getColor(R.color.seal_error))

                itemView.alpha = 1.0f
                imagePlaceholder.alpha = 0.45f
                nameText.setTextColor(context.getColor(R.color.seal_text_secondary))
                categoryText.setTextColor(context.getColor(R.color.seal_text_secondary))
                priceText.setTextColor(context.getColor(R.color.seal_text_secondary))
            }

            // Regular click launches ProductDetailsActivity passing the menu item ID
            itemView.setOnClickListener {
                onItemClick(item)
                val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                    putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, item.id)
                }
                context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onItemLongClick(item)
                true
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FirestoreMenuItem>() {
        override fun areItemsTheSame(oldItem: FirestoreMenuItem, newItem: FirestoreMenuItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FirestoreMenuItem, newItem: FirestoreMenuItem) =
            oldItem == newItem
    }
}