package com.example.mysupermarketapplication.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a product in the supermarket.
 * This entity corresponds to the 'product_table' in the Room database.
 * All user-visible text fields use [LocalizedText] for multilingual support.
 *
 * @property id Auto-generated unique identifier for the product.
 * @property name Localized product name.
 * @property description Localized brief description.
 * @property price Product price.
 * @property unit Localized unit of measurement (e.g., "€/piece", "€/kg").
 * @property category Localized product category (e.g., "Dairy", "Fresh Food").
 * @property imageResId Drawable resource ID for the product image.
 * @property availability Localized availability status (e.g., "In Stock").
 * @property nutritionalInfo Optional localized nutritional information.
 * @property offer Optional localized active offer.
 * @property ingredients Optional localized list of ingredients.
 */
@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: LocalizedText,
    val description: LocalizedText,
    val price: Double,
    val unit: LocalizedText,
    val category: LocalizedText,
    @DrawableRes val imageResId: Int,
    val availability: LocalizedText,
    val nutritionalInfo: LocalizedText? = null,
    val offer: LocalizedText? = null,
    val ingredients: LocalizedText? = null
)
