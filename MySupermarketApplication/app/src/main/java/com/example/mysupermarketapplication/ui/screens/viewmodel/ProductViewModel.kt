package com.example.mysupermarketapplication.ui.screens.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.*
import com.example.mysupermarketapplication.util.unaccent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Defines the available sorting orders for products.
 */
enum class ProductSortOrder {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC
}

/**
 * ViewModel for managing product data, cart, wishlist, and purchase history.
 * It provides UI-related data and handles user interactions.
 *
 * @param context The application context, used for localized strings.
 * @param productDao Data Access Object for products.
 * @param cartDao Data Access Object for cart items.
 * @param wishlistDao Data Access Object for wishlist items.
 * @param purchaseDao Data Access Object for purchases and purchase items.
 */
class ProductViewModel(
    private val context: Context,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    internal val wishlistDao: WishlistDao,
    private val purchaseDao: PurchaseDao
) : ViewModel() {

    // --- UI State Flows ---
    // These MutableStateFlows hold the current state of UI filters and controls,
    // which the UI observes for updates.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categories = MutableStateFlow<List<LocalizedText>>(emptyList())
    val categories: StateFlow<List<LocalizedText>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<LocalizedText?>(null)
    val selectedCategory: StateFlow<LocalizedText?> = _selectedCategory.asStateFlow()

    private val _onlyOffers = MutableStateFlow(false)
    val onlyOffers: StateFlow<Boolean> = _onlyOffers.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.NAME_ASC)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    // --- Data Flows from DAOs, Transformed for UI ---

    /**
     * [StateFlow] representing the current list of items in the user's cart.
     * The items are observed from the `cartDao` and then sorted alphabetically by product name.
     * Uses `WhileSubscribed(5000)` to keep the flow active for 5 seconds after no active collectors,
     * balancing resource usage and responsiveness.
     */
    val cartItems: StateFlow<List<CartItem>> = cartDao.getAllCartItems()
        .map { items -> items.sortedBy { it.name.getLocalized(context) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [StateFlow] representing the total cost of all items currently in the cart.
     * It observes changes in `cartItems` and recalculates the sum of (price * quantity).
     */
    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * [StateFlow] representing the current list of items in the user's wishlist.
     * Items are observed from the `wishlistDao` and sorted alphabetically by product name.
     */
    val wishlistItems: StateFlow<List<WishlistItem>> = wishlistDao.getAllWishlistItems()
        .map { items -> items.sortedBy { it.name.getLocalized(context) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [StateFlow] providing the main list of products to be displayed.
     * This flow is a combination of raw product data and several UI state filters:
     * `_searchQuery`, `_selectedCategory`, `_onlyOffers`, and `_sortOrder`.
     * Any change in these upstream flows triggers a recalculation and emission of the filtered
     * and sorted product list. Error handling is included to prevent crashes during filtering/sorting.
     */
    val products: StateFlow<List<Product>> =
        combine(
            productDao.getAllProducts(), // All raw products from the database.
            _searchQuery,             // User's current search input.
            _selectedCategory,        // Currently selected category filter.
            _onlyOffers,              // Boolean to filter by offers.
            _sortOrder                // Current sorting preference.
        ) { allProducts, query, category, offersOnly, sortOrder ->
            try {
                // Apply filtering logic based on search query, category, and offers.
                val filteredProducts = allProducts.filter { product ->
                    val matchesQuery = query.isBlank() || product.name.getLocalized(context)
                        .unaccent().contains(query.unaccent(), ignoreCase = true)
                    val matchesCategory = category == null || product.category.english == category.english
                    val matchesOffers = !offersOnly || product.offer != null
                    matchesQuery && matchesCategory && matchesOffers
                }

                // Apply sorting logic based on the selected sort order.
                when (sortOrder) {
                    ProductSortOrder.NAME_ASC -> filteredProducts.sortedBy { it.name.getLocalized(context) }
                    ProductSortOrder.NAME_DESC -> filteredProducts.sortedByDescending { it.name.getLocalized(context) }
                    ProductSortOrder.PRICE_ASC -> filteredProducts.sortedBy { it.price }
                    ProductSortOrder.PRICE_DESC -> filteredProducts.sortedByDescending { it.price }
                }
            } catch (e: Exception) {
                // Log any errors during filtering or sorting to aid debugging.
                Log.e("ProductViewModel", "Error filtering/sorting products: ${e.message}", e)
                emptyList() // Return an empty list to avoid UI breaking.
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [StateFlow] holding the complete history of user purchases,
     * including the purchase details and associated items.
     */
    val purchaseHistory: StateFlow<List<PurchaseWithItems>> = purchaseDao.getAllPurchasesWithItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [StateFlow] holding a map of total spending for each month.
     * The keys are formatted as "YYYY-MM" (e.g., "2023-01") and values are the total amount.
     */
    private val _monthlyExpenses = MutableStateFlow<Map<String, Double>>(emptyMap())
    val monthlyExpenses: StateFlow<Map<String, Double>> = _monthlyExpenses.asStateFlow()

    /**
     * [StateFlow] holding a map of total spending for each week.
     * The keys are formatted as "YYYY-W" (e.g., "2023-15") and values are the total amount.
     */
    private val _weeklyExpenses = MutableStateFlow<Map<String, Double>>(emptyMap())
    val weeklyExpenses: StateFlow<Map<String, Double>> = _weeklyExpenses.asStateFlow()

    // --- Initialization Block ---
    init {
        // These functions are called immediately when the ViewModel is created
        // to populate initial data for the UI.
        loadCategories()
        loadMonthlyExpenses()
        loadWeeklyExpenses()
    }

    // --- Data Loading Functions ---

    /**
     * Loads distinct product categories from the database and updates the [_categories] state.
     * Categories are sorted alphabetically by their English name.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                productDao.getAllProducts().collect { products ->
                    _categories.value = products
                        .map { it.category }
                        .distinctBy { it.english } // Ensures only unique categories are listed (e.g., "Dairy" only once).
                        .sortedBy { it.english } // Sorts categories alphabetically.
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading categories: ${e.message}", e)
                _categories.value = emptyList() // Clear categories on error.
            }
        }
    }

    // --- Public Functions for UI Interaction ---

    /**
     * Retrieves a single product by its ID.
     * @param id The ID of the product to retrieve.
     * @return A [Flow] emitting the [Product] or null if not found.
     */
    fun getProductById(id: Int): Flow<Product?> = productDao.getProductById(id)

    /**
     * Checks if a product is currently in the wishlist.
     * @param productId The ID of the product to check.
     * @return A [Flow] emitting `true` if the product is in the wishlist, `false` otherwise.
     */
    fun isProductInWishlist(productId: Int): Flow<Boolean> {
        return wishlistDao.getWishlistItemByProductId(productId).map { it != null }
    }

    /**
     * Updates the current search query, triggering a re-filter of products.
     * @param query The new search query string.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Updates the currently selected product category filter, triggering a re-filter of products.
     * @param category The selected category, or `null` to show all categories.
     */
    fun updateSelectedCategory(category: LocalizedText?) {
        _selectedCategory.value = category
    }

    /**
     * Toggles the filter to show only products that have offers.
     * @param enabled `true` to show only offers, `false` to show all products.
     */
    fun toggleOnlyOffers(enabled: Boolean) {
        _onlyOffers.value = enabled
    }

    /**
     * Updates the current product sorting order, triggering a re-sort of products.
     * @param sortOrder The new [ProductSortOrder].
     */
    fun updateSortOrder(sortOrder: ProductSortOrder) {
        _sortOrder.value = sortOrder
    }

    /**
     * Adds a product to the cart. If the product already exists in the cart, its quantity is incremented.
     * If not, a new cart item is created with a quantity of 1.
     * Errors during database operations are logged and re-thrown to allow UI feedback.
     * @param product The [Product] to add to the cart.
     */
    fun addProductToCart(product: Product) {
        viewModelScope.launch {
            try {
                val existingItem = cartDao.getCartItemByProductId(product.id)
                if (existingItem != null) {
                    // If item exists, update its quantity.
                    cartDao.update(existingItem.copy(quantity = existingItem.quantity + 1))
                } else {
                    // If item is new, insert it with quantity 1.
                    val newCartItem = CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        quantity = 1,
                        unit = product.unit
                    )
                    cartDao.insert(newCartItem)
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error adding product to cart: ${e.message}", e)
                throw e // Re-throw to allow the UI to handle the error (e.g., show a Toast).
            }
        }
    }

    /**
     * Updates an existing cart item. This is typically used to change the quantity of an item.
     * Errors are logged and re-thrown.
     * @param cartItem The [CartItem] with updated information.
     */
    fun updateCartItem(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartDao.update(cartItem)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error updating cart item: ${e.message}", e)
                throw e // Re-throw to allow the UI to handle the error.
            }
        }
    }

    /**
     * Removes a product from the cart.
     * Errors are logged and re-thrown.
     * @param cartItem The [CartItem] to remove.
     */
    fun removeProductFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartDao.delete(cartItem)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error removing product from cart: ${e.message}", e)
                throw e // Re-throw to allow the UI to handle the error.
            }
        }
    }

    /**
     * Toggles a product's presence in the wishlist. If it's currently in the wishlist, it's removed.
     * Otherwise, it's added.
     * Errors are logged and re-thrown.
     * @param product The [Product] to toggle.
     * @param currentlyInWishlist Current status of the product in the wishlist (determines add or remove).
     */
    fun toggleProductWishlistStatus(product: Product, currentlyInWishlist: Boolean) {
        viewModelScope.launch {
            try {
                if (currentlyInWishlist) {
                    // If in wishlist, find and delete the corresponding item.
                    val wishlistItem = wishlistItems.value.find { it.productId == product.id }
                    wishlistItem?.let { wishlistDao.delete(it) }
                } else {
                    // If not in wishlist, create and insert a new wishlist item.
                    val wishlistItem = WishlistItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        unit = product.unit,
                        imageResId = product.imageResId
                    )
                    wishlistDao.insert(wishlistItem)
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling wishlist status: ${e.message}", e)
                throw e // Re-throw to allow the UI to handle the error.
            }
        }
    }

    /**
     * Saves all current cart items as a new purchase record in the purchase history database.
     * After successful saving, the current cart is cleared.
     * This operation attempts to save all items, logging errors for individual items but
     * allowing the overall process to continue. If the initial purchase insertion fails,
     * the entire operation will fail.
     */
    suspend fun saveCartToPurchaseHistoryAndClearCart() {
        try {
            val currentCartItems = cartItems.value
            if (currentCartItems.isEmpty()) return // Do nothing if cart is empty.

            val currentTime = System.currentTimeMillis()
            val newPurchase = Purchase(purchaseDate = currentTime)
            // Insert the main purchase record and get its generated ID.
            val purchaseId = purchaseDao.insertPurchase(newPurchase)

            // Fetch all products once to efficiently link correct image resources to purchase items.
            val productsWithImages = productDao.getAllProducts().first() // .first() to get the current list immediately.
            val productMap = productsWithImages.associateBy { it.id }

            currentCartItems.forEach { cartItem ->
                try {
                    val correspondingProduct = productMap[cartItem.productId]
                    val purchaseItem = PurchaseItem(
                        purchaseId = purchaseId,
                        productId = cartItem.productId,
                        name = cartItem.name,
                        price = cartItem.price,
                        unit = cartItem.unit,
                        quantity = cartItem.quantity,
                        // Use product's image ID, or a fallback if the product is somehow not found.
                        imageResId = correspondingProduct?.imageResId ?: R.drawable.ic_launcher_background,
                        purchaseDate = currentTime
                    )
                    purchaseDao.insertPurchaseItem(purchaseItem)
                } catch (e: Exception) {
                    // Log error for individual item insertion but don't stop the whole checkout.
                    Log.e("ProductViewModel", "Error creating or inserting purchase item for cartItem ID ${cartItem.productId}: ${e.message}", e)
                }
            }

            // Clear the cart only after attempting to save all items to purchase history.
            cartDao.deleteAllCartItems()
        } catch (e: Exception) {
            // Log and re-throw severe errors that affect the entire checkout process.
            Log.e("ProductViewModel", "Error saving cart to purchase history: ${e.message}", e)
            throw e // Re-throw to allow the UI to show a Toast for the entire checkout process failure.
        }
    }

    /**
     * Adds items from a previous purchase back into the current shopping cart.
     * If an item already exists in the cart, its quantity will be increased by the amount
     * from the repeated purchase. New items will be added.
     * Errors during this process are logged, but the function attempts to process all items.
     * @param items The list of [PurchaseItem]s from a previous purchase to re-add to the cart.
     */
    fun repeatPurchase(items: List<PurchaseItem>) {
        viewModelScope.launch {
            try {
                items.forEach { item ->
                    try {
                        // Retrieve the product details to ensure current price/unit/name.
                        val product = productDao.getProductById(item.productId).first()
                        if (product != null) {
                            val existingCartItem = cartItems.value.find { it.productId == product.id }
                            if (existingCartItem != null) {
                                // If already in cart, update quantity.
                                cartDao.update(existingCartItem.copy(quantity = existingCartItem.quantity + item.quantity))
                            } else {
                                // If not in cart, insert as a new item.
                                cartDao.insert(
                                    CartItem(
                                        productId = product.id,
                                        name = product.name,
                                        price = product.price,
                                        unit = product.unit,
                                        quantity = item.quantity
                                    )
                                )
                            }
                        } else {
                            Log.w("ProductViewModel", "Product with ID ${item.productId} not found for repeat purchase. Skipping item.")
                        }
                    } catch (itemError: Exception) {
                        // Log error for individual item but continue with others.
                        Log.e("ProductViewModel", "Error processing individual item in repeat purchase for ID ${item.productId}: ${itemError.message}", itemError)
                    }
                }
            } catch (e: Exception) {
                // Log and re-throw any overarching errors during the repeat purchase operation.
                Log.e("ProductViewModel", "Error repeating purchase: ${e.message}", e)
                throw e // Re-throw to allow the UI to show a Toast for the overall repeat purchase failure.
            }
        }
    }

    // --- Expense Calculation Functions ---

    /**
     * Calculates the total expenses for each month from the purchase history.
     * This method observes changes in `purchaseHistory` and updates the `_monthlyExpenses` StateFlow.
     * Dates are formatted to "YYYY-MM" for grouping.
     */
    private fun loadMonthlyExpenses() {
        viewModelScope.launch {
            try {
                // Collect all purchases with their items.
                purchaseDao.getAllPurchasesWithItems().collect { purchasesWithItems ->
                    val expensesMap = mutableMapOf<String, Double>()
                    purchasesWithItems.forEach { purchaseWithItems ->
                        try {
                            // Extract year and month from the purchase date.
                            val calendar = Calendar.getInstance().apply { timeInMillis = purchaseWithItems.purchase.purchaseDate }
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed.
                            val monthKey = "$year-${month.toString().padStart(2, '0')}" // Format as "YYYY-MM".

                            // Calculate total cost for the current purchase.
                            val totalForPurchase = purchaseWithItems.items.sumOf { it.price * it.quantity }
                            // Add to the monthly total, or initialize if new month.
                            expensesMap[monthKey] = (expensesMap[monthKey] ?: 0.0) + totalForPurchase
                        } catch (itemError: Exception) {
                            Log.e("ProductViewModel", "Error processing purchase data for monthly expenses: ${itemError.message}", itemError)
                        }
                    }
                    _monthlyExpenses.value = expensesMap // Update the StateFlow.
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading monthly expenses from database: ${e.message}", e)
                _monthlyExpenses.value = emptyMap() // Reset to empty map on error.
            }
        }
    }

    /**
     * Calculates the total expenses for each week from the purchase history.
     * This method observes changes in `purchaseHistory` and updates the `_weeklyExpenses` StateFlow.
     * Dates are formatted to "YYYY-W" for grouping.
     */
    private fun loadWeeklyExpenses() {
        viewModelScope.launch {
            try {
                // Formatter for "YYYY-W" (year-week_of_year) format.
                val weekFormatter = SimpleDateFormat("yyyy-w", Locale.getDefault())

                // Collect all purchases with their items.
                purchaseDao.getAllPurchasesWithItems().collect { purchasesWithItems ->
                    val expensesMap = mutableMapOf<String, Double>()
                    purchasesWithItems.forEach { purchaseWithItems ->
                        try {
                            // Extract week key from the purchase date using the formatter.
                            val calendar = Calendar.getInstance().apply { timeInMillis = purchaseWithItems.purchase.purchaseDate }
                            val weekKey = weekFormatter.format(calendar.time)

                            // Calculate total cost for the current purchase.
                            val totalForPurchase = purchaseWithItems.items.sumOf { it.price * it.quantity }
                            // Add to the weekly total, or initialize if new week.
                            expensesMap[weekKey] = (expensesMap[weekKey] ?: 0.0) + totalForPurchase
                        } catch (itemError: Exception) {
                            Log.e("ProductViewModel", "Error processing purchase data for weekly expenses: ${itemError.message}", itemError)
                        }
                    }
                    _weeklyExpenses.value = expensesMap // Update the StateFlow.
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading weekly expenses from database: ${e.message}", e)
                _weeklyExpenses.value = emptyMap() // Reset to empty map on error.
            }
        }
    }
}

/**
 * Factory for creating a [ProductViewModel] with its required dependencies.
 * This pattern is essential for ViewModels that have constructor parameters,
 * ensuring they can be correctly instantiated by the Android framework.
 */
class ProductViewModelFactory(
    private val context: Context,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val wishlistDao: WishlistDao,
    private val purchaseDao: PurchaseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is ProductViewModel.
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // If it is, create and return an instance of ProductViewModel with provided DAOs and context.
            return ProductViewModel(context, productDao, cartDao, wishlistDao, purchaseDao) as T
        }
        // If an unsupported ViewModel class is requested, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
