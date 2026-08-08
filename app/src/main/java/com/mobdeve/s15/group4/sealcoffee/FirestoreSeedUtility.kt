package com.mobdeve.s15.group4.sealcoffee

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSeedUtility {
    fun seedInfoIfNeeded() {
        val firestore = FirebaseFirestore.getInstance()
        val menuRef = firestore.collection("menu")

        menuRef.limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val now = System.currentTimeMillis()
                    val dayMillis = 86_400_000L

                    // 1. Seed Menu Items
                    val menuItems = listOf(
                        mapOf(
                            "seedKey" to "signature_latte",
                            "name" to "Signature Latte",
                            "category" to "Coffee",
                            "description" to "Espresso with steamed milk and a smooth caramel finish.",
                            "ingredients" to listOf("Espresso", "Steamed milk", "Caramel", "Sea salt cream"),
                            "basePriceCentavos" to 16500L,
                            "imageKey" to "signature_latte",
                            "featured" to true,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "coffee_spanish_latte",
                            "name" to "Spanish Latte",
                            "category" to "Coffee",
                            "description" to "Creamy espresso blend sweetened with condensed milk.",
                            "ingredients" to listOf("Espresso", "Fresh milk", "Condensed milk"),
                            "basePriceCentavos" to 17500L,
                            "imageKey" to "spanish_latte",
                            "featured" to true,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "coffee_americano",
                            "name" to "Americano",
                            "category" to "Coffee",
                            "description" to "Bold espresso topped with hot water for a clean finish.",
                            "ingredients" to listOf("Espresso", "Filtered water"),
                            "basePriceCentavos" to 12000L,
                            "imageKey" to "americano",
                            "featured" to false,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "coffee_mocha",
                            "name" to "Cafe Mocha",
                            "category" to "Coffee",
                            "description" to "Espresso, milk, and chocolate with a balanced sweetness.",
                            "ingredients" to listOf("Espresso", "Milk", "Chocolate sauce"),
                            "basePriceCentavos" to 17000L,
                            "imageKey" to "cafe_mocha",
                            "featured" to false,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "noncoffee_matcha",
                            "name" to "Matcha Cream",
                            "category" to "Non-Coffee",
                            "description" to "Earthy matcha latte with a light cream topping.",
                            "ingredients" to listOf("Matcha", "Milk", "Vanilla cream"),
                            "basePriceCentavos" to 18000L,
                            "imageKey" to "matcha_cream",
                            "featured" to true,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "coffee_cold_brew",
                            "name" to "Cold Brew",
                            "category" to "Coffee",
                            "description" to "Slow-steeped coffee served over ice.",
                            "ingredients" to listOf("Cold brew coffee", "Ice"),
                            "basePriceCentavos" to 15000L,
                            "imageKey" to "cold_brew",
                            "featured" to false,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "snack_croissant",
                            "name" to "Butter Croissant",
                            "category" to "Snacks",
                            "description" to "Flaky butter croissant warmed before serving.",
                            "ingredients" to listOf("Butter pastry", "Sea salt"),
                            "basePriceCentavos" to 9500L,
                            "imageKey" to "butter_croissant",
                            "featured" to false,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "snack_muffin",
                            "name" to "Blueberry Muffin",
                            "category" to "Desserts",
                            "description" to "Soft muffin packed with blueberries.",
                            "ingredients" to listOf("Blueberries", "Vanilla batter", "Sugar crumb"),
                            "basePriceCentavos" to 10500L,
                            "imageKey" to "blueberry_muffin",
                            "featured" to false,
                            "available" to true,
                            "archived" to false
                        ),
                        mapOf(
                            "seedKey" to "dessert_cheesecake",
                            "name" to "Mini Cheesecake",
                            "category" to "Desserts",
                            "description" to "Creamy cheesecake with a buttery crumb base.",
                            "ingredients" to listOf("Cream cheese", "Strawberry", "Vanilla"),
                            "basePriceCentavos" to 13500L,
                            "imageKey" to "mini_cheesecake",
                            "featured" to false,
                            "available" to false,
                            "archived" to false
                        )
                    )

                    for (item in menuItems) {
                        val docId = item["seedKey"] as String
                        menuRef.document(docId).set(item)
                            .addOnSuccessListener { Log.d("Seed", "Added item $docId successfully") }
                            .addOnFailureListener { e -> Log.e("Seed", "Failed to add item $docId", e) }
                    }

                    // 2. Seed Multiple Orders for Testing (Completed, Preparing, Delayed, Ready for Pickup, Pending)
                    val orders = listOf(
                        mapOf(
                            "orderNumber" to "SC-DEMO-0001",
                            "customerId" to "seed_customer_mika",
                            "customerName" to "Mika Santos",
                            "status" to "COMPLETED",
                            "placedAt" to (now - (dayMillis * 2)),
                            "subtotalCentavos" to 26000L,
                            "totalCentavos" to 26000L,
                            "orderType" to "Pickup",
                            "items" to listOf(
                                mapOf(
                                    "menuItemId" to "signature_latte",
                                    "itemName" to "Signature Latte",
                                    "category" to "Coffee",
                                    "quantity" to 1,
                                    "size" to "Regular",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "",
                                    "unitPriceCentavos" to 16500L,
                                    "lineTotalCentavos" to 16500L
                                ),
                                mapOf(
                                    "menuItemId" to "snack_croissant",
                                    "itemName" to "Butter Croissant",
                                    "category" to "Snacks",
                                    "quantity" to 1,
                                    "size" to "Single",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "",
                                    "unitPriceCentavos" to 9500L,
                                    "lineTotalCentavos" to 9500L
                                )
                            )
                        ),
                        mapOf(
                            "orderNumber" to "SC-DEMO-0002",
                            "customerId" to "seed_customer_cheska",
                            "customerName" to "Cheska Martinez",
                            "status" to "PREPARING",
                            "placedAt" to (now - 3_600_000L), // 1 hour ago
                            "subtotalCentavos" to 35000L,
                            "totalCentavos" to 35000L,
                            "orderType" to "Pickup",
                            "items" to listOf(
                                mapOf(
                                    "menuItemId" to "coffee_spanish_latte",
                                    "itemName" to "Spanish Latte",
                                    "category" to "Coffee",
                                    "quantity" to 2,
                                    "size" to "Large",
                                    "addOns" to listOf("Extra Shot"),
                                    "notes" to "Less sweet please",
                                    "unitPriceCentavos" to 17500L,
                                    "lineTotalCentavos" to 35000L
                                )
                            )
                        ),
                        mapOf(
                            "orderNumber" to "SC-DEMO-0003",
                            "customerId" to "seed_customer_steven",
                            "customerName" to "Steven Universe",
                            "status" to "PENDING",
                            "placedAt" to (now - 900_000L), // 15 mins ago
                            "subtotalCentavos" to 28500L,
                            "totalCentavos" to 28500L,
                            "orderType" to "Pickup",
                            "items" to listOf(
                                mapOf(
                                    "menuItemId" to "noncoffee_matcha",
                                    "itemName" to "Matcha Cream",
                                    "category" to "Non-Coffee",
                                    "quantity" to 1,
                                    "size" to "Regular",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "",
                                    "unitPriceCentavos" to 18000L,
                                    "lineTotalCentavos" to 18000L
                                ),
                                mapOf(
                                    "menuItemId" to "snack_muffin",
                                    "itemName" to "Blueberry Muffin",
                                    "category" to "Desserts",
                                    "quantity" to 1,
                                    "size" to "Standard",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "",
                                    "unitPriceCentavos" to 10500L,
                                    "lineTotalCentavos" to 10500L
                                )
                            )
                        ),
                        mapOf(
                            "orderNumber" to "SC-DEMO-0004",
                            "customerId" to "seed_customer_alex",
                            "customerName" to "Alex Rivera",
                            "status" to "READY_FOR_PICKUP",
                            "placedAt" to (now - 1_800_000L), // 30 mins ago
                            "subtotalCentavos" to 15000L,
                            "totalCentavos" to 15000L,
                            "orderType" to "Pickup",
                            "items" to listOf(
                                mapOf(
                                    "menuItemId" to "coffee_cold_brew",
                                    "itemName" to "Cold Brew",
                                    "category" to "Coffee",
                                    "quantity" to 1,
                                    "size" to "Regular",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "Extra ice",
                                    "unitPriceCentavos" to 15000L,
                                    "lineTotalCentavos" to 15000L
                                )
                            )
                        ),
                        mapOf(
                            "orderNumber" to "SC-DEMO-0005",
                            "customerId" to "seed_customer_bea",
                            "customerName" to "Bea Alonzo",
                            "status" to "COMPLETED",
                            "placedAt" to (now - (dayMillis)), // Yesterday
                            "subtotalCentavos" to 33750L,
                            "totalCentavos" to 33750L,
                            "orderType" to "Pickup",
                            "items" to listOf(
                                mapOf(
                                    "menuItemId" to "coffee_americano",
                                    "itemName" to "Americano",
                                    "category" to "Coffee",
                                    "quantity" to 1,
                                    "size" to "Regular",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "",
                                    "unitPriceCentavos" to 12000L,
                                    "lineTotalCentavos" to 12000L
                                ),
                                mapOf(
                                    "menuItemId" to "coffee_mocha",
                                    "itemName" to "Cafe Mocha",
                                    "category" to "Coffee",
                                    "quantity" to 1,
                                    "size" to "Large",
                                    "addOns" to emptyList<String>(),
                                    "notes" to "More chocolate drizzle",
                                    "unitPriceCentavos" to 17000L,
                                    "lineTotalCentavos" to 17000L
                                )
                            )
                        )
                    )

                    for (order in orders) {
                        val orderId = order["orderNumber"] as String
                        firestore.collection("orders").document(orderId).set(order)
                            .addOnSuccessListener { Log.d("Seed", "Added order $orderId successfully") }
                            .addOnFailureListener { e -> Log.e("Seed", "Failed to add order $orderId", e) }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Seed", "Failed to check menu collection", e)
            }
    }
}