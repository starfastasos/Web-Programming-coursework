@file:Suppress("DEPRECATION")

package com.example.mysupermarketapplication.ui.screens.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mysupermarketapplication.R

/**
 * Composable function for displaying the user's Profile Screen.
 * This screen shows user information and provides options to navigate to statistics
 * or to exit the application.
 *
 * @param onBackClick A lambda function invoked when the back arrow in the top app bar is clicked.
 * @param onStatisticsClick A lambda function invoked when the "Statistics" button is clicked,
 * typically navigating to a screen displaying user statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    val context = LocalContext.current // Get the current context for Toast messages and logging.

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    // Back button in the top app bar.
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Top app bar background color.
                    titleContentColor = MaterialTheme.colorScheme.primary // Title text color.
                ),
                modifier = Modifier.shadow(6.dp) // Add a subtle shadow to the top app bar.
            )
        }
    ) { paddingValues ->

        // AnimatedVisibility for screen entry/exit animations.
        AnimatedVisibility(
            visible = true, // The screen content is always visible once composed.
            enter = fadeIn(), // Fades in when entering.
            exit = fadeOut(), // Fades out when exiting.
            modifier = Modifier.padding(paddingValues) // Apply padding from the Scaffold.
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp), // Overall padding for the column.
                horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally.
                verticalArrangement = Arrangement.Top // Align content to the top.
            ) {
                Spacer(modifier = Modifier.height(32.dp)) // Space from the top.

                // Card displaying user profile information.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge, // Large rounded corners for the card.
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Dynamic surface color with elevation.
                    ),
                    elevation = CardDefaults.cardElevation(8.dp) // Card shadow.
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp), // Padding inside the card.
                        horizontalAlignment = Alignment.CenterHorizontally // Center content within the card.
                    ) {
                        // Circular surface for the profile icon.
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape), // Clip to a circular shape.
                            color = MaterialTheme.colorScheme.tertiaryContainer, // Background color for the icon.
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer // Icon color.
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person, // Default person icon.
                                contentDescription = stringResource(R.string.profile_icon_description), // Content description for accessibility.
                                modifier = Modifier.size(72.dp), // Size of the icon.
                                tint = MaterialTheme.colorScheme.onTertiaryContainer // Tint color for the icon.
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp)) // Space below the icon.

                        // Displays the user's name. In a real application, this data would typically come from a user session or database.
                        Text(
                            text = stringResource(R.string.username_label, "Alex Smith"), // Hardcoded for demo, replace with dynamic data.
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp)) // Space between name and email.

                        // Displays the user's email. Similar to the name, this would be dynamic in a real application.
                        Text(
                            text = stringResource(R.string.email_label, "asmith@example.com"), // Hardcoded for demo, replace with dynamic data.
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp)) // Space below the profile info card.

                // Column for action buttons (Statistics, Exit).
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp) // Spacing between buttons.
                ) {
                    // Button to navigate to the statistics screen.
                    Button(
                        onClick = {
                            try {
                                onStatisticsClick() // Invoke the provided callback.
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error navigating to statistics: ${e.message}", e) // Log any errors.
                                Toast.makeText(context, "Error opening statistics.", Toast.LENGTH_SHORT).show() // Show user feedback.
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 18.dp), // Vertical padding for the button content.
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Primary color for the button.
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart, // Statistics icon.
                            contentDescription = null, // Icon is decorative, text provides context.
                            modifier = Modifier.padding(end = 12.dp) // Padding to the right of the icon.
                        )
                        Text(
                            stringResource(R.string.statistics_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Outlined button to exit the application.
                    OutlinedButton(
                        onClick = {
                            try {
                                // This action attempts to exit the application process.
                                android.os.Process.killProcess(android.os.Process.myPid())
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error trying to exit application: ${e.message}", e) // Log any errors.
                                Toast.makeText(context, "Failed to exit application.", Toast.LENGTH_SHORT).show() // Show user feedback.
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 18.dp), // Vertical padding for the button content.
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), // Error color for the text and icon.
                        border = ButtonDefaults.outlinedButtonBorder.copy( // Custom border for the outlined button.
                            width = 2.dp,
                            brush = SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) // Semi-transparent error color for the border.
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout, // Logout icon.
                            contentDescription = null, // Icon is decorative, text provides context.
                            modifier = Modifier.padding(end = 12.dp) // Padding to the right of the icon.
                        )
                        Text(
                            stringResource(R.string.exit_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
