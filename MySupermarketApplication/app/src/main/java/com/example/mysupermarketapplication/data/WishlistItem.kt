package com.example.mysupermarketapplication.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an item in the user's wishlist.
 * Stores product details with localized text support.
 */
@Entity(tableName = "wishlist_item_table")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val name: LocalizedText,
    val price: Double,
    val unit: LocalizedText,
    @DrawableRes
    val imageResId: Int
)
