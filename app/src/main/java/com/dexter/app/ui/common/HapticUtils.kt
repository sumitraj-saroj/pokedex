package com.dexter.app.ui.common

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

class HapticUtils(
    private val view: View,
    private val hapticFeedback: HapticFeedback
) {
    /**
     * Subtle tap for simple toggles/selections.
     */
    fun lightTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * For positive outcomes (correct quiz answer, achievement unlocked).
     */
    fun successPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * For negative outcomes (wrong quiz answer).
     */
    fun errorPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberHapticUtils(): HapticUtils {
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(view, hapticFeedback) {
        HapticUtils(view, hapticFeedback)
    }
}
