package com.dexter.app.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.local.AchievementDao
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.repository.TrainerPreferencesRepository
import com.dexter.app.domain.engine.AchievementEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementDao: AchievementDao,
    private val achievementEngine: AchievementEngine,
    private val userCollectionDao: UserCollectionDao,
    private val quizScoreDao: QuizScoreDao,
    private val trainerPreferencesRepository: TrainerPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Trigger audit
            val caught = userCollectionDao.observeCaughtCount()
            val scores = quizScoreDao.observeAllQuizScores()
            val trainer = trainerPreferencesRepository.trainerDataFlow

            combine(
                achievementEngine.observeAchievements(),
                _uiState
            ) { list, currentState ->
                val filtered = if (currentState.selectedCategory == "All") {
                    list
                } else {
                    list.filter { it.category == currentState.selectedCategory }
                }

                currentState.copy(
                    isLoading = false,
                    achievements = filtered
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
