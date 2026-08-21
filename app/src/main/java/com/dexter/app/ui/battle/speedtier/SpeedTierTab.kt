package com.dexter.app.ui.battle.speedtier

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.SpeedTierEntry
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.ui.battle.SpeedTierUiState
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedTierTab(
    uiState: SpeedTierUiState,
    allPokemon: List<Pokemon>,
    onLevelChange: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterSelect: (SpeedTierCategory?) -> Unit,
    onUserPokemonSelected: (Pokemon) -> Unit,
    onUserNatureChange: (PokemonNature) -> Unit,
    onUserEvChange: (Int) -> Unit,
    onUserIvChange: (Int) -> Unit,
    onUserStatStageChange: (Int) -> Unit,
    onUserScarfToggle: (Boolean) -> Unit,
    onUserBoosterToggle: (Boolean) -> Unit,
    onUserSwiftSwimToggle: (Boolean) -> Unit,
    onUserTailwindToggle: (Boolean) -> Unit,
    onUserParalyzedToggle: (Boolean) -> Unit,
    onOpenUserPokemonPicker: () -> Unit,
    onCloseUserPokemonPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userEntry = uiState.ladder.firstOrNull { it.isUserPokemon }
    val userSpeed = userEntry?.calculatedSpeed ?: 0

    val filteredLadder by remember(uiState.ladder, uiState.searchQuery, uiState.selectedCategoryFilter) {
        derivedStateOf {
            uiState.ladder.filter { entry ->
                val matchesSearch = if (uiState.searchQuery.isBlank()) {
                    true
                } else {
                    val q = uiState.searchQuery.trim().lowercase()
                    entry.pokemonName.lowercase().contains(q) ||
                    entry.spreadDescription.lowercase().contains(q) ||
                    entry.calculatedSpeed.toString().contains(q)
                }

                val matchesCategory = if (uiState.selectedCategoryFilter == null) {
                    true
                } else {
                    entry.category == uiState.selectedCategoryFilter || entry.isUserPokemon
                }

                matchesSearch && matchesCategory
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdgePadding,
            end = Dimens.ScreenEdgePadding,
            top = Dimens.Compact,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Default)
    ) {
        // 1. "My Pokémon" Configurator Header Card
        item(key = "user_configurator") {
            UserSpeedConfiguratorCard(
                uiState = uiState,
                userSpeed = userSpeed,
                onOpenPicker = onOpenUserPokemonPicker,
                onLevelChange = onLevelChange,
                onNatureChange = onUserNatureChange,
                onEvChange = onUserEvChange,
                onIvChange = onUserIvChange,
                onStageChange = onUserStatStageChange,
                onScarfToggle = onUserScarfToggle,
                onBoosterToggle = onUserBoosterToggle,
                onSwiftSwimToggle = onUserSwiftSwimToggle,
                onTailwindToggle = onUserTailwindToggle,
                onParalyzedToggle = onUserParalyzedToggle
            )
        }

        // 2. Search & Tier Filter Chips
        item(key = "filters") {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search Pokémon or speed value...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { onCategoryFilterSelect(null) },
                            label = { Text("All Tiers (${uiState.ladder.size})", fontSize = 11.sp) }
                        )
                    }
                    items(SpeedTierCategory.entries.size) { index ->
                        val category = SpeedTierCategory.entries[index]
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == category,
                            onClick = {
                                onCategoryFilterSelect(if (uiState.selectedCategoryFilter == category) null else category)
                            },
                            label = { Text(category.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // 3. Ladder Count Summary
        item(key = "ladder_summary") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed Tier Ladder (Sorted by In-Game Speed)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${filteredLadder.size} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 4. Speed Ladder Items
        itemsIndexed(
            items = filteredLadder,
            key = { _, entry -> entry.id }
        ) { index, entry ->
            SpeedLadderItemCard(
                rank = index + 1,
                entry = entry,
                userSpeed = userSpeed
            )
        }
    }

    // User Pokemon Picker
    if (uiState.isSelectingUserPokemon) {
        PokemonPickerBottomSheet(
            onDismissRequest = onCloseUserPokemonPicker,
            onPokemonSelected = onUserPokemonSelected,
            pokemonList = allPokemon,
            title = "Select Pokémon for Speed Tier"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserSpeedConfiguratorCard(
    uiState: SpeedTierUiState,
    userSpeed: Int,
    onOpenPicker: () -> Unit,
    onLevelChange: (Int) -> Unit,
    onNatureChange: (PokemonNature) -> Unit,
    onEvChange: (Int) -> Unit,
    onIvChange: (Int) -> Unit,
    onStageChange: (Int) -> Unit,
    onScarfToggle: (Boolean) -> Unit,
    onBoosterToggle: (Boolean) -> Unit,
    onSwiftSwimToggle: (Boolean) -> Unit,
    onTailwindToggle: (Boolean) -> Unit,
    onParalyzedToggle: (Boolean) -> Unit
) {
    val pokemon = uiState.userPokemon ?: return
    var isExpanded by remember { mutableStateOf(false) }
    var showNatureDropdown by remember { mutableStateOf(false) }

    val goldGradient = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(goldGradient),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: "My Pokémon" + Level Switch + Final Speed Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "My Benchmark Pokémon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = uiState.level == 50,
                        onClick = { onLevelChange(50) },
                        label = { Text("Lv. 50 (VGC)", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = uiState.level == 100,
                        onClick = { onLevelChange(100) },
                        label = { Text("Lv. 100", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Info: Sprite, Name, Calculated Speed Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pokemon.spriteUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = pokemon.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pokemon.capitalizedName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Base Speed: ${pokemon.stats?.speed ?: 100}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Final In-Game Speed Big Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$userSpeed",
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SPEED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Modifiers Chips: Nature, Scarf, Booster, Swift Swim, Tailwind
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nature
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showNatureDropdown = true },
                        label = { Text("${uiState.userNature.displayName} (+${uiState.userNature.getMultiplier(com.dexter.app.domain.battle.model.StatType.SPEED)}x)", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                    )
                    DropdownMenu(
                        expanded = showNatureDropdown,
                        onDismissRequest = { showNatureDropdown = false }
                    ) {
                        PokemonNature.entries.forEach { nat ->
                            DropdownMenuItem(
                                text = { Text("${nat.displayName} (${nat.description})") },
                                onClick = {
                                    onNatureChange(nat)
                                    showNatureDropdown = false
                                }
                            )
                        }
                    }
                }

                // Choice Scarf
                FilterChip(
                    selected = uiState.userHasScarf,
                    onClick = { onScarfToggle(!uiState.userHasScarf) },
                    label = { Text("🧣 Choice Scarf (1.5x)", fontSize = 11.sp) }
                )

                // Booster Energy Speed
                FilterChip(
                    selected = uiState.userHasBooster,
                    onClick = { onBoosterToggle(!uiState.userHasBooster) },
                    label = { Text("⚡ Booster Energy Spe (1.5x)", fontSize = 11.sp) }
                )

                // Swift Swim / Chlorophyll
                FilterChip(
                    selected = uiState.userHasSwiftSwim,
                    onClick = { onSwiftSwimToggle(!uiState.userHasSwiftSwim) },
                    label = { Text("🌧️ Swift Swim / Weather (2x)", fontSize = 11.sp) }
                )

                // Tailwind
                FilterChip(
                    selected = uiState.userHasTailwind,
                    onClick = { onTailwindToggle(!uiState.userHasTailwind) },
                    label = { Text("💨 Tailwind (2x)", fontSize = 11.sp) }
                )

                // Paralyzed
                FilterChip(
                    selected = uiState.userIsParalyzed,
                    onClick = { onParalyzedToggle(!uiState.userIsParalyzed) },
                    label = { Text("⚡ Paralyzed (0.5x)", fontSize = 11.sp) }
                )

                OutlinedButton(
                    onClick = onOpenPicker,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Change Pokémon", fontSize = 11.sp)
                }
            }

            // Expandable EV / IV / Stage Controls
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Sliders & Stat Boosts" else "Adjust EVs (${uiState.userEv}), IVs (${uiState.userIv}) & Stat Stages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    // EV Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speed EV: ${uiState.userEv}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { onEvChange(252) }) { Text("252 (Max)", fontSize = 10.sp) }
                            TextButton(onClick = { onEvChange(0) }) { Text("0 (Min)", fontSize = 10.sp) }
                        }
                    }
                    Slider(
                        value = uiState.userEv.toFloat(),
                        onValueChange = { onEvChange(it.toInt()) },
                        valueRange = 0f..252f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // IV Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speed IV: ${uiState.userIv}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { onIvChange(31) }) { Text("31 (Max)", fontSize = 10.sp) }
                            TextButton(onClick = { onIvChange(0) }) { Text("0 (Trick Room)", fontSize = 10.sp) }
                        }
                    }
                    Slider(
                        value = uiState.userIv.toFloat(),
                        onValueChange = { onIvChange(it.toInt()) },
                        valueRange = 0f..31f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Stat Stage Boosts (-6 to +6)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speed Stage: ${if (uiState.userStatStage > 0) "+${uiState.userStatStage}" else "${uiState.userStatStage}"}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onStageChange(uiState.userStatStage - 1) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Remove, null, Modifier.size(16.dp))
                            }
                            Text("${uiState.userStatStage}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onStageChange(uiState.userStatStage + 1) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedLadderItemCard(
    rank: Int,
    entry: SpeedTierEntry,
    userSpeed: Int
) {
    val isUser = entry.isUserPokemon
    val speedDiff = entry.calculatedSpeed - userSpeed

    val cardBorder = if (isUser) {
        androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB300))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }

    val cardBackground = if (isUser) {
        Color(0xFFFFB300).copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            // Pokemon Sprite
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(entry.primaryType.seedColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(entry.spriteUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = entry.pokemonName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name + Spread Description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.capitalizedName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUser) FontWeight.Black else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFB300)
                        ) {
                            Text(
                                text = "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = entry.spreadDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Speed Stat & Differential Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.calculatedSpeed}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUser) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                )

                if (!isUser && userSpeed > 0) {
                    val (diffText, diffColor) = when {
                        speedDiff > 0 -> "+$speedDiff (Faster)" to Color(0xFFEF5350)
                        speedDiff == 0 -> "SPEED TIE" to Color(0xFFFFB300)
                        else -> "$speedDiff (Outsped)" to Color(0xFF4CAF50)
                    }

                    Text(
                        text = diffText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }
            }
        }
    }
}
