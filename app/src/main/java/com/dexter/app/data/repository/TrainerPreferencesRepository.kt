package com.dexter.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.trainerDataStore by preferencesDataStore(name = "trainer_preferences")

data class TrainerData(
    val totalXp: Int,
    val level: Int,
    val loginStreak: Int,
    val lastLoginDate: String,
    val avatarPokemonId: Int = 25
) {
    companion object {
        fun calculateLevel(xp: Int): Int {
            if (xp <= 0) return 1
            return kotlin.math.floor(kotlin.math.sqrt(xp.toDouble() / 50.0)).toInt().coerceAtLeast(1)
        }

        fun xpForLevel(level: Int): Int {
            return level * level * 50
        }

        val AVATAR_OPTIONS = listOf(
            1,   // Bulbasaur
            4,   // Charmander
            7,   // Squirtle
            25,  // Pikachu
            144, // Articuno
            145, // Zapdos
            146, // Moltres
            150, // Mewtwo
            151, // Mew
            243, // Raikou
            244, // Entei
            245, // Suicune
            249, // Lugia
            250  // Ho-Oh
        )
    }
}

@Singleton
class TrainerPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val totalXpKey = intPreferencesKey("total_xp")
    private val loginStreakKey = intPreferencesKey("login_streak")
    private val lastLoginDateKey = stringPreferencesKey("last_login_date")
    private val avatarPokemonIdKey = intPreferencesKey("avatar_pokemon_id")

    val trainerDataFlow: Flow<TrainerData> = context.trainerDataStore.data.map { prefs ->
        val xp = prefs[totalXpKey] ?: 0
        val streak = prefs[loginStreakKey] ?: 1
        val lastDate = prefs[lastLoginDateKey] ?: ""
        val avatarId = prefs[avatarPokemonIdKey] ?: 25
        TrainerData(
            totalXp = xp,
            level = TrainerData.calculateLevel(xp),
            loginStreak = streak,
            lastLoginDate = lastDate,
            avatarPokemonId = avatarId
        )
    }

    suspend fun addXp(amount: Int) {
        if (amount <= 0) return
        context.trainerDataStore.edit { prefs ->
            val current = prefs[totalXpKey] ?: 0
            prefs[totalXpKey] = current + amount
        }
    }

    suspend fun setAvatarPokemonId(avatarId: Int) {
        context.trainerDataStore.edit { prefs ->
            prefs[avatarPokemonIdKey] = avatarId
        }
    }

    suspend fun updateDailyStreak() {
        val today = LocalDate.now()
        val todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        context.trainerDataStore.edit { prefs ->
            val lastDateString = prefs[lastLoginDateKey] ?: ""
            val currentStreak = prefs[loginStreakKey] ?: 0

            if (lastDateString.isEmpty()) {
                prefs[loginStreakKey] = 1
                prefs[lastLoginDateKey] = todayString
            } else {
                try {
                    val lastDate = LocalDate.parse(lastDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                    val daysBetween = ChronoUnit.DAYS.between(lastDate, today)

                    when {
                        daysBetween == 1L -> {
                            prefs[loginStreakKey] = currentStreak + 1
                            prefs[lastLoginDateKey] = todayString
                        }
                        daysBetween > 1L -> {
                            prefs[loginStreakKey] = 1
                            prefs[lastLoginDateKey] = todayString
                        }
                        daysBetween == 0L -> {
                            if (currentStreak == 0) {
                                prefs[loginStreakKey] = 1
                            }
                        }
                    }
                } catch (e: Exception) {
                    prefs[loginStreakKey] = 1
                    prefs[lastLoginDateKey] = todayString
                }
            }
        }
    }
}
