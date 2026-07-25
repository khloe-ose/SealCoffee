package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var filterButtons: Map<String, Button>
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

        filterButtons = mapOf(
            FILTER_ALL to findViewById(R.id.filterAllButton),
            MenuCategory.COFFEE.label to findViewById(R.id.filterCoffeeButton),
            MenuCategory.NON_COFFEE.label to findViewById(R.id.filterNonCoffeeButton),
            MenuCategory.SNACKS.label to findViewById(R.id.filterSnacksButton),
            MenuCategory.DESSERTS.label to findViewById(R.id.filterDessertsButton)
        )

        filterButtons.forEach { (filter, button) ->
            button.setOnClickListener {
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
        filterButtons.forEach { (filter, button) ->
            val isSelected = filter == selectedFilter

            if (isSelected) {

                button.setBackgroundResource(R.drawable.bg_chip_selected)
                button.setTextColor(getColor(R.color.seal_navy))
            } else {

                button.setBackgroundResource(R.drawable.bg_chip)
                button.setTextColor(getColor(R.color.white))
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
