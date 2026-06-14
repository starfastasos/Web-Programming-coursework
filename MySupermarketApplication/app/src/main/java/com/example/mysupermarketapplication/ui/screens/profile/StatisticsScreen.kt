package com.example.mysupermarketapplication.ui.screens.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays the statistics screen where users can toggle between monthly and weekly spending.
 * It shows either a list of expenses or an empty state message when no data is available.
 *
 * @param monthlyExpenses A map where keys are month strings (e.g., "YYYY-MM") and values are the total spending for that month.
 * @param weeklyExpenses A map where keys are week strings (e.g., "YYYY-W") and values are the total spending for that week.
 * @param onBackClick Callback function to navigate back to the previous screen (e.g., Profile screen).
 * @param onNavigateToProductList Callback function to navigate to the ProductListScreen, typically used when there are no statistics to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    monthlyExpenses: Map<String, Double>,
    weeklyExpenses: Map<String, Double>,
    onBackClick: () -> Unit,
    onNavigateToProductList: () -> Unit
) {
    // State to manage whether monthly or weekly expenses are currently being shown.
    var showMonthly by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Segmented button row to switch between monthly and weekly views.
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(0.85f) // Occupy 85% of the width.
            ) {
                // Monthly expenses button.
                SegmentedButton(
                    selected = showMonthly,
                    onClick = { showMonthly = true },
                    shape = MaterialTheme.shapes.extraSmall, // Smaller rounded corners.
                    modifier = Modifier.weight(1f), // Distribute weight evenly.
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.monthly_expenses_label))
                }
                // Weekly expenses button.
                SegmentedButton(
                    selected = !showMonthly,
                    onClick = { showMonthly = false },
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.weight(1f),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.weekly_expenses_label))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Determine which set of expenses to display based on the `showMonthly` state.
            val currentExpenses = if (showMonthly) monthlyExpenses else weeklyExpenses
            // Determine the appropriate empty message based on the current view.
            val emptyMessage =
                if (showMonthly) R.string.no_monthly_statistics_available else R.string.no_weekly_statistics_available

            // AnimatedContent handles the transition between the empty state and the populated list.
            AnimatedContent(
                targetState = currentExpenses.isEmpty(), // The key state that triggers the animation.
                transitionSpec = {
                    // Defines the animation for content entering and exiting.
                    // Content slides in/out vertically and fades in/out.
                    (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300))
                        )
                        .using(SizeTransform(clip = false)) // Allows the content to transform its size naturally.
                }, label = "Statistics_Content_Transition"
            ) { isEmpty ->
                if (isEmpty) {
                    // Content displayed when there are no statistics available for the selected period.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Information icon.
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null, // Decorative icon.
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        // Text informing the user about the lack of statistics.
                        Text(
                            text = stringResource(emptyMessage),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Button to navigate to the product list, encouraging shopping to generate data.
                        Button(
                            onClick = onNavigateToProductList, // Callback to navigate to ProductListScreen.
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(stringResource(R.string.start_shopping_to_see_stats), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    // Display a list of expense cards when statistics are available.
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp), // Padding at the bottom of the list.
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between each expense card.
                    ) {
                        // Sort expenses by period in descending order (most recent first).
                        items(currentExpenses.entries.toList().sortedByDescending { it.key }) { (period, total) ->
                            ExpenseCard(period = period, total = total, isMonthly = showMonthly)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays an expense entry card showing the formatted period and total spending.
 * This card provides a clear, concise summary of spending for a given period.
 *
 * @param period The raw period string (e.g., "YYYY-MM" for month, "YYYY-W" for week).
 * @param total The total spending amount for the period, formatted to two decimal places.
 * @param isMonthly A boolean indicating whether the period represents a month (true) or a week (false).
 */
@SuppressLint("DefaultLocale")
@Composable
fun ExpenseCard(period: String, total: Double, isMonthly: Boolean) {
    // Format the period string for display based on whether it's monthly or weekly.
    val formattedPeriod = if (isMonthly) {
        val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        // Format to full month name and year (e.g., "January 2023").
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        try {
            parser.parse(period)?.let { formatter.format(it) } ?: period // Parse and format, or use raw period on error.
        } catch (e: Exception) {
            Log.e("StatisticsScreen", "Error parsing monthly period: $period", e)
            period // Fallback to raw period if parsing fails.
        }
    } else {
        val parts = period.split("-")
        if (parts.size == 2) {
            // Format for weekly periods (e.g., "Week 1 of 2023").
            stringResource(R.string.week_of_year_format, parts[1].toInt(), parts[0])
        } else {
            period // Fallback to raw period if format is unexpected.
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Card height adapts to its content.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Card background.
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Card shadow.
        shape = MaterialTheme.shapes.medium // Medium rounded corners.
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp), // Padding inside the card.
            horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly horizontally.
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Display the formatted period, capitalizing the first letter.
                Text(
                    text = formattedPeriod.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Display text indicating whether it's total monthly or weekly expenses.
                Text(
                    text = if (isMonthly)
                        stringResource(R.string.total_monthly_expenses)
                    else
                        stringResource(R.string.total_weekly_expenses),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary // Primary color for emphasis.
                )
            }
            // Display the total spending amount, formatted to two decimal places with a Euro symbol.
            Text(
                text = "${String.format("%.2f", total)}€",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary, // Primary color for prominence.
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
