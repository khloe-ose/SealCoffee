package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.domain.MenuCategory
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {
    private val menuAdapter = MenuItemAdapter(
        onItemClick = ::openProductDetails,
        onItemLongClick = ::showQuickPreview
    )

    private lateinit var filterCards: Map<String, CardView>
    private lateinit var filterTexts: Map<String, TextView>
    private var selectedFilter = FILTER_ALL
    private var allItems: List<MenuItemEntity> = emptyList()
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_customer_menu)

        CustomerNavigation.bind(this, CustomerDestination.MENU)

        findViewById<RecyclerView>(R.id.menuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerMenuActivity)
            adapter = menuAdapter
        }
        emptyText = findViewById(R.id.menuEmptyText)

        filterCards = mapOf(
            FILTER_ALL to findViewById(R.id.cardFilterAll),
            MenuCategory.COFFEE.label to findViewById(R.id.cardFilterCoffee),
            MenuCategory.NON_COFFEE.label to findViewById(R.id.cardFilterNonCoffee),
            MenuCategory.SNACKS.label to findViewById(R.id.cardFilterSnacks),
            MenuCategory.DESSERTS.label to findViewById(R.id.cardFilterDesserts)
        )

        filterTexts = mapOf(
            FILTER_ALL to findViewById(R.id.filterAll),
            MenuCategory.COFFEE.label to findViewById(R.id.filterCoffee),
            MenuCategory.NON_COFFEE.label to findViewById(R.id.filterNonCoffee),
            MenuCategory.SNACKS.label to findViewById(R.id.filterSnacks),
            MenuCategory.DESSERTS.label to findViewById(R.id.filterDesserts)
        )

        filterCards.forEach { (filter, card) ->
            card.setOnClickListener {
                selectedFilter = filter
                applyFilter()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeCustomerMenu().collect {
                    allItems = it
                    applyFilter()
                }
            }
        }
    }

    private fun applyFilter() {
        filterCards.forEach { (filter, card) ->
            val isSelected = filter == selectedFilter
            val textView = filterTexts[filter]

            if (isSelected) {
                card.setCardBackgroundColor(getColor(R.color.seal_navy))
                textView?.setTextColor(getColor(R.color.white))
            } else {
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"))
                textView?.setTextColor(android.graphics.Color.parseColor("#374151"))
            }
        }

        val filteredItems = allItems.filter {
            selectedFilter == FILTER_ALL || it.category == selectedFilter
        }
        menuAdapter.submitList(filteredItems)
        emptyText.visibility = if (filteredItems.isEmpty()) View.VISIBLE else View.GONE
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

    private companion object {
        const val FILTER_ALL = "All"
    }
}
