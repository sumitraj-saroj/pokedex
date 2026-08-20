package com.dexter.app.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
    val focusManager = LocalFocusManager.current

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = query, selection = TextRange(query.length)))
    }

    LaunchedEffect(query) {
        if (query != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = query,
                selection = TextRange(query.length)
            )
        }
    }

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
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    if (newValue.text != query) {
                        onQueryChange(newValue.text)
                    }
                },
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
                    if (textFieldValue.text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                textFieldValue = TextFieldValue("", selection = TextRange(0))
                                onQueryChange("")
                            },
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
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

        // Inline Quick-Filter Chips Bar (Types, Special Categories, Generations)
        QuickFilterChipsRow(
            selectedGenerations = selectedGenerations,
            onGenerationToggle = onGenerationToggle,
            selectedTypes = selectedTypes,
            onTypeToggle = onTypeToggle,
            selectedSpecialCategories = selectedSpecialCategories,
            onSpecialCategoryToggle = onSpecialCategoryToggle,
            modifier = Modifier.padding(top = Dimens.Tight / 2)
        )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFilterChipsRow(
    selectedGenerations: Set<Int>,
    onGenerationToggle: (Int) -> Unit,
    selectedTypes: Set<PokemonType>,
    onTypeToggle: (PokemonType) -> Unit,
    selectedSpecialCategories: Set<SpecialCategory>,
    onSpecialCategoryToggle: (SpecialCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rapid Special Category Toggles (e.g. Legendary, Mythical, Starter, Pseudo-Legendary)
        items(
            items = SpecialCategory.entries.take(4),
            key = { "cat_${it.name}" }
        ) { category ->
            val isSelected = selectedSpecialCategories.contains(category)
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "cat_scale"
            )
            val textColor = if (isSelected) {
                if (category.chipColor.luminance() > 0.5f) Color.Black else Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    haptics.lightTick()
                    onSpecialCategoryToggle(category)
                },
                modifier = Modifier.scale(scale),
                shape = CircleShape,
                label = {
                    Text(
                        text = "${category.emoji} ${category.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.chipColor,
                    selectedLabelColor = textColor,
                    selectedLeadingIconColor = textColor,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Generation Shortcuts
        items(
            items = (1..9).toList(),
            key = { "gen_$it" }
        ) { gen ->
            val isSelected = selectedGenerations.contains(gen)
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "gen_scale"
            )

            val romanGen = when (gen) {
                1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"
                6 -> "VI"; 7 -> "VII"; 8 -> "VIII"; 9 -> "IX"; else -> "$gen"
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    haptics.lightTick()
                    onGenerationToggle(gen)
                },
                modifier = Modifier.scale(scale),
                shape = CircleShape,
                label = {
                    Text(
                        text = "Gen $romanGen",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // All Element Types
        items(
            items = PokemonType.entries,
            key = { "type_${it.typeName}" }
        ) { type ->
            val isSelected = selectedTypes.contains(type)
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "type_scale"
            )
            val textColor = if (isSelected) type.seedColor.contentColorForSeed() else MaterialTheme.colorScheme.onSurfaceVariant

            FilterChip(
                selected = isSelected,
                onClick = {
                    haptics.lightTick()
                    onTypeToggle(type)
                },
                modifier = Modifier.scale(scale),
                shape = CircleShape,
                label = {
                    Text(
                        text = type.typeName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = type.seedColor,
                    selectedLabelColor = textColor,
                    selectedLeadingIconColor = textColor,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

