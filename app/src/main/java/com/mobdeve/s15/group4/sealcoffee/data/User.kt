package com.mobdeve.s15.group4.sealcoffee.data

data class User(
    val id: String,
    val fullName: String,
    val birthday: String = "",
    val email: String,
    val phone: String,
    val role: String,
    val address: String = "",
    val loyaltyPoints: Int = 0,
    val avatarInitials: String = ""
)
