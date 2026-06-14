package com.example.mysupermarketapplication.util

/**
 * Defines all navigation routes and their associated argument keys for the Supermarket App.
 *
 * This object centralizes route management, making navigation consistent and reducing errors
 * from hardcoding strings throughout the application.
 */
object ScreenDestinations {
    // --- Main application screen routes ---

    /**
     * Route for the main product listing screen, where users can browse products.
     */
    const val PRODUCT_LIST = "productList"

    /**
     * Base route for the product detail screen. This route requires a product ID argument.
     * Use [productDetail] to construct the full route with an ID.
     */
    const val PRODUCT_DETAIL_ROUTE = "productDetail"

    /**
     * Route for the shopping cart screen, where users manage items before checkout.
     */
    const val CART = "cart"

    /**
     * Route for the user's wishlist, where saved products are displayed.
     */
    const val WISHLIST = "wishlist"

    /**
     * Route for the purchase history screen, showing past transactions.
     */
    const val PURCHASE_HISTORY = "purchaseHistory"

    /**
     * Route for the user profile screen.
     */
    const val PROFILE = "profile"

    /**
     * Route for the spending statistics screen, accessible from the profile.
     */
    const val STATISTICS = "statistics"

    // --- Navigation argument keys ---

    /**
     * Key used to pass the product ID as an argument to the product detail screen.
     */
    const val PRODUCT_DETAIL_ID_KEY = "productId"

    // --- Helper functions for constructing routes ---

    /**
     * Constructs the full navigation route to a specific product's detail screen.
     *
     * @param productId The unique identifier of the product to display.
     * @return A string representing the complete navigation route, e.g., "productDetail/123".
     */
    fun productDetail(productId: Int) = "$PRODUCT_DETAIL_ROUTE/$productId"
}
