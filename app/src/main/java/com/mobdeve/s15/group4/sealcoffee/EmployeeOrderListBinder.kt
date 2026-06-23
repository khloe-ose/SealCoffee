package com.mobdeve.s15.group4.sealcoffee

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.data.DummyData
import com.mobdeve.s15.group4.sealcoffee.data.Order

object EmployeeOrderListBinder {
    fun bind(activity: Activity, title: String, subtitle: String, orders: List<Order>) {
        activity.findViewById<TextView>(R.id.employeeOrderListTitleText).text = title
        activity.findViewById<TextView>(R.id.employeeOrderListSubtitleText).text = subtitle

        val adapter = EmployeeOrderAdapter { order ->
            activity.startActivity(
                Intent(activity, EmployeeOrderDetailsActivity::class.java)
                    .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, order.id)
            )
        }

        val recyclerView = activity.findViewById<RecyclerView>(R.id.employeeOrderRecyclerView).apply {
            layoutManager = LinearLayoutManager(activity)
            this.adapter = adapter
        }

        adapter.submitOrders(orders)
        attachOrderGestures(activity, recyclerView, adapter)
    }

    private fun attachOrderGestures(
        activity: Activity,
        recyclerView: RecyclerView,
        adapter: EmployeeOrderAdapter
    ) {
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
                if (position == RecyclerView.NO_POSITION) return

                val order = adapter.getOrderAt(position) ?: return
                val updatedOrder = if (direction == ItemTouchHelper.LEFT) {
                    order.copy(status = "Delayed")
                } else {
                    order.copy(status = order.status.nextEmployeeStatus())
                }

                updateSharedOrder(updatedOrder)
                adapter.updateOrder(position, updatedOrder)

                val message = if (updatedOrder.status == order.status) {
                    "${order.id} remains ${order.status}"
                } else {
                    "${order.id} marked ${updatedOrder.status}"
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun updateSharedOrder(updatedOrder: Order) {
        val index = DummyData.employeeOrders.indexOfFirst { it.id == updatedOrder.id }
        if (index != -1) {
            DummyData.employeeOrders[index] = updatedOrder
        }
    }

    private fun String.nextEmployeeStatus(): String {
        return when (this) {
            "Pending" -> "Preparing"
            "Preparing" -> "Ready for Pickup"
            "Ready for Pickup" -> "Completed"
            else -> this
        }
    }
}
