package com.example.mysupermarketapplication.ui.screens.cart

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.CartItem

/**
 * Composable function for displaying the Cart Screen.
 * This screen shows the list of items currently in the user's shopping cart,
 * allows for quantity adjustments, item removal, and initiates the checkout process.
 *
 * @param cartItems The list of [CartItem] objects currently in the cart.
 * @param cartTotal The total calculated cost of all items in the cart.
 * @param onQuantityChange A lambda function invoked when a user changes the quantity of a cart item.
 * It receives the [CartItem] and the new quantity.
 * @param onRemoveItem A lambda function invoked when a user requests to remove a cart item.
 * It receives the [CartItem] to be removed.
 * @param onCheckout A lambda function invoked when the user clicks the "Checkout" button.
 * @param onBackClick A lambda function invoked when the user clicks the back button in the top app bar.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    cartTotal: Double,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onCheckout: () -> Unit,
    onBackClick: () -> Unit
) {
    // Scaffold provides the basic visual structure for a screen with a top bar, bottom bar, and content area.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Title of the TopAppBar.
                    Text(
                        text = stringResource(R.string.cart_icon_description),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // Navigation icon (back arrow) in the TopAppBar.
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Conditionally show the BottomAppBar only if the cart is not empty.
            // Includes animated visibility for a smoother user experience.
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                // Define enter animations: fade in and slide in from bottom.
                enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                // Define exit animations: fade out and slide out to bottom.
                exit = fadeOut(tween(300)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
            ) {
                BottomAppBar(
                    // Customize background color and elevation for the BottomAppBar.
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Display the "Total Cost" label.
                            Text(
                                text = stringResource(R.string.total_cost_prefix),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Display the calculated total cost.
                            Text(
                                text = "${String.format("%.2f", cartTotal)}€",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Checkout button.
                        Button(
                            onClick = onCheckout,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.height(56.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text(stringResource(R.string.checkout_button), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Check if the cart is empty.
        if (cartItems.isEmpty()) {
            // Display an empty cart message and a "Start Shopping" button.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shopping cart icon for empty state.
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.empty_cart_message),
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Empty cart message text.
                Text(
                    text = stringResource(R.string.empty_cart_message),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Button to navigate back to product list to "start shopping".
                Button(
                    onClick = onBackClick,
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(stringResource(R.string.start_shopping_to_see_stats), style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            // If the cart is not empty, display a scrollable list of cart items.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp) // Spacing between each cart item card.
            ) {
                // Iterate through the cart items and create a CartItemCard for each.
                items(cartItems, key = { it.id }) { item ->
                    CartItemCard(
                        cartItem = item,
                        onQuantityChange = onQuantityChange,
                        onRemoveItem = onRemoveItem
                    )
                }
            }
        }
    }
}

/**
 * Composable function for displaying a single item in the cart as a card.
 *
 * @param cartItem The [CartItem] to be displayed.
 * @param onQuantityChange A lambda function invoked when the quantity of this item is changed.
 * @param onRemoveItem A lambda function invoked when this item is to be removed from the cart.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit
) {
    val context = LocalContext.current // Get the current context for string localization.

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Display product name, localized.
                    Text(
                        text = cartItem.name.getLocalized(context),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Display price per unit, localized.
                    Text(
                        text = "${cartItem.price} ${cartItem.unit.getLocalized(context)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Display the total cost for this specific item (price * quantity).
                    Text(
                        text = "${stringResource(R.string.total_prefix)} ${String.format("%.2f", cartItem.price * cartItem.quantity)}€",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // IconButton to remove the item from the cart.
                IconButton(
                    onClick = { onRemoveItem(cartItem) },
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_item),
                        tint = MaterialTheme.colorScheme.error // Use error color for delete icon.
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Divider to visually separate item details from quantity controls.
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease quantity button.
                IconButton(
                    onClick = { onQuantityChange(cartItem, cartItem.quantity - 1) },
                    enabled = cartItem.quantity > 1 // Disable if quantity is 1 to prevent going below 1.
                ) {
                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.edit_quantity))
                }

                // AnimatedContent to smoothly animate quantity changes.
                AnimatedContent(
                    targetState = cartItem.quantity,
                    // Define transition for quantity changes: slide and fade in/out.
                    transitionSpec = {
                        (slideInVertically(tween(200)) + fadeIn(tween(200))) togetherWith
                                (slideOutVertically(tween(200)) + fadeOut(tween(200)))
                    },
                    label = "QuantityChange" // Label for debugging and inspection.
                ) { quantity ->
                    // Display the current quantity.
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                // Increase quantity button.
                IconButton(onClick = { onQuantityChange(cartItem, cartItem.quantity + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.edit_quantity))
                }
            }
        }
    }
}
