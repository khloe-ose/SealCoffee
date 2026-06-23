package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData
import com.mobdeve.s15.group4.sealcoffee.data.MenuItem

class CustomerMenuActivity : AppCompatActivity() {
    private val menuAdapter = MenuItemAdapter(
        onItemClick = ::openProductDetails,
        onItemLongClick = ::showQuickPreview
    )

    private lateinit var filterButtons: Map<String, Button>
    private var selectedFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_menu)

        CustomerNavigation.bind(this, CustomerDestination.MENU)

        findViewById<RecyclerView>(R.id.menuRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CustomerMenuActivity)
            adapter = menuAdapter
        }

        filterButtons = mapOf(
            "All" to findViewById(R.id.filterAllButton),
            "Coffee" to findViewById(R.id.filterCoffeeButton),
            "Non-Coffee" to findViewById(R.id.filterNonCoffeeButton),
            "Snacks" to findViewById(R.id.filterSnacksButton),
            "Desserts" to findViewById(R.id.filterDessertsButton)
        )

        filterButtons.forEach { (filter, button) ->
            button.setOnClickListener {
                selectedFilter = filter
                applyFilter()
            }
        }

        applyFilter()
    }

    private fun applyFilter() {
        filterButtons.forEach { (filter, button) ->
            val isSelected = filter == selectedFilter
            button.setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
            button.setTextColor(getColor(if (isSelected) R.color.white else R.color.seal_navy))
        }

        val filteredItems = DummyData.menuItems.filter {
            selectedFilter == "All" || it.category == selectedFilter
        }
        menuAdapter.submitItems(filteredItems)
    }

    private fun openProductDetails(item: MenuItem) {
        startActivity(
            Intent(this, ProductDetailsActivity::class.java)
                .putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, item.id)
        )
    }

    private fun showQuickPreview(item: MenuItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_preview, null)
        dialogView.findViewById<TextView>(R.id.previewImagePlaceholder).text = item.name.initials()
        dialogView.findViewById<TextView>(R.id.previewNameText).text = item.name
        dialogView.findViewById<TextView>(R.id.previewDescriptionText).text = item.description

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
}
