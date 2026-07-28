package com.dexter.app.domain.engine

import com.dexter.app.data.local.AchievementDao
import com.dexter.app.data.local.AchievementEntity
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.repository.TrainerPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

@Singleton
class AchievementEngine @Inject constructor(
    private val achievementDao: AchievementDao,
    private val quizScoreDao: QuizScoreDao,
    private val userCollectionDao: UserCollectionDao,
    private val trainerPreferencesRepository: TrainerPreferencesRepository
) {
    companion object {
        val PREDEFINED_ACHIEVEMENTS = listOf(
            // Catching
            AchievementEntity("catch_1", "First Catch", "Catch your first Pokémon", "Catching", 0, 1),
            AchievementEntity("catch_10", "Novice Collector", "Catch 10 Pokémon", "Catching", 0, 10),
            AchievementEntity("catch_50", "Avid Collector", "Catch 50 Pokémon", "Catching", 0, 50),
            AchievementEntity("catch_150", "Kanto Master", "Catch 150 Pokémon", "Catching", 0, 150),

            // Quiz
            AchievementEntity("quiz_1", "First Steps", "Answer 1 quiz question correctly", "Quiz", 0, 1),
            AchievementEntity("quiz_25", "Trivia Scholar", "Answer 25 quiz questions correctly", "Quiz", 0, 25),
            AchievementEntity("quiz_100", "Poké Genius", "Answer 100 quiz questions correctly", "Quiz", 0, 100),
            AchievementEntity("quiz_streak_5", "On a Roll", "Achieve a quiz streak of 5", "Quiz", 0, 5),
            AchievementEntity("quiz_streak_10", "Unstoppable", "Achieve a quiz streak of 10", "Quiz", 0, 10),

            // Engagement
            AchievementEntity("streak_1", "Welcome Trainer", "Log in for 1 day", "Engagement", 0, 1),
            AchievementEntity("streak_3", "Consistent Explorer", "Maintain a 3-day login streak", "Engagement", 0, 3),
            AchievementEntity("streak_7", "Dedicated Master", "Maintain a 7-day login streak", "Engagement", 0, 7),
            AchievementEntity("level_5", "Rising Star", "Reach Trainer Level 5", "Engagement", 0, 5),
            AchievementEntity("level_10", "Elite Champion", "Reach Trainer Level 10", "Engagement", 0, 10)
        )
    }

    suspend fun initializeAchievements() {
        val existing = achievementDao.observeAllAchievements()
        // Ensure defaults exist
        PREDEFINED_ACHIEVEMENTS.forEach { defaultAch ->
            val inDb = achievementDao.getAchievementById(defaultAch.id)
            if (inDb == null) {
                achievementDao.upsertAchievement(defaultAch)
            }
        }
    }

    suspend fun auditAchievements(
        caughtCount: Int,
        totalCorrectQuiz: Int,
        bestQuizStreak: Int,
        loginStreak: Int,
        trainerLevel: Int
    ) {
        initializeAchievements()

        fun update(id: String, progress: Int) {
            val defaultAch = PREDEFINED_ACHIEVEMENTS.firstOrNull { it.id == id } ?: return
            // We use coroutine or direct DB lookup
        }

        val updates = mutableListOf<AchievementEntity>()

        for (def in PREDEFINED_ACHIEVEMENTS) {
            val current = achievementDao.getAchievementById(def.id) ?: def
            val newProgress = when (def.id) {
                "catch_1", "catch_10", "catch_50", "catch_150" -> caughtCount
                "quiz_1", "quiz_25", "quiz_100" -> totalCorrectQuiz
                "quiz_streak_5", "quiz_streak_10" -> bestQuizStreak
                "streak_1", "streak_3", "streak_7" -> loginStreak
                "level_5", "level_10" -> trainerLevel
                else -> 0
            }.coerceAtMost(def.maxProgress)

            val isNowUnlocked = newProgress >= def.maxProgress
            val unlockTimestamp = if (isNowUnlocked && !current.isUnlocked) {
                System.currentTimeMillis()
            } else current.unlockedAt

            if (newProgress != current.currentProgress || isNowUnlocked != current.isUnlocked) {
                updates.add(
                    current.copy(
                        currentProgress = newProgress,
                        isUnlocked = isNowUnlocked || current.isUnlocked,
                        unlockedAt = unlockTimestamp
                    )
                )
            }
        }

        if (updates.isNotEmpty()) {
            achievementDao.upsertAchievements(updates)
        }
    }

    fun observeAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.observeAllAchievements()
    }
}
