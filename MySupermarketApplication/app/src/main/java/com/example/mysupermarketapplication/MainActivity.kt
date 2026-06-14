package com.example.mysupermarketapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mysupermarketapplication.application.SupermarketApplication
import com.example.mysupermarketapplication.data.WishlistItem
import com.example.mysupermarketapplication.ui.screens.cart.CartScreen
import com.example.mysupermarketapplication.ui.screens.viewmodel.ProductSortOrder
import com.example.mysupermarketapplication.ui.screens.detail.ProductDetailScreen
import com.example.mysupermarketapplication.ui.screens.history.PurchaseHistoryScreen
import com.example.mysupermarketapplication.ui.screens.list.ProductListScreen
import com.example.mysupermarketapplication.ui.screens.profile.ProfileScreen
import com.example.mysupermarketapplication.ui.screens.profile.StatisticsScreen
import com.example.mysupermarketapplication.ui.screens.viewmodel.ProductViewModel
import com.example.mysupermarketapplication.ui.screens.viewmodel.ProductViewModelFactory
import com.example.mysupermarketapplication.ui.screens.wishlist.WishlistScreen
import com.example.mysupermarketapplication.ui.theme.MySupermarketApplicationTheme
import com.example.mysupermarketapplication.util.ScreenDestinations
import com.example.mysupermarketapplication.util.collectAsStateLifecycleAware
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Initialize ProductViewModel using a custom factory.
    // This factory provides the necessary Data Access Objects (DAOs) from the application class
    // to the ViewModel, ensuring it has access to the database operations.
    private val productViewModel: ProductViewModel by viewModels {
        try {
            // Attempt to cast the application to SupermarketApplication to get DAOs.
            val app = application as SupermarketApplication
            ProductViewModelFactory(
                applicationContext,
                app.productDao,
                app.cartDao,
                app.wishlistDao,
                app.purchaseDao
            )
        } catch (e: Exception) {
            // Log a detailed error message if ViewModel initialization fails.
            Log.e("MainActivity", "Error initializing ProductViewModel: ${e.message}", e)
            // Display a user-friendly error message to the user.
            Toast.makeText(
                this,
                "Application error: Cannot load data. Please restart.",
                Toast.LENGTH_LONG
            ).show()
            // Re-throw the exception as a RuntimeException. This will crash the app,
            // indicating a critical and unrecoverable error during startup that needs attention.
            throw RuntimeException("Failed to initialize ProductViewModel.", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the Compose UI for the entire application.
        setContent {
            MySupermarketApplicationTheme {
                // Use a Surface to apply the background color from the theme and fill the screen.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start the main Composable function for the supermarket application,
                    // passing the initialized ProductViewModel.
                    SupermarketApp(productViewModel = productViewModel)
                }
            }
        }
    }
}

@Composable
fun SupermarketApp(productViewModel: ProductViewModel = viewModel()) {
    // Initialize NavController to manage navigation state within the application.
    val navController = rememberNavController()
    // Create a CoroutineScope tied to the composition's lifecycle for launching suspend functions.
    val coroutineScope = rememberCoroutineScope()
    // Get the current Android context, useful for displaying Toasts or accessing resources.
    val context = LocalContext.current

    // Collect various UI state flows from the ViewModel.
    // These states are observed using collectAsStateLifecycleAware, which ensures
    // that updates are collected only when the Composable is active (lifecycle-aware).
    val searchQuery by productViewModel.searchQuery.collectAsStateLifecycleAware(initialValue = "")
    val selectedCategory by productViewModel.selectedCategory.collectAsStateLifecycleAware(initialValue = null)
    val onlyOffers by productViewModel.onlyOffers.collectAsStateLifecycleAware(initialValue = false)
    val sortOrder by productViewModel.sortOrder.collectAsStateLifecycleAware(initialValue = ProductSortOrder.NAME_ASC)
    val availableCategories by productViewModel.categories.collectAsStateLifecycleAware(initialValue = emptyList())

    // Define the navigation graph using NavHost.
    // The startDestination specifies the initial screen when the app launches.
    NavHost(navController = navController, startDestination = ScreenDestinations.PRODUCT_LIST) {

        // Composable for the Product List screen.
        composable(ScreenDestinations.PRODUCT_LIST) {
            // Collect the list of products from the ViewModel.
            val products by productViewModel.products.collectAsStateLifecycleAware(initialValue = emptyList())
            ProductListScreen(
                products = products,
                // Navigation actions for various buttons/clicks on the product list screen.
                onProductClick = { productId -> navController.navigate(ScreenDestinations.productDetail(productId)) },
                onCartClick = { navController.navigate(ScreenDestinations.CART) },
                onWishlistClick = { navController.navigate(ScreenDestinations.WISHLIST) },
                onPurchaseHistoryClick = { navController.navigate(ScreenDestinations.PURCHASE_HISTORY) },
                onProfileClick = { navController.navigate(ScreenDestinations.PROFILE) },
                // Callbacks for search, category, offers, and sort order changes.
                searchQuery = searchQuery,
                onSearchQueryChange = productViewModel::updateSearchQuery,
                availableCategories = availableCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = productViewModel::updateSelectedCategory,
                onlyOffers = onlyOffers,
                onOnlyOffersChanged = productViewModel::toggleOnlyOffers,
                currentSortOrder = sortOrder,
                onSortOrderSelected = productViewModel::updateSortOrder
            )
        }

        // Composable for the Product Detail screen, accepting a product ID as an argument.
        composable("${ScreenDestinations.PRODUCT_DETAIL_ROUTE}/{${ScreenDestinations.PRODUCT_DETAIL_ID_KEY}}") { backStackEntry ->
            // Extract the product ID from the navigation arguments.
            val productId = backStackEntry.arguments?.getString(ScreenDestinations.PRODUCT_DETAIL_ID_KEY)?.toIntOrNull()
            productId?.let {
                // Fetch the product details by ID from the ViewModel.
                val product by productViewModel.getProductById(it).collectAsStateLifecycleAware(initialValue = null)
                product?.let { prod ->
                    ProductDetailScreen(
                        product = prod,
                        onBackClick = { navController.popBackStack() }, // Navigate back.
                        onAddToCart = { item ->
                            // Add product to cart and show a Toast message.
                            try {
                                productViewModel.addProductToCart(item)
                                Toast.makeText(context, R.string.success_add_to_cart, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                // Log and show error if adding to cart fails.
                                Log.e("SupermarketApp", "Error adding product to cart: ${e.message}", e)
                                Toast.makeText(context, R.string.error_add_to_cart, Toast.LENGTH_SHORT).show()
                            }
                        },
                        productViewModel = productViewModel // Pass ViewModel for wishlist toggling
                    )
                }
            }
        }

        // Composable for the Cart screen.
        composable(ScreenDestinations.CART) {
            // Collect cart items and total from the ViewModel.
            val cartItems by productViewModel.cartItems.collectAsStateLifecycleAware(initialValue = emptyList())
            val cartTotal by productViewModel.cartTotal.collectAsStateLifecycleAware(initialValue = 0.0)

            CartScreen(
                cartItems = cartItems,
                cartTotal = cartTotal,
                onQuantityChange = { item, newQuantity ->
                    // Update item quantity in cart or remove if quantity becomes 0.
                    try {
                        if (newQuantity > 0) {
                            productViewModel.updateCartItem(item.copy(quantity = newQuantity))
                        } else {
                            productViewModel.removeProductFromCart(item)
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Cart update error: ${e.message}", e)
                        Toast.makeText(context, R.string.error_updating_cart, Toast.LENGTH_SHORT).show()
                    }
                },
                onRemoveItem = { item ->
                    // Remove item from cart.
                    try {
                        productViewModel.removeProductFromCart(item)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Remove item error: ${e.message}", e)
                        Toast.makeText(context, R.string.error_updating_cart, Toast.LENGTH_SHORT).show()
                    }
                },
                onCheckout = {
                    // Perform checkout process in a coroutine.
                    coroutineScope.launch {
                        try {
                            productViewModel.saveCartToPurchaseHistoryAndClearCart()
                            // Navigate back to product list after successful checkout,
                            // clearing the back stack up to the product list.
                            navController.navigate(ScreenDestinations.PRODUCT_LIST) {
                                popUpTo(ScreenDestinations.PRODUCT_LIST) { inclusive = true }
                            }
                            Toast.makeText(context, R.string.purchase_successful, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Checkout error: ${e.message}", e)
                            Toast.makeText(context, R.string.error_during_checkout, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBackClick = { navController.popBackStack() } // Navigate back.
            )
        }

        // Composable for the Wishlist screen.
        composable(ScreenDestinations.WISHLIST) {
            // Collect wishlist items from the ViewModel.
            val wishlistItems by productViewModel.wishlistItems.collectAsStateLifecycleAware(initialValue = emptyList())
            WishlistScreen(
                wishlistItems = wishlistItems,
                onRemoveItem = { item ->
                    // Remove item from wishlist and show a Toast.
                    try {
                        productViewModel.removeProductFromWishlist(item)
                        Toast.makeText(context, R.string.success_removing_from_wishlist, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Wishlist removal error: ${e.message}", e)
                        Toast.makeText(context, R.string.error_removing_from_wishlist, Toast.LENGTH_SHORT).show()
                    }
                },
                onBackClick = { navController.popBackStack() } // Navigate back.
            )
        }

        // Composable for the Purchase History screen.
        composable(ScreenDestinations.PURCHASE_HISTORY) {
            // Collect purchase history from the ViewModel.
            val purchaseHistory by productViewModel.purchaseHistory.collectAsStateLifecycleAware(initialValue = emptyList())
            // Get the repeat purchase message from string resources.
            val repeatPurchaseMessage = stringResource(R.string.purchase_repeated_message)

            PurchaseHistoryScreen(
                purchaseHistory = purchaseHistory,
                onBackClick = { navController.popBackStack() }, // Navigate back.
                onRepeatPurchase = { purchase ->
                    // Repeat a past purchase, adding items to the cart, in a coroutine.
                    coroutineScope.launch {
                        try {
                            productViewModel.repeatPurchase(purchase)
                            Toast.makeText(context, repeatPurchaseMessage, Toast.LENGTH_SHORT).show()
                            // Navigate to the cart after repeating the purchase.
                            navController.navigate(ScreenDestinations.CART)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Repeat purchase error: ${e.message}", e)
                            Toast.makeText(context, R.string.error_repeat_purchase, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onNavigateToProductList = {
                    // Navigate to product list, clearing the back stack.
                    navController.navigate(ScreenDestinations.PRODUCT_LIST) {
                        popUpTo(ScreenDestinations.PRODUCT_LIST) { inclusive = true }
                    }
                }
            )
        }

        // Composable for the Profile screen.
        composable(ScreenDestinations.PROFILE) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }, // Navigate back.
                onStatisticsClick = {
                    // Navigate to the Statistics screen.
                    navController.navigate(ScreenDestinations.STATISTICS)
                }
            )
        }

        // Composable for the Statistics screen.
        composable(ScreenDestinations.STATISTICS) {
            // Collect monthly and weekly expense data from the ViewModel.
            val monthlyExpenses by productViewModel.monthlyExpenses.collectAsStateLifecycleAware(initialValue = emptyMap())
            val weeklyExpenses by productViewModel.weeklyExpenses.collectAsStateLifecycleAware(initialValue = emptyMap())

            StatisticsScreen(
                monthlyExpenses = monthlyExpenses,
                weeklyExpenses = weeklyExpenses,
                onBackClick = { navController.popBackStack() }, // Navigate back.
                onNavigateToProductList = {
                    // Navigate to product list, clearing the back stack.
                    navController.navigate(ScreenDestinations.PRODUCT_LIST) {
                        popUpTo(ScreenDestinations.PRODUCT_LIST) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Extension function for ProductViewModel to encapsulate the logic for removing a product from the wishlist.
// This function runs within the ViewModel's own viewModelScope, ensuring proper lifecycle management
// for database operations.
private fun ProductViewModel.removeProductFromWishlist(item: WishlistItem) {
    viewModelScope.launch {
        try {
            // Call the delete method on the wishlist DAO to remove the item from the database.
            wishlistDao.delete(item)
        } catch (e: Exception) {
            // Log any errors that occur during the wishlist removal process.
            Log.e("ProductViewModel", "Error removing product from wishlist", e)
            // Note: Error handling for the UI (e.g., Toast) is typically done in the Composable
            // that calls this function, as it has access to the Context.
        }
    }
}
