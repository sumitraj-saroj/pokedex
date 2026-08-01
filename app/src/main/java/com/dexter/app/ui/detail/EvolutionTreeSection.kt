package com.dexter.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.EvolutionNode
import com.dexter.app.ui.theme.Dimens

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
            .padding(vertical = Dimens.Tight, horizontal = Dimens.Micro),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EvolutionCard(
            node = base,
            onClick = { onPokemonClick(base.speciesId) },
            modifier = Modifier.weight(1f)
        )

        ConditionArrow(conditionText = middle.conditionText)

        EvolutionCard(
            node = middle,
            onClick = { onPokemonClick(middle.speciesId) },
            modifier = Modifier.weight(1f)
        )

        if (finalNode != null) {
            ConditionArrow(conditionText = finalNode.conditionText)
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
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = Dimens.MinTouchTarget)
            .clip(RoundedCornerShape(Dimens.Tight))
            .clickable(onClick = onClick)
            .padding(Dimens.Micro),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(node.spriteUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = node.speciesName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(38.dp)
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
    }
}

@Composable
private fun ConditionArrow(conditionText: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        if (conditionText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(Dimens.Tight),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = Dimens.Micro / 2)
            ) {
                Text(
                    text = conditionText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Evolves to",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}
