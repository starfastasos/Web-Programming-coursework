package com.example.mysupermarketapplication.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents an item purchased in a specific Purchase.
 *
 * The foreign key links each item to a Purchase.
 * Deleting a Purchase will automatically delete its associated items (CASCADE).
 */
@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["purchaseId"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val purchaseId: Long,       // ID of the Purchase this item belongs to
    val productId: Int,         // ID of the purchased product
    val name: LocalizedText,    // Localized product name
    val price: Double,          // Price per unit at purchase time
    val unit: LocalizedText,    // Localized unit of measurement
    val quantity: Int,          // Quantity purchased
    @DrawableRes
    val imageResId: Int,        // Drawable resource for product image
    val purchaseDate: Long      // Timestamp of the purchase
)
