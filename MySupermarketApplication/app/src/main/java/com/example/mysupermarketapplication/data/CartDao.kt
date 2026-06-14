package com.example.mysupermarketapplication.data

import android.content.Context
import android.util.Log // Import for Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch // Import for Flow exception handling

/**
 * DAO for accessing and manipulating [CartItem] entities in the database.
 * This interface defines methods for inserting, updating, deleting, and querying cart items,
 * with basic exception handling implemented particularly for Flow operations.
 */
@Dao
interface CartDao {

    /**
     * Inserts a new cart item or replaces an existing one if there's a conflict based on primary key.
     * This is a suspend function, meaning it should be called from a coroutine.
     * Database-related exceptions from this operation are typically caught by the calling layer (e.g., Repository or ViewModel).
     * @param cartItem The [CartItem] to be inserted or replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem)

    /**
     * Updates an existing cart item in the database.
     * This is a suspend function, meaning it should be called from a coroutine.
     * Exceptions from this operation are typically handled by the caller.
     * @param cartItem The [CartItem] to be updated.
     */
    @Update
    suspend fun update(cartItem: CartItem)

    /**
     * Deletes a specific cart item from the database.
     * This is a suspend function, meaning it should be called from a coroutine.
     * Exceptions from this operation are typically handled by the caller.
     * @param cartItem The [CartItem] to be deleted.
     */
    @Delete
    suspend fun delete(cartItem: CartItem)

    /**
     * Retrieves all cart items from the database as a [Flow] of a list of [CartItem]s.
     * This allows for observing real-time updates to the cart items.
     * Room handles most underlying database query exceptions for Flow; however,
     * subsequent operations on this Flow (like map, filter) can introduce new exception points.
     * @return A [Flow] emitting the current list of [CartItem]s.
     */
    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItems(): Flow<List<CartItem>>

    /**
     * Retrieves all cart items, localizes their names, and sorts them by the localized name.
     * Includes basic exception handling within the Flow pipeline for both the localization/sorting
     * process and any upstream Flow errors.
     * @param context The Android [Context] required for localization.
     * @return A [Flow] emitting the list of localized and sorted [CartItem]s.
     */
    fun getAllCartItemsLocalized(context: Context): Flow<List<CartItem>> {
        return getAllCartItems()
            // Apply a transformation (map) to localize and sort the items.
            .map { cartItems ->
                // Use a try-catch block to handle potential exceptions during localization or sorting.
                try {
                    // Attempt to sort cart items by their localized name.
                    cartItems.sortedBy { it.name.getLocalized(context) }
                } catch (e: Exception) {
                    // Log the error if localization or sorting fails.
                    Log.e("CartDao", "Error localizing or sorting cart items: ${e.message}", e)
                    // In case of an error during localization/sorting, return the original unsorted/unlocalized list.
                    // This prevents the entire flow from crashing and allows the UI to display at least the raw data.
                    cartItems
                }
            }
            // Use the Flow's catch operator to handle any exceptions that propagate upstream in the Flow.
            // This includes potential issues with the database query itself, though Room often handles these internally.
            .catch { e ->
                Log.e("CartDao", "Error in getAllCartItemsLocalized Flow pipeline: ${e.message}", e)
                // Emit an empty list in case of a critical error upstream, ensuring the Flow does not complete abruptly.
                // This keeps the observation alive and provides a graceful fallback for the UI.
                emit(emptyList())
            }
    }

    /**
     * Finds a single cart item by its productId.
     * This is a suspend function, meaning it should be called from a coroutine.
     * Returns `null` if no item with the given productId is found.
     * Exceptions from this operation are typically handled by the caller.
     * @param productId The ID of the product to search for.
     * @return The [CartItem] if found, otherwise `null`.
     */
    @Query("SELECT * FROM cart_item_table WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: Int): CartItem?

    /**
     * Deletes all cart items from the database.
     * This is a suspend function, meaning it should be called from a coroutine.
     * Exceptions from this operation are typically handled by the caller.
     */
    @Query("DELETE FROM cart_item_table")
    suspend fun deleteAllCartItems()
}
