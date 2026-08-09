package com.dexter.app.ui.profile

import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.data.repository.TrainerData

data class ProfileUiState(
    val isLoading: Boolean = true,
    val trainerData: TrainerData = TrainerData(0, 1, 1, ""),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val nextLevelXp: Int = 200,
    val currentLevelBaseXp: Int = 0,
    val caughtCount: Int = 0,
    val totalQuizCorrect: Int = 0,
    val bestQuizStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val unlockedAchievementsCount: Int = 0
)
