package com.dexter.app.ui.detail

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.dexter.app.domain.model.PokemonAbility
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@Composable
fun MovesAndAbilitiesSection(
    abilities: List<PokemonAbility>,
    moves: List<PokemonMove>,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()
    val tabs = listOf("Abilities", "Level-Up", "TM Moves")

    val levelUpMoves = moves.filter { it.learnMethod == "level-up" }.sortedBy { it.levelLearnedAt }
    val tmMoves = moves.filter { it.learnMethod == "machine" || it.learnMethod == "tm" }

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
                text = "MOVES & ABILITIES",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Compact)
            )

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.Compact))
                    .padding(bottom = Dimens.Compact)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            hapticUtils.lightTick()
                            selectedTabIndex = index
                        },
                        modifier = Modifier.defaultMinSize(minHeight = Dimens.MinTouchTarget),
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> AbilitiesTabContent(abilities = abilities)
                1 -> MovesTabContent(movesList = levelUpMoves, isLevelUp = true)
                2 -> MovesTabContent(movesList = tmMoves, isLevelUp = false)
            }
        }
    }
}



@Composable
private fun AbilitiesTabContent(abilities: List<PokemonAbility>) {
    if (abilities.isEmpty()) {
        Text(
            text = "Loading ability data...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Tight)) {
            abilities.forEach { ability ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.Section))
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(Dimens.Section),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = ability.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (ability.isHidden) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(Dimens.Micro))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = Dimens.Micro, vertical = Dimens.Micro / 2)
                                        ) {
                                            Text(
                                                text = "HIDDEN",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(Dimens.Tight))
                                    }
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand ability"
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )

                        AnimatedVisibility(visible = expanded) {
                            Text(
                                text = ability.effectText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Dimens.Default, end = Dimens.Default, bottom = Dimens.Default)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovesTabContent(movesList: List<PokemonMove>, isLevelUp: Boolean) {
    if (movesList.isEmpty()) {
        Text(
            text = if (isLevelUp) "No level-up moves listed." else "No TM moves listed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Tight)) {
            movesList.take(25).forEach { move ->
                var expanded by remember { mutableStateOf(false) }
                val detail = move.detail

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.Section))
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(Dimens.Section),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            leadingContent = if (isLevelUp) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(Dimens.Micro))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = Dimens.Tight / 2, vertical = Dimens.Micro / 2)
                                    ) {
                                        Text(
                                            text = "Lv. ${move.levelLearnedAt}",
                                            style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            } else null,
                            headlineContent = {
                                Text(
                                    text = move.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    detail?.type?.let { type ->
                                        TypeChip(type = type, isCompact = true)
                                        Spacer(modifier = Modifier.width(Dimens.Tight))
                                    }
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand move details"
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )

                        AnimatedVisibility(visible = expanded) {
                            Column(modifier = Modifier.padding(start = Dimens.Default, end = Dimens.Default, bottom = Dimens.Default)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Power: ${detail?.power ?: "--"}",
                                        style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Accuracy: ${detail?.accuracy?.let { "$it%" } ?: "--"}",
                                        style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Category: ${detail?.damageClass?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Physical"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.Micro))
                                Text(
                                    text = detail?.effectText ?: "No description available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
