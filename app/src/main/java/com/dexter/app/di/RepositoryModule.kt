package com.dexter.app.di

import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.PokemonRepositoryImpl
import com.dexter.app.data.repository.TcgCardRepository
import com.dexter.app.data.repository.TcgCardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        impl: PokemonRepositoryImpl
    ): PokemonRepository

    @Binds
    @Singleton
    abstract fun bindTcgCardRepository(
        impl: TcgCardRepositoryImpl
    ): TcgCardRepository
}
