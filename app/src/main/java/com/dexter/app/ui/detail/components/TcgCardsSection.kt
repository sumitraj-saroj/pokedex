package com.dexter.app.ui.detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.TcgCard
import com.dexter.app.ui.common.shimmerLoadingAnimation
import com.dexter.app.ui.detail.TcgCardsUiState
import com.dexter.app.ui.theme.Dimens

@Composable
fun TcgCardsSection(
    uiState: TcgCardsUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCardForPreview by remember { mutableStateOf<TcgCard?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Default),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Default)
                .animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = "TCG Cards",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.Default)
                )

                Spacer(modifier = Modifier.width(Dimens.Tight))

                val headerText = when (uiState) {
                    is TcgCardsUiState.Success -> {
                        val artworkCount = uiState.cards.count { it.hasImage }
                        "TCG Cards ($artworkCount Scanned / ${uiState.cards.size} Total)"
                    }
                    else -> "TCG Cards"
                }

                Text(
                    text = headerText.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Compact))

            when (uiState) {
                is TcgCardsUiState.Loading -> {
                    TcgCardsLoadingSkeleton()
                }
                is TcgCardsUiState.Empty -> {
                    TcgCardsEmptyState()
                }
                is TcgCardsUiState.Error -> {
                    TcgCardsErrorState(
                        message = uiState.message,
                        onRetry = onRetry
                    )
                }
                is TcgCardsUiState.Success -> {
                    TcgCardsGridContent(
                        allCards = uiState.cards,
                        onCardClick = { card -> selectedCardForPreview = card }
                    )
                }
            }
        }
    }

    selectedCardForPreview?.let { card ->
        TcgCardDetailDialog(
            card = card,
            onDismissRequest = { selectedCardForPreview = null }
        )
    }
}

@Composable
private fun TcgCardsGridContent(
    allCards: List<TcgCard>,
    onCardClick: (TcgCard) -> Unit
) {
    var showOnlyArtwork by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val filteredCards = remember(allCards, showOnlyArtwork) {
        if (showOnlyArtwork) {
            allCards.filter { it.hasImage }
        } else {
            allCards
        }
    }

    val initialLimit = 8
    val displayCards = remember(filteredCards, expanded) {
        if (expanded || filteredCards.size <= initialLimit) filteredCards else filteredCards.take(initialLimit)
    }
    val rows = remember(displayCards) { displayCards.chunked(2) }

    val artworkCount = remember(allCards) { allCards.count { it.hasImage } }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
    ) {
        // Filter Chips Row
        if (artworkCount < allCards.size) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.Micro),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showOnlyArtwork,
                    onClick = {
                        showOnlyArtwork = true
                        expanded = false
                    },
                    label = {
                        Text(
                            text = "Artwork Scans ($artworkCount)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                FilterChip(
                    selected = !showOnlyArtwork,
                    onClick = {
                        showOnlyArtwork = false
                        expanded = false
                    },
                    label = {
                        Text(
                            text = "All Releases (${allCards.size})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }

        if (displayCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.Default),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cards match the selected filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            rows.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
                ) {
                    TcgCardItem(
                        card = pair[0],
                        onClick = { onCardClick(pair[0]) },
                        modifier = Modifier.weight(1f)
                    )
                    if (pair.size > 1) {
                        TcgCardItem(
                            card = pair[1],
                            onClick = { onCardClick(pair[1]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (filteredCards.size > initialLimit) {
            Spacer(modifier = Modifier.height(Dimens.Tight))
            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (expanded) "Show Fewer Cards" else "View All (${filteredCards.size} Cards)",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(Dimens.Micro))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand TCG Cards"
                    )
                }
            }
        }
    }
}

@Composable
private fun TcgCardsLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Tight)) {
        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                repeat(2) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.Default),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.Tight),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f)
                                    .clip(RoundedCornerShape(Dimens.Compact))
                                    .shimmerLoadingAnimation()
                            )
                            Spacer(modifier = Modifier.height(Dimens.Tight))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(Dimens.Micro))
                                    .shimmerLoadingAnimation()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TcgCardsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.Default),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CatchingPokemon,
                contentDescription = "No TCG Cards",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Tight))
        Text(
            text = "No TCG cards found for this Pokémon",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TcgCardsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Compact),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Default),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(Dimens.Tight))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(Dimens.Tight))
            OutlinedButton(
                onClick = onRetry
            ) {
                Text(text = "Retry")
            }
        }
    }
}
