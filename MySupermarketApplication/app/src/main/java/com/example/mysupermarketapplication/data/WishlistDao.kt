package com.example.mysupermarketapplication.data

import android.content.Context
import android.util.Log // Import for Log to log exceptions
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch // Import for Flow exception handling

@Dao
interface WishlistDao {

    /**
     * Inserts or replaces a WishlistItem.
     * This is a suspend function; exceptions from this operation
     * are typically handled by the caller (e.g., Repository or ViewModel).
     * @param item The [WishlistItem] to be inserted or replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistItem)

    /**
     * Deletes a WishlistItem.
     * This is a suspend function; exceptions from this operation
     * are typically handled by the caller.
     * @param item The [WishlistItem] to be deleted.
     */
    @Delete
    suspend fun delete(item: WishlistItem)

    /**
     * Returns all wishlist items as a Flow, updating on data changes.
     * Includes basic exception handling for the Flow.
     * @return A [Flow] emitting a list of [WishlistItem]s.
     * Emits an empty list in case of an error during Flow collection.
     */
    @Query("SELECT * FROM wishlist_item_table")
    fun getAllWishlistItems(): Flow<List<WishlistItem>> {
        return getAllWishlistItemsInternal() // Call the internal query method
            .catch { e ->
                // Catch any exceptions that might occur during the Flow's collection
                Log.e("WishlistDao", "Error fetching all wishlist items: ${e.message}", e)
                // Emit an empty list on error to gracefully handle the situation
                emit(emptyList())
            }
    }

    // Internal function to separate the Room query from Flow operations
    @Query("SELECT * FROM wishlist_item_table")
    fun getAllWishlistItemsInternal(): Flow<List<WishlistItem>>

    /**
     * Returns all wishlist items sorted by localized name.
     * Sorting is done in Kotlin to support dynamic localization.
     * Includes basic exception handling for both the localization/sorting process
     * and any upstream Flow errors.
     * @param context The Android [Context] required for localization.
     * @return A [Flow] emitting the list of localized and sorted [WishlistItem]s.
     * Emits the original list (if sorting fails) or an empty list (if upstream error occurs).
     */
    fun getAllWishlistItemsLocalized(context: Context): Flow<List<WishlistItem>> =
        getAllWishlistItems()
            .map { items ->
                // Attempt to sort and localize, catching any exceptions during this transformation
                try {
                    items.sortedBy { it.name.getLocalized(context) }
                } catch (e: Exception) {
                    Log.e("WishlistDao", "Error localizing or sorting wishlist items: ${e.message}", e)
                    // Return original list or an empty list if critical, to prevent app crash
                    // For sorting/localization, returning the original list is often a good fallback.
                    items
                }
            }
            .catch { e ->
                // Catch any exceptions that might occur upstream in the Flow,
                // such as issues with the database query itself (though Room handles most of this).
                Log.e("WishlistDao", "Error in getAllWishlistItemsLocalized Flow: ${e.message}", e)
                emit(emptyList()) // Emit an empty list on critical errors to keep the flow alive
            }

    /**
     * Returns a Flow emitting a WishlistItem by productId, or null if not found.
     * Emits updates if the item changes.
     * Includes basic exception handling for the Flow.
     * @param productId The ID of the product to search for.
     * @return A [Flow] emitting the [WishlistItem] or `null` if not found or an error occurs.
     */
    @Query("SELECT * FROM wishlist_item_table WHERE productId = :productId LIMIT 1")
    fun getWishlistItemByProductId(productId: Int): Flow<WishlistItem?> {
        return getWishlistItemByProductIdInternal(productId) // Call the internal query method
            .catch { e ->
                // Catch any exceptions that might occur during the Flow's collection
                Log.e("WishlistDao", "Error fetching wishlist item by product ID ($productId): ${e.message}", e)
                emit(null) // Emit null on error to gracefully handle the situation
            }
    }

    // Internal function to separate the Room query from Flow operations
    @Query("SELECT * FROM wishlist_item_table WHERE productId = :productId LIMIT 1")
    fun getWishlistItemByProductIdInternal(productId: Int): Flow<WishlistItem?>
}
