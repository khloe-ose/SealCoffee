package com.mobdeve.s15.group4.sealcoffee

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobdeve.s15.group4.sealcoffee.data.CartConfiguration
import com.mobdeve.s15.group4.sealcoffee.data.CheckoutResult
import com.mobdeve.s15.group4.sealcoffee.data.MenuEditorInput
import com.mobdeve.s15.group4.sealcoffee.data.RegistrationResult
import com.mobdeve.s15.group4.sealcoffee.data.SealCoffeeRepository
import com.mobdeve.s15.group4.sealcoffee.data.local.AppDatabase
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.data.local.UserEntity
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.ProductOptions
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationInput
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: SealCoffeeRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = SealCoffeeRepository(database)
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun registration_rejectsCaseInsensitiveDuplicateAndAuthenticatesPassword() = runBlocking {
        val input = registration("New.User@example.com")
        val first = repository.register(input)
        val duplicate = repository.register(input.copy(email = " new.user@EXAMPLE.COM "))

        assertTrue(first is RegistrationResult.Success)
        assertTrue(duplicate is RegistrationResult.DuplicateEmail)
        assertNotNull(repository.authenticate("NEW.USER@example.com", "Strong123!"))
        assertEquals(null, repository.authenticate("new.user@example.com", "Wrong123!"))
    }

    @Test
    fun cart_addMergeUpdateRemove_isPersistentAndCustomerScoped() = runBlocking {
        val firstUser = insertUser("one@example.com")
        val secondUser = insertUser("two@example.com")
        val menuId = insertMenu()
        val config = CartConfiguration(
            2,
            ProductOptions.SIZE_LARGE,
            listOf(ProductOptions.ADD_ON_OAT_MILK),
            "Less ice"
        )

        repository.addToCart(firstUser, menuId, config).getOrThrow()
        repository.addToCart(firstUser, menuId, config.copy(quantity = 1)).getOrThrow()
        repository.addToCart(secondUser, menuId, config.copy(quantity = 1)).getOrThrow()
        var firstCart = database.cartDao().getCart(firstUser)

        assertEquals(1, firstCart.size)
        assertEquals(3, firstCart.single().cartItem.quantity)
        assertEquals(1, database.cartDao().getCart(secondUser).size)
        assertTrue(repository.updateCartQuantity(firstUser, firstCart.single().cartItem.id, 4))
        firstCart = database.cartDao().getCart(firstUser)
        assertEquals(4, firstCart.single().cartItem.quantity)
        assertNotNull(repository.removeCartItem(firstUser, firstCart.single().cartItem.id))
        assertTrue(database.cartDao().getCart(firstUser).isEmpty())
        assertEquals(1, database.cartDao().getCart(secondUser).size)
    }

    @Test
    fun checkout_createsSnapshotsTotalsAndClearsOnlyOrderingCustomerCart() = runBlocking {
        val firstUser = insertUser("checkout@example.com")
        val secondUser = insertUser("other@example.com")
        val menuId = insertMenu(price = 16_500)
        val config = CartConfiguration(
            2,
            ProductOptions.SIZE_LARGE,
            listOf(ProductOptions.ADD_ON_EXTRA_SHOT),
            "No sugar"
        )
        repository.addToCart(firstUser, menuId, config).getOrThrow()
        repository.addToCart(secondUser, menuId, config.copy(quantity = 1)).getOrThrow()

        val result = repository.checkout(firstUser)
        assertTrue(result is CheckoutResult.Success)
        val success = result as CheckoutResult.Success
        val order = repository.getOrder(success.orderId)

        assertNotNull(order)
        assertEquals(OrderStatus.PENDING, OrderStatus.fromStorage(order!!.order.status))
        assertEquals(42_000, order.order.totalCentavos)
        assertEquals("Test Latte", order.items.single().itemNameSnapshot)
        assertEquals("No sugar", order.items.single().notesSnapshot)
        assertTrue(database.cartDao().getCart(firstUser).isEmpty())
        assertEquals(1, database.cartDao().getCart(secondUser).size)
        assertTrue(repository.observeCustomerOrders(firstUser, OrderStatus.active).first().all {
            it.order.customerId == firstUser
        })
        assertTrue(repository.observeCustomerOrders(secondUser, OrderStatus.active).first().isEmpty())
    }

    @Test
    fun menuCrudAvailabilityAndArchive_updatesQueriesWithoutRewritingOrders() = runBlocking {
        val menuId = repository.saveMenuItem(
            MenuEditorInput(
                name = "Offline Cocoa",
                category = "Non-Coffee",
                description = "Rich local cocoa drink",
                ingredients = listOf("Cocoa", "Milk"),
                basePriceCentavos = 14_500,
                available = true
            )
        ).getOrThrow()

        assertEquals("Offline Cocoa", repository.getMenuItem(menuId)?.name)
        assertTrue(repository.setAvailability(menuId, false))
        assertFalse(repository.getMenuItem(menuId)!!.available)
        assertTrue(repository.archiveMenuItem(menuId))
        assertTrue(repository.observeCustomerMenu().first().none { it.id == menuId })
        assertTrue(repository.observeEmployeeMenu().first().none { it.id == menuId })
    }

    private suspend fun insertUser(email: String): Long =
        database.userDao().insert(
            UserEntity(
                fullName = email.substringBefore("@"),
                birthDate = "2000-01-01",
                email = email,
                contactNumber = "+639171234567",
                passwordHash = "test-only",
                passwordSalt = "test-only",
                role = UserRole.CUSTOMER.name,
                createdAt = 1
            )
        )

    private suspend fun insertMenu(price: Long = 16_500): Long =
        database.menuDao().insert(
            MenuItemEntity(
                name = "Test Latte",
                category = "Coffee",
                description = "A test coffee drink",
                ingredientsCsv = "Espresso",
                basePriceCentavos = price,
                imageKey = "custom"
            )
        )

    private fun registration(email: String) = RegistrationInput(
        firstName = "New",
        lastName = "User",
        birthDateIso = "2000-01-01",
        email = email,
        contactNumber = "+639171234567",
        password = "Strong123!"
    )
}
