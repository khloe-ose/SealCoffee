package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

object EmployeeOrderListBinder {
    fun bind(
        activity: AppCompatActivity,
        title: String,
        subtitle: String,
        statuses: Set<String>
    ) {
        activity.findViewById<TextView>(R.id.employeeOrderListTitleText).text = title
        activity.findViewById<TextView>(R.id.employeeOrderListSubtitleText).text = subtitle
        val emptyText = activity.findViewById<TextView>(R.id.employeeOrderEmptyText)

        val adapter = EmployeeOrderAdapter { order ->
            val orderNumber = order["orderNumber"] as? String ?: return@EmployeeOrderAdapter
            activity.startActivity(
                Intent(activity, EmployeeOrderDetailsActivity::class.java)
                    .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_NUMBER, orderNumber)
            )
        }

        val recyclerView = activity.findViewById<RecyclerView>(R.id.employeeOrderRecyclerView).apply {
            layoutManager = LinearLayoutManager(activity)
            this.adapter = adapter
        }

        attachOrderGestures(activity, recyclerView, adapter)

        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    emptyText.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                val orders = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.toMutableMap() ?: return@mapNotNull null
                    data["documentId"] = doc.id
                    val status = (data["status"] as? String ?: "").uppercase()
                    if (statuses.isEmpty() || statuses.contains(status)) {
                        data
                    } else {
                        null
                    }
                }

                adapter.submitList(orders)
                emptyText.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
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
                val order = adapter.itemAt(position)
                if (order == null) {
                    if (position != RecyclerView.NO_POSITION) adapter.notifyItemChanged(position)
                    return
                }

                val docId = order["documentId"] as? String
                val orderNumber = order["orderNumber"] as? String ?: "Order"
                val current = (order["status"] as? String)?.uppercase() ?: "PENDING"

                if (current == "COMPLETED") {
                    adapter.notifyItemChanged(position)
                    Toast.makeText(activity, R.string.completed_order_no_swipe, Toast.LENGTH_SHORT).show()
                    return
                }

                if (docId == null) {
                    adapter.notifyItemChanged(position)
                    return
                }

                val db = FirebaseFirestore.getInstance()

                if (direction == ItemTouchHelper.LEFT) {
                    if (current == "DELAYED") {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(activity, R.string.already_delayed, Toast.LENGTH_SHORT).show()
                    } else {
                        db.collection("orders").document(docId)
                            .update("status", "DELAYED")
                            .addOnSuccessListener {
                                Toast.makeText(
                                    activity,
                                    activity.getString(R.string.order_marked_status, orderNumber, "Delayed"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                adapter.notifyItemChanged(position)
                                Toast.makeText(activity, "Failed to update status", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Right swipe advances the status flow: PENDING -> PREPARING -> READY -> COMPLETED
                    val nextStatus = when (current) {
                        "PENDING" -> "PREPARING"
                        "PREPARING" -> "READY_FOR_PICKUP"
                        "READY_FOR_PICKUP" -> "COMPLETED"
                        "DELAYED" -> "PREPARING"
                        else -> "COMPLETED"
                    }

                    db.collection("orders").document(docId)
                        .update("status", nextStatus)
                        .addOnSuccessListener {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.order_marked_status, orderNumber, nextStatus.lowercase().replaceFirstChar { it.uppercase() }),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            adapter.notifyItemChanged(position)
                            Toast.makeText(activity, "Failed to update status", Toast.LENGTH_SHORT).show()
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