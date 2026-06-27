package com.mobdeve.s15.group4.sealcoffee.data

import com.mobdeve.s15.group4.sealcoffee.R


    object DummyData {
        val menuItems = listOf(
            MenuItem(
                id = "signature_latte",
                name = "Signature Latte",
                category = "Coffee",
                description = "Espresso with steamed milk and a smooth caramel finish.",
                ingredients = listOf("Espresso", "Steamed milk", "Caramel", "Sea salt cream"),
                price = 165.0,
                imageResId = R.drawable.img_signature_latte,
                isFeatured = true
            ),
            MenuItem(
                id = "coffee_spanish_latte",
                name = "Spanish Latte",
                category = "Coffee",
                description = "Creamy espresso blend sweetened with condensed milk.",
                ingredients = listOf("Espresso", "Fresh milk", "Condensed milk"),
                price = 175.0,
                imageResId = R.drawable.img_spanish_latte,
                isFeatured = true
            ),
            MenuItem(
                id = "coffee_americano",
                name = "Americano",
                category = "Coffee",
                description = "Bold espresso topped with hot water for a clean finish.",
                ingredients = listOf("Espresso", "Filtered water"),
                price = 120.0,
                imageResId = R.drawable.img_americano
            ),
            MenuItem(
                id = "coffee_mocha",
                name = "Cafe Mocha",
                category = "Coffee",
                description = "Espresso, milk, and chocolate with a balanced sweetness.",
                ingredients = listOf("Espresso", "Milk", "Chocolate sauce"),
                price = 170.0,
                imageResId = R.drawable.img_cafe_mocha
            ),
            MenuItem(
                id = "noncoffee_matcha",
                name = "Matcha Cream",
                category = "Non-Coffee",
                description = "Earthy matcha latte with a light cream topping.",
                ingredients = listOf("Matcha", "Milk", "Vanilla cream"),
                price = 180.0,
                imageResId = R.drawable.img_matcha_cream,
                isFeatured = true
            ),
            MenuItem(
                id = "coffee_cold_brew",
                name = "Cold Brew",
                category = "Coffee",
                description = "Slow-steeped coffee served over ice.",
                ingredients = listOf("Cold brew coffee", "Ice"),
                price = 150.0,
                imageResId = R.drawable.img_cold_brew
            ),
            MenuItem(
                id = "snack_croissant",
                name = "Butter Croissant",
                category = "Snacks",
                description = "Flaky butter croissant warmed before serving.",
                ingredients = listOf("Butter pastry", "Sea salt"),
                price = 95.0,
                imageResId = R.drawable.img_butter_croissant
            ),
            MenuItem(
                id = "snack_muffin",
                name = "Blueberry Muffin",
                category = "Desserts",
                description = "Soft muffin packed with blueberries.",
                ingredients = listOf("Blueberries", "Vanilla batter", "Sugar crumb"),
                price = 105.0,
                imageResId = R.drawable.img_blueberry_muffin
            ),
            MenuItem(
                id = "dessert_cheesecake",
                name = "Mini Cheesecake",
                category = "Desserts",
                description = "Creamy cheesecake with a buttery crumb base.",
                ingredients = listOf("Cream cheese", "Strawberry", "Vanilla"),
                price = 135.0,
                imageResId = R.drawable.img_mini_cheesecake,
                isAvailable = false
            )
        )

        val cartItems = listOf(
            CartItem(
                id = "cart_001",
                menuItem = menuItems[0],
                quantity = 1,
                size = "Regular",
                temperature = "Iced",
                addOns = listOf("Extra espresso shot")
            ),
            CartItem(
                id = "cart_002",
                menuItem = menuItems[6],
                quantity = 2,
                size = "Single",
                temperature = "Warm",
                notes = "Serve with napkins"
            )
        )

        val customerOrders = listOf(
            Order(
                id = "ORD-1007",
                customerName = "Mika Santos",
                customerEmail = "mika.santos@gmail.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1007_1",
                        menuItemId = "coffee_spanish_latte",
                        name = "Spanish Latte",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Iced",
                        unitPrice = 175.0
                    ),
                    OrderItem(
                        id = "order_item_1007_2",
                        menuItemId = "snack_muffin",
                        name = "Blueberry Muffin",
                        quantity = 1,
                        size = "Single",
                        temperature = "Warm",
                        unitPrice = 105.0
                    )
                ),
                status = "Ready for pickup",
                orderType = "Pickup",
                paymentMethod = "GCash",
                placedAt = "Today, 9:18 AM",
                pickupTime = "Today, 9:35 AM"
            ),
            Order(
                id = "ORD-1008",
                customerName = "Mika Santos",
                customerEmail = "mika.santos@gmail.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1008_1",
                        menuItemId = "coffee_americano",
                        name = "Americano",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Hot",
                        unitPrice = 120.0
                    )
                ),
                status = "Preparing",
                orderType = "Pickup",
                paymentMethod = "Cash",
                placedAt = "Today, 9:16",
                pickupTime = "Today, 9:30 AM"
            ),
            Order(
                id = "ORD-1003",
                customerName = "Mika Santos",
                customerEmail = "mika.santos@gmail.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1003_1",
                        menuItemId = "coffee_americano",
                        name = "Americano",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Hot",
                        unitPrice = 120.0
                    )
                ),
                status = "Completed",
                orderType = "Pickup",
                paymentMethod = "Cash",
                placedAt = "Yesterday, 3:42 PM",
                pickupTime = "Yesterday, 3:50 PM"
            ),
            Order(
                id = "ORD-0998",
                customerName = "Mika Santos",
                customerEmail = "mika.santos@gmail.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_0998_1",
                        menuItemId = "coffee_cold_brew",
                        name = "Cold Brew",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Iced",
                        unitPrice = 150.0
                    ),
                    OrderItem(
                        id = "order_item_0998_2",
                        menuItemId = "dessert_cheesecake",
                        name = "Mini Cheesecake",
                        quantity = 1,
                        size = "Single",
                        temperature = "Chilled",
                        unitPrice = 135.0
                    )
                ),
                status = "Completed",
                orderType = "Pickup",
                paymentMethod = "Card",
                placedAt = "June 20, 2026, 11:08 AM",
                pickupTime = "June 20, 2026, 11:25 AM"
            )
        )

        val employeeOrders = mutableListOf(
            Order(
                id = "ORD-1011",
                customerName = "Luis Reyes",
                customerEmail = "luis.reyes@example.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1011_1",
                        menuItemId = "signature_latte",
                        name = "Signature Latte",
                        quantity = 2,
                        size = "Regular",
                        temperature = "Iced",
                        unitPrice = 165.0,
                        addOns = listOf("Less ice"),
                        notes = "Use oat milk for one cup."
                    )
                ),
                status = "Preparing",
                orderType = "Pickup",
                paymentMethod = "Card",
                placedAt = "Today, 10:04 AM",
                pickupTime = "Today, 10:20 AM"
            ),
            Order(
                id = "ORD-1010",
                customerName = "Ana Cruz",
                customerEmail = "ana.cruz@example.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1010_1",
                        menuItemId = "noncoffee_matcha",
                        name = "Matcha Cream",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Iced",
                        unitPrice = 180.0,
                        notes = "No whipped cream."
                    ),
                    OrderItem(
                        id = "order_item_1010_2",
                        menuItemId = "snack_croissant",
                        name = "Butter Croissant",
                        quantity = 1,
                        size = "Single",
                        temperature = "Warm",
                        unitPrice = 95.0
                    )
                ),
                status = "Pending",
                orderType = "Pickup",
                paymentMethod = "Cash",
                placedAt = "Today, 9:58 AM",
                pickupTime = "Today, 10:12 AM"
            ),
            Order(
                id = "ORD-1009",
                customerName = "Nina Flores",
                customerEmail = "nina.flores@example.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1009_1",
                        menuItemId = "coffee_mocha",
                        name = "Cafe Mocha",
                        quantity = 1,
                        size = "Large",
                        temperature = "Hot",
                        unitPrice = 170.0,
                        addOns = listOf("Caramel drizzle")
                    ),
                    OrderItem(
                        id = "order_item_1009_2",
                        menuItemId = "snack_muffin",
                        name = "Blueberry Muffin",
                        quantity = 2,
                        size = "Single",
                        temperature = "Warm",
                        unitPrice = 105.0
                    )
                ),
                status = "Ready for Pickup",
                orderType = "Pickup",
                paymentMethod = "GCash",
                placedAt = "Today, 9:44 AM",
                pickupTime = "Today, 10:00 AM"
            ),
            Order(
                id = "ORD-1008",
                customerName = "Paolo Lim",
                customerEmail = "paolo.lim@example.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1008_1",
                        menuItemId = "coffee_americano",
                        name = "Americano",
                        quantity = 1,
                        size = "Regular",
                        temperature = "Hot",
                        unitPrice = 120.0
                    )
                ),
                status = "Completed",
                orderType = "Pickup",
                paymentMethod = "Cash",
                placedAt = "Today, 8:55 AM",
                pickupTime = "Today, 9:05 AM"
            ),
            Order(
                id = "ORD-1006",
                customerName = "Bea Navarro",
                customerEmail = "bea.navarro@example.com",
                items = listOf(
                    OrderItem(
                        id = "order_item_1006_1",
                        menuItemId = "coffee_cold_brew",
                        name = "Cold Brew",
                        quantity = 2,
                        size = "Regular",
                        temperature = "Iced",
                        unitPrice = 150.0,
                        notes = "Customer is waiting near window seat."
                    )
                ),
                status = "Delayed",
                orderType = "Pickup",
                paymentMethod = "Card",
                placedAt = "Today, 8:30 AM",
                pickupTime = "Today, 8:45 AM"
            )
        )

        val profileData = User(
            id = "user_customer_001",
            fullName = "Mika Santos",
            birthday = "March 14, 2002",
            email = "mika.santos@gmail.com",
            phone = "+63 917 555 0148",
            role = "Customer",
            address = "2401 Taft Avenue, Manila",
            loyaltyPoints = 128,
            avatarInitials = "MS"
        )

        val employeeProfileData = User(
            id = "user_employee_001",
            fullName = "Carlo Dela Cruz",
            birthday = "August 9, 1999",
            email = "carlo.staff@sealcoffee.com",
            phone = "+63 917 555 0192",
            role = "Employee",
            loyaltyPoints = 0,
            avatarInitials = "CD"
        )
    }
