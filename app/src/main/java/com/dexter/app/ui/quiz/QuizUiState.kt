package com.dexter.app.ui.quiz

import com.dexter.app.domain.model.Pokemon

data class QuizUiState(
    val isLoading: Boolean = true,
    val targetPokemon: Pokemon? = null,
    val options: List<Pokemon> = emptyList(),
    val lives: Int = 3,
    val currentStreak: Int = 0,
    val sessionScore: Int = 0,
    val totalXpEarned: Int = 0,
    val correctCount: Int = 0,
    val bestStreak: Int = 0,
    val isAnswered: Boolean = false,
    val selectedOptionId: Int? = null,
    val isGameOver: Boolean = false,
    val isPlayingAudio: Boolean = false
)
