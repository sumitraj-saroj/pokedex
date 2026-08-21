package com.dexter.app.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.PokemonGeneration
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizGenerationFilterBottomSheet(
    selectedGenerations: Set<Int>,
    generationCounts: Map<Int, Int>,
    totalMatchingCount: Int,
    onToggleGeneration: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onSelectPreset: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val haptics = rememberHapticUtils()
    val isAllSelected = selectedGenerations.isEmpty() || selectedGenerations.size == 9

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sheet Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight / 2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Grid4x4,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Quiz Generations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isAllSelected) {
                            "All 9 Generations included ($totalMatchingCount Pokémon)"
                        } else {
                            "${selectedGenerations.size} Generation${if (selectedGenerations.size > 1) "s" else ""} selected ($totalMatchingCount Pokémon)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAllSelected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isAllSelected) {
                        TextButton(onClick = {
                            haptics.selectionTick()
                            onSelectAll()
                        }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Select All",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(Dimens.Micro))
                            Text(
                                text = "All Gens",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Sheet"
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = Dimens.ScreenEdgePadding,
                    vertical = Dimens.Tight
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                // Presets Section
                item {
                    Text(
                        text = "QUICK PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        val isAllPreset = isAllSelected
                        val isClassicPreset = selectedGenerations == setOf(1, 2, 3)
                        val isMiddlePreset = selectedGenerations == setOf(4, 5, 6)
                        val isModernPreset = selectedGenerations == setOf(7, 8, 9)

                        // All Gens Preset
                        FilterChip(
                            selected = isAllPreset,
                            onClick = {
                                haptics.selectionTick()
                                onSelectAll()
                            },
                            shape = RoundedCornerShape(if (isAllPreset) Dimens.Major else Dimens.Compact),
                            label = {
                                Text(
                                    text = "All Gens (1–9)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isAllPreset) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isAllPreset) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )

                        // Classic Preset (Gen 1-3)
                        FilterChip(
                            selected = isClassicPreset,
                            onClick = {
                                haptics.selectionTick()
                                onSelectPreset(setOf(1, 2, 3))
                            },
                            shape = RoundedCornerShape(if (isClassicPreset) Dimens.Major else Dimens.Compact),
                            label = {
                                Text(
                                    text = "Classic (Gen 1–3)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isClassicPreset) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isClassicPreset) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )

                        // Middle Era Preset (Gen 4-6)
                        FilterChip(
                            selected = isMiddlePreset,
                            onClick = {
                                haptics.selectionTick()
                                onSelectPreset(setOf(4, 5, 6))
                            },
                            shape = RoundedCornerShape(if (isMiddlePreset) Dimens.Major else Dimens.Compact),
                            label = {
                                Text(
                                    text = "Middle (Gen 4–6)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isMiddlePreset) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isMiddlePreset) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )

                        // Modern Preset (Gen 7-9)
                        FilterChip(
                            selected = isModernPreset,
                            onClick = {
                                haptics.selectionTick()
                                onSelectPreset(setOf(7, 8, 9))
                            },
                            shape = RoundedCornerShape(if (isModernPreset) Dimens.Major else Dimens.Compact),
                            label = {
                                Text(
                                    text = "Modern (Gen 7–9)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isModernPreset) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isModernPreset) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.Tight))
                    Text(
                        text = "SELECT GENERATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // List of All 9 Generations with Cards
                items(PokemonGeneration.ALL, key = { it.number }) { gen ->
                    val isSelected = selectedGenerations.contains(gen.number) || isAllSelected
                    val count = generationCounts[gen.number] ?: (gen.maxId - gen.minId + 1)

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "gen_card_bg"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "gen_card_border"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics.selectionTick()
                                onToggleGeneration(gen.number)
                            },
                        shape = RoundedCornerShape(Dimens.Compact),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(1.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Gen Number Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = "Gen ${gen.number}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${gen.regionName} Region",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${gen.dexRange} • $count Pokémon",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Selection Check indicator
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sticky Bottom Action CTA
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                ) {
                    Button(
                        onClick = {
                            haptics.successPulse()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(Dimens.Major),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Tight))
                        Text(
                            text = if (totalMatchingCount > 0) {
                                "Play Quiz with $totalMatchingCount Pokémon"
                            } else {
                                "No Pokémon in Selection"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
