package com.dexter.app.ui.region

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import com.dexter.app.domain.model.region.WildSpawn
import com.dexter.app.ui.common.GlassmorphicTopAppBar
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegionMapScreen(
    uiState: RegionMapUiState,
    onBackClick: () -> Unit,
    onRegionSelect: (Int) -> Unit,
    onLocationSelect: (String) -> Unit,
    onFilterTypeSelect: (LocationType?) -> Unit,
    onPokemonClick: (Int) -> Unit,
    onMapStyleSelect: (com.dexter.app.domain.model.region.MapStyle) -> Unit,
    onToggleAudioTheme: () -> Unit,
    onPlayPokemonCry: (Int, String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectSearchResult: (Int, String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticUtils()
    var isSearchExpanded by remember { mutableStateOf(false) }

    val currentRegion = uiState.selectedRegion ?: return
    val selectedLoc = uiState.selectedLocation

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            GlassmorphicTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "${currentRegion.name} Region",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Gen ${currentRegion.number} • ${currentRegion.japaneseName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onClearSearch()
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search Spawns",
                            tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Search Bar Expandable Header
                AnimatedVisibility(visible = isSearchExpanded) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search Pokémon or Location (e.g., Pikachu, Mewtwo, Cerulean)") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = if (uiState.searchQuery.isNotBlank()) {
                                {
                                    IconButton(onClick = onClearSearch) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        )
                    }
                }

                // If Search has results, show search dropdown list
                if (uiState.isSearchActive) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No locations or Pokémon found for \"${uiState.searchQuery}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(uiState.searchResults) { (region, location) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptics.selectionTick()
                                            onSelectSearchResult(region.number, location.id)
                                            isSearchExpanded = false
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = location.type.emoji,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = location.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "${region.name} Region • ${location.type.displayName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = location.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Go",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    return@Column
                }

                // Horizontal Generation & Region Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.regions.forEach { reg ->
                        val isSelected = reg.number == uiState.selectedRegionNumber
                        val cornerRadius by animateDpAsState(
                            targetValue = if (isSelected) Dimens.Major else Dimens.Compact,
                            animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                            label = "region_tab_shape"
                        )

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptics.selectionTick()
                                onRegionSelect(reg.number)
                            },
                            shape = RoundedCornerShape(cornerRadius),
                            label = {
                                Text(
                                    text = "Gen ${reg.number} • ${reg.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Main Scrollable Body
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Region Hero Overview Card
                    item {
                        RegionHeroCard(
                            region = currentRegion,
                            pokemonMap = uiState.allPokemonMap,
                            isPlayingAudio = uiState.isPlayingAudio,
                            onToggleAudio = onToggleAudioTheme,
                            onPokemonClick = onPokemonClick
                        )
                    }

                    // Map Section Header & Type Filters
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "INTERACTIVE ROUTE MAP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap node to inspect",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Quick Location Type Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val isAll = uiState.filterType == null
                                FilterChip(
                                    selected = isAll,
                                    onClick = {
                                        haptics.selectionTick()
                                        onFilterTypeSelect(null)
                                    },
                                    label = { Text("All (${currentRegion.locations.size})", maxLines = 1) }
                                )

                                FilterChip(
                                    selected = uiState.filterType == LocationType.CITY || uiState.filterType == LocationType.TOWN,
                                    onClick = {
                                        haptics.selectionTick()
                                        onFilterTypeSelect(if (uiState.filterType == LocationType.CITY) null else LocationType.CITY)
                                    },
                                    label = { Text("🏙️ Cities & Towns", maxLines = 1) }
                                )

                                FilterChip(
                                    selected = uiState.filterType == LocationType.LEGENDARY_LAIR,
                                    onClick = {
                                        haptics.selectionTick()
                                        onFilterTypeSelect(if (uiState.filterType == LocationType.LEGENDARY_LAIR) null else LocationType.LEGENDARY_LAIR)
                                    },
                                    label = { Text("⭐ Legendaries", maxLines = 1) }
                                )

                                FilterChip(
                                    selected = uiState.filterType == LocationType.CAVE || uiState.filterType == LocationType.MOUNTAIN || uiState.filterType == LocationType.DUNGEON,
                                    onClick = {
                                        haptics.selectionTick()
                                        onFilterTypeSelect(if (uiState.filterType == LocationType.CAVE) null else LocationType.CAVE)
                                    },
                                    label = { Text("🪨 Caves & Dungeons", maxLines = 1) }
                                )
                            }

                            // Visual Map Style Switcher (Pixel vs Tactical vs Art)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                com.dexter.app.domain.model.region.MapStyle.entries.forEach { style ->
                                    val isSelected = uiState.mapStyle == style
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptics.selectionTick()
                                            onMapStyleSelect(style)
                                        },
                                        label = {
                                            Text(
                                                text = "${style.icon} ${style.displayName}",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Map Canvas Visualizer
                    item {
                        RegionMapVisualizer(
                            region = currentRegion,
                            selectedLocationId = uiState.selectedLocationId,
                            mapStyle = uiState.mapStyle,
                            onLocationSelect = onLocationSelect
                        )
                    }

                    // Location Detail Inspector
                    if (selectedLoc != null) {
                        item {
                            LocationDetailInspectorCard(
                                location = selectedLoc,
                                pokemonMap = uiState.allPokemonMap,
                                onPokemonClick = onPokemonClick,
                                onPlayCry = onPlayPokemonCry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionHeroCard(
    region: Region,
    pokemonMap: Map<Int, com.dexter.app.domain.model.Pokemon>,
    isPlayingAudio: Boolean,
    onToggleAudio: () -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    var isLoreExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Tagline
            Text(
                text = region.tagline.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description / Lore
            Text(
                text = region.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isLoreExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // Structured Metadata Grid: Professor & Villain Team
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Professor Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "RESEARCHER",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = region.professor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Villain Team Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🏴‍☠️", style = MaterialTheme.typography.bodySmall)
                        Column {
                            Text(
                                text = "VILLAIN ORG",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = region.villainTeam,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Read Lore Expander Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { isLoreExpanded = !isLoreExpanded }) {
                    Text(
                        text = if (isLoreExpanded) "Show Less" else "Read Full Lore",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Starter Pokémon Trio (Equal-Width Cards)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "REGIONAL STARTERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    region.starterIds.forEach { starterId ->
                        val pokemon = pokemonMap[starterId]
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPokemonClick(starterId) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val artwork = pokemon?.officialArtworkUrl ?: pokemon?.spriteUrl
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(artwork)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = pokemon?.name,
                                    modifier = Modifier.size(36.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = pokemon?.capitalizedName ?: "#$starterId",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Regional Music Soundscape Player
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(onClick = onToggleAudio) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlayingAudio) "Pause Music" else "Play Music",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = region.audioThemeTitle.ifBlank { "${region.name} Regional Theme" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = region.musicTheme,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (isPlayingAudio) {
                        AnimatedSoundVisualizer()
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedSoundVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp).padding(horizontal = 4.dp)
    ) {
        Surface(
            modifier = Modifier.width(3.dp).height(h1.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.primary
        ) {}
        Surface(
            modifier = Modifier.width(3.dp).height(h2.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.primary
        ) {}
        Surface(
            modifier = Modifier.width(3.dp).height(h3.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.primary
        ) {}
    }
}

@Composable
private fun LocationDetailInspectorCard(
    location: RegionLocation,
    pokemonMap: Map<Int, com.dexter.app.domain.model.Pokemon>,
    onPokemonClick: (Int) -> Unit,
    onPlayCry: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Location Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = location.type.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = location.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description
            Text(
                text = location.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Gym Leader Card (if present)
            location.gymLeader?.let { leader ->
                GymLeaderCard(
                    leader = leader,
                    pokemonMap = pokemonMap,
                    onPokemonClick = onPokemonClick
                )
            }

            // Legendary Encounter Card (if present)
            location.legendary?.let { leg ->
                LegendaryEncounterCard(
                    legendary = leg,
                    pokemonMap = pokemonMap,
                    onPokemonClick = onPokemonClick,
                    onPlayCry = onPlayCry
                )
            }

            // Wild Spawns List (if present)
            if (location.wildSpawns.isNotEmpty()) {
                WildSpawnsSection(
                    spawns = location.wildSpawns,
                    pokemonMap = pokemonMap,
                    onPokemonClick = onPokemonClick,
                    onPlayCry = onPlayCry
                )
            }

            // Music / Atmosphere snippet
            if (location.musicThemeDescription.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = location.musicThemeDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GymLeaderCard(
    leader: com.dexter.app.domain.model.region.GymLeader,
    pokemonMap: Map<Int, com.dexter.app.domain.model.Pokemon>,
    onPokemonClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Leader Name & Specialty Type Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = leader.badgeEmoji, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Column {
                        Text(
                            text = leader.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = leader.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${leader.specialtyType.typeName.uppercase()} GYM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Row 2: Badge Name on Left, Ace Pokémon on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${leader.badgeName} (${leader.badgeEmoji})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                val ace = pokemonMap[leader.acePokemonId]
                Surface(
                    modifier = Modifier.clickable { onPokemonClick(leader.acePokemonId) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val artwork = ace?.officialArtworkUrl ?: ace?.spriteUrl
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artwork)
                                .crossfade(true)
                                .build(),
                            contentDescription = leader.acePokemonName,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Ace: ${leader.acePokemonName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendaryEncounterCard(
    legendary: com.dexter.app.domain.model.region.LegendaryEncounter,
    pokemonMap: Map<Int, com.dexter.app.domain.model.Pokemon>,
    onPokemonClick: (Int) -> Unit,
    onPlayCry: (Int, String) -> Unit
) {
    val pokemon = pokemonMap[legendary.pokemonId]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPokemonClick(legendary.pokemonId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3E2723).copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val artwork = pokemon?.officialArtworkUrl ?: pokemon?.spriteUrl
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artwork)
                        .crossfade(true)
                        .build(),
                    contentDescription = legendary.pokemonName,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LEGENDARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    IconButton(
                        onClick = { onPlayCry(legendary.pokemonId, legendary.pokemonName) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Cry",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "${legendary.pokemonName} (Lv. ${legendary.level})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = legendary.requirementText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WildSpawnsSection(
    spawns: List<WildSpawn>,
    pokemonMap: Map<Int, com.dexter.app.domain.model.Pokemon>,
    onPokemonClick: (Int) -> Unit,
    onPlayCry: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "WILD POKÉMON SPAWNS (${spawns.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            spawns.forEach { spawn ->
                val pokemon = pokemonMap[spawn.pokemonId]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPokemonClick(spawn.pokemonId) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val artwork = pokemon?.spriteUrl ?: pokemon?.officialArtworkUrl
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(artwork)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = spawn.pokemonName,
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = spawn.pokemonName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Lv. ${spawn.minLevel}–${spawn.maxLevel} • ${spawn.method}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Rarity Chip
                            val rarityColor = when (spawn.rarity.lowercase()) {
                                "very common" -> Color(0xFF81C784)
                                "common" -> Color(0xFF64B5F6)
                                "uncommon" -> Color(0xFFFFB74D)
                                "rare" -> Color(0xFFBA68C8)
                                "very rare" -> Color(0xFFE57373)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = rarityColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = spawn.rarity,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = rarityColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { onPlayCry(spawn.pokemonId, spawn.pokemonName) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Cry",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
