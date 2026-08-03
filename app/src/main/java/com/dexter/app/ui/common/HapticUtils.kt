package com.dexter.app.ui.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
    private val hapticFeedback: HapticFeedback,
    private val isEnabled: Boolean = true
) {
    private val vibrator: Vibrator? by lazy {
        val context = view.context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Subtle tap for simple UI clicks and button taps.
     */
    fun lightClick() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            if (!performed) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun lightTick() {
        lightClick()
    }

    /**
     * Selection tick for tab switches, filter chip toggles, list snaps.
     */
    fun selectionTick() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            if (!performed) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Medium impact for adding favorites, changing variants, or toggling key settings.
     */
    fun mediumImpact() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            if (!performed) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Heavy impact for team member add/remove, legendary flip, critical actions.
     */
    fun heavyImpact() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            if (!performed) {
                vibrateOneShot(80, 255)
            }
        } else {
            vibrateOneShot(80, 255)
        }
    }

    /**
     * Positive outcome pulse (correct answer, achievement unlocked).
     */
    fun successPulse() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            if (!performed) {
                vibrateWaveform(longArrayOf(0, 40, 60, 80), intArrayOf(0, 150, 0, 255))
            }
        } else {
            vibrateWaveform(longArrayOf(0, 40, 60, 80), intArrayOf(0, 150, 0, 255))
        }
    }

    /**
     * Negative outcome pulse (wrong answer, duplicate team member warning).
     */
    fun errorPulse() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val performed = view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            if (!performed) {
                vibrateWaveform(longArrayOf(0, 60, 40, 60), intArrayOf(0, 200, 0, 200))
            }
        } else {
            vibrateWaveform(longArrayOf(0, 60, 40, 60), intArrayOf(0, 200, 0, 200))
        }
    }

    /**
     * Dynamic celebration pulse (quiz streak milestone, level up, legendary encounter).
     */
    fun waveformPulse() {
        if (!isEnabled) return
        vibrateWaveform(
            longArrayOf(0, 50, 50, 50, 50, 120),
            intArrayOf(0, 100, 0, 180, 0, 255)
        )
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(timings, -1)
                }
            }
        }
    }
}

@Composable
fun rememberHapticUtils(isEnabled: Boolean = true): HapticUtils {
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(view, hapticFeedback, isEnabled) {
        HapticUtils(view, hapticFeedback, isEnabled)
    }
}
