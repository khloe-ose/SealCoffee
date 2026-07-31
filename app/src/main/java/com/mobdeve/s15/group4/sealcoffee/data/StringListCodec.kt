package com.mobdeve.s15.group4.sealcoffee.data

object StringListCodec {
    private const val SEPARATOR = '\u001F'

    fun encode(values: Collection<String>): String =
        values.map(String::trim)
            .filter(String::isNotBlank)
            .map { it.replace(SEPARATOR, ' ') }
            .distinct()
            .joinToString(SEPARATOR.toString())

    fun decode(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(SEPARATOR).filter(String::isNotBlank)
}
