package com.example.mysupermarketapplication.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects a [Flow] as a [State] in a lifecycle-aware manner.
 *
 * This function automatically starts and stops collecting the flow based on the lifecycle
 * of the composable that calls it. This helps prevent memory leaks and unnecessary UI updates.
 *
 * @param initialValue The value used by the [State] before the [Flow] emits its first item.
 * @param context The [CoroutineContext] to collect the [Flow] in. Defaults to [EmptyCoroutineContext].
 * @return A [State] that holds the latest value emitted by the [Flow], triggering recomposition on updates.
 */
@Composable
fun <T> Flow<T>.collectAsStateLifecycleAware(
    initialValue: T,
    context: CoroutineContext = EmptyCoroutineContext
): State<T> = collectAsState(initialValue, context)

/**
 * Collects a [StateFlow] as a [State] in a lifecycle-aware manner.
 *
 * This function automatically manages the collection of the [StateFlow] based on the composable's
 * lifecycle. Since [StateFlow] always has a current value, no initial value is needed.
 *
 * @param context The [CoroutineContext] to collect the [StateFlow] in. Defaults to [EmptyCoroutineContext].
 * @return A [State] that holds the latest value from the [StateFlow], triggering recomposition on updates.
 */
@Composable
fun <T> StateFlow<T>.collectAsStateLifecycleAware(
    context: CoroutineContext = EmptyCoroutineContext
): State<T> = collectAsState(context)
