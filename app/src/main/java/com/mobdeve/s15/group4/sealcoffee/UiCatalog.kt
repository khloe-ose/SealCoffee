package com.mobdeve.s15.group4.sealcoffee

import android.util.Log
import android.widget.ImageView
import coil.load
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigDecimal

object ImageCatalog {
    private const val SUPABASE_BASE_URL = "https://elaszfebuwzfsexfbnrc.supabase.co/storage/v1/object/public/drawable/"

    fun urlFor(key: String): String {
        val timestamp = System.currentTimeMillis()
        val url = "${SUPABASE_BASE_URL}img_$key.png?t=$timestamp"
        Log.d("ImageCatalog", "Loading image for $key from: $url")
        return url
    }
}

fun ImageView.loadSupabaseImage(key: String?, fallbackResId: Int = R.drawable.img_custom) {
    val url = ImageCatalog.urlFor(key ?: "")
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