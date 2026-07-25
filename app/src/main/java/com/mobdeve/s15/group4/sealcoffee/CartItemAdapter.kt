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
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.data.local.CartItemWithMenu
import com.mobdeve.s15.group4.sealcoffee.domain.PricingCalculator
import java.text.NumberFormat

class CartItemAdapter(
    private val onDecrease: (CartItemWithMenu) -> Unit,
    private val onIncrease: (CartItemWithMenu) -> Unit,
    private val onEdit: (CartItemWithMenu) -> Unit
) : ListAdapter<CartItemWithMenu, CartItemAdapter.CartItemViewHolder>(DiffCallback) {

    fun itemAt(position: Int): CartItemWithMenu? = currentList.getOrNull(position)

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

        fun bind(item: CartItemWithMenu) {
            image.setImageResource(ImageCatalog.resourceFor(item.menuItem.imageKey))
            image.contentDescription = item.menuItem.name
            nameText.text = item.menuItem.name
            val addOns = StringListCodec.decode(item.cartItem.addOnsCsv)
            metaText.text = buildString {
                append(item.cartItem.size)
                if (addOns.isNotEmpty()) append(" • ${addOns.joinToString()}")
                if (item.cartItem.notes.isNotBlank()) {
                    append("\n${itemView.context.getString(R.string.cart_item_notes, item.cartItem.notes)}")
                }
                if (!item.menuItem.available || item.menuItem.archived) {
                    append("\n${itemView.context.getString(R.string.cart_item_unavailable)}")
                }
            }
            metaText.setTextColor(
                itemView.context.getColor(
                    if (item.menuItem.available && !item.menuItem.archived) {
                        R.color.seal_text_secondary
                    } else {
                        R.color.seal_error
                    }
                )
            )
            val unit = PricingCalculator.unitPriceCentavos(
                item.menuItem.basePriceCentavos,
                item.menuItem.category,
                item.cartItem.size,
                addOns
            )
            priceText.text = PricingCalculator.lineTotalCentavos(unit, item.cartItem.quantity).formatMoney()
            quantityText.text = NumberFormat.getIntegerInstance().format(item.cartItem.quantity)
            decreaseButton.setOnClickListener { onDecrease(item) }
            increaseButton.setOnClickListener { onIncrease(item) }
            editButton.setOnClickListener { onEdit(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CartItemWithMenu>() {
        override fun areItemsTheSame(oldItem: CartItemWithMenu, newItem: CartItemWithMenu) =
            oldItem.cartItem.id == newItem.cartItem.id

        override fun areContentsTheSame(oldItem: CartItemWithMenu, newItem: CartItemWithMenu) =
            oldItem == newItem
    }
}
