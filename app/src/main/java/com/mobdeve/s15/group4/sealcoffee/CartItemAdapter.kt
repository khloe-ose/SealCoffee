package com.mobdeve.s15.group4.sealcoffee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.CartItem

class CartItemAdapter(
    private val onDecrease: (CartItem) -> Unit,
    private val onIncrease: (CartItem) -> Unit,
    private val onEdit: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {
    private val items = mutableListOf<CartItem>()

    fun submitItems(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.cartItemNameText)
        private val metaText = itemView.findViewById<TextView>(R.id.cartItemMetaText)
        private val priceText = itemView.findViewById<TextView>(R.id.cartItemPriceText)
        private val quantityText = itemView.findViewById<TextView>(R.id.cartItemQuantityText)
        private val decreaseButton = itemView.findViewById<Button>(R.id.cartDecreaseButton)
        private val increaseButton = itemView.findViewById<Button>(R.id.cartIncreaseButton)
        private val editButton = itemView.findViewById<TextView>(R.id.cartEditButton)

        fun bind(item: CartItem) {
            nameText.text = item.menuItem.name
            metaText.text = buildString {
                append(item.size)
                append(" / ")
                append(item.temperature)
                if (item.addOns.isNotEmpty()) {
                    append(" / ")
                    append(item.addOns.joinToString())
                }
            }
            priceText.text = item.lineTotal.formatPrice()
            quantityText.text = item.quantity.toString()
            decreaseButton.setOnClickListener { onDecrease(item) }
            increaseButton.setOnClickListener { onIncrease(item) }
            editButton.setOnClickListener { onEdit(item) }
        }
    }
}
