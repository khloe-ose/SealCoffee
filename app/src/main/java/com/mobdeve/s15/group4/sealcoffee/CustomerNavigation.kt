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
            "menu",
            CustomerMenuActivity::class.java
        )

        bindItem(
            activity,
            R.id.navCartButton,
            R.id.navCartIcon, R.id.navCart,
            CustomerDestination.CART,
            selected,
            "cart",
            CartActivity::class.java
        )

        bindItem(
            activity,
            R.id.navOrdersButton,
            R.id.navOrdersIcon, R.id.navOrders,
            CustomerDestination.ORDERS,
            selected,
            "orders",
            CustomerOrdersActivity::class.java
        )

        bindItem(
            activity,
            R.id.navProfileButton,
            R.id.navProfileIcon, R.id.navProfile,
            CustomerDestination.PROFILE,
            selected,
            "profile",
            ProfileActivity::class.java
        )
    }

    private fun bindItem(
        activity: Activity,
        containerId: Int,
        iconId: Int, textId: Int,
        destination: CustomerDestination,
        selected: CustomerDestination,
        iconName: String,
        activityClass: Class<out Activity>
    ) {
        val navItem = activity.findViewById<LinearLayout>(containerId)
        val icon = activity.findViewById<ImageView>(iconId)
        val text = activity.findViewById<TextView>(textId)

        val isSelected = destination == selected


        val iconRes = if (isSelected) {
            activity.resources.getIdentifier(
                "ic_${iconName}_clicked",
                "drawable",
                activity.packageName
            )
        } else {
            activity.resources.getIdentifier("ic_$iconName", "drawable", activity.packageName)
        }
        icon.setImageResource(iconRes)


        text.setTextColor(
            ContextCompat.getColor(activity, if (isSelected) R.color.white else R.color.seal_navy)
        )


        navItem.setBackgroundResource(
            if (isSelected)
                R.drawable.bg_nav_item_selected
            else
                R.drawable.bg_nav_item
        )

        navItem.setOnClickListener {
            if (!isSelected) {
                activity.startActivity(
                    Intent(activity, activityClass)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }

        }

    }
}