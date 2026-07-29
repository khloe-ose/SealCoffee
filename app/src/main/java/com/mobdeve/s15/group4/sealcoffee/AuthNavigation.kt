package com.mobdeve.s15.group4.sealcoffee

import android.app.Activity
import android.content.Intent
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole

val Activity.sealApp: SealCoffeeApplication
    get() = application as SealCoffeeApplication

object AuthNavigation {
    fun requireRole(activity: Activity, expectedRole: UserRole): Boolean {
        val session = activity.sealApp.session
        if (session.hasSession && session.role == expectedRole) return true
        session.clear()

        activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
        return false
    }

    fun requireAuthenticated(activity: Activity): Boolean {
        if (activity.sealApp.session.hasSession) return true
        activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
        return false
    }

    fun routeAuthenticated(activity: Activity) {
        val destination = when (activity.sealApp.session.role) {
            UserRole.CUSTOMER -> CustomerMenuActivity::class.java
            UserRole.EMPLOYEE -> EmployeeDashboardActivity::class.java
            null -> LoginActivity::class.java
        }
        activity.startActivity(
            Intent(activity, destination)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    fun logout(activity: Activity) {
        activity.sealApp.session.clear()
        activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }
}