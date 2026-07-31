package com.dexter.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.SpecialCategory
import com.dexter.app.ui.home.SortOption
import com.dexter.app.ui.home.SortOrder
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenFilterPage: () -> Unit,
    selectedGenerations: Set<Int> = emptySet(),
    onGenerationToggle: (Int) -> Unit = {},
    selectedTypes: Set<PokemonType> = emptySet(),
    onTypeToggle: (PokemonType) -> Unit = {},
    selectedSpecialCategories: Set<SpecialCategory> = emptySet(),
    onSpecialCategoryToggle: (SpecialCategory) -> Unit = {},
    sortOption: SortOption = SortOption.NUMBER,
    sortOrder: SortOrder = SortOrder.ASCENDING,
    onSortOptionReset: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    totalActiveFilters: Int = selectedGenerations.size + selectedTypes.size + selectedSpecialCategories.size + (if (sortOption != SortOption.NUMBER || sortOrder != SortOrder.ASCENDING) 1 else 0),
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = totalActiveFilters > 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.MinTouchTarget)
                    .clip(RoundedCornerShape(Dimens.Section)),
                placeholder = {
                    Text(
                        text = "Search Pokémon...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(Dimens.MinTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.Section),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            Spacer(modifier = Modifier.width(Dimens.Tight))

            // Badged Filter Icon Button that opens dedicated filter page
            BadgedBox(
                badge = {
                    if (hasActiveFilters) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "$totalActiveFilters",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                Surface(
                    modifier = Modifier
                        .size(Dimens.MinTouchTarget)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenFilterPage),
                    color = if (hasActiveFilters) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Open Filters",
                            tint = if (hasActiveFilters) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        // Removable Active Filter Pills Row
        val haptics = com.dexter.app.ui.common.rememberHapticUtils()
        AnimatedVisibility(visible = hasActiveFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Tight)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear All Pill
                FilterChip(
                    selected = true,
                    onClick = {
                        haptics.lightTick()
                        onClearFilters()
                    },
                    shape = RoundedCornerShape(Dimens.Compact),
                    label = {
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear All Filters",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                // Sort Pill
                if (sortOption != SortOption.NUMBER || sortOrder != SortOrder.ASCENDING) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            haptics.lightTick()
                            onSortOptionReset()
                        },
                        shape = RoundedCornerShape(Dimens.Compact),
                        label = {
                            Text(
                                text = "Sort: ${sortOption.displayName} (${if (sortOrder == SortOrder.ASCENDING) "Asc" else "Desc"})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reset Sort",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                // Special Categories Pills
                selectedSpecialCategories.forEach { category ->
                    val chipContentColor = if (category.chipColor.luminance() > 0.5f) Color.Black else Color.White
                    FilterChip(
                        selected = true,
                        onClick = {
                            haptics.lightTick()
                            onSpecialCategoryToggle(category)
                        },
                        shape = RoundedCornerShape(Dimens.Compact),
                        label = {
                            Text(
                                text = "${category.emoji} ${category.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove ${category.displayName}",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.chipColor,
                            selectedLabelColor = chipContentColor,
                            selectedTrailingIconColor = chipContentColor
                        )
                    )
                }

                // Generations Pills
                selectedGenerations.forEach { gen ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            haptics.lightTick()
                            onGenerationToggle(gen)
                        },
                        shape = RoundedCornerShape(Dimens.Compact),
                        label = {
                            Text(
                                text = "Gen $gen",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Gen $gen",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Types Pills
                selectedTypes.forEach { type ->
                    val textColor = type.seedColor.contentColorForSeed()
                    FilterChip(
                        selected = true,
                        onClick = {
                            haptics.lightTick()
                            onTypeToggle(type)
                        },
                        shape = RoundedCornerShape(Dimens.Compact),
                        label = {
                            Text(
                                text = type.typeName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove type ${type.typeName}",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = type.seedColor,
                            selectedLabelColor = textColor,
                            selectedTrailingIconColor = textColor
                        )
                    )
                }
            }
        }
    }
}
