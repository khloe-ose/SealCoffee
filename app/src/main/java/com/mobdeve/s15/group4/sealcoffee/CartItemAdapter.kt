package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

class CartItemAdapter(
    private val onDecrease: (Map<String, Any>) -> Unit,
    private val onIncrease: (Map<String, Any>) -> Unit,
    private val onEdit: (Map<String, Any>) -> Unit
) : ListAdapter<Map<String, Any>, CartItemAdapter.CartItemViewHolder>(DiffCallback) {

    fun itemAt(position: Int): Map<String, Any>? = currentList.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) = holder.bind(getItem(position))

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.cartItemImage)
        private val nameText = itemView.findViewById<TextView>(R.id.cartItemNameText)
        private val metaText = itemView.findViewById<TextView>(R.id.cartItemMetaText)
        private val priceText = itemView.findViewById<TextView>(R.id.cartItemPriceText)
        private val quantityText = itemView.findViewById<TextView>(R.id.cartItemQuantityText)
        private val decreaseButton = itemView.findViewById<ImageButton>(R.id.cartDecreaseButton)
        private val increaseButton = itemView.findViewById<ImageButton>(R.id.cartIncreaseButton)
        private val editButton = itemView.findViewById<TextView>(R.id.cartEditButton)

        fun bind(item: Map<String, Any>) {
            val itemName = item["name"] as? String ?: "Item"
            nameText.text = itemName

            val rawImageKey = item["imageKey"] as? String ?: ""
            val cleanKey = rawImageKey.substringBeforeLast(".").removePrefix("img_")
            val imageRes = ImageCatalog.resourceFor(cleanKey)
            image.loadSupabaseImage(imageRes, fallbackResId = R.drawable.ic_launcher_foreground)
            image.contentDescription = itemName

            val size = item["size"] as? String ?: "Regular"
            @Suppress("UNCHECKED_CAST")
            val addOns = item["addOns"] as? List<String> ?: emptyList()
            val notes = item["notes"] as? String ?: ""

            metaText.text = buildString {
                append(size)
                if (addOns.isNotEmpty()) append(" • ${addOns.joinToString()}")
                if (notes.isNotBlank()) {
                    append("\n${itemView.context.getString(R.string.cart_item_notes, notes)}")
                }
            }
            metaText.setTextColor(itemView.context.getColor(R.color.seal_text_secondary))

            val unitPrice = (item["unitPriceCentavos"] as? Number)?.toInt() ?: 0
            val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
            val lineTotal = unitPrice * quantity

            priceText.text = lineTotal.formatMoney()
            quantityText.text = NumberFormat.getIntegerInstance().format(quantity)

            decreaseButton.setOnClickListener { onDecrease(item) }
            increaseButton.setOnClickListener { onIncrease(item) }
            editButton.setOnClickListener { onEdit(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {

            val oldId = oldItem["id"] ?: oldItem["cartItemId"]
            val newId = newItem["id"] ?: newItem["cartItemId"]
            return oldId == newId
        }

        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem == newItem
        }
    }
}