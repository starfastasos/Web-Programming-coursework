package com.example.mysupermarketapplication.ui.screens.wishlist

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.WishlistItem

/**
 * Screen displaying the user's wishlist.
 * It shows a list of items or an empty state message if no items are present.
 *
 * @param wishlistItems The list of items currently in the wishlist.
 * @param onRemoveItem Lambda function to be invoked when an item needs to be removed from the wishlist.
 * @param onBackClick Lambda function to be invoked when the back button in the top app bar is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material 3 APIs like TopAppBar.
@Composable
fun WishlistScreen(
    wishlistItems: List<WishlistItem>,
    onRemoveItem: (WishlistItem) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        // Define the top app bar for the screen.
        topBar = {
            TopAppBar(
                title = {
                    // Display the title of the screen, localized and styled.
                    Text(
                        text = stringResource(R.string.wishlist_icon_description),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    // Back button in the top app bar.
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, // Standard back arrow icon.
                            contentDescription = stringResource(R.string.back_button_description) // Content description for accessibility.
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Background color of the top app bar.
                    titleContentColor = MaterialTheme.colorScheme.primary // Color for the title text.
                ),
                modifier = Modifier.shadow(elevation = 8.dp) // Add a shadow for visual separation.
            )
        }
    ) { paddingValues ->
        // Conditional rendering: show empty state or list of items.
        if (wishlistItems.isEmpty()) {
            // Display a message and icon when the wishlist is empty.
            EmptyWishlist(paddingValues)
        } else {
            // Display the list of wishlist items using LazyColumn for efficient scrolling.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize() // Occupy the maximum available space.
                    .padding(paddingValues) // Apply padding from the Scaffold.
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Additional horizontal and vertical padding.
                verticalArrangement = Arrangement.spacedBy(14.dp) // Space items vertically.
            ) {
                // Iterate through the wishlist items and create a card for each.
                items(wishlistItems, key = { it.id }) { item ->
                    WishlistItemCard(item = item, onRemoveItem = onRemoveItem)
                }
            }
        }
    }
}

/**
 * Displays the UI for an empty wishlist state.
 * It shows a decorative icon and an informative message to the user.
 *
 * @param paddingValues The padding values provided by the parent Scaffold.
 */
@Composable
fun EmptyWishlist(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the available space.
            .padding(paddingValues) // Apply padding from Scaffold.
            .padding(24.dp), // Additional padding around the content.
        verticalArrangement = Arrangement.Center, // Center content vertically.
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally.
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder, // Heart outline icon.
            contentDescription = null, // This is a decorative icon, so content description is null.
            modifier = Modifier.size(120.dp), // Set icon size.
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // Tint with a semi-transparent primary color.
        )
        Spacer(modifier = Modifier.height(28.dp)) // Vertical space between icon and text.
        Text(
            text = stringResource(R.string.empty_wishlist_message), // Localized message for empty wishlist.
            style = MaterialTheme.typography.headlineMedium, // Apply headline medium typography.
            textAlign = TextAlign.Center, // Center align the text.
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Text color.
            modifier = Modifier.fillMaxWidth() // Make text fill width.
        )
    }
}

/**
 * A Card composable displaying an individual wishlist item.
 * It includes the item's image, name, price with unit, and a button to remove it.
 *
 * @param item The [WishlistItem] to display.
 * @param onRemoveItem Lambda function invoked when the remove button is clicked.
 */
@SuppressLint("DefaultLocale")
@Composable
fun WishlistItemCard(item: WishlistItem, onRemoveItem: (WishlistItem) -> Unit) {
    val context = LocalContext.current // Access the current Android context for localized strings.

    Card(
        modifier = Modifier
            .fillMaxWidth() // Card fills the width of its parent.
            .wrapContentHeight() // Card wraps its content height.
            .padding(vertical = 4.dp), // Vertical padding for spacing between cards.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Custom background color for the card.
        elevation = CardDefaults.cardElevation(6.dp), // Apply elevation for a shadow effect.
        shape = RoundedCornerShape(16.dp) // Rounded corners for the card.
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // Row fills the width of the card.
                .padding(16.dp), // Padding inside the card.
            verticalAlignment = Alignment.CenterVertically // Vertically align items in the center.
        ) {
            Image(
                painter = painterResource(id = item.imageResId), // Load image from resource ID.
                contentDescription = item.name.getLocalized(context), // Content description for accessibility using localized name.
                contentScale = ContentScale.Crop, // Crop image to fill bounds.
                modifier = Modifier
                    .size(80.dp) // Set image size.
                    .clip(RoundedCornerShape(12.dp)) // Clip image to rounded corners.
                    .background( // Add a subtle gradient background to the image.
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.width(16.dp)) // Horizontal space between image and text.

            Column(
                modifier = Modifier.weight(1f), // Column takes available horizontal space.
                verticalArrangement = Arrangement.Center // Center content vertically within the column.
            ) {
                Text(
                    text = item.name.getLocalized(context), // Display localized product name.
                    style = MaterialTheme.typography.titleMedium, // Apply title medium typography.
                    fontWeight = FontWeight.Bold // Make text bold.
                )
                Spacer(modifier = Modifier.height(6.dp)) // Vertical space between name and price.
                Text(
                    text = "${String.format("%.2f", item.price)}${item.unit.getLocalized(context)}", // Display formatted price and localized unit.
                    style = MaterialTheme.typography.bodyMedium, // Apply body medium typography.
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Text color for price/unit.
                )
            }

            // Button to remove the item from the wishlist.
            IconButton(
                onClick = { onRemoveItem(item) }, // Trigger onRemoveItem lambda on click.
                modifier = Modifier
                    .size(44.dp) // Set icon button size.
                    .clip(CircleShape) // Clip button to a circle shape.
                    .background(MaterialTheme.colorScheme.errorContainer) // Background color indicating an error/removal action.
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, // Delete icon.
                    contentDescription = stringResource(R.string.remove_from_wishlist), // Content description for accessibility.
                    tint = MaterialTheme.colorScheme.error // Tint the icon with the error color.
                )
            }
        }
    }
}
