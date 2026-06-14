package com.example.mysupermarketapplication.ui.screens.history

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import com.example.mysupermarketapplication.data.PurchaseItem
import com.example.mysupermarketapplication.data.PurchaseWithItems
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable function for displaying the Purchase History Screen.
 * This screen lists all past purchases made by the user, showing details for each purchase
 * and providing an option to re-purchase items.
 *
 * @param purchaseHistory A list of [PurchaseWithItems] representing the user's complete purchase history.
 * @param onBackClick A lambda function invoked when the back arrow in the top app bar is clicked.
 * @param onRepeatPurchase A lambda function invoked when the "Repeat Purchase" button is clicked for a specific history entry.
 * It takes a list of [PurchaseItem]s from that past purchase to add to the current cart.
 * @param onNavigateToProductList A lambda function to navigate the user back to the Product List screen.
 * This is used when the purchase history is empty.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    purchaseHistory: List<PurchaseWithItems>,
    onBackClick: () -> Unit,
    onRepeatPurchase: (List<PurchaseItem>) -> Unit,
    onNavigateToProductList: () -> Unit // Callback to navigate to ProductListScreen when history is empty.
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.purchase_history_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    // Back button in the top app bar.
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->

        // Use AnimatedContent to smoothly transition between the empty history state and the populated list state.
        AnimatedContent(
            targetState = purchaseHistory.isEmpty(), // The key state that triggers the animation.
            transitionSpec = {
                // Defines the animation for content entering and exiting.
                // Content slides in from half its height and fades in, while old content slides out and fades out.
                (fadeIn() + slideInVertically(initialOffsetY = { it / 2 })).togetherWith(
                    fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                )
            },
            label = "PurchaseHistoryContent" // A label for debugging and tooling.
        ) { isEmpty ->
            if (isEmpty) {
                // Content displayed when there is no purchase history.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // History icon as a visual indicator for empty state.
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null, // No specific content description needed as it's purely decorative here.
                        modifier = Modifier.size(110.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    // Message indicating no purchase history.
                    Text(
                        text = stringResource(R.string.no_purchase_history),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Informative text to encourage starting shopping.
                    Text(
                        text = stringResource(R.string.start_shopping_to_see_stats),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Add some space before the button.

                    // Button to navigate to the Product List screen.
                    Button(
                        onClick = onNavigateToProductList, // Invokes the provided callback.
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.start_shopping_to_see_stats), style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                // Content displayed when there is purchase history.
                // A LazyColumn is used for efficient rendering of a potentially long list of purchases.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp) // Spacing between each purchase history card.
                ) {
                    // Iterate through each purchase entry and create a PurchaseHistoryCard for it.
                    items(purchaseHistory, key = { it.purchase.purchaseId }) { purchaseWithItems ->
                        PurchaseHistoryCard(
                            purchaseWithItems = purchaseWithItems,
                            onRepeatPurchase = { onRepeatPurchase(purchaseWithItems.items) } // Pass the items for re-purchase.
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function for displaying a single purchase entry as a card in the history.
 * It shows the purchase date, total cost, and a list of items within that purchase.
 *
 * @param purchaseWithItems A [PurchaseWithItems] object containing the purchase details and its associated items.
 * @param onRepeatPurchase A lambda function invoked when the "Repeat Purchase" button within this card is clicked.
 */
@SuppressLint("DefaultLocale")
@Composable
fun PurchaseHistoryCard(
    purchaseWithItems: PurchaseWithItems,
    onRepeatPurchase: () -> Unit
) {
    // Format the timestamp into a human-readable date and time string.
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(purchaseWithItems.purchase.purchaseDate))

    // Calculate the total cost for this specific purchase by summing up price * quantity for each item.
    val totalCost = purchaseWithItems.items.sumOf { it.price * it.quantity }
    val context = LocalContext.current // Get the current context for string localization.

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Card background color.
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Card shadow.
        shape = RoundedCornerShape(16.dp) // Rounded corners for the card.
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row displaying the purchase date and total cost.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly horizontally.
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.date_prefix) + " " + formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Subdued color for date.
                )
                Text(
                    text = "${String.format("%.2f", totalCost)}€",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary // Prominent color for total cost.
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Column to list all individual purchased items within this transaction.
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                purchaseWithItems.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image of the purchased item.
                        Image(
                            painter = painterResource(item.imageResId),
                            contentDescription = item.name.getLocalized(context), // Localized content description.
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border( // Subtle border around the image.
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentScale = ContentScale.Crop // Crop image to fill bounds.
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        // Item name and quantity/price details.
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name.getLocalized(context),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.quantity} × ${String.format("%.2f", item.price)}${item.unit.getLocalized(context)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Total cost for this individual item.
                        Text(
                            text = "${String.format("%.2f", item.price * item.quantity)}€",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Divider to separate items list from the repeat purchase button.
            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Button to repeat the purchase.
            Button(
                onClick = onRepeatPurchase,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp), // Pill-shaped button.
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // Secondary color for distinct action.
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null, // Decorative icon.
                    modifier = Modifier.padding(end = 10.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
                Text(
                    stringResource(R.string.repeat_purchase_button),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
