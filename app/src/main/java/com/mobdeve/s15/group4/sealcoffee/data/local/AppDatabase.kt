package com.mobdeve.s15.group4.sealcoffee.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.PasswordHasher
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole

@Database(
    entities = [
        UserEntity::class,
        MenuItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun menuDao(): MenuDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val DATABASE_NAME = "sealcoffee.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).addCallback(SeedCallback()).build().also { instance = it }
            }
    }
}

private class SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        val customerPassword = PasswordHasher.create("Coffee123!".toCharArray())
        val employeePassword = PasswordHasher.create("Staff123!".toCharArray())

        db.execSQL(
            """
            INSERT INTO users
            (id, full_name, birth_date, email, contact_number, password_hash, password_salt, role, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                1L, "Mika Santos", "2002-03-14", "mika.santos@gmail.com", "+63 917 555 0148",
                customerPassword.hash, customerPassword.salt, UserRole.CUSTOMER.name, now
            )
        )
        db.execSQL(
            """
            INSERT INTO users
            (id, full_name, birth_date, email, contact_number, password_hash, password_salt, role, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                2L, "Carlo Dela Cruz", "1999-08-09", "carlo.staff@sealcoffee.com", "+63 917 555 0192",
                employeePassword.hash, employeePassword.salt, UserRole.EMPLOYEE.name, now
            )
        )

        val menu = listOf(
            SeedMenu(1, "signature_latte", "Signature Latte", "Coffee", "Espresso with steamed milk and a smooth caramel finish.", listOf("Espresso", "Steamed milk", "Caramel", "Sea salt cream"), 16_500, "signature_latte", true),
            SeedMenu(2, "coffee_spanish_latte", "Spanish Latte", "Coffee", "Creamy espresso blend sweetened with condensed milk.", listOf("Espresso", "Fresh milk", "Condensed milk"), 17_500, "spanish_latte", true),
            SeedMenu(3, "coffee_americano", "Americano", "Coffee", "Bold espresso topped with hot water for a clean finish.", listOf("Espresso", "Filtered water"), 12_000, "americano"),
            SeedMenu(4, "coffee_mocha", "Cafe Mocha", "Coffee", "Espresso, milk, and chocolate with a balanced sweetness.", listOf("Espresso", "Milk", "Chocolate sauce"), 17_000, "cafe_mocha"),
            SeedMenu(5, "noncoffee_matcha", "Matcha Cream", "Non-Coffee", "Earthy matcha latte with a light cream topping.", listOf("Matcha", "Milk", "Vanilla cream"), 18_000, "matcha_cream", true),
            SeedMenu(6, "coffee_cold_brew", "Cold Brew", "Coffee", "Slow-steeped coffee served over ice.", listOf("Cold brew coffee", "Ice"), 15_000, "cold_brew"),
            SeedMenu(7, "snack_croissant", "Butter Croissant", "Snacks", "Flaky butter croissant warmed before serving.", listOf("Butter pastry", "Sea salt"), 9_500, "butter_croissant"),
            SeedMenu(8, "snack_muffin", "Blueberry Muffin", "Desserts", "Soft muffin packed with blueberries.", listOf("Blueberries", "Vanilla batter", "Sugar crumb"), 10_500, "blueberry_muffin"),
            SeedMenu(9, "dessert_cheesecake", "Mini Cheesecake", "Desserts", "Creamy cheesecake with a buttery crumb base.", listOf("Cream cheese", "Strawberry", "Vanilla"), 13_500, "mini_cheesecake", available = false)
        )
        menu.forEach { item ->
            db.execSQL(
                """
                INSERT INTO menu_items
                (id, seed_key, name, category, description, ingredients_csv, base_price_centavos,
                 image_key, featured, available, archived)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """.trimIndent(),
                arrayOf<Any?>(
                    item.id, item.seedKey, item.name, item.category, item.description,
                    StringListCodec.encode(item.ingredients), item.priceCentavos, item.imageKey,
                    if (item.featured) 1 else 0, if (item.available) 1 else 0
                )
            )
        }

        val historicalTime = now - 86_400_000L
        db.execSQL(
            """
            INSERT INTO orders
            (id, order_number, customer_id, status, placed_at, subtotal_centavos, total_centavos,
             order_type, payment_label)
            VALUES (1, 'SC-DEMO-0001', 1, ?, ?, 26000, 26000, 'Pickup', 'Paid at counter')
            """.trimIndent(),
            arrayOf<Any?>(OrderStatus.COMPLETED.name, historicalTime)
        )
        db.execSQL(
            """
            INSERT INTO order_items
            (id, order_id, menu_item_id, item_name_snapshot, category_snapshot, quantity,
             size_snapshot, add_ons_snapshot_csv, notes_snapshot, unit_price_centavos, line_total_centavos)
            VALUES (1, 1, 1, 'Signature Latte', 'Coffee', 1, 'Regular', '', '', 16500, 16500)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO order_items
            (id, order_id, menu_item_id, item_name_snapshot, category_snapshot, quantity,
             size_snapshot, add_ons_snapshot_csv, notes_snapshot, unit_price_centavos, line_total_centavos)
            VALUES (2, 1, 7, 'Butter Croissant', 'Snacks', 1, 'Single', '', '', 9500, 9500)
            """.trimIndent()
        )
    }
}

private data class SeedMenu(
    val id: Long,
    val seedKey: String,
    val name: String,
    val category: String,
    val description: String,
    val ingredients: List<String>,
    val priceCentavos: Long,
    val imageKey: String,
    val featured: Boolean = false,
    val available: Boolean = true
)
