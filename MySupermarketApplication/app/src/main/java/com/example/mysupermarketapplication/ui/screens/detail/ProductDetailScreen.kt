package com.example.mysupermarketapplication.ui.screens.detail

import android.content.Context
import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.Product
import com.example.mysupermarketapplication.ui.screens.list.getLocalizedCategory
import com.example.mysupermarketapplication.ui.screens.viewmodel.ProductViewModel

/**
 * Composable function to display the detailed screen for a single product.
 * This screen provides comprehensive information about a product,
 * including its image, name, category, price, description, and options to add to cart or wishlist.
 *
 * @param product The [Product] object to display.
 * @param onBackClick Lambda to be invoked when the back button in the top app bar is pressed.
 * @param onAddToCart Lambda to be invoked when the "Add to Cart" button is pressed.
 * This function typically adds the current product to the user's shopping cart.
 * @param productViewModel The [ProductViewModel] to interact with product-related data,
 * particularly for managing the wishlist status of products.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    productViewModel: ProductViewModel
) {
    // Remember the scroll state to enable vertical scrolling for the content.
    val scrollState = rememberScrollState()
    // Collects the wishlist status of the current product as a State.
    // This allows the UI to react to changes in the wishlist status (e.g., icon change).
    val isInWishlist by productViewModel.isProductInWishlist(product.id).collectAsState(initial = false)
    // Get the current context, useful for accessing resources like strings.
    val context = LocalContext.current

    // Scaffold provides the basic visual structure for a screen with a top app bar and content area.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name.getLocalized(context)) }, // Display product name as title.
                navigationIcon = {
                    // Back button in the top left corner.
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                actions = {
                    // Wishlist toggle button in the top right corner.
                    IconButton(
                        onClick = {
                            try {
                                // Toggle the product's wishlist status via the ViewModel.
                                productViewModel.toggleProductWishlistStatus(product, isInWishlist)
                                // Display a success Toast message based on the action (added or removed).
                                val messageResId = if (isInWishlist) R.string.success_removing_from_wishlist else R.string.success_add_to_wishlist
                                Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                // Log the error for debugging purposes.
                                Log.e("ProductDetailScreen", "Error toggling wishlist status: ${e.message}", e)
                                // Display an error Toast message to the user.
                                val errorResId = if (isInWishlist) R.string.error_removing_from_wishlist else R.string.error_add_to_wishlist
                                Toast.makeText(context, errorResId, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        // Change icon based on whether the product is in the wishlist.
                        // Filled heart if in wishlist, outlined heart otherwise.
                        Icon(
                            imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isInWishlist)
                                stringResource(R.string.remove_from_wishlist)
                            else
                                stringResource(R.string.add_to_wishlist),
                            // Change tint based on wishlist status for visual feedback.
                            tint = if (isInWishlist) MaterialTheme.colorScheme.error // Red tint for favorited
                            else MaterialTheme.colorScheme.onSurface // Default tint for not favorited
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main content column for the product details, allowing scrolling.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState) // Makes the content scrollable.
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Composable to display the product's image within a stylized card.
            ProductImageCard(product, context)

            Spacer(modifier = Modifier.height(24.dp))

            // Display the product's name with a large, bold style.
            Text(
                text = product.name.getLocalized(context),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Composable to display the product's category and availability status.
            ProductCategoryAndAvailability(product, context)

            Spacer(modifier = Modifier.height(16.dp))

            // Composable to display the product's price and any available offer.
            ProductPriceAndOffer(product, context)

            Spacer(modifier = Modifier.height(16.dp))

            // Section for product description.
            SectionTitle(text = stringResource(R.string.description_label))
            Text(
                text = product.description.getLocalized(context),
                style = MaterialTheme.typography.bodyLarge
            )

            // Optional: Product ingredients section. Only displayed if ingredients data is available.
            product.ingredients?.let {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(text = stringResource(R.string.ingredients_label))
                Text(
                    text = it.getLocalized(context),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Optional: Product nutritional information section. Only displayed if data is available.
            product.nutritionalInfo?.let {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(text = stringResource(R.string.nutritional_info_label))
                Text(
                    text = it.getLocalized(context),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "Add to Cart" button, styled to fill the width.
            Button(
                onClick = { onAddToCart(product) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary // Use primary color for the button.
                )
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null, // Content description is provided by the button's text.
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_to_cart))
            }

            Spacer(modifier = Modifier.height(16.dp)) // Adds space at the bottom of the scrollable content.
        }
    }
}

/**
 * Displays the product's image within a styled [Card].
 * The image is clipped to rounded corners and has a subtle gradient background.
 *
 * @param product The product whose image is to be displayed.
 * @param context The current [Context] for localized strings.
 */
@Composable
private fun ProductImageCard(product: Product, context: Context) {
    Card(
        shape = RoundedCornerShape(20.dp), // Rounded corners for the card.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Background color for the card.
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Card shadow.
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Fixed height for the image card to ensure consistent layout.
    ) {
        Box(
            modifier = Modifier
                // Add a subtle vertical gradient background behind the image for visual appeal.
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), // Light primary color at top.
                            MaterialTheme.colorScheme.surfaceVariant // Surface variant color at bottom.
                        )
                    )
                )
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(product.imageResId), // Load image from resources.
                contentDescription = product.name.getLocalized(context), // Localized content description for accessibility.
                contentScale = ContentScale.Crop, // Crops the image to fill the bounds while maintaining aspect ratio.
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)) // Clips the image to match the card's rounded corners.
            )
        }
    }
}

/**
 * Displays the product's category and availability status in a horizontally arranged row.
 *
 * @param product The product whose details are to be displayed.
 * @param context The current [Context] for localized strings.
 */
@Composable
private fun ProductCategoryAndAvailability(product: Product, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Spaces items evenly horizontally.
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display localized category and availability, separated by a bullet point.
        Text(
            text = "${getLocalizedCategory(context, product.category)} • ${product.availability.getLocalized(context)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Subdued color for descriptive text.
        )
    }
}

/**
 * Displays the product's price and any associated offer.
 * The price stands out with the primary color, and offers are highlighted in an error color.
 *
 * @param product The product whose price and offer are to be displayed.
 * @param context The current [Context] for localized strings.
 */
@Composable
private fun ProductPriceAndOffer(product: Product, context: Context) {
    // Display the product's price and unit, formatted to two decimal places.
    Text(
        text = "${stringResource(R.string.price_prefix)} ${String.format("%.2f", product.price)}${product.unit.getLocalized(context)}",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary // Primary color for the price to make it visually prominent.
    )

    // Conditionally displays the offer text only if an offer exists for the product.
    product.offer?.let {
        Text(
            text = "${stringResource(R.string.offer_prefix)} ${it.getLocalized(context)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error, // Error color for offers to draw immediate attention.
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * A reusable composable for displaying a consistent section title within the detail screen.
 *
 * @param text The string text to be displayed as a section title.
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}
