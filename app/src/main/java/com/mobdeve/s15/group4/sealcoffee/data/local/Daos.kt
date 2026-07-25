package com.mobdeve.s15.group4.sealcoffee.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Insert
    suspend fun insert(user: UserEntity): Long
}

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_items WHERE archived = 0 ORDER BY featured DESC, name COLLATE NOCASE")
    fun observeCustomerMenu(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE archived = 0 ORDER BY category, name COLLATE NOCASE")
    fun observeEmployeeMenu(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: MenuItemEntity): Long

    @Update
    suspend fun update(item: MenuItemEntity): Int

    @Query("UPDATE menu_items SET available = :available WHERE id = :id AND archived = 0")
    suspend fun setAvailability(id: Long, available: Boolean): Int

    @Query("UPDATE menu_items SET archived = 1, available = 0 WHERE id = :id")
    suspend fun archive(id: Long): Int
}

@Dao
interface CartDao {
    @Transaction
    @Query("SELECT * FROM cart_items WHERE customer_id = :customerId ORDER BY id DESC")
    fun observeCart(customerId: Long): Flow<List<CartItemWithMenu>>

    @Transaction
    @Query("SELECT * FROM cart_items WHERE customer_id = :customerId ORDER BY id")
    suspend fun getCart(customerId: Long): List<CartItemWithMenu>

    @Query(
        """
        SELECT * FROM cart_items
        WHERE customer_id = :customerId AND menu_item_id = :menuItemId
          AND size = :size AND add_ons_csv = :addOnsCsv AND notes = :notes
        LIMIT 1
        """
    )
    suspend fun findMatching(
        customerId: Long,
        menuItemId: Long,
        size: String,
        addOnsCsv: String,
        notes: String
    ): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE id = :id AND customer_id = :customerId LIMIT 1")
    suspend fun findOwned(id: Long, customerId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: CartItemEntity): Long

    @Update
    suspend fun update(item: CartItemEntity): Int

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id AND customer_id = :customerId")
    suspend fun updateQuantity(id: Long, customerId: Long, quantity: Int): Int

    @Query("DELETE FROM cart_items WHERE id = :id AND customer_id = :customerId")
    suspend fun deleteOwned(id: Long, customerId: Long): Int

    @Query("DELETE FROM cart_items WHERE customer_id = :customerId")
    suspend fun clearCustomerCart(customerId: Long): Int
}

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND status IN (:statuses) ORDER BY placed_at DESC")
    fun observeCustomerOrders(customerId: Long, statuses: List<String>): Flow<List<OrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status IN (:statuses) ORDER BY placed_at DESC")
    fun observeOrdersByStatuses(statuses: List<String>): Flow<List<OrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderDetails(orderId: Long): OrderWithDetails?

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String): Int

    @Query(
        """
        SELECT COUNT(*) AS total_count,
          COALESCE(SUM(CASE WHEN status IN ('PENDING','PREPARING','READY_FOR_PICKUP') THEN 1 ELSE 0 END), 0) AS active_count,
          COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed_count,
          COALESCE(SUM(CASE WHEN status = 'DELAYED' THEN 1 ELSE 0 END), 0) AS delayed_count
        FROM orders
        """
    )
    fun observeDashboardCounts(): Flow<DashboardCounts>

    @Transaction
    @Query(
        """
        SELECT o.* FROM orders o INNER JOIN users u ON u.id = o.customer_id
        WHERE :query = ''
           OR u.full_name LIKE '%' || :query || '%' COLLATE NOCASE
           OR u.email LIKE '%' || :query || '%' COLLATE NOCASE
           OR o.order_number LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY o.placed_at DESC
        """
    )
    fun observeCustomerHistory(query: String): Flow<List<OrderWithDetails>>
}
