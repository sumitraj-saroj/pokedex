package com.dexter.app.data.repository

import com.dexter.app.data.remote.TcgdexApi
import com.dexter.app.domain.mapper.toDomain
import com.dexter.app.domain.model.TcgCard
import com.dexter.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TcgCardRepositoryImpl @Inject constructor(
    private val tcgdexApi: TcgdexApi
) : TcgCardRepository {

    override fun getCardsForPokemon(pokemonName: String): Flow<Resource<List<TcgCard>>> = flow {
        emit(Resource.Loading)
        try {
            // TCGdex search query syntax: "eq:Pikachu"
            val formattedName = pokemonName.trim().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            val dtos = tcgdexApi.getCardsByPokemonName("eq:$formattedName")
            val cards = dtos.toDomain()
            emit(Resource.Success(cards))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch TCG cards", e))
        }
    }
}
