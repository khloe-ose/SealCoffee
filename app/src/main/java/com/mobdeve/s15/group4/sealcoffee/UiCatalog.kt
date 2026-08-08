package com.mobdeve.s15.group4.sealcoffee

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigDecimal

object ImageCatalog {
    fun resourceFor(key: String): Int = when (key) {
        "signature_latte" -> R.drawable.img_signature_latte
        "spanish_latte" -> R.drawable.img_spanish_latte
        "americano" -> R.drawable.img_americano
        "cafe_mocha" -> R.drawable.img_cafe_mocha
        "matcha_cream" -> R.drawable.img_matcha_cream
        "cold_brew" -> R.drawable.img_cold_brew
        "butter_croissant" -> R.drawable.img_butter_croissant
        "blueberry_muffin" -> R.drawable.img_blueberry_muffin
        "mini_cheesecake" -> R.drawable.img_mini_cheesecake
        else -> R.drawable.img_custom
    }
}

fun Int.formatMoney(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))
        .format(BigDecimal.valueOf(this.toLong()).movePointLeft(2))

fun Long.formatMoney(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))
        .format(BigDecimal.valueOf(this).movePointLeft(2))

fun Long.formatDateTime(): String =
    SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(this))

fun String.initials(): String =
    trim().split(Regex("\\s+")).filter(String::isNotEmpty).take(2)
        .joinToString("") { it.take(1).uppercase(Locale.getDefault()) }