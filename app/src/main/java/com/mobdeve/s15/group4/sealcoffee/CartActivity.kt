package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mobdeve.s15.group4.sealcoffee.data.CheckoutResult
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.data.local.CartItemWithMenu
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {
    private val cartAdapter = CartItemAdapter(
        onDecrease = ::decreaseQuantity,
        onIncrease = ::increaseQuantity,
        onEdit = ::openEditor
    )
    private lateinit var subtotalText: TextView
    private lateinit var totalText: TextView
    private lateinit var emptyText: TextView
    private lateinit var warningText: TextView
    private lateinit var placeOrderButton: Button
    private var cartItems: List<CartItemWithMenu> = emptyList()
    private var checkoutInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_cart)
        CustomerNavigation.bind(this, CustomerDestination.CART)

        subtotalText = findViewById(R.id.cartSubtotalText)
        totalText = findViewById(R.id.cartTotalText)
        emptyText = findViewById(R.id.cartEmptyText)
        warningText = findViewById(R.id.cartWarningText)
        placeOrderButton = findViewById(R.id.placeOrderButton)

        val recyclerView = findViewById<RecyclerView>(R.id.cartRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
        attachCartGestures(recyclerView)
        placeOrderButton.setOnClickListener { confirmCheckout() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeCart(sealApp.session.userId).collect {
                    cartItems = it
                    renderCart()
                }
            }
        }
    }

    private fun renderCart() {
        cartAdapter.submitList(cartItems)
        val subtotal = cartItems.sumOf(sealApp.repository::priceCartItem)
        subtotalText.text = subtotal.formatMoney()
        totalText.text = subtotal.formatMoney()
        emptyText.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
        val hasUnavailable = cartItems.any { !it.menuItem.available || it.menuItem.archived }
        warningText.visibility = if (hasUnavailable) View.VISIBLE else View.GONE
        placeOrderButton.isEnabled = cartItems.isNotEmpty() && !hasUnavailable && !checkoutInProgress
    }

    private fun decreaseQuantity(item: CartItemWithMenu) {
        if (item.cartItem.quantity == 1) {
            Toast.makeText(this, R.string.quantity_minimum, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            sealApp.repository.updateCartQuantity(
                sealApp.session.userId,
                item.cartItem.id,
                item.cartItem.quantity - 1
            )
        }
    }

    private fun increaseQuantity(item: CartItemWithMenu) {
        if (item.cartItem.quantity >= 99) return
        lifecycleScope.launch {
            sealApp.repository.updateCartQuantity(
                sealApp.session.userId,
                item.cartItem.id,
                item.cartItem.quantity + 1
            )
        }
    }

    private fun openEditor(item: CartItemWithMenu) {
        startActivity(
            Intent(this, ProductDetailsActivity::class.java)
                .putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, item.menuItem.id)
                .putExtra(ProductDetailsActivity.EXTRA_CART_ITEM_ID, item.cartItem.id)
                .putExtra(ProductDetailsActivity.EXTRA_QUANTITY, item.cartItem.quantity)
                .putExtra(ProductDetailsActivity.EXTRA_SIZE, item.cartItem.size)
                .putStringArrayListExtra(
                    ProductDetailsActivity.EXTRA_ADD_ONS,
                    ArrayList(StringListCodec.decode(item.cartItem.addOnsCsv))
                )
                .putExtra(ProductDetailsActivity.EXTRA_NOTES, item.cartItem.notes)
        )
    }

    private fun confirmCheckout() {
        if (checkoutInProgress || cartItems.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_order)
            .setMessage(R.string.pickup_payment_message)
            .setPositiveButton(R.string.place_order_btn) { _, _ -> performCheckout() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performCheckout() {
        checkoutInProgress = true
        renderCart()
        lifecycleScope.launch {
            when (val result = sealApp.repository.checkout(sealApp.session.userId)) {
                is CheckoutResult.Success -> {
                    Toast.makeText(
                        this@CartActivity,
                        getString(R.string.order_confirmed, result.orderNumber),
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(
                        Intent(this@CartActivity, CustomerOrdersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                }
                is CheckoutResult.Failure -> {
                    Toast.makeText(this@CartActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
            checkoutInProgress = false
            renderCart()
        }
    }

    private fun attachCartGestures(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = cartAdapter.itemAt(position)
                if (item == null) {
                    if (position != RecyclerView.NO_POSITION) cartAdapter.notifyItemChanged(position)
                    return
                }
                if (direction == ItemTouchHelper.RIGHT) {
                    cartAdapter.notifyItemChanged(position)
                    openEditor(item)
                } else {
                    lifecycleScope.launch {
                        val removed = sealApp.repository.removeCartItem(
                            sealApp.session.userId,
                            item.cartItem.id
                        )
                        if (removed == null) {
                            cartAdapter.notifyItemChanged(position)
                            return@launch
                        }
                        Snackbar.make(
                            findViewById(R.id.cartRoot),
                            getString(R.string.removed_from_cart, item.menuItem.name),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.undo) {
                            lifecycleScope.launch {
                                if (!sealApp.repository.restoreCartItem(removed)) {
                                    Toast.makeText(
                                        this@CartActivity,
                                        R.string.unable_to_restore,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.show()
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val isEdit = dX > 0
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = ContextCompat.getColor(
                            this@CartActivity,
                            if (isEdit) R.color.seal_navy else R.color.seal_error
                        )
                    }
                    val left = if (isEdit) itemView.left.toFloat() else itemView.right + dX
                    val right = if (isEdit) itemView.left + dX else itemView.right.toFloat()
                    canvas.drawRect(left, itemView.top.toFloat(), right, itemView.bottom.toFloat(), paint)
                    paint.color = ContextCompat.getColor(this@CartActivity, R.color.white)
                    paint.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        15f,
                        resources.displayMetrics
                    )
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.textAlign = if (isEdit) Paint.Align.LEFT else Paint.Align.RIGHT
                    val x = if (isEdit) itemView.left + 24f else itemView.right - 24f
                    val y = itemView.top + itemView.height / 2f - (paint.ascent() + paint.descent()) / 2f
                    canvas.drawText(getString(if (isEdit) R.string.swipe_edit else R.string.swipe_remove), x, y, paint)
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}
