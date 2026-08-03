package com.dexter.app.data.repository

import com.dexter.app.domain.model.TcgCard
import com.dexter.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface TcgCardRepository {
    fun getCardsForPokemon(pokemonName: String): Flow<Resource<List<TcgCard>>>
}
