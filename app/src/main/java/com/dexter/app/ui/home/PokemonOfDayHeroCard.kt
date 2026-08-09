package com.dexter.app.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.fillMaxSize
import com.dexter.app.ui.common.contentColorForSeed
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.common.bouncyClickable
import com.dexter.app.ui.common.glassmorphicContainer
import com.dexter.app.ui.common.holographicShimmer
import com.dexter.app.ui.common.rememberBreathingYOffset
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.common.spatialExpressiveSpring
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import com.dexter.app.ui.theme.blendTypeSeedColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonOfDayHeroCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val haptics = rememberHapticUtils()
    val primarySeed = pokemon.primaryType.seedColor
    val blendedSeed = blendTypeSeedColors(primarySeed, pokemon.secondaryType?.seedColor)
    val breathingY = rememberBreathingYOffset(maxOffsetDp = 6.dp, durationMillis = 2600)

    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            blendedSeed.copy(alpha = 0.35f),
            primarySeed.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
        ),
        radius = 800f
    )

    val cardModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            modifier.sharedBounds(
                rememberSharedContentState(key = "pokemon_card_${pokemon.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = spatialExpressiveSpring()
            )
        }
    } else {
        modifier
    }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .bouncyClickable(hapticUtils = haptics, onClick = onClick)
            .holographicShimmer(
                enabled = pokemon.isLegendary || pokemon.isMythical,
                shape = RoundedCornerShape(24.dp)
            )
            .glassmorphicContainer(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
                borderColor = primarySeed.copy(alpha = 0.5f),
                borderWidth = 1.5.dp,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = Dimens.ElevationLevel2
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
                .padding(Dimens.Default)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Row: Featured Gold Badge & Pokédex Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gold "Pokémon of the Day" Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Unspecified,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFB300)
                                    )
                                )
                            )
                            .padding(horizontal = Dimens.Compact, vertical = Dimens.Micro * 1.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF3E2723),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "POKÉMON OF THE DAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3E2723),
                                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                            )
                        }
                    }

                    // Number Badge
                    Text(
                        text = pokemon.formattedNumber,
                        style = StatNumberStyle.copy(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Compact))

                // Content Layout: Image + Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sprite Artwork
                    Box(
                        modifier = Modifier
                            .weight(0.42f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = "pokemon_image_${pokemon.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = spatialExpressiveSpring()
                                )
                            }
                        } else {
                            Modifier
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pokemon.officialArtworkUrl ?: pokemon.spriteUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = pokemon.capitalizedName,
                            contentScale = ContentScale.Fit,
                            modifier = imageModifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimens.Default))

                    // Text & Stats Column
                    Column(
                        modifier = Modifier.weight(0.58f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        Text(
                            text = pokemon.capitalizedName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (pokemon.category.isNotBlank()) {
                            Text(
                                text = pokemon.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Type Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Micro),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TypeChip(type = pokemon.primaryType, isCompact = true)
                            pokemon.secondaryType?.let {
                                TypeChip(type = it, isCompact = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.Micro))

                        // Stats Summary Row
                        pokemon.stats?.let { stats ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = primarySeed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "BST ${stats.total}",
                                            style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = "HP ${stats.hp}",
                                        style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Compact))

                // Bottom Action Button: Quick Catch / Explore Details
                Button(
                    onClick = {
                        haptics.mediumImpact()
                        onClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primarySeed,
                        contentColor = primarySeed.contentColorForSeed()
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CatchingPokemon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Tight / 2))
                        Text(
                            text = "Explore ${pokemon.capitalizedName}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
