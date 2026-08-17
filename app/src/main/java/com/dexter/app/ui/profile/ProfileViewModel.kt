package com.dexter.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.local.AchievementDao
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.data.repository.AuthRepository
import com.dexter.app.data.repository.ThemePreferencesRepository
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
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val authRepository: AuthRepository,
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
                authRepository.authStateFlow,
                trainerPreferencesRepository.trainerDataFlow,
                themePreferencesRepository.themeModeFlow,
                userCollectionDao.observeCaughtCount(),
                combine(
                    quizScoreDao.observeAllQuizScores(),
                    achievementDao.observeUnlockedCount()
                ) { scores, unlocked -> scores to unlocked }
            ) { authState, trainer, themeMode, caught, (scores, unlockedCount) ->
                val totalCorrect = scores.sumOf { it.correctCount }
                val bestStreak = scores.map { it.bestStreak }.maxOrNull() ?: 0
                val gamesPlayed = scores.size

                val currentLvl = trainer.level
                val currentBaseXp = TrainerData.xpForLevel(currentLvl)
                val nextLvlXp = TrainerData.xpForLevel(currentLvl + 1)

                ProfileUiState(
                    isLoading = false,
                    authState = authState,
                    trainerData = trainer,
                    themeMode = themeMode,
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

                val trainer = newState.trainerData
                achievementEngine.auditAchievements(
                    caughtCount = newState.caughtCount,
                    totalCorrectQuiz = newState.totalQuizCorrect,
                    bestQuizStreak = newState.bestQuizStreak,
                    loginStreak = trainer.loginStreak,
                    trainerLevel = trainer.level
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun selectAvatar(avatarId: Int) {
        viewModelScope.launch {
            trainerPreferencesRepository.setAvatarPokemonId(avatarId)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            trainerPreferencesRepository.setHapticEnabled(enabled)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            themePreferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleThemeMode() {
        viewModelScope.launch {
            val nextMode = when (_uiState.value.themeMode) {
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.LIGHT
                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            }
            themePreferencesRepository.setThemeMode(nextMode)
        }
    }
}
