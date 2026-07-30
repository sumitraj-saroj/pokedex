package com.dexter.app.ui.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.QuizScoreEntity
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.TrainerPreferencesRepository
import com.dexter.app.domain.engine.AchievementEngine
import com.dexter.app.domain.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import java.util.LinkedList

@HiltViewModel
class QuizViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pokemonRepository: PokemonRepository,
    private val quizScoreDao: QuizScoreDao,
    private val userCollectionDao: UserCollectionDao,
    private val trainerPreferencesRepository: TrainerPreferencesRepository,
    private val achievementEngine: AchievementEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var allPokemonPool: List<Pokemon> = emptyList()
    private val targetQueue: LinkedList<Pokemon> = LinkedList()
    private var autoAdvanceJob: Job? = null

    init {
        viewModelScope.launch {
            pokemonRepository.observeAllPokemon().collect { list ->
                if (list.isNotEmpty()) {
                    allPokemonPool = list
                    if (_uiState.value.targetPokemon == null) {
                        refillQueueAndPrecache()
                        _uiState.update { it.copy(isLoading = false) }
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    private fun refillQueueAndPrecache() {
        if (allPokemonPool.isEmpty()) return
        while (targetQueue.size < 5) {
            val candidate = allPokemonPool.random()
            targetQueue.add(candidate)
            precacheOfficialArtwork(candidate)
        }
    }

    private fun precacheOfficialArtwork(pokemon: Pokemon) {
        val artworkUrl = pokemon.officialArtworkUrl ?: pokemon.spriteUrl
        if (artworkUrl.isNotBlank()) {
            val request = ImageRequest.Builder(context)
                .data(artworkUrl)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    fun loadNextQuestion() {
        autoAdvanceJob?.cancel()
        if (allPokemonPool.size < 4) return

        refillQueueAndPrecache()

        val target = if (targetQueue.isNotEmpty()) targetQueue.removeFirst() else allPokemonPool.random()
        refillQueueAndPrecache()

        val distractors = allPokemonPool.filter { it.id != target.id }.shuffled().take(3)
        val options = (distractors + target).shuffled()

        _uiState.update { current ->
            current.copy(
                targetPokemon = target,
                options = options,
                isAnswered = false,
                selectedOptionId = null
            )
        }

        precacheOfficialArtwork(target)
        targetQueue.forEach { precacheOfficialArtwork(it) }
    }

    fun selectOption(pokemonId: Int, hapticUtils: com.dexter.app.ui.common.HapticUtils) {
        val state = _uiState.value
        if (state.isAnswered || state.isGameOver || state.targetPokemon == null) return

        autoAdvanceJob?.cancel()
        val isCorrect = pokemonId == state.targetPokemon.id

        if (isCorrect) {
            hapticUtils.successPulse()
            val earnedXp = 10
            val newStreak = state.currentStreak + 1
            val newBest = maxOf(state.bestStreak, newStreak)
            val newScore = state.sessionScore + (100 * newStreak)
            val newXp = state.totalXpEarned + earnedXp
            val newCorrect = state.correctCount + 1

            _uiState.update { current ->
                current.copy(
                    isAnswered = true,
                    selectedOptionId = pokemonId,
                    currentStreak = newStreak,
                    bestStreak = newBest,
                    sessionScore = newScore,
                    totalXpEarned = newXp,
                    correctCount = newCorrect
                )
            }

            // Auto advance after 1.2 seconds
            autoAdvanceJob = viewModelScope.launch {
                delay(1200)
                if (!_uiState.value.isGameOver) {
                    loadNextQuestion()
                }
            }
        } else {
            hapticUtils.errorPulse()
            val newLives = state.lives - 1
            val isGameOver = newLives <= 0

            _uiState.update { current ->
                current.copy(
                    isAnswered = true,
                    selectedOptionId = pokemonId,
                    lives = newLives,
                    currentStreak = 0,
                    isGameOver = isGameOver
                )
            }

            if (isGameOver) {
                saveGameSession()
            } else {
                // Auto advance after 1.5 seconds on incorrect answer
                autoAdvanceJob = viewModelScope.launch {
                    delay(1500)
                    if (!_uiState.value.isGameOver) {
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    fun restartGame() {
        autoAdvanceJob?.cancel()
        _uiState.update {
            QuizUiState(
                isLoading = false,
                lives = 3,
                currentStreak = 0,
                sessionScore = 0,
                totalXpEarned = 0,
                correctCount = 0,
                bestStreak = 0,
                isGameOver = false
            )
        }
        loadNextQuestion()
    }

    private fun saveGameSession() {
        val state = _uiState.value
        viewModelScope.launch {
            quizScoreDao.insertQuizScore(
                QuizScoreEntity(
                    score = state.sessionScore,
                    bestStreak = state.bestStreak,
                    correctCount = state.correctCount
                )
            )

            if (state.totalXpEarned > 0) {
                trainerPreferencesRepository.addXp(state.totalXpEarned)
            }

            val caughtCount = userCollectionDao.observeCaughtCount().firstOrNull() ?: 0
            val allScores = quizScoreDao.observeAllQuizScores().firstOrNull() ?: emptyList()
            val totalCorrect = allScores.sumOf { it.correctCount }
            val overallBestStreak = (allScores.map { it.bestStreak } + state.bestStreak).maxOrNull() ?: 0
            val trainer = trainerPreferencesRepository.trainerDataFlow.firstOrNull()

            achievementEngine.auditAchievements(
                caughtCount = caughtCount,
                totalCorrectQuiz = totalCorrect,
                bestQuizStreak = overallBestStreak,
                loginStreak = trainer?.loginStreak ?: 1,
                trainerLevel = trainer?.level ?: 1
            )
        }
    }
}
