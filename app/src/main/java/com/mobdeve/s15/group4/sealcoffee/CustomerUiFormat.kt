package com.mobdeve.s15.group4.sealcoffee

fun Double.formatPrice(): String = "PHP %.2f".format(this)

fun String.initials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
}
