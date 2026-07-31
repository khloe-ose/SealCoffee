package com.mobdeve.s15.group4.sealcoffee.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.mobdeve.s15.group4.sealcoffee.data.local.AppDatabase
import com.mobdeve.s15.group4.sealcoffee.data.local.CartItemEntity
import com.mobdeve.s15.group4.sealcoffee.data.local.CartItemWithMenu
import com.mobdeve.s15.group4.sealcoffee.data.local.MenuItemEntity
import com.mobdeve.s15.group4.sealcoffee.data.local.OrderEntity
import com.mobdeve.s15.group4.sealcoffee.data.local.OrderItemEntity
import com.mobdeve.s15.group4.sealcoffee.data.local.UserEntity
import com.mobdeve.s15.group4.sealcoffee.domain.MenuCategory
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.PasswordHasher
import com.mobdeve.s15.group4.sealcoffee.domain.PricingCalculator
import com.mobdeve.s15.group4.sealcoffee.domain.ProductOptions
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationInput
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationValidator
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface RegistrationResult {
    data class Success(val user: UserEntity) : RegistrationResult
    data class Invalid(val errors: Map<com.mobdeve.s15.group4.sealcoffee.domain.RegistrationField, String>) : RegistrationResult
    data object DuplicateEmail : RegistrationResult
    data class Failure(val message: String) : RegistrationResult
}

sealed interface CheckoutResult {
    data class Success(val orderId: Long, val orderNumber: String) : CheckoutResult
    data class Failure(val message: String) : CheckoutResult
}

data class CartConfiguration(
    val quantity: Int,
    val size: String,
    val addOns: List<String>,
    val notes: String = ""
)

data class MenuEditorInput(
    val id: Long = 0,
    val name: String,
    val category: String,
    val description: String,
    val ingredients: List<String>,
    val basePriceCentavos: Long,
    val available: Boolean
)

class SealCoffeeRepository(val database: AppDatabase) {
    private val users = database.userDao()
    private val menu = database.menuDao()
    private val cart = database.cartDao()
    private val orders = database.orderDao()

    fun observeCustomerMenu() = menu.observeCustomerMenu()
    fun observeEmployeeMenu() = menu.observeEmployeeMenu()
    fun observeCart(customerId: Long) = cart.observeCart(customerId)
    fun observeUser(userId: Long) = users.observeById(userId)
    fun observeDashboardCounts() = orders.observeDashboardCounts()
    fun observeOrders(statuses: Collection<OrderStatus>) =
        orders.observeOrdersByStatuses(statuses.map { it.name })

    fun observeCustomerOrders(customerId: Long, statuses: Collection<OrderStatus>) =
        orders.observeCustomerOrders(customerId, statuses.map { it.name })

    fun observeCustomerHistory(query: String) = orders.observeCustomerHistory(query.trim())

    suspend fun getMenuItem(id: Long) = menu.findById(id)
    suspend fun getOrder(id: Long) = orders.getOrderDetails(id)
    suspend fun getUser(id: Long) = users.findById(id)

    suspend fun register(input: RegistrationInput): RegistrationResult {
        val errors = RegistrationValidator.validate(input)
        if (errors.isNotEmpty()) return RegistrationResult.Invalid(errors)
        val email = RegistrationValidator.normaliseEmail(input.email)
        if (users.findByEmail(email) != null) return RegistrationResult.DuplicateEmail

        val password = input.password.toCharArray()
        return try {
            val digest = PasswordHasher.create(password)
            val fullName = "${input.firstName.trim()} ${input.lastName.trim()}".replace(Regex("\\s+"), " ")
            val entity = UserEntity(
                fullName = fullName,
                birthDate = input.birthDateIso.trim(),
                email = email,
                contactNumber = input.contactNumber.trim(),
                passwordHash = digest.hash,
                passwordSalt = digest.salt,
                role = UserRole.CUSTOMER.name,
                createdAt = System.currentTimeMillis()
            )
            val id = users.insert(entity)
            RegistrationResult.Success(entity.copy(id = id))
        } catch (_: SQLiteConstraintException) {
            RegistrationResult.DuplicateEmail
        } catch (error: Exception) {
            RegistrationResult.Failure(error.message ?: "Unable to create the account")
        } finally {
            password.fill('\u0000')
        }
    }

    suspend fun authenticate(emailInput: String, passwordInput: String): UserEntity? {
        val email = RegistrationValidator.normaliseEmail(emailInput)
        val user = users.findByEmail(email) ?: return null
        val password = passwordInput.toCharArray()
        return try {
            user.takeIf { PasswordHasher.verify(password, user.passwordHash, user.passwordSalt) }
        } finally {
            password.fill('\u0000')
        }
    }

    suspend fun addToCart(customerId: Long, menuItemId: Long, configuration: CartConfiguration): Result<Unit> =
        runCatching {
            require(configuration.quantity > 0) { "Quantity must be at least one" }
            database.withTransaction {
                val item = menu.findById(menuItemId)
                    ?: error("This product no longer exists")
                check(item.available && !item.archived) { "This product is currently unavailable" }
                val size = PricingCalculator.normaliseSize(item.category, configuration.size)
                val addOns = PricingCalculator.normaliseAddOns(item.category, configuration.addOns)
                val addOnsCsv = StringListCodec.encode(addOns)
                val notes = configuration.notes.trim().take(200)
                val existing = cart.findMatching(customerId, menuItemId, size, addOnsCsv, notes)
                if (existing == null) {
                    cart.insert(
                        CartItemEntity(
                            customerId = customerId,
                            menuItemId = menuItemId,
                            quantity = configuration.quantity,
                            size = size,
                            addOnsCsv = addOnsCsv,
                            notes = notes
                        )
                    )
                } else {
                    cart.update(existing.copy(quantity = Math.addExact(existing.quantity, configuration.quantity)))
                }
            }
        }

    suspend fun updateCartConfiguration(
        customerId: Long,
        cartItemId: Long,
        configuration: CartConfiguration
    ): Result<Unit> = runCatching {
        require(configuration.quantity > 0) { "Quantity must be at least one" }
        database.withTransaction {
            val existing = cart.findOwned(cartItemId, customerId) ?: error("Cart item was not found")
            val item = menu.findById(existing.menuItemId) ?: error("Product was not found")
            check(item.available && !item.archived) { "This product is currently unavailable" }
            val size = PricingCalculator.normaliseSize(item.category, configuration.size)
            val addOns = StringListCodec.encode(PricingCalculator.normaliseAddOns(item.category, configuration.addOns))
            val notes = configuration.notes.trim().take(200)
            val matching = cart.findMatching(customerId, existing.menuItemId, size, addOns, notes)
            if (matching != null && matching.id != existing.id) {
                cart.update(matching.copy(quantity = Math.addExact(matching.quantity, configuration.quantity)))
                cart.deleteOwned(existing.id, customerId)
            } else {
                cart.update(existing.copy(quantity = configuration.quantity, size = size, addOnsCsv = addOns, notes = notes))
            }
        }
    }

    suspend fun updateCartQuantity(customerId: Long, cartItemId: Long, quantity: Int): Boolean {
        if (quantity < 1) return false
        return cart.updateQuantity(cartItemId, customerId, quantity) == 1
    }

    suspend fun removeCartItem(customerId: Long, cartItemId: Long): CartItemEntity? {
        val existing = cart.findOwned(cartItemId, customerId) ?: return null
        return existing.takeIf { cart.deleteOwned(cartItemId, customerId) == 1 }
    }

    suspend fun restoreCartItem(item: CartItemEntity): Boolean =
        runCatching { cart.insert(item); true }.getOrDefault(false)

    suspend fun checkout(customerId: Long): CheckoutResult = try {
        database.withTransaction {
            val items = cart.getCart(customerId)
            if (items.isEmpty()) return@withTransaction CheckoutResult.Failure("Your cart is empty")
            val unavailable = items.firstOrNull { it.menuItem.archived || !it.menuItem.available }
            if (unavailable != null) {
                return@withTransaction CheckoutResult.Failure("${unavailable.menuItem.name} is unavailable")
            }
            val priced = items.map { it to priceCartItem(it) }
            val subtotal = PricingCalculator.orderTotalCentavos(priced.map { it.second })
            val orderNumber = generateOrderNumber()
            val orderId = orders.insertOrder(
                OrderEntity(
                    orderNumber = orderNumber,
                    customerId = customerId,
                    status = OrderStatus.PENDING.name,
                    placedAt = System.currentTimeMillis(),
                    subtotalCentavos = subtotal,
                    totalCentavos = subtotal
                )
            )
            orders.insertOrderItems(priced.map { (cartLine, lineTotal) ->
                val unitPrice = PricingCalculator.unitPriceCentavos(
                    cartLine.menuItem.basePriceCentavos,
                    cartLine.menuItem.category,
                    cartLine.cartItem.size,
                    StringListCodec.decode(cartLine.cartItem.addOnsCsv)
                )
                OrderItemEntity(
                    orderId = orderId,
                    menuItemId = cartLine.menuItem.id,
                    itemNameSnapshot = cartLine.menuItem.name,
                    categorySnapshot = cartLine.menuItem.category,
                    quantity = cartLine.cartItem.quantity,
                    sizeSnapshot = cartLine.cartItem.size,
                    addOnsSnapshotCsv = cartLine.cartItem.addOnsCsv,
                    notesSnapshot = cartLine.cartItem.notes,
                    unitPriceCentavos = unitPrice,
                    lineTotalCentavos = lineTotal
                )
            })
            cart.clearCustomerCart(customerId)
            CheckoutResult.Success(orderId, orderNumber)
        }
    } catch (error: Exception) {
        CheckoutResult.Failure(error.message ?: "The order could not be placed")
    }

    suspend fun saveMenuItem(input: MenuEditorInput): Result<Long> = runCatching {
        require(input.name.trim().length >= 2) { "Enter a product name" }
        val category = MenuCategory.fromLabel(input.category)?.label ?: error("Choose a valid category")
        require(input.description.trim().length >= 5) { "Enter a useful description" }
        require(input.ingredients.isNotEmpty()) { "Enter at least one ingredient" }
        require(input.basePriceCentavos > 0) { "Price must be greater than zero" }
        val existing = input.id.takeIf { it > 0 }?.let { menu.findById(it) }
        val entity = MenuItemEntity(
            id = existing?.id ?: 0,
            seedKey = existing?.seedKey,
            name = input.name.trim(),
            category = category,
            description = input.description.trim(),
            ingredientsCsv = StringListCodec.encode(input.ingredients),
            basePriceCentavos = input.basePriceCentavos,
            imageKey = existing?.imageKey ?: "custom",
            featured = existing?.featured ?: false,
            available = input.available,
            archived = false
        )
        if (existing == null) menu.insert(entity) else {
            check(menu.update(entity) == 1) { "Menu item was not found" }
            entity.id
        }
    }

    suspend fun setAvailability(id: Long, available: Boolean): Boolean =
        menu.setAvailability(id, available) == 1

    suspend fun archiveMenuItem(id: Long): Boolean = menu.archive(id) == 1

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Boolean =
        orders.updateStatus(orderId, status.name) == 1

    suspend fun advanceOrder(orderId: Long): Result<OrderStatus> = runCatching {
        database.withTransaction {
            val current = orders.getOrderDetails(orderId) ?: error("Order was not found")
            val currentStatus = OrderStatus.fromStorage(current.order.status) ?: error("Order status is invalid")
            val next = currentStatus.nextPreparationStatus()
            check(next != currentStatus) {
                if (currentStatus == OrderStatus.DELAYED) {
                    "Choose a status manually for delayed orders"
                } else {
                    "Completed orders cannot be advanced"
                }
            }
            check(orders.updateStatus(orderId, next.name) == 1) { "Order status was not updated" }
            next
        }
    }

    fun priceCartItem(item: CartItemWithMenu): Long {
        val unit = PricingCalculator.unitPriceCentavos(
            item.menuItem.basePriceCentavos,
            item.menuItem.category,
            item.cartItem.size,
            StringListCodec.decode(item.cartItem.addOnsCsv)
        )
        return PricingCalculator.lineTotalCentavos(unit, item.cartItem.quantity)
    }

    private fun generateOrderNumber(): String {
        val timestamp = SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())
        val suffix = UUID.randomUUID().toString().take(4).uppercase(Locale.ROOT)
        return "SC-$timestamp-$suffix"
    }
}
