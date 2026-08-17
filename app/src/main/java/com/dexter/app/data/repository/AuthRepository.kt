package com.dexter.app.data.repository

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val id: String,
    val email: String,
    val trainerName: String,
    val avatarPokemonId: Int = 25,
    val token: String
)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val user: AuthUser? = null
)

interface AuthRepository {
    val authStateFlow: Flow<AuthState>
    
    suspend fun login(email: String, password: String): Result<AuthUser>
    
    suspend fun register(trainerName: String, email: String, password: String, avatarPokemonId: Int = 25): Result<AuthUser>
    
    suspend fun logout()
}
