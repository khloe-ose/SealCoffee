package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.CartItem
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class CartActivity : AppCompatActivity() {
    private val cartItems = DummyData.cartItems.toMutableList()
    private val cartAdapter = CartItemAdapter(
        onDecrease = ::decreaseQuantity,
        onIncrease = ::increaseQuantity,
        onEdit = ::showEditDialog
    )

    private lateinit var subtotalText: TextView
    private lateinit var totalText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        CustomerNavigation.bind(this, CustomerDestination.CART)

        subtotalText = findViewById(R.id.cartSubtotalText)
        totalText = findViewById(R.id.cartTotalText)

        val cartRecyclerView = findViewById<RecyclerView>(R.id.cartRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
        attachCartGestures(cartRecyclerView)

        findViewById<Button>(R.id.placeOrderButton).setOnClickListener {
            Toast.makeText(this, "Order placed for pickup", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CurrentOrderStatusActivity::class.java))
        }

        refreshCart()
    }

    private fun attachCartGestures(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION || position !in cartItems.indices) return

                val item = cartItems[position]
                if (direction == ItemTouchHelper.LEFT) {
                    cartItems.removeAt(position)
                    Toast.makeText(this@CartActivity, "${item.menuItem.name} removed from cart", Toast.LENGTH_SHORT).show()
                    refreshCart()
                } else {
                    Toast.makeText(this@CartActivity, "Editing ${item.menuItem.name}", Toast.LENGTH_SHORT).show()
                    showEditDialog(item)
                    cartAdapter.notifyItemChanged(position)
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun decreaseQuantity(item: CartItem) {
        val index = cartItems.indexOfFirst { it.id == item.id }
        if (index == -1) return

        if (cartItems[index].quantity > 1) {
            cartItems[index] = cartItems[index].copy(quantity = cartItems[index].quantity - 1)
        } else {
            Toast.makeText(this, "Quantity cannot go below 1", Toast.LENGTH_SHORT).show()
        }
        refreshCart()
    }

    private fun increaseQuantity(item: CartItem) {
        val index = cartItems.indexOfFirst { it.id == item.id }
        if (index == -1) return

        cartItems[index] = cartItems[index].copy(quantity = cartItems[index].quantity + 1)
        refreshCart()
    }

    private fun showEditDialog(item: CartItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_cart_item, null)
        dialogView.findViewById<TextView>(R.id.editCartItemNameText).text = item.menuItem.name
        dialogView.findViewById<TextView>(R.id.editCartItemDetailsText).text =
            "${item.size} / ${item.temperature}"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Apply Changes") { _, _ ->
                Toast.makeText(this, "Cart item updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshCart() {
        cartAdapter.submitItems(cartItems)
        val subtotal = cartItems.sumOf { it.lineTotal }
        subtotalText.text = subtotal.formatPrice()
        totalText.text = subtotal.formatPrice()
    }
}
