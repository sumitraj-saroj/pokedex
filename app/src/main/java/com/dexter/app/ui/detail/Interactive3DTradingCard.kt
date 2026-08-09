package com.dexter.app.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.common.HoloFoilStyle
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.common.drawHoloFoilOverlay
import com.dexter.app.ui.common.rememberBreathingYOffset
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.common.rememberTiltSensorState
import com.dexter.app.ui.common.spatialExpressiveSpring
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Interactive3DTradingCard(
    pokemon: Pokemon,
    activeName: String,
    activeArtworkUrl: String?,
    activePrimaryType: PokemonType,
    activeSecondaryType: PokemonType?,
    activeStats: PokemonStats?,
    activeHeightM: Double,
    activeWeightKg: Double,
    isSpecialPokemon: Boolean,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    var isFlipped by remember { mutableStateOf(false) }
    val hapticUtils = rememberHapticUtils()
    val coroutineScope = rememberCoroutineScope()

    val flipRotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "CardFlip3DRotation"
    )

    val density = LocalDensity.current.density
    val cardShape = RoundedCornerShape(Dimens.Section)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .graphicsLayer {
                this.rotationY = flipRotationY
                cameraDistance = 14f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                hapticUtils.mediumImpact()
                isFlipped = !isFlipped
            },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
    ) {
        if (flipRotationY <= 90f) {
            // FRONT SIDE: Clean 3-Layered Trading Card Architecture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
            ) {
                // =========================================================================
                // LAYER 1: BASE LAYER (Card frame, background texture, headers, stats)
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    activePrimaryType.seedColor.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        )
                ) {
                    // Subtle Pokéball Watermark
                    Icon(
                        imageVector = Icons.Default.CatchingPokemon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                    )

                    // Header Overlay (HP, Gen, Types)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = activePrimaryType.seedColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "HP ${activeStats?.hp ?: 100} • GEN ${pokemon.effectiveGeneration}",
                                style = StatNumberStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypeChip(type = activePrimaryType)
                            activeSecondaryType?.let {
                                Spacer(modifier = Modifier.width(4.dp))
                                TypeChip(type = it)
                            }
                        }
                    }

                    // Footer Overlay (Name, Flip Badge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.BottomStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pokemon.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Tap Flip Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipToBack,
                                    contentDescription = "Flip Card",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3D Flip",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // LAYER 2: POP-OUT SPRITE LAYER
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp, top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = activeArtworkUrl,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "MainArtworkCrossfade"
                    ) { targetUrl ->
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
                                .data(targetUrl)
                                .crossfade(150)
                                .build(),
                            contentDescription = activeName,
                            contentScale = ContentScale.Fit,
                            modifier = imageModifier.fillMaxSize(0.75f)
                        )
                    }
                }
            }
        } else {
            // BACK SIDE (Rotated 180° so text isn't mirrored)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                activePrimaryType.seedColor.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    )
                    .padding(Dimens.Default),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = activePrimaryType.seedColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "GEN ${pokemon.effectiveGeneration} • ${pokemon.formattedNumber}",
                                style = StatNumberStyle.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pokemon.collection?.isCaught == true) {
                                Icon(
                                    imageVector = Icons.Default.CatchingPokemon,
                                    contentDescription = "Caught",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (pokemon.collection?.isFavorite == true) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Center Trading Card Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = pokemon.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Dimens.Tight))

                        // Type Badges
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TypeChip(type = activePrimaryType)
                            activeSecondaryType?.let {
                                Spacer(modifier = Modifier.width(Dimens.Tight))
                                TypeChip(type = it)
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.Tight))

                        // Stat Summary Badges Row
                        activeStats?.let { stats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBadge(label = "BST", value = "${stats.total}")
                                StatBadge(label = "HP", value = "${stats.hp}")
                                StatBadge(label = "ATK", value = "${stats.attack}")
                                StatBadge(label = "SPD", value = "${stats.speed}")
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.Tight))

                        // Size comparison info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Height,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activeHeightM}m / ${activeWeightKg}kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Bottom Flip Back Hint
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipToBack,
                            contentDescription = "Flip Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to flip front 🔄",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = StatNumberStyle.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
