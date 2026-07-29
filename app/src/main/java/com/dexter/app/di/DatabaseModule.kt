package com.dexter.app.di

import android.content.Context
import androidx.room.Room
import com.dexter.app.data.local.DexterDatabase
import com.dexter.app.data.local.EvolutionDao
import com.dexter.app.data.local.PokemonAbilityDao
import com.dexter.app.data.local.PokemonDao
import com.dexter.app.data.local.PokemonFormDao
import com.dexter.app.data.local.PokemonMoveDao
import com.dexter.app.data.local.TeamMemberDao
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.local.QuizScoreDao
import com.dexter.app.data.local.AchievementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DexterDatabase {
        return Room.databaseBuilder(
            context,
            DexterDatabase::class.java,
            "dexter_database.db"
        )
        .createFromAsset("database/dexter_database.db")
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun providePokemonDao(db: DexterDatabase): PokemonDao = db.pokemonDao()

    @Provides
    fun provideUserCollectionDao(db: DexterDatabase): UserCollectionDao = db.userCollectionDao()

    @Provides
    fun provideEvolutionDao(db: DexterDatabase): EvolutionDao = db.evolutionDao()

    @Provides
    fun providePokemonMoveDao(db: DexterDatabase): PokemonMoveDao = db.pokemonMoveDao()

    @Provides
    fun providePokemonAbilityDao(db: DexterDatabase): PokemonAbilityDao = db.pokemonAbilityDao()

    @Provides
    fun providePokemonFormDao(db: DexterDatabase): PokemonFormDao = db.pokemonFormDao()

    @Provides
    fun provideTeamMemberDao(db: DexterDatabase): TeamMemberDao = db.teamMemberDao()

    @Provides
    fun provideQuizScoreDao(db: DexterDatabase): QuizScoreDao = db.quizScoreDao()

    @Provides
    fun provideAchievementDao(db: DexterDatabase): AchievementDao = db.achievementDao()
}
