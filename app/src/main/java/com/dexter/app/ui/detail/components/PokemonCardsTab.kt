package com.dexter.app.ui.detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dexter.app.ui.detail.TcgCardsUiState

@Composable
fun PokemonCardsTab(
    uiState: TcgCardsUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    TcgCardsSection(
        uiState = uiState,
        onRetry = onRetry,
        modifier = modifier
    )
}
