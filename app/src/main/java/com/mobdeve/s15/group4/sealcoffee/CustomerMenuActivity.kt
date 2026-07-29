package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {
    private val menuAdapter = MenuItemAdapter(
        onItemClick = ::openProductDetails,
        onItemLongClick = ::showQuickPreview
    )

    private lateinit var emptyText: TextView
    private lateinit var filterMap: Map<String, androidx.cardview.widget.CardView>
    private var selectedFilter = "All"
    private var allItems: List<MenuItemEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_customer_menu)

        CustomerNavigation.bind(this, CustomerDestination.MENU)

        emptyText = findViewById(R.id.menuEmptyText)

        findViewById<RecyclerView>(R.id.menuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerMenuActivity)
            adapter = menuAdapter
        }

        filterMap = mapOf(
            "All" to findViewById(R.id.cardFilterAll),
            "Coffee" to findViewById(R.id.cardFilterCoffee),
            "Non-Coffee" to findViewById(R.id.cardFilterNonCoffee),
            "Snacks" to findViewById(R.id.cardFilterSnacks),
            "Desserts" to findViewById(R.id.cardFilterDesserts)
        )

        filterMap.forEach { (category, cardView) ->
            cardView.setOnClickListener {
                selectedFilter = category
                updateFilterUI()
                applyFilterAndSubmit()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeCustomerMenu().collect { items ->
                    allItems = items
                    applyFilterAndSubmit()
                }
            }
        }
    }

    private fun updateFilterUI() {
        filterMap.forEach { (category, cardView) ->
            val textView = cardView.getChildAt(0) as? TextView
            if (category == selectedFilter) {
                // Active state
                cardView.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
                textView?.setTextColor(Color.WHITE)
            } else {
                // Inactive state
                cardView.setCardBackgroundColor(Color.parseColor("#E5E7EB"))
                textView?.setTextColor(Color.parseColor("#374151"))
            }
        }
    }

    private fun applyFilterAndSubmit() {
        val filtered = if (selectedFilter == "All") {
            allItems
        } else {
            allItems.filter { it.category.equals(selectedFilter, ignoreCase = true) }
        }

        menuAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "No items found for '$selectedFilter'."
        } else {
            emptyText.visibility = View.GONE
        }
    }

    private fun openProductDetails(item: MenuItemEntity) {
        startActivity(
            Intent(this, ProductDetailsActivity::class.java)
                .putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, item.id)
        )
    }

    private fun showQuickPreview(item: MenuItemEntity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.previewImagePlaceholder)
        imageView.setImageResource(ImageCatalog.resourceFor(item.imageKey))
        imageView.contentDescription = item.name
        dialogView.findViewById<TextView>(R.id.previewNameText).text = item.name
        dialogView.findViewById<TextView>(R.id.previewDescriptionText).text = item.description

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.close, null)
            .show()
    }
}