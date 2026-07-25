package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class OrderHistoryActivity : AppCompatActivity() {
    private val query = MutableStateFlow("")
    private val adapter = OrderHistoryAdapter { details ->
        startActivity(
            Intent(this, EmployeeOrderDetailsActivity::class.java)
                .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, details.order.id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.EMPLOYEE)) return
        setContentView(R.layout.activity_customer_order_history)
        val emptyText = findViewById<TextView>(R.id.customerHistoryEmptyText)
        findViewById<RecyclerView>(R.id.customerOrderHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = this@OrderHistoryActivity.adapter
        }
        findViewById<EditText>(R.id.customerHistorySearchInput).doAfterTextChanged {
            query.value = it?.toString().orEmpty()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                query.debounce(200).flatMapLatest(sealApp.repository::observeCustomerHistory)
                    .collect {
                        adapter.submitList(it)
                        emptyText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
            }
        }
    }
}
