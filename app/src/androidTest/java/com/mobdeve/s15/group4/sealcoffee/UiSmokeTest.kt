package com.mobdeve.s15.group4.sealcoffee

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobdeve.s15.group4.sealcoffee.domain.OrderStatus
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiSmokeTest {
    @Before
    fun startSignedOut() {
        val app = ApplicationProvider.getApplicationContext<Context>() as SealCoffeeApplication
        app.session.clear()
    }

    @After
    fun clearSession() {
        val app = ApplicationProvider.getApplicationContext<Context>() as SealCoffeeApplication
        app.session.clear()
    }

    @Test
    fun loginScreen_displaysRequiredAuthenticationControls() {
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withId(R.id.emailInput)).check(matches(isDisplayed()))
            onView(withId(R.id.passwordInput)).check(matches(isDisplayed()))
            onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun signedInCustomer_canOpenAndChangeMenuFilter() {
        val app = ApplicationProvider.getApplicationContext<Context>() as SealCoffeeApplication
        val customer = runBlocking {
            app.repository.authenticate("mika.santos@gmail.com", "Coffee123!")
        }!!
        check(customer.role == UserRole.CUSTOMER.name)
        app.session.save(customer)

        ActivityScenario.launch(CustomerMenuActivity::class.java).use {
            onView(withId(R.id.filterCoffeeButton)).perform(click())
            onView(withId(R.id.menuRecyclerView)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun customerCanAddCheckoutAndEmployeeCanUpdateStatus() {
        val app = ApplicationProvider.getApplicationContext<Context>() as SealCoffeeApplication
        val customer = runBlocking {
            app.repository.authenticate("mika.santos@gmail.com", "Coffee123!")
        }!!
        app.session.save(customer)
        val menuItem = runBlocking {
            app.repository.observeCustomerMenu().first().first { it.available }
        }

        val productIntent = Intent(app, ProductDetailsActivity::class.java)
            .putExtra(ProductDetailsActivity.EXTRA_MENU_ITEM_ID, menuItem.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ActivityScenario.launch<ProductDetailsActivity>(productIntent).use {
            onView(withId(R.id.productDetailsScrollView)).perform(swipeUp(), swipeUp())
            onView(allOf(withId(R.id.addToCartButton), isEnabled(), isDisplayed()))
                .perform(click())
            onView(withText(app.getString(R.string.added_to_cart, 1, menuItem.name)))
                .check(matches(isDisplayed()))
        }

        ActivityScenario.launch(CartActivity::class.java).use {
            onView(allOf(withId(R.id.placeOrderButton), isEnabled())).perform(click())
            onView(withText(R.string.place_order_btn)).perform(click())
            onView(allOf(withId(R.id.menuTitleText), withText(R.string.orders_navbar_name)))
                .check(matches(isDisplayed()))
        }

        val order = runBlocking {
            app.repository.observeCustomerOrders(customer.id, OrderStatus.active)
                .first()
                .maxBy { it.order.placedAt }
        }
        val employee = runBlocking {
            app.repository.authenticate("carlo.staff@sealcoffee.com", "Staff123!")
        }!!
        app.session.save(employee)

        val detailsIntent = Intent(app, EmployeeOrderDetailsActivity::class.java)
            .putExtra(EmployeeOrderDetailsActivity.EXTRA_ORDER_ID, order.order.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ActivityScenario.launch<EmployeeOrderDetailsActivity>(detailsIntent).use {
            onView(withId(R.id.employeeOrderDetailsRoot)).perform(swipeUp(), swipeUp())
            onView(allOf(withId(R.id.statusPreparingRadio), isDisplayed())).perform(click())
            onView(allOf(withId(R.id.applyStatusButton), isEnabled(), isDisplayed())).perform(click())
            onView(withText("Status: Preparing")).check(matches(isDisplayed()))
        }

        val updated = runBlocking { app.repository.getOrder(order.order.id) }
        assertEquals(OrderStatus.PREPARING.name, updated?.order?.status)
    }
}
