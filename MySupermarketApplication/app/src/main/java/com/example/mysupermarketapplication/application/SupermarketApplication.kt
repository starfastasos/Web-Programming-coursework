package com.example.mysupermarketapplication.application

import android.app.Application
import android.util.Log
import com.example.mysupermarketapplication.data.AppDatabase
import com.example.mysupermarketapplication.data.CartDao
import com.example.mysupermarketapplication.data.ProductDao
import com.example.mysupermarketapplication.data.PurchaseDao
import com.example.mysupermarketapplication.data.WishlistDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Custom [Application] class for the Supermarket app.
 * This class handles the global initialization of the Room database and its Data Access Objects (DAOs).
 * It includes basic error handling for critical startup components.
 */
class SupermarketApplication : Application() {

    /**
     * An application-wide [CoroutineScope] used for launching background tasks.
     * [SupervisorJob] ensures that if one child coroutine fails, it doesn't cancel other coroutines
     * launched within this scope.
     */
    val applicationScope = CoroutineScope(SupervisorJob())

    /**
     * Lazily initialized instance of the [AppDatabase].
     * The database is created only when it's first accessed, improving app startup performance.
     * A [RuntimeException] is thrown if database initialization fails, indicating a critical error.
     */
    val database: AppDatabase by lazy {
        try {
            AppDatabase.getDatabase(this, applicationScope)
        } catch (e: Exception) {
            // Log the error for debugging. A database failure typically means the app can't function.
            Log.e("SupermarketApp", "Error initializing database: ${e.message}", e)
            throw RuntimeException("Failed to initialize the application database.", e)
        }
    }

    /**
     * Lazily initialized [ProductDao] for accessing product-related data.
     * Throws a [RuntimeException] if initialization fails.
     */
    val productDao: ProductDao by lazy {
        try {
            database.productDao()
        } catch (e: Exception) {
            Log.e("SupermarketApp", "Error initializing ProductDao: ${e.message}", e)
            throw RuntimeException("Failed to initialize ProductDao.", e)
        }
    }

    /**
     * Lazily initialized [CartDao] for managing items in the shopping cart.
     * Throws a [RuntimeException] if initialization fails.
     */
    val cartDao: CartDao by lazy {
        try {
            database.cartDao()
        } catch (e: Exception) {
            Log.e("SupermarketApp", "Error initializing CartDao: ${e.message}", e)
            throw RuntimeException("Failed to initialize CartDao.", e)
        }
    }

    /**
     * Lazily initialized [WishlistDao] for managing the user's wishlist items.
     * Throws a [RuntimeException] if initialization fails.
     */
    val wishlistDao: WishlistDao by lazy {
        try {
            database.wishlistDao()
        } catch (e: Exception) {
            Log.e("SupermarketApp", "Error initializing WishlistDao: ${e.message}", e)
            throw RuntimeException("Failed to initialize WishlistDao.", e)
        }
    }

    /**
     * Lazily initialized [PurchaseDao] for managing purchase history data.
     * Throws a [RuntimeException] if initialization fails.
     */
    val purchaseDao: PurchaseDao by lazy {
        try {
            database.purchaseDao()
        } catch (e: Exception) {
            Log.e("SupermarketApp", "Error initializing PurchaseDao: ${e.message}", e)
            throw RuntimeException("Failed to initialize PurchaseDao.", e)
        }
    }
}
