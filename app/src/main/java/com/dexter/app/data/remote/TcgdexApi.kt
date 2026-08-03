package com.dexter.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TcgdexApi {
    @GET("cards")
    suspend fun getCardsByPokemonName(
        @Query("name") query: String
    ): List<TcgdexCardDto>
}
