package com.dexter.app.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine as kotlinCombine

/**
 * Combines 5 flows using the standard combine operator.
 */
@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    transform: suspend (T1, T2, T3, T4, T5) -> R
): Flow<R> = kotlinCombine(
    flow1, flow2, flow3, flow4, flow5
) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5
    )
}
