package com.mobdeve.s15.group4.sealcoffee.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class CartItemWithMenu(
    @Embedded val cartItem: CartItemEntity,
    @Relation(parentColumn = "menu_item_id", entityColumn = "id")
    val menuItem: MenuItemEntity
)

data class OrderWithDetails(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "customer_id", entityColumn = "id")
    val customer: UserEntity,
    @Relation(parentColumn = "id", entityColumn = "order_id")
    val items: List<OrderItemEntity>
)

data class DashboardCounts(
    @ColumnInfo(name = "total_count") val totalCount: Int,
    @ColumnInfo(name = "active_count") val activeCount: Int,
    @ColumnInfo(name = "completed_count") val completedCount: Int,
    @ColumnInfo(name = "delayed_count") val delayedCount: Int
)
