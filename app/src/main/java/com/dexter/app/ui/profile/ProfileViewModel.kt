package com.dexter.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.local.AchievementDao
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.repository.TrainerData
import com.dexter.app.data.repository.TrainerPreferencesRepository
import com.dexter.app.domain.engine.AchievementEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val trainerPreferencesRepository: TrainerPreferencesRepository,
    private val quizScoreDao: QuizScoreDao,
    private val userCollectionDao: UserCollectionDao,
    private val achievementDao: AchievementDao,
    private val achievementEngine: AchievementEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            trainerPreferencesRepository.updateDailyStreak()

            combine(
                trainerPreferencesRepository.trainerDataFlow,
                userCollectionDao.observeCaughtCount(),
                quizScoreDao.observeAllQuizScores(),
                achievementDao.observeUnlockedCount()
            ) { trainer, caught, scores, unlockedCount ->
                val totalCorrect = scores.sumOf { it.correctCount }
                val bestStreak = scores.map { it.bestStreak }.maxOrNull() ?: 0
                val gamesPlayed = scores.size

                val currentLvl = trainer.level
                val currentBaseXp = TrainerData.xpForLevel(currentLvl)
                val nextLvlXp = TrainerData.xpForLevel(currentLvl + 1)

                achievementEngine.auditAchievements(
                    caughtCount = caught,
                    totalCorrectQuiz = totalCorrect,
                    bestQuizStreak = bestStreak,
                    loginStreak = trainer.loginStreak,
                    trainerLevel = trainer.level
                )

                ProfileUiState(
                    isLoading = false,
                    trainerData = trainer,
                    currentLevelBaseXp = currentBaseXp,
                    nextLevelXp = nextLvlXp,
                    caughtCount = caught,
                    totalQuizCorrect = totalCorrect,
                    bestQuizStreak = bestStreak,
                    gamesPlayed = gamesPlayed,
                    unlockedAchievementsCount = unlockedCount
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectAvatar(avatarId: Int) {
        viewModelScope.launch {
            trainerPreferencesRepository.setAvatarPokemonId(avatarId)
        }
    }
}
