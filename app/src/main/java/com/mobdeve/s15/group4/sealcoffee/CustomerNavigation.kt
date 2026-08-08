package com.mobdeve.s15.group4.sealcoffee

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

enum class CustomerDestination {
    MENU,
    CART,
    ORDERS,
    PROFILE
}

object CustomerNavigation {

    fun bind(activity: Activity, selected: CustomerDestination) {
        bindItem(
            activity,
            R.id.navMenuButton,
            R.id.navMenuIcon, R.id.navMenu,
            CustomerDestination.MENU,
            selected,
            R.drawable.ic_menu,
            R.drawable.ic_menu_clicked,
            CustomerMenuActivity::class.java
        )

        bindItem(
            activity,
            R.id.navCartButton,
            R.id.navCartIcon, R.id.navCart,
            CustomerDestination.CART,
            selected,
            R.drawable.ic_cart,
            R.drawable.ic_cart_clicked,
            CartActivity::class.java
        )

        bindItem(
            activity,
            R.id.navOrdersButton,
            R.id.navOrdersIcon, R.id.navOrders,
            CustomerDestination.ORDERS,
            selected,
            R.drawable.ic_orders,
            R.drawable.ic_orders_clicked,
            CustomerOrdersActivity::class.java
        )

        bindItem(
            activity,
            R.id.navProfileButton,
            R.id.navProfileIcon, R.id.navProfile,
            CustomerDestination.PROFILE,
            selected,
            R.drawable.ic_profile,
            R.drawable.ic_profile_clicked,
            ProfileActivity::class.java
        )
    }

    private fun bindItem(
        activity: Activity,
        containerId: Int,
        iconId: Int, textId: Int,
        destination: CustomerDestination,
        selected: CustomerDestination,
        defaultIconRes: Int,
        selectedIconRes: Int,
        activityClass: Class<out Activity>
    ) {
        val navItem = activity.findViewById<LinearLayout>(containerId) ?: return
        val icon = activity.findViewById<ImageView>(iconId) ?: return
        val text = activity.findViewById<TextView>(textId) ?: return

        val isSelected = destination == selected

        icon.setImageResource(if (isSelected) selectedIconRes else defaultIconRes)

        text.setTextColor(
            ContextCompat.getColor(activity, if (isSelected) android.R.color.white else R.color.seal_navy)
        )

        navItem.setBackgroundResource(
            if (isSelected)
                R.drawable.bg_nav_item_selected
            else
                R.drawable.bg_nav_item
        )

        navItem.setOnClickListener {
            if (!isSelected) {
                val intent = Intent(activity, activityClass)
                activity.startActivity(intent)
                activity.finish() // Ends the current activity to optimize memory and prevent stacking
            }
        }
    }
}