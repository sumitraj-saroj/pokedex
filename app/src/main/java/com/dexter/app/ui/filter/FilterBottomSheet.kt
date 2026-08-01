package com.dexter.app.ui.filter

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.SpecialCategory
import com.dexter.app.ui.common.contentColorForSeed
import com.dexter.app.ui.home.HomeUiState
import com.dexter.app.ui.home.SortOption
import com.dexter.app.ui.home.SortOrder
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onSortOptionSelect: (SortOption) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
    onGenerationToggle: (Int) -> Unit,
    onTypeToggle: (PokemonType) -> Unit,
    onSpecialCategoryToggle: (SpecialCategory) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    // Live matching count calculation with exact dual type & OR category logic
    val matchingCount by remember(
        uiState.pokemonList,
        uiState.searchQuery,
        uiState.selectedGenerations,
        uiState.selectedTypes,
        uiState.selectedSpecialCategories
    ) {
        derivedStateOf {
            uiState.pokemonList.count { pokemon ->
                val matchesQuery = if (uiState.searchQuery.isBlank()) {
                    true
                } else {
                    val q = uiState.searchQuery.trim().lowercase()
                    pokemon.name.lowercase().contains(q) || pokemon.number.toString().contains(q)
                }

                val matchesGen = uiState.selectedGenerations.isEmpty() || uiState.selectedGenerations.contains(pokemon.effectiveGeneration)

                val matchesType = if (uiState.selectedTypes.isEmpty()) {
                    true
                } else {
                    uiState.selectedTypes.any { requiredType ->
                        pokemon.primaryType == requiredType || pokemon.secondaryType == requiredType
                    }
                }

                val matchesSpecial = if (uiState.selectedSpecialCategories.isEmpty()) {
                    true
                } else {
                    uiState.selectedSpecialCategories.any { category ->
                        category.matches(pokemon)
                    }
                }

                matchesQuery && matchesGen && matchesType && matchesSpecial
            }
        }
    }

    val hasActiveFilters = uiState.totalActiveFiltersCount > 0

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
                    Text(
                        text = "Filter & Sort",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasActiveFilters) {
                        Text(
                            text = "${uiState.totalActiveFiltersCount} active filter${if (uiState.totalActiveFiltersCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasActiveFilters) {
                        TextButton(onClick = onClearFilters) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset All",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(Dimens.Micro))
                            Text(
                                text = "Reset All",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
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
                verticalArrangement = Arrangement.spacedBy(Dimens.Section)
            ) {
                // Sort & Order Section
                item {
                    FilterCategoryCard(
                        title = "Sort Order",
                        icon = Icons.AutoMirrored.Filled.Sort,
                        subtitle = "Select metric and direction"
                    ) {
                        Text(
                            text = "METRIC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                        ) {
                            SortOption.entries.forEach { option ->
                                val selected = uiState.sortOption == option
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (selected) Dimens.Major else Dimens.Compact,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                    label = "sort_option_shape"
                                )
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSortOptionSelect(option) },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    leadingIcon = if (selected) {
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
                        }

                        Spacer(modifier = Modifier.height(Dimens.Tight))

                        Text(
                            text = "DIRECTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                        ) {
                            SortOrder.entries.forEach { order ->
                                val selected = uiState.sortOrder == order
                                val icon = if (order == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (selected) Dimens.Major else Dimens.Compact,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                    label = "sort_order_shape"
                                )
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSortOrderSelect(order) },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            text = order.displayName,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Special Categories Section
                item {
                    val count = uiState.selectedSpecialCategories.size
                    FilterCategoryCard(
                        title = "Special Categories",
                        icon = Icons.Default.Stars,
                        subtitle = if (count > 0) "$count selected" else "Legendaries, Mythicals, Starters & more"
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                        ) {
                            SpecialCategory.entries.forEach { category ->
                                val selected = uiState.selectedSpecialCategories.contains(category)
                                val chipContentColor = if (category.chipColor.luminance() > 0.5f) Color.Black else Color.White
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (selected) Dimens.Major else Dimens.Compact,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                    label = "special_chip_shape"
                                )

                                FilterChip(
                                    selected = selected,
                                    onClick = { onSpecialCategoryToggle(category) },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            text = "${category.emoji} ${category.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = category.chipColor,
                                        selectedLabelColor = chipContentColor,
                                        selectedLeadingIconColor = chipContentColor
                                    )
                                )
                            }
                        }
                    }
                }

                // Generations Section
                item {
                    val count = uiState.selectedGenerations.size
                    FilterCategoryCard(
                        title = "Generations",
                        icon = Icons.Default.Grid4x4,
                        subtitle = if (count > 0) "$count selected" else "Gen 1 through Gen 9"
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                        ) {
                            (1..9).forEach { gen ->
                                val selected = uiState.selectedGenerations.contains(gen)
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (selected) Dimens.Major else Dimens.Compact,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                    label = "gen_chip_shape"
                                )

                                FilterChip(
                                    selected = selected,
                                    onClick = { onGenerationToggle(gen) },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            text = "Gen $gen",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Pokémon Types Section
                item {
                    val count = uiState.selectedTypes.size
                    FilterCategoryCard(
                        title = "Pokémon Types",
                        icon = Icons.Default.Category,
                        subtitle = if (count > 0) "$count selected (2 types matches dual-type)" else "Filter by single or dual elemental types"
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                        ) {
                            PokemonType.entries.forEach { type ->
                                val selected = uiState.selectedTypes.contains(type)
                                val textColor = type.seedColor.contentColorForSeed()
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (selected) Dimens.Major else Dimens.Compact,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                    label = "type_chip_shape"
                                )

                                FilterChip(
                                    selected = selected,
                                    onClick = { onTypeToggle(type) },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            text = type.typeName.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = type.seedColor,
                                        selectedLabelColor = textColor,
                                        selectedLeadingIconColor = textColor
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Sticky Bottom Action CTA
            val haptics = com.dexter.app.ui.common.rememberHapticUtils()
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
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Tight))
                        Text(
                            text = if (matchingCount > 0) "Show $matchingCount Pokémon" else "No Pokémon Match Filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCategoryCard(
    title: String,
    icon: ImageVector,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Section),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Section)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Dimens.Tight)
            ) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Dimens.Tight))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            content()
        }
    }
}
