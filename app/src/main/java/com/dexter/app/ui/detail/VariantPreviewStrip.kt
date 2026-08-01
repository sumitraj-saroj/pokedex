package com.dexter.app.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonVariant
import com.dexter.app.ui.theme.Dimens

@Composable
fun VariantPreviewStrip(
    pokemon: Pokemon,
    variants: List<PokemonVariant>,
    selectedVariant: PokemonVariant,
    onVariantSelected: (PokemonVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    if (variants.isEmpty()) return
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.ScreenEdgePadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
    ) {
        items(
            items = variants,
            key = { it.id }
        ) { variant ->
            val isSelected = variant.id == selectedVariant.id

            VariantPreviewCard(
                pokemon = pokemon,
                variant = variant,
                isSelected = isSelected,
                onClick = {
                    haptics.lightTick()
                    onVariantSelected(variant)
                }
            )
        }
    }
}

@Composable
private fun VariantPreviewCard(
    pokemon: Pokemon,
    variant: PokemonVariant,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val elevation = if (isSelected) {
        Dimens.ElevationLevel3
    } else {
        Dimens.ElevationLevel1
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .width(76.dp)
            .clickable(onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Tight, horizontal = Dimens.Micro),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(variant.getThumbnailUrl(pokemon))
                        .crossfade(true)
                        .build(),
                    contentDescription = variant.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Micro))

            Text(
                text = variant.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
