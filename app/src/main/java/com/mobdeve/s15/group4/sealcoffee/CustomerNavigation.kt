package com.mobdeve.s15.group4.sealcoffee

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import androidx.core.content.ContextCompat

enum class CustomerDestination {
    MENU,
    CART,
    STATUS,
    HISTORY,
    PROFILE
}

object CustomerNavigation {
    fun bind(activity: Activity, selected: CustomerDestination) {
        bindItem(activity, R.id.navMenuButton, CustomerDestination.MENU, selected, CustomerMenuActivity::class.java)
        bindItem(activity, R.id.navCartButton, CustomerDestination.CART, selected, CartActivity::class.java)
        bindItem(activity, R.id.navStatusButton, CustomerDestination.STATUS, selected, CurrentOrderStatusActivity::class.java)
        bindItem(activity, R.id.navHistoryButton, CustomerDestination.HISTORY, selected, OrderHistoryActivity::class.java)
        bindItem(activity, R.id.navProfileButton, CustomerDestination.PROFILE, selected, ProfileActivity::class.java)
    }

    private fun bindItem(
        activity: Activity,
        viewId: Int,
        destination: CustomerDestination,
        selected: CustomerDestination,
        activityClass: Class<out Activity>
    ) {
        val navItem = activity.findViewById<TextView>(viewId)
        val isSelected = destination == selected
        navItem.setBackgroundResource(if (isSelected) R.drawable.bg_nav_item_selected else R.drawable.bg_nav_item)
        navItem.setTextColor(
            ContextCompat.getColor(
                activity,
                if (isSelected) R.color.white else R.color.seal_navy
            )
        )
        navItem.setOnClickListener {
            if (!isSelected) {
                activity.startActivity(
                    Intent(activity, activityClass).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }
        }
    }
}
