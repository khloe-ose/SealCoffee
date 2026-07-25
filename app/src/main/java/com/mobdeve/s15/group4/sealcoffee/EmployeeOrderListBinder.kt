package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import kotlinx.coroutines.launch

object EmployeeOrderListBinder {
    fun bind(
        activity: AppCompatActivity,
        title: String,
        subtitle: String,
        statuses: Set<OrderStatus>
    ) {
        activity.findViewById<TextView>(R.id.employeeOrderListTitleText).text = title
        activity.findViewById<TextView>(R.id.employeeOrderListSubtitleText).text = subtitle
        val emptyText = activity.findViewById<TextView>(R.id.employeeOrderEmptyText)
        val adapter = EmployeeOrderAdapter { details ->
            activity.startActivity(
                Intent(activity, EmployeeOrderDetailsActivity::class.java)
                    .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, details.order.id)
            )
        }
        val recyclerView = activity.findViewById<RecyclerView>(R.id.employeeOrderRecyclerView).apply {
            layoutManager = LinearLayoutManager(activity)
            this.adapter = adapter
        }
        attachOrderGestures(activity, recyclerView, adapter)
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activity.sealApp.repository.observeOrders(statuses).collect {
                    adapter.submitList(it)
                    emptyText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun attachOrderGestures(
        activity: AppCompatActivity,
        recyclerView: RecyclerView,
        adapter: EmployeeOrderAdapter
    ) {
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
                val details = adapter.itemAt(position)
                if (details == null) {
                    if (position != RecyclerView.NO_POSITION) adapter.notifyItemChanged(position)
                    return
                }
                val current = OrderStatus.fromStorage(details.order.status)
                if (current == null || current == OrderStatus.COMPLETED) {
                    adapter.notifyItemChanged(position)
                    Toast.makeText(activity, R.string.completed_order_no_swipe, Toast.LENGTH_SHORT).show()
                    return
                }
                activity.lifecycleScope.launch {
                    if (direction == ItemTouchHelper.LEFT) {
                        if (current == OrderStatus.DELAYED) {
                            adapter.notifyItemChanged(position)
                            Toast.makeText(activity, R.string.already_delayed, Toast.LENGTH_SHORT).show()
                        } else if (activity.sealApp.repository.updateOrderStatus(
                                details.order.id,
                                OrderStatus.DELAYED
                            )
                        ) {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.order_marked_status, details.order.orderNumber, OrderStatus.DELAYED.label),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            adapter.notifyItemChanged(position)
                        }
                    } else {
                        activity.sealApp.repository.advanceOrder(details.order.id)
                            .onSuccess { next ->
                                Toast.makeText(
                                    activity,
                                    activity.getString(R.string.order_marked_status, details.order.orderNumber, next.label),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .onFailure {
                                adapter.notifyItemChanged(position)
                                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
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
                    val isAdvance = dX > 0
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = ContextCompat.getColor(
                            activity,
                            if (isAdvance) R.color.seal_success else R.color.seal_error
                        )
                    }
                    val left = if (isAdvance) itemView.left.toFloat() else itemView.right + dX
                    val right = if (isAdvance) itemView.left + dX else itemView.right.toFloat()
                    canvas.drawRect(left, itemView.top.toFloat(), right, itemView.bottom.toFloat(), paint)
                    paint.color = ContextCompat.getColor(activity, R.color.white)
                    paint.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        14f,
                        activity.resources.displayMetrics
                    )
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.textAlign = if (isAdvance) Paint.Align.LEFT else Paint.Align.RIGHT
                    val x = if (isAdvance) itemView.left + 22f else itemView.right - 22f
                    val y = itemView.top + itemView.height / 2f - (paint.ascent() + paint.descent()) / 2f
                    canvas.drawText(
                        activity.getString(if (isAdvance) R.string.swipe_advance else R.string.swipe_delay),
                        x,
                        y,
                        paint
                    )
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}
