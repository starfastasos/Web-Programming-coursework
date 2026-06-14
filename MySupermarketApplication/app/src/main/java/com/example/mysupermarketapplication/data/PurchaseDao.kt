package com.example.mysupermarketapplication.data

import android.util.Log // Import for Log to log exceptions
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Relation
import androidx.room.Embedded
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch // Import for Flow exception handling

/**
 * Represents a purchase along with all its associated items.
 */
data class PurchaseWithItems(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "purchaseId",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItem>
)

@Dao
interface PurchaseDao {

    /**
     * Inserts a new Purchase and returns its generated ID.
     * This is a suspend function; exceptions from this operation
     * are typically handled by the caller (e.g., Repository or ViewModel).
     * @param purchase The [Purchase] to be inserted.
     * @return The generated ID of the new purchase.
     */
    @Insert
    suspend fun insertPurchase(purchase: Purchase): Long

    /**
     * Inserts a PurchaseItem linked to a Purchase.
     * This is a suspend function; exceptions from this operation
     * are typically handled by the caller.
     * @param item The [PurchaseItem] to be inserted.
     */
    @Insert
    suspend fun insertPurchaseItem(item: PurchaseItem)

    /**
     * Returns all purchases with their items, sorted by most recent.
     * Uses a transaction to ensure data consistency.
     * Emits updates as data changes.
     * Includes basic exception handling for the Flow.
     *
     * @return A [Flow] emitting a list of [PurchaseWithItems].
     * Emits an empty list in case of an error during Flow collection.
     */
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>> {
        return getAllPurchasesWithItemsInternal() // Call the internal query method
            .catch { e ->
                // Catch any exceptions that might occur during the Flow's collection,
                // such as issues with the database query or relation mapping.
                Log.e("PurchaseDao", "Error fetching all purchases with items: ${e.message}", e)
                // Emit an empty list on error to gracefully handle the situation and
                // prevent the Flow from crashing, keeping the observer alive.
                emit(emptyList())
            }
    }

    // Internal function to separate the Room query from Flow operations
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchasesWithItemsInternal(): Flow<List<PurchaseWithItems>>
}
