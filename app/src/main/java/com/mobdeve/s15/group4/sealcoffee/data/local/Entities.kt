package com.mobdeve.s15.group4.sealcoffee.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "birth_date") val birthDate: String,
    @ColumnInfo(name = "email", collate = ColumnInfo.NOCASE) val email: String,
    @ColumnInfo(name = "contact_number") val contactNumber: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "password_salt") val passwordSalt: String,
    val role: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "menu_items",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["archived", "available"])
    ]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "seed_key") val seedKey: String? = null,
    val name: String,
    val category: String,
    val description: String,
    @ColumnInfo(name = "ingredients_csv") val ingredientsCsv: String,
    @ColumnInfo(name = "base_price_centavos") val basePriceCentavos: Long,
    @ColumnInfo(name = "image_key") val imageKey: String,
    val featured: Boolean = false,
    val available: Boolean = true,
    val archived: Boolean = false
)

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["menu_item_id"]),
        Index(
            value = ["customer_id", "menu_item_id", "size", "add_ons_csv", "notes"],
            unique = true
        )
    ]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "customer_id") val customerId: Long,
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long,
    val quantity: Int,
    val size: String,
    @ColumnInfo(name = "add_ons_csv") val addOnsCsv: String,
    val notes: String = ""
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["order_number"], unique = true),
        Index(value = ["customer_id"]),
        Index(value = ["status"]),
        Index(value = ["placed_at"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_number") val orderNumber: String,
    @ColumnInfo(name = "customer_id") val customerId: Long,
    val status: String,
    @ColumnInfo(name = "placed_at") val placedAt: Long,
    @ColumnInfo(name = "subtotal_centavos") val subtotalCentavos: Long,
    @ColumnInfo(name = "total_centavos") val totalCentavos: Long,
    @ColumnInfo(name = "order_type") val orderType: String = "Pickup",
    @ColumnInfo(name = "payment_label") val paymentLabel: String = "Pay at counter"
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_item_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["order_id"]), Index(value = ["menu_item_id"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "menu_item_id") val menuItemId: Long?,
    @ColumnInfo(name = "item_name_snapshot") val itemNameSnapshot: String,
    @ColumnInfo(name = "category_snapshot") val categorySnapshot: String,
    val quantity: Int,
    @ColumnInfo(name = "size_snapshot") val sizeSnapshot: String,
    @ColumnInfo(name = "add_ons_snapshot_csv") val addOnsSnapshotCsv: String,
    @ColumnInfo(name = "notes_snapshot") val notesSnapshot: String,
    @ColumnInfo(name = "unit_price_centavos") val unitPriceCentavos: Long,
    @ColumnInfo(name = "line_total_centavos") val lineTotalCentavos: Long
)
