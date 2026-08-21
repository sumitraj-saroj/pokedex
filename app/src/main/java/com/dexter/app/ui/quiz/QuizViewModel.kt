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
    private val achievementEngine: AchievementEngine,
    private val quizAudioPlayer: QuizAudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var allPokemonPool: List<Pokemon> = emptyList()
    private val targetQueue: LinkedList<Pokemon> = LinkedList()
    private var autoAdvanceJob: Job? = null

    init {
        viewModelScope.launch {
            quizAudioPlayer.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlayingAudio = playing) }
            }
        }

        viewModelScope.launch {
            pokemonRepository.observeAllPokemon().collect { list ->
                if (list.isNotEmpty()) {
                    allPokemonPool = list
                    val genCounts = list.groupingBy { it.effectiveGeneration }.eachCount()
                    val filteredPool = getFilteredPool()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generationCounts = genCounts,
                            availablePokemonCount = filteredPool.size
                        )
                    }
                    if (_uiState.value.targetPokemon == null) {
                        refillQueueAndPrecache()
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    private fun getFilteredPool(gens: Set<Int> = _uiState.value.selectedGenerations): List<Pokemon> {
        return if (gens.isEmpty() || gens.size == 9) {
            allPokemonPool
        } else {
            allPokemonPool.filter { gens.contains(it.effectiveGeneration) }
        }
    }

    fun toggleGeneration(generation: Int) {
        val current = _uiState.value.selectedGenerations
        val next = if (current.isEmpty() || current.size == 9) {
            // If all are selected, tapping a generation isolates to just that generation
            setOf(generation)
        } else if (current.contains(generation)) {
            val updated = current - generation
            if (updated.isEmpty()) emptySet() else updated
        } else {
            val updated = current + generation
            if (updated.size == 9) emptySet() else updated
        }
        applyGenerationSelection(next)
    }

    fun selectAllGenerations() {
        applyGenerationSelection(emptySet())
    }

    fun selectGenerationPreset(generations: Set<Int>) {
        val next = if (generations.size == 9) emptySet() else generations
        applyGenerationSelection(next)
    }

    fun clearGenerationFilter() {
        applyGenerationSelection(emptySet())
    }

    private fun applyGenerationSelection(newGenerations: Set<Int>) {
        autoAdvanceJob?.cancel()
        targetQueue.clear()
        val pool = getFilteredPool(newGenerations)

        _uiState.update { current ->
            current.copy(
                selectedGenerations = newGenerations,
                availablePokemonCount = pool.size,
                targetPokemon = null,
                options = emptyList(),
                isAnswered = false,
                selectedOptionId = null
            )
        }

        if (pool.isNotEmpty()) {
            refillQueueAndPrecache()
            loadNextQuestion()
        }
    }

    private fun refillQueueAndPrecache() {
        val pool = getFilteredPool()
        if (pool.isEmpty()) return
        while (targetQueue.size < 5) {
            val candidate = pool.random()
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
        val pool = getFilteredPool()
        if (pool.size < 4) {
            if (pool.isNotEmpty()) {
                val target = pool.random()
                _uiState.update {
                    it.copy(
                        targetPokemon = target,
                        options = pool.shuffled(),
                        isAnswered = false,
                        selectedOptionId = null
                    )
                }
                precacheOfficialArtwork(target)
            }
            return
        }

        refillQueueAndPrecache()

        val target = if (targetQueue.isNotEmpty()) targetQueue.removeFirst() else pool.random()
        refillQueueAndPrecache()

        val distractors = mutableListOf<Pokemon>()
        val poolSize = pool.size
        val usedIds = mutableSetOf(target.id)
        val random = kotlin.random.Random
        while (distractors.size < 3 && usedIds.size < poolSize) {
            val candidate = pool[random.nextInt(poolSize)]
            if (usedIds.add(candidate.id)) {
                distractors.add(candidate)
            }
        }
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
            val earnedXp = 10
            val newStreak = state.currentStreak + 1
            val newBest = maxOf(state.bestStreak, newStreak)
            val newScore = state.sessionScore + (100 * newStreak)
            val newXp = state.totalXpEarned + earnedXp
            val newCorrect = state.correctCount + 1

            try {
                if (newStreak > 0 && newStreak % 5 == 0) {
                    hapticUtils.waveformPulse()
                } else {
                    hapticUtils.successPulse()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

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
        targetQueue.clear()
        val pool = getFilteredPool()
        _uiState.update {
            it.copy(
                isLoading = false,
                lives = 3,
                currentStreak = 0,
                sessionScore = 0,
                totalXpEarned = 0,
                correctCount = 0,
                bestStreak = 0,
                isGameOver = false,
                isAnswered = false,
                selectedOptionId = null,
                availablePokemonCount = pool.size
            )
        }
        refillQueueAndPrecache()
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

    fun playCry() {
        val target = _uiState.value.targetPokemon ?: return
        quizAudioPlayer.playCry(target.cryAudioUrl)
    }

    override fun onCleared() {
        super.onCleared()
        quizAudioPlayer.release()
    }
}
