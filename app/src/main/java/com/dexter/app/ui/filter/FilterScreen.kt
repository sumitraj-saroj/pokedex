package com.dexter.app.ui.filter

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
fun FilterScreen(
    uiState: HomeUiState,
    onBackClick: () -> Unit,
    onSortOptionSelect: (SortOption) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
    onGenerationToggle: (Int) -> Unit,
    onTypeToggle: (PokemonType) -> Unit,
    onSpecialCategoryToggle: (SpecialCategory) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Live matching count with updated exact dual-type & OR category logic
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

    val tabs = listOf(
        FilterTabItem("Types", uiState.selectedTypes.size, Icons.Default.Category),
        FilterTabItem("Generations", uiState.selectedGenerations.size, Icons.Default.Grid4x4),
        FilterTabItem("Special", uiState.selectedSpecialCategories.size, Icons.Default.Stars),
        FilterTabItem("Sort & Order", if (uiState.sortOption != SortOption.NUMBER || uiState.sortOrder != SortOrder.ASCENDING) 1 else 0, Icons.AutoMirrored.Filled.Sort)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                com.dexter.app.ui.common.GlassmorphicTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Filter & Sort",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasActiveFilters) {
                                Text(
                                    text = "${uiState.totalActiveFiltersCount} active filter${if (uiState.totalActiveFiltersCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (hasActiveFilters) {
                            TextButton(onClick = {
                                haptics.lightTick()
                                onClearFilters()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset All",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Dimens.Micro))
                                Text(
                                    text = "Reset All",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )

                // Compact M3 Tab Row
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                haptics.lightTick()
                                selectedTabIndex = index
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    if (tab.badgeCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = "${tab.badgeCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Section)
                ) {
                    Button(
                        onClick = {
                            haptics.successPulse()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(Dimens.Major),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Tight))
                        Text(
                            text = if (matchingCount > 0) "Apply Filters ($matchingCount Pokémon)" else "No Pokémon Match Filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Section)
        ) {
            Crossfade(
                targetState = selectedTabIndex,
                label = "filter_tab_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TypesTabContent(
                        selectedTypes = uiState.selectedTypes,
                        onTypeToggle = onTypeToggle
                    )
                    1 -> GenerationsTabContent(
                        selectedGenerations = uiState.selectedGenerations,
                        onGenerationToggle = onGenerationToggle
                    )
                    2 -> SpecialTabContent(
                        selectedCategories = uiState.selectedSpecialCategories,
                        onCategoryToggle = onSpecialCategoryToggle
                    )
                    3 -> SortTabContent(
                        selectedSortOption = uiState.sortOption,
                        selectedSortOrder = uiState.sortOrder,
                        onSortOptionSelect = onSortOptionSelect,
                        onSortOrderSelect = onSortOrderSelect
                    )
                }
            }
        }
    }
}

private data class FilterTabItem(
    val title: String,
    val badgeCount: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypesTabContent(
    selectedTypes: Set<PokemonType>,
    onTypeToggle: (PokemonType) -> Unit
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
    ) {
        item {
            Text(
                text = "POKÉMON TYPES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
            )
            Text(
                text = "Select 1 type to filter, or select 2 types to show exact dual-type Pokémon (e.g. Fire + Water)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.Tight)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                PokemonType.entries.forEach { type ->
                    val selected = selectedTypes.contains(type)
                    val textColor = type.seedColor.contentColorForSeed()
                    val cornerRadius by animateDpAsState(
                        targetValue = if (selected) Dimens.Major else Dimens.Compact,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                        label = "type_chip_shape"
                    )

                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptics.lightTick()
                            onTypeToggle(type)
                        },
                        shape = RoundedCornerShape(cornerRadius),
                        label = {
                            Text(
                                text = type.typeName.uppercase(),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenerationsTabContent(
    selectedGenerations: Set<Int>,
    onGenerationToggle: (Int) -> Unit
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
    ) {
        item {
            Text(
                text = "GENERATIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
            )
            Text(
                text = "Select generations to include (Gen 1 through Gen 9)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.Tight)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                (1..9).forEach { gen ->
                    val selected = selectedGenerations.contains(gen)
                    val cornerRadius by animateDpAsState(
                        targetValue = if (selected) Dimens.Major else Dimens.Compact,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                        label = "gen_chip_shape"
                    )

                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptics.lightTick()
                            onGenerationToggle(gen)
                        },
                        shape = RoundedCornerShape(cornerRadius),
                        label = {
                            Text(
                                text = "Gen $gen",
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialTabContent(
    selectedCategories: Set<SpecialCategory>,
    onCategoryToggle: (SpecialCategory) -> Unit
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
    ) {
        item {
            Text(
                text = "SPECIAL CATEGORIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
            )
            Text(
                text = "Filter by Legendary, Mythical, Starters, Fossils, Ultra Beasts & more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.Tight)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                SpecialCategory.entries.forEach { category ->
                    val selected = selectedCategories.contains(category)
                    val chipContentColor = if (category.chipColor.luminance() > 0.5f) Color.Black else Color.White
                    val cornerRadius by animateDpAsState(
                        targetValue = if (selected) Dimens.Major else Dimens.Compact,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                        label = "special_chip_shape"
                    )

                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptics.lightTick()
                            onCategoryToggle(category)
                        },
                        shape = RoundedCornerShape(cornerRadius),
                        label = {
                            Text(
                                text = "${category.emoji} ${category.displayName}",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortTabContent(
    selectedSortOption: SortOption,
    selectedSortOrder: SortOrder,
    onSortOptionSelect: (SortOption) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Section)
    ) {
        item {
            Text(
                text = "SORT BY METRIC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                SortOption.entries.forEach { option ->
                    val selected = selectedSortOption == option
                    val cornerRadius by animateDpAsState(
                        targetValue = if (selected) Dimens.Major else Dimens.Compact,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                        label = "sort_option_shape"
                    )
                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptics.lightTick()
                            onSortOptionSelect(option)
                        },
                        shape = RoundedCornerShape(cornerRadius),
                        label = {
                            Text(
                                text = option.displayName,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        item {
            Text(
                text = "SORT DIRECTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                SortOrder.entries.forEach { order ->
                    val selected = selectedSortOrder == order
                    val icon = if (order == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    val cornerRadius by animateDpAsState(
                        targetValue = if (selected) Dimens.Major else Dimens.Compact,
                        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                        label = "sort_order_shape"
                    )
                    FilterChip(
                        selected = selected,
                        onClick = {
                            haptics.lightTick()
                            onSortOrderSelect(order)
                        },
                        shape = RoundedCornerShape(cornerRadius),
                        label = {
                            Text(
                                text = order.displayName,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
