package com.mobdeve.s15.group4.sealcoffee

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import coil.load

class CustomerMenuActivity : AppCompatActivity() {
    private val menuAdapter = MenuItemAdapter(
        onItemClick = { item ->

        },
        onItemLongClick = ::showQuickPreview
    )

    private lateinit var emptyText: TextView
    private lateinit var filterMap: Map<String, androidx.cardview.widget.CardView>
    private var selectedFilter = "All"
    private var allItems: List<FirestoreMenuItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthNavigation.requireRole(this, "customer") { isAuthorized ->
            if (!isAuthorized) return@requireRole

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
                    observeFirestoreMenu().collect { items ->
                        allItems = items
                        applyFilterAndSubmit()
                    }
                }
            }
        }
    }

    private fun observeFirestoreMenu(): Flow<List<FirestoreMenuItem>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("menu")
            .whereEqualTo("archived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreMenuItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    private fun updateFilterUI() {
        filterMap.forEach { (category, cardView) ->
            val textView = cardView.getChildAt(0) as? TextView
            if (category == selectedFilter) {
                cardView.setCardBackgroundColor(getColor(R.color.seal_navy))
                textView?.setTextColor(Color.WHITE)
            } else {
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

        val sortedList = filtered.sortedWith(
            compareByDescending<FirestoreMenuItem> { it.available }
                .thenBy { it.category }
                .thenBy { it.name }
        )

        menuAdapter.submitList(sortedList)
        if (sortedList.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "No items found for '$selectedFilter'."
        } else {
            emptyText.visibility = View.GONE
        }
    }

    private fun showQuickPreview(item: FirestoreMenuItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.previewImagePlaceholder)
        imageView.loadSupabaseImage(item.imageKey)
        imageView.contentDescription = item.name
        dialogView.findViewById<TextView>(R.id.previewNameText).text = item.name
        dialogView.findViewById<TextView>(R.id.previewDescriptionText).text = item.description

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.close, null)
            .show()
    }
}