package com.ireum.daily.core.util

import java.util.Locale

fun String.normalizeMenuName(): String =
    filterNot(Char::isWhitespace).lowercase(Locale.KOREAN)

fun String.matchesFavoriteMenu(favoriteMenus: Iterable<String>): Boolean {
    val normalizedDish = normalizeMenuName()
    return favoriteMenus.any { favorite ->
        val normalizedFavorite = favorite.normalizeMenuName()
        normalizedFavorite.isNotBlank() && normalizedDish.contains(normalizedFavorite)
    }
}

fun findMatchingFavoriteMenus(
    dishes: Iterable<String>,
    favoriteMenus: Iterable<String>
): List<String> =
    favoriteMenus
        .filter { favorite ->
            val normalizedFavorite = favorite.normalizeMenuName()
            normalizedFavorite.isNotBlank() && dishes.any { dish ->
                dish.normalizeMenuName().contains(normalizedFavorite)
            }
        }
        .sorted()
