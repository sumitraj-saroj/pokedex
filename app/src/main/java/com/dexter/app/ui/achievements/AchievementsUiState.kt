package com.dexter.app.ui.achievements

import com.dexter.app.data.local.AchievementEntity

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val selectedCategory: String = "All", // "All", "Catching", "Quiz", "Engagement"
    val achievements: List<AchievementEntity> = emptyList()
)
