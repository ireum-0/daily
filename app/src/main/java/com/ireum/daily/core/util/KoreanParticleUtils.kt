package com.ireum.daily.core.util

fun String.subjectParticle(): String =
    if (lastHangulHasFinalConsonant()) "이" else "가"

private fun String.lastHangulHasFinalConsonant(): Boolean {
    val lastHangul = lastOrNull { char -> char in '가'..'힣' } ?: return false
    return (lastHangul.code - HANGUL_BASE_CODE) % HANGUL_JONGSUNG_COUNT != 0
}

private const val HANGUL_BASE_CODE = 0xAC00
private const val HANGUL_JONGSUNG_COUNT = 28
