package com.dexter.app.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.EvolutionNode
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@Composable
fun EvolutionTreeSection(
    evolutionNodes: List<EvolutionNode>,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
        ) {
            Text(
                text = "EVOLUTION TREE",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Compact)
            )

            if (evolutionNodes.isEmpty()) {
                Text(
                    text = "Loading evolution data...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (evolutionNodes.size == 1) {
                Text(
                    text = "This Pokémon does not evolve.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val baseNode = evolutionNodes.firstOrNull { it.evolvesFromSpeciesId == null } ?: evolutionNodes.first()
                val stage2Nodes = evolutionNodes.filter { it.evolvesFromSpeciesId == baseNode.speciesId }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                ) {
                    stage2Nodes.forEach { stage2Node ->
                        val stage3Nodes = evolutionNodes.filter { it.evolvesFromSpeciesId == stage2Node.speciesId }

                        if (stage3Nodes.isNotEmpty()) {
                            stage3Nodes.forEach { stage3Node ->
                                EvolutionLineRow(
                                    base = baseNode,
                                    middle = stage2Node,
                                    finalNode = stage3Node,
                                    onPokemonClick = onPokemonClick
                                )
                            }
                        } else {
                            EvolutionLineRow(
                                base = baseNode,
                                middle = stage2Node,
                                finalNode = null,
                                onPokemonClick = onPokemonClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvolutionLineRow(
    base: EvolutionNode,
    middle: EvolutionNode,
    finalNode: EvolutionNode?,
    onPokemonClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.Compact))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(Dimens.Compact)
            )
            .padding(vertical = Dimens.Tight, horizontal = Dimens.Micro),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EvolutionCard(
            node = base,
            onClick = { onPokemonClick(base.speciesId) },
            modifier = Modifier.weight(1f)
        )

        EvolutionFlowConnector(
            conditionText = middle.conditionText,
            modifier = Modifier.width(52.dp)
        )

        EvolutionCard(
            node = middle,
            onClick = { onPokemonClick(middle.speciesId) },
            modifier = Modifier.weight(1f)
        )

        if (finalNode != null) {
            EvolutionFlowConnector(
                conditionText = finalNode.conditionText,
                modifier = Modifier.width(52.dp)
            )
            EvolutionCard(
                node = finalNode,
                onClick = { onPokemonClick(finalNode.speciesId) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EvolutionCard(
    node: EvolutionNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "EvolutionCardScale"
    )

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = Dimens.MinTouchTarget)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(Dimens.Compact))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.selectionTick()
                    onClick()
                }
            )
            .padding(Dimens.Micro),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(node.spriteUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = node.speciesName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Micro))
        Text(
            text = node.capitalizedName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "#${node.speciesId.toString().padStart(4, '0')}",
            style = StatNumberStyle.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EvolutionFlowConnector(
    conditionText: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        if (conditionText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                shadowElevation = 2.dp,
                modifier = Modifier.padding(bottom = Dimens.Micro / 2)
            ) {
                Text(
                    text = conditionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                val startY = size.height / 2f
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = Offset(0f, startY),
                    end = Offset(size.width, startY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Evolves to",
                tint = primaryColor,
                modifier = Modifier
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            )
        }
    }
}
