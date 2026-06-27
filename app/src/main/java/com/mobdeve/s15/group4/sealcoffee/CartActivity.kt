package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val editCartItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val itemId = data.getStringExtra("extra_item_id")
            val index = cartItems.indexOfFirst { it.id == itemId }

            if (index != -1) {
                val newSize = data.getStringExtra("extra_size") ?: "Regular"
                val newQuantity = data.getIntExtra("extra_quantity", 1)
                val newAddOns = data.getStringArrayListExtra("extra_addons") ?: arrayListOf()

                cartItems[index] = cartItems[index].copy(
                    size = newSize,
                    quantity = newQuantity,
                    addOns = newAddOns
                )
            }
        }
        refreshCart()
    }

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
            startActivity(Intent(this, CustomerOrdersActivity::class.java))
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
                    Toast.makeText(
                        this@CartActivity,
                        "${item.menuItem.name} removed from cart",
                        Toast.LENGTH_SHORT
                    ).show()
                    refreshCart()
                } else {
                    Toast.makeText(
                        this@CartActivity,
                        "Editing ${item.menuItem.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEditDialog(item)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val iconSize = 100
                    val offset = 200
                    val iconMargin = 40

                    if (dX > 0) { //swipe right to edit
                        val editIcon = androidx.core.content.ContextCompat.getDrawable(recyclerView.context,R.drawable.ic_cart_edit)
                        if (editIcon != null) {
                            if (dX > iconMargin) {
                                val iconLeft = itemView.left + iconMargin
                                val iconTop = itemView.top + offset
                                editIcon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                                editIcon.draw(c)
                            }
                        }
                    } else if (dX < 0) {  //swipe left to delete
                        val deleteIcon = androidx.core.content.ContextCompat.getDrawable(recyclerView.context,R.drawable.ic_delete)
                        if (deleteIcon != null) {
                            if (dX < -iconMargin) {
                                val iconRight = itemView.right - iconMargin
                                val iconTop = itemView.top + offset
                                deleteIcon.setBounds(iconRight - iconSize, iconTop, iconRight, iconTop + iconSize)
                                deleteIcon.draw(c)
                            }
                        }

                    }
                }
                super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
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
        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, item.menuItem.id)
            putExtra("extra_item_id", item.id)
            putExtra("extra_quantity", item.quantity)
            putExtra("extra_size", item.size)
            putStringArrayListExtra("extra_addons", ArrayList(item.addOns))
            putExtra("extra_edit_mode", true)
        }

        editCartItemLauncher.launch(intent)
    }


    private fun refreshCart() {
        cartAdapter.submitItems(cartItems)
        val subtotal = cartItems.sumOf { it.lineTotal }
        subtotalText.text = subtotal.formatPrice()
        totalText.text = subtotal.formatPrice()
    }
}
