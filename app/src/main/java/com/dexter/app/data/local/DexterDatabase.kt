package com.dexter.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PokemonEntity::class,
        PokemonStatsEntity::class,
        UserCollectionEntity::class,
        EvolutionEntity::class,
        PokemonMoveEntity::class,
        MoveDetailEntity::class,
        PokemonAbilityEntity::class,
        PokemonFormEntity::class,
        TeamMemberEntity::class,
        QuizScoreEntity::class,
        AchievementEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class DexterDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun userCollectionDao(): UserCollectionDao
    abstract fun evolutionDao(): EvolutionDao
    abstract fun pokemonMoveDao(): PokemonMoveDao
    abstract fun pokemonAbilityDao(): PokemonAbilityDao
    abstract fun pokemonFormDao(): PokemonFormDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun quizScoreDao(): QuizScoreDao
    abstract fun achievementDao(): AchievementDao
}
