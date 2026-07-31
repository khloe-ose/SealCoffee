package com.mobdeve.s15.group4.sealcoffee

import android.content.Context
import androidx.core.content.edit
import com.mobdeve.s15.group4.sealcoffee.data.local.UserEntity
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("sealcoffee_session", Context.MODE_PRIVATE)

    val userId: Long get() = preferences.getLong(KEY_USER_ID, NO_USER)
    val role: UserRole? get() = UserRole.fromStorage(preferences.getString(KEY_ROLE, null))
    val hasSession: Boolean get() = userId != NO_USER && role != null

    fun save(user: UserEntity) {
        preferences.edit {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_ROLE, user.role)
        }
    }

    fun clear() {
        preferences.edit { clear() }
    }

    private companion object {
        const val KEY_USER_ID = "active_user_id"
        const val KEY_ROLE = "active_user_role"
        const val NO_USER = -1L
    }
}
