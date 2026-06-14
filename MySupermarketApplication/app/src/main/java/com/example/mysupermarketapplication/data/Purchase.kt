package com.example.mysupermarketapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a purchase transaction.
 * Each purchase can have multiple purchase items.
 */
@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val purchaseId: Long = 0L,
    val purchaseDate: Long
)
