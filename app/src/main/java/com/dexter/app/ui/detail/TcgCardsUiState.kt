package com.dexter.app.ui.detail

import com.dexter.app.domain.model.TcgCard

sealed interface TcgCardsUiState {
    object Loading : TcgCardsUiState
    data class Success(val cards: List<TcgCard>) : TcgCardsUiState
    object Empty : TcgCardsUiState
    data class Error(val message: String) : TcgCardsUiState
}
