package com.example.mysupermarketapplication.data

import android.util.Log // Import for Log to log exceptions
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch // Import for Flow exception handling

/**
 * Data Access Object (DAO) for the [Product] entity.
 * Defines methods to interact with the product_table in the Room database,
 * now including basic exception handling for Flow operations.
 */
@Dao
interface ProductDao {

    /**
     * Inserts a product into the database.
     * Replaces existing product if there's a conflict on primary key.
     * This is a suspend function; exceptions are typically handled by the caller.
     * @param product The [Product] to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    /**
     * Updates an existing product.
     * This is a suspend function; exceptions are typically handled by the caller.
     * @param product The [Product] to be updated.
     */
    @Update
    suspend fun update(product: Product)

    /**
     * Deletes a product from the database.
     * This is a suspend function; exceptions are typically handled by the caller.
     * @param product The [Product] to be deleted.
     */
    @Delete
    suspend fun delete(product: Product)

    /**
     * Fetches a product by its ID.
     * Returns a Flow emitting updates on this product.
     * Includes basic exception handling for the Flow.
     *
     * @param id The product's unique identifier.
     * @return A [Flow] emitting the [Product] or `null` if not found or an error occurs.
     */
    @Query("SELECT * FROM product_table WHERE id = :id")
    fun getProductById(id: Int): Flow<Product?> {
        return getProductByIdInternal(id) // Call the internal query method
            .catch { e ->
                // Catch any exceptions that might occur during the Flow's collection
                Log.e("ProductDao", "Error fetching product by ID ($id): ${e.message}", e)
                emit(null) // Emit null on error to gracefully handle the situation
            }
    }

    // Internal function to separate the Room query from Flow operations
    @Query("SELECT * FROM product_table WHERE id = :id")
    fun getProductByIdInternal(id: Int): Flow<Product?>

    /**
     * Inserts multiple products in a single transaction.
     * Replaces any conflicting entries.
     * This is a suspend function; exceptions are typically handled by the caller.
     * @param products The list of [Product]s to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    /**
     * Retrieves all products from the database.
     * Includes basic exception handling for the Flow.
     * Note: Sorting and localization-based filtering should be handled
     * in the ViewModel or repository layer for flexibility.
     * @return A [Flow] emitting the list of [Product]s.
     */
    @Query("SELECT * FROM product_table")
    fun getAllProducts(): Flow<List<Product>> {
        return getAllProductsInternal() // Call the internal query method
            .catch { e ->
                // Catch any exceptions that might occur during the Flow's collection
                Log.e("ProductDao", "Error fetching all products: ${e.message}", e)
                emit(emptyList()) // Emit an empty list on error to gracefully handle the situation
            }
    }

    // Internal function to separate the Room query from Flow operations
    @Query("SELECT * FROM product_table")
    fun getAllProductsInternal(): Flow<List<Product>>

    /**
     * Deletes all products from the database.
     * This is a suspend function; exceptions are typically handled by the caller.
     */
    @Query("DELETE FROM product_table")
    suspend fun deleteAllProducts()
}
