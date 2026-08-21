package com.dexter.app.widget

import com.dexter.app.data.local.PokemonDao
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.TeamMemberDao
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.TrainerPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun pokemonRepository(): PokemonRepository
    fun pokemonDao(): PokemonDao
    fun teamMemberDao(): TeamMemberDao
    fun quizScoreDao(): QuizScoreDao
    fun trainerPreferencesRepository(): TrainerPreferencesRepository
}
