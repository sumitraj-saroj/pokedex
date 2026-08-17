package com.dexter.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trainerPreferencesRepository: TrainerPreferencesRepository
) : AuthRepository {

    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userIdKey = stringPreferencesKey("user_id")
    private val emailKey = stringPreferencesKey("user_email")
    private val trainerNameKey = stringPreferencesKey("trainer_name")
    private val avatarIdKey = intPreferencesKey("user_avatar_id")
    private val tokenKey = stringPreferencesKey("auth_token")

    // Predefined demo accounts for quick testing
    val demoAccounts = mapOf(
        "ash@kanto.com" to Triple("Ash Ketchum", "pikachu123", 25),
        "misty@cerulean.com" to Triple("Misty Waterflower", "starmie123", 121),
        "brock@pewter.com" to Triple("Brock Harrison", "onix123", 95)
    )

    override val authStateFlow: Flow<AuthState> = context.authDataStore.data.map { prefs ->
        val isLoggedIn = prefs[isLoggedInKey] ?: false
        if (!isLoggedIn) {
            AuthState(isLoggedIn = false, user = null)
        } else {
            val user = AuthUser(
                id = prefs[userIdKey] ?: "user_guest",
                email = prefs[emailKey] ?: "",
                trainerName = prefs[trainerNameKey] ?: "Pokémon Trainer",
                avatarPokemonId = prefs[avatarIdKey] ?: 25,
                token = prefs[tokenKey] ?: ""
            )
            AuthState(isLoggedIn = true, user = user)
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        // Simulate network latency for practice
        delay(1000)

        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty."))
        }

        val demo = demoAccounts[cleanEmail]
        val trainerName: String
        val avatarId: Int

        if (demo != null) {
            if (demo.second != cleanPassword) {
                return Result.failure(IllegalArgumentException("Incorrect password for $cleanEmail."))
            }
            trainerName = demo.first
            avatarId = demo.third
        } else {
            // For general mock login, extract username from email prefix if no exact match
            if (cleanPassword.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
            }
            trainerName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } + " Trainer"
            avatarId = 25
        }

        val userId = "usr_" + UUID.nameUUIDFromBytes(cleanEmail.toByteArray()).toString().take(8)
        val token = "mock_jwt_token_" + System.currentTimeMillis()

        val authUser = AuthUser(
            id = userId,
            email = cleanEmail,
            trainerName = trainerName,
            avatarPokemonId = avatarId,
            token = token
        )

        // Save session in DataStore
        context.authDataStore.edit { prefs ->
            prefs[isLoggedInKey] = true
            prefs[userIdKey] = userId
            prefs[emailKey] = cleanEmail
            prefs[trainerNameKey] = trainerName
            prefs[avatarIdKey] = avatarId
            prefs[tokenKey] = token
        }

        // Sync trainer avatar in TrainerPreferencesRepository
        trainerPreferencesRepository.setAvatarPokemonId(avatarId)

        return Result.success(authUser)
    }

    override suspend fun register(
        trainerName: String,
        email: String,
        password: String,
        avatarPokemonId: Int
    ): Result<AuthUser> {
        // Simulate network latency for practice
        delay(1000)

        val cleanName = trainerName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Trainer name must be at least 2 characters."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (cleanPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val userId = "usr_" + UUID.nameUUIDFromBytes(cleanEmail.toByteArray()).toString().take(8)
        val token = "mock_jwt_token_" + System.currentTimeMillis()

        val authUser = AuthUser(
            id = userId,
            email = cleanEmail,
            trainerName = cleanName,
            avatarPokemonId = avatarPokemonId,
            token = token
        )

        // Save session in DataStore
        context.authDataStore.edit { prefs ->
            prefs[isLoggedInKey] = true
            prefs[userIdKey] = userId
            prefs[emailKey] = cleanEmail
            prefs[trainerNameKey] = cleanName
            prefs[avatarIdKey] = avatarPokemonId
            prefs[tokenKey] = token
        }

        // Sync trainer avatar
        trainerPreferencesRepository.setAvatarPokemonId(avatarPokemonId)

        return Result.success(authUser)
    }

    override suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs[isLoggedInKey] = false
            prefs.remove(userIdKey)
            prefs.remove(emailKey)
            prefs.remove(trainerNameKey)
            prefs.remove(avatarIdKey)
            prefs.remove(tokenKey)
        }
    }
}
