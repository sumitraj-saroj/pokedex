package com.dexter.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val bestStreak: Int,
    val correctCount: Int,
    val datePlayed: Long = System.currentTimeMillis()
)

@Dao
interface QuizScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizScore(score: QuizScoreEntity)

    @Query("SELECT * FROM quiz_scores ORDER BY score DESC")
    fun observeAllQuizScores(): Flow<List<QuizScoreEntity>>

    @Query("SELECT MAX(bestStreak) FROM quiz_scores")
    fun observeBestStreak(): Flow<Int?>

    @Query("SELECT SUM(correctCount) FROM quiz_scores")
    fun observeTotalCorrectCount(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM quiz_scores")
    fun observeGamesPlayedCount(): Flow<Int?>
}
