package com.example.mysupermarketapplication.ui.screens.list

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.LocalizedText
import com.example.mysupermarketapplication.data.Product
import com.example.mysupermarketapplication.ui.screens.viewmodel.ProductSortOrder

/**
 * Composable function for displaying the main Product List Screen.
 * This screen shows a list of products, along with search, filter, and sort options.
 * It also provides navigation to other parts of the application like Cart, Wishlist, etc.
 *
 * @param products The list of [Product] objects to display, usually filtered and sorted.
 * @param onProductClick Lambda invoked when a product item is clicked, navigating to its detail screen.
 * @param onCartClick Lambda invoked to navigate to the Cart screen.
 * @param onWishlistClick Lambda invoked to navigate to the Wishlist screen.
 * @param onPurchaseHistoryClick Lambda invoked to navigate to the Purchase History screen.
 * @param onProfileClick Lambda invoked to navigate to the Profile screen.
 * @param searchQuery The current search query string applied to the product list.
 * @param onSearchQueryChange Lambda to update the search query when the user types.
 * @param availableCategories A list of [LocalizedText] representing all available product categories.
 * @param selectedCategory The currently selected category filter, or null if "All Categories" is selected.
 * @param onCategorySelected Lambda to update the selected category filter.
 * @param onlyOffers Boolean indicating if only products with offers should be shown.
 * @param onOnlyOffersChanged Lambda to toggle the "only offers" filter.
 * @param currentSortOrder The currently selected sorting order for the products.
 * @param onSortOrderSelected Lambda to update the product sorting order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onPurchaseHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    availableCategories: List<LocalizedText>,
    selectedCategory: LocalizedText?,
    onCategorySelected: (LocalizedText?) -> Unit,
    onlyOffers: Boolean,
    onOnlyOffersChanged: (Boolean) -> Unit,
    currentSortOrder: ProductSortOrder,
    onSortOrderSelected: (ProductSortOrder) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Action icons in the TopAppBar for navigation to other screens.
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile_icon_description))
                    }
                    IconButton(onClick = onPurchaseHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = stringResource(R.string.purchase_history_icon_description))
                    }
                    IconButton(onClick = onWishlistClick) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.wishlist_icon_description))
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.cart_icon_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search input field for filtering products by name.
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(stringResource(R.string.search_products_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp) // Rounded corners for the search bar.
            )

            val context = LocalContext.current // Get current context for localization.

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown menu for category selection filter.
                var expandedCategory by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f).wrapContentSize(Alignment.TopStart)) {
                    ElevatedButton(
                        onClick = { expandedCategory = true },
                        modifier = Modifier.fillMaxWidth(0.95f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(selectedCategory?.let { getLocalizedCategory(context, it) } ?: stringResource(R.string.all_categories_label))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.width(IntrinsicSize.Max) // Dropdown width adjusts to content.
                    ) {
                        // "All Categories" option to clear the category filter.
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_categories_label)) },
                            onClick = {
                                onCategorySelected(null)
                                expandedCategory = false
                            }
                        )
                        // List all available categories as dropdown menu items.
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(getLocalizedCategory(context, category)) },
                                onClick = {
                                    onCategorySelected(category)
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Filter chip to toggle "only offers" filter.
                FilterChip(
                    selected = onlyOffers,
                    onClick = { onOnlyOffersChanged(!onlyOffers) },
                    label = {
                        Text(
                            stringResource(R.string.offers_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.wrapContentWidth(),
                    leadingIcon = if (onlyOffers) {
                        // Display a favorite icon when "only offers" is selected.
                        { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Dropdown menu for sorting options (e.g., by name, by price).
                var expandedSort by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f).wrapContentSize(Alignment.TopEnd)) {
                    ElevatedButton(
                        onClick = { expandedSort = true },
                        modifier = Modifier.fillMaxWidth(0.95f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                    ) {
                        // Display the currently selected sort order.
                        Text(
                            when (currentSortOrder) {
                                ProductSortOrder.NAME_ASC -> stringResource(R.string.sort_name_asc)
                                ProductSortOrder.NAME_DESC -> stringResource(R.string.sort_name_desc)
                                ProductSortOrder.PRICE_ASC -> stringResource(R.string.sort_price_asc)
                                ProductSortOrder.PRICE_DESC -> stringResource(R.string.sort_price_desc)
                            }
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false },
                        modifier = Modifier.width(IntrinsicSize.Max) // Dropdown width adjusts to content.
                    ) {
                        // Iterate through all defined sorting options.
                        ProductSortOrder.entries.forEach { sortOrder ->
                            DropdownMenuItem(
                                text = {
                                    // Display localized name for each sort option.
                                    Text(
                                        when (sortOrder) {
                                            ProductSortOrder.NAME_ASC -> stringResource(R.string.sort_name_asc)
                                            ProductSortOrder.NAME_DESC -> stringResource(R.string.sort_name_desc)
                                            ProductSortOrder.PRICE_ASC -> stringResource(R.string.sort_price_asc)
                                            ProductSortOrder.PRICE_DESC -> stringResource(R.string.sort_price_desc)
                                        }
                                    )
                                },
                                onClick = {
                                    onSortOrderSelected(sortOrder) // Update the sort order.
                                    expandedSort = false // Close the dropdown.
                                }
                            )
                        }
                    }
                }
            }

            // Conditionally display a message if no products match the current filters/search.
            if (products.isEmpty()) {
                Text(
                    stringResource(R.string.no_products_available),
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Display the list of products using LazyColumn for efficient scrolling.
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp), // Spacing between product items.
                    contentPadding = PaddingValues(bottom = 20.dp) // Padding at the bottom of the list.
                ) {
                    items(products) { product ->
                        ProductListItem(
                            product = product,
                            onClick = { onProductClick(product.id) } // Handle product item click.
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function for displaying a single product item in the list.
 * It includes the product's image, name, category, price, offer details, and ingredients.
 *
 * @param product The [Product] object to display.
 * @param onClick Lambda invoked when this product item is clicked.
 */
@SuppressLint("DefaultLocale")
@Composable
fun ProductListItem(
    product: Product,
    onClick: () -> Unit
) {
    val context = LocalContext.current // Get current context for localization.

    // MutableInteractionSource to track press states for custom click animations.
    val interactionSource = remember { MutableInteractionSource() }
    // Collect the press state of the interaction source.
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale and transparency to provide visual feedback when the item is pressed.
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "scaleAnim")
    val alpha by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f, label = "alphaAnim")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Apply scale and alpha animations based on press state.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            // Make the card clickable, with null indication to handle custom visual feedback.
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(16.dp)) // Clip content to rounded corners.
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)), // Add a subtle border.
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Card shadow.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Background color of the card.
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image.
            Image(
                painter = painterResource(product.imageResId),
                contentDescription = product.name.getLocalized(context), // Localized content description.
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop // Crop image to fill the bounds.
            )

            Column(modifier = Modifier.weight(1f)) {
                // Product name.
                Text(
                    text = product.name.getLocalized(context),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Product category.
                    Text(
                        text = getLocalizedCategory(context, product.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Display offer details if an offer exists.
                if (product.offer != null) {
                    // Show offer label and price with strikethrough for original price.
                    Text(
                        text = "${stringResource(R.string.offer_prefix)} ${product.offer.getLocalized(context)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, // Primary color for offers.
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.2f", product.price)}${product.unit.getLocalized(context)}",
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough, // Strikethrough for original price.
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    // Show normal price if no offer.
                    Text(
                        text = "${String.format("%.2f", product.price)}${product.unit.getLocalized(context)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary // Secondary color for normal price.
                    )
                }

                // Show ingredients if available.
                product.ingredients?.let { ingredients ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ingredients_prefix) + " " + ingredients.getLocalized(context),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get the localized string for a given product category.
 *
 * @param context The current [Context] to access string resources.
 * @param categoryLocalizedText The [LocalizedText] object representing the category.
 * @return The localized category string.
 */
@Composable
fun getLocalizedCategory(context: Context, categoryLocalizedText: LocalizedText): String {
    // Get the category key from the LocalizedText object using the current context's locale.
    val categoryKey = categoryLocalizedText.getLocalized(context)
    // Map the category key to the corresponding string resource ID.
    val resourceId = when (categoryKey) {
        "Dairy" -> R.string.category_dairy
        "Fresh Food" -> R.string.category_fresh_food
        "Cleaning Products" -> R.string.category_cleaning
        "Frozen" -> R.string.category_frozen
        "Beverages" -> R.string.category_beverages
        "Meat" -> R.string.category_meat
        "Oils & Vinegars" -> R.string.category_oils_vinegars
        "Fruits & Vegetables" -> R.string.category_fruits_vegetables
        "Snacks & Sweets" -> R.string.category_snacks
        "Household Items" -> R.string.category_household
        "Dry Food" -> R.string.category_dry_food
        "Pasta & Rice" -> R.string.category_pasta_rice
        "Bakery" -> R.string.category_bakery
        else -> 0 // Default to 0 if no matching resource ID is found.
    }
    // Return the localized string from resources if found, otherwise return the original category key.
    return if (resourceId != 0) context.getString(resourceId) else categoryKey
}
