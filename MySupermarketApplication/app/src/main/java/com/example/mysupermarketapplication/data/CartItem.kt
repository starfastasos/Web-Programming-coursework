package com.example.mysupermarketapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an item in the shopping cart.
 *
 * @property id Auto-generated unique identifier for the cart item.
 * @property productId The ID of the associated product.
 * @property name The localized name of the product.
 * @property price The price per unit of the product.
 * @property unit The localized unit of measurement (e.g., "kg", "pcs").
 * @property quantity The quantity of this product in the cart. Defaults to 1.
 */
@Entity(tableName = "cart_item_table")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val name: LocalizedText,
    val price: Double,
    val unit: LocalizedText,
    var quantity: Int = 1
)
