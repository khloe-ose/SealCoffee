package com.mobdeve.s15.group4.sealcoffee

import android.widget.ImageView
import coil.load
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigDecimal

object ImageCatalog {

    private const val SUPABASE_STORAGE_URL = "https://elaszfebuwzfsexfbnrc.supabase.co/storage/v1/object/public/drawable"

    fun resourceFor(key: String): String {
        val fileName = when (key) {
            "signature_latte" -> "img_signature_latte.jpg"
            "spanish_latte" -> "img_spanish_latte.jpg"
            "americano" -> "img_americano.jpg"
            "cafe_mocha" -> "img_cafe_mocha.jpg"
            "matcha_cream" -> "img_matcha_cream.jpg"
            "cold_brew" -> "img_cold_brew.jpg"
            "butter_croissant" -> "img_butter_croissant.jpg"
            "blueberry_muffin" -> "img_blueberry_muffin.jpg"
            "mini_cheesecake" -> "img_mini_cheesecake.jpg"
            else -> "img_custom.jpg"
        }
        return "$SUPABASE_STORAGE_URL/$fileName"
    }
}

fun ImageView.loadSupabaseImage(key: String?, fallbackResId: Int = R.drawable.img_custom) {
    val url = ImageCatalog.resourceFor(key ?: "")
    this.load(url) {
        crossfade(true)
        placeholder(R.drawable.img_custom)
        error(R.drawable.img_custom)
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