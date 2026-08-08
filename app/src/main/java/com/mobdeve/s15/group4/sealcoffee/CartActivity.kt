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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CartActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var subtotalText: TextView
    private lateinit var totalText: TextView
    private lateinit var emptyText: TextView
    private lateinit var warningText: TextView
    private lateinit var placeOrderButton: Button
    private lateinit var cartRecyclerView: RecyclerView

    private lateinit var cartItemAdapter: CartItemAdapter
    private val cartItemsList = mutableListOf<Map<String, Any>>()
    private var checkoutInProgress = false

    companion object {
        private const val MAX_ITEM_QUANTITY = 10      // Max units for a single menu item row
        private const val MAX_TOTAL_CART_ITEMS = 15   // Max combined item quantity for checkout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        CustomerNavigation.bind(this, CustomerDestination.CART)

        subtotalText = findViewById(R.id.cartSubtotalText)
        totalText = findViewById(R.id.cartTotalText)
        emptyText = findViewById(R.id.cartEmptyText)
        warningText = findViewById(R.id.cartWarningText)
        placeOrderButton = findViewById(R.id.placeOrderButton)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        cartItemAdapter = CartItemAdapter(
            onDecrease = { item -> decreaseQuantity(item) },
            onIncrease = { item -> increaseQuantity(item) },
            onEdit = { item -> openEditor(item) }
        )
        cartRecyclerView.adapter = cartItemAdapter

        attachCartGestures(cartRecyclerView)
        placeOrderButton.setOnClickListener { confirmCheckout() }

        fetchCartItems()
    }

    private fun fetchCartItems() {
        AuthNavigation.requireAuthenticated(this) { isAuthenticated ->
            if (!isAuthenticated) return@requireAuthenticated

            val userId = auth.currentUser?.uid ?: return@requireAuthenticated

            db.collection("users").document(userId).collection("cart")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error loading cart: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    cartItemsList.clear()
                    snapshot?.documents?.forEach { doc ->
                        val data = doc.data ?: mutableMapOf()
                        data["id"] = doc.id // get for updates/deletions
                        cartItemsList.add(data)
                    }

                    renderCart()
                }
        }
    }

    private fun renderCart() {
        cartItemAdapter.submitList(cartItemsList.toList())

        val subtotal = cartItemsList.sumOf { item ->
            val unitPrice = (item["unitPriceCentavos"] as? Number)?.toInt() ?: 0
            val qty = (item["quantity"] as? Number)?.toInt() ?: 1
            unitPrice * qty
        }

        subtotalText.text = subtotal.formatMoney()
        totalText.text = subtotal.formatMoney()

        emptyText.visibility = if (cartItemsList.isEmpty()) View.VISIBLE else View.GONE
        placeOrderButton.isEnabled = cartItemsList.isNotEmpty() && !checkoutInProgress
    }

    private fun updateQuantity(itemId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                db.collection("users").document(userId).collection("cart")
                    .document(itemId)
                    .update("quantity", newQuantity)
                    .await()
            } catch (e: Exception) {
                Toast.makeText(this@CartActivity, "Failed to update quantity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun increaseQuantity(item: Map<String, Any>) {
        val currentQty = (item["quantity"] as? Number)?.toInt() ?: 1
        val itemId = item["id"] as? String ?: return

        if (currentQty >= MAX_ITEM_QUANTITY) {
            Toast.makeText(this, "Maximum of $MAX_ITEM_QUANTITY units allowed per item.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTotalItems = cartItemsList.sumOf { (it["quantity"] as? Number)?.toInt() ?: 1 }
        if (currentTotalItems >= MAX_TOTAL_CART_ITEMS) {
            showAlertLimitReached("Cart Limit Reached", "Standard orders are capped at a maximum of $MAX_TOTAL_CART_ITEMS items total. For larger group orders, please split your transaction.")
            return
        }

        updateQuantity(itemId, currentQty + 1)
    }

    private fun decreaseQuantity(item: Map<String, Any>) {
        val currentQty = (item["quantity"] as? Number)?.toInt() ?: 1
        val itemId = item["id"] as? String ?: return

        if (currentQty <= 1) {
            Toast.makeText(this, R.string.quantity_minimum, Toast.LENGTH_SHORT).show()
            return
        }

        updateQuantity(itemId, currentQty - 1)
    }

    private fun openEditor(item: Map<String, Any>) {
        val cartItemId = item["id"] as? String ?: return
        val menuItemId = item["menuItemId"] as? String ?: return
        val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
        val size = item["size"] as? String ?: "Regular"
        @Suppress("UNCHECKED_CAST")
        val addOns = item["addOns"] as? ArrayList<String> ?: arrayListOf()
        val notes = item["notes"] as? String ?: ""

        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, menuItemId)
            putExtra(ProductDetailsActivity.EXTRA_CART_ITEM_ID, cartItemId)
            putExtra(ProductDetailsActivity.EXTRA_QUANTITY, quantity)
            putExtra(ProductDetailsActivity.EXTRA_SIZE, size)
            putStringArrayListExtra(ProductDetailsActivity.EXTRA_ADD_ONS, addOns)
            putExtra(ProductDetailsActivity.EXTRA_NOTES, notes)
        }
        startActivity(intent)
    }

    private fun confirmCheckout() {
        if (checkoutInProgress || cartItemsList.isEmpty()) return

        val totalQuantity = cartItemsList.sumOf { (it["quantity"] as? Number)?.toInt() ?: 1 }

        if (totalQuantity > MAX_TOTAL_CART_ITEMS) {
            showAlertLimitReached(
                "Large Order Limitation",
                "Your order contains $totalQuantity items. Standard mobile pickup orders are limited to $MAX_TOTAL_CART_ITEMS items to ensure quick preparation times."
            )
            return
        }

        if (totalQuantity >= 6) {
            AlertDialog.Builder(this)
                .setTitle("Group Order Notice")
                .setMessage("You are placing a group order ($totalQuantity items). Please allow an extra 20-30 minutes preparation time at the counter.")
                .setPositiveButton("Proceed") { _, _ -> showPaymentConfirmationDialog() }
                .setNegativeButton(R.string.cancel, null)
                .show()
            return
        }

        showPaymentConfirmationDialog()
    }

    private fun showPaymentConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_order)
            .setMessage(R.string.pickup_payment_message)
            .setPositiveButton(R.string.place_order_btn) { _, _ -> performCheckout() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAlertLimitReached(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Understood", null)
            .show()
    }

    private fun performCheckout() {
        AuthNavigation.requireAuthenticated(this) { isAuthenticated ->
            if (!isAuthenticated) return@requireAuthenticated

            val userId = auth.currentUser?.uid ?: return@requireAuthenticated
            checkoutInProgress = true
            renderCart()

            lifecycleScope.launch {
                try {
                    val orderNumber = "SC-${System.currentTimeMillis().toString().takeLast(6)}"
                    val subtotal = cartItemsList.sumOf { item ->
                        val unitPrice = (item["unitPriceCentavos"] as? Number)?.toInt() ?: 0
                        val qty = (item["quantity"] as? Number)?.toInt() ?: 1
                        unitPrice * qty
                    }

                    var customerName = auth.currentUser?.displayName ?: "Valued Customer"
                    try {
                        val userDoc = db.collection("users").document(userId).get().await()
                        val nameFromDb = userDoc.getString("fullName")
                        if (!nameFromDb.isNullOrBlank()) {
                            customerName = nameFromDb
                        }
                    } catch (e: Exception) {
                        // Fallback to auth display name or default
                    }

                    val orderData = hashMapOf(
                        "orderNumber" to orderNumber,
                        "customerId" to userId,
                        "customerName" to customerName,
                        "status" to "PENDING",
                        "placedAt" to System.currentTimeMillis(),
                        "subtotalCentavos" to subtotal.toLong(),
                        "totalCentavos" to subtotal.toLong(),
                        "orderType" to "Pickup",
                        "items" to cartItemsList
                    )

                    db.collection("orders").document(orderNumber).set(orderData).await()

                    val cartSnapshot = db.collection("users").document(userId).collection("cart").get().await()
                    for (doc in cartSnapshot.documents) {
                        doc.reference.delete().await()
                    }

                    Toast.makeText(this@CartActivity, "Order placed successfully: $orderNumber", Toast.LENGTH_LONG).show()
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@CartActivity, "Checkout failed: ${e.message}", Toast.LENGTH_LONG).show()
                    checkoutInProgress = false
                    renderCart()
                }
            }
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
                val item = cartItemAdapter.itemAt(position)
                if (item == null) {
                    if (position != RecyclerView.NO_POSITION) cartItemAdapter.notifyItemChanged(position)
                    return
                }

                val userId = auth.currentUser?.uid ?: return
                val itemId = item["id"] as? String ?: return

                if (direction == ItemTouchHelper.RIGHT) {
                    cartItemAdapter.notifyItemChanged(position)
                    openEditor(item)
                } else {
                    lifecycleScope.launch {
                        try {
                            val snapshot = db.collection("users").document(userId).collection("cart")
                                .document(itemId).get().await()
                            val removedData = snapshot.data

                            if (removedData == null) {
                                cartItemAdapter.notifyItemChanged(position)
                                return@launch
                            }

                            db.collection("users").document(userId).collection("cart")
                                .document(itemId).delete().await()

                            val itemName = item["name"] as? String ?: "Item"

                            Snackbar.make(
                                findViewById(android.R.id.content),
                                getString(R.string.removed_from_cart, itemName),
                                Snackbar.LENGTH_LONG
                            ).setAction(R.string.undo) {
                                lifecycleScope.launch {
                                    try {
                                        db.collection("users").document(userId).collection("cart")
                                            .document(itemId).set(removedData).await()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@CartActivity,
                                            R.string.unable_to_restore,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }.show()
                        } catch (e: Exception) {
                            cartItemAdapter.notifyItemChanged(position)
                            Toast.makeText(this@CartActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                        }
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