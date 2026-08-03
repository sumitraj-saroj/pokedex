package com.dexter.app.ui.detail

import androidx.compose.runtime.Immutable
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.domain.model.EvolutionNode
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonAbility
import com.dexter.app.domain.model.PokemonForm
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.domain.model.PokemonVariant

@Immutable
data class DetailUiState(
    val pokemon: Pokemon? = null,
    val isLoading: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val selectedVariant: PokemonVariant = PokemonVariant.Official,
    val evolutionNodes: List<EvolutionNode> = emptyList(),
    val moves: List<PokemonMove> = emptyList(),
    val abilities: List<PokemonAbility> = emptyList(),
    val forms: List<PokemonForm> = emptyList(),
    val tcgCardsUiState: TcgCardsUiState = TcgCardsUiState.Loading
)
