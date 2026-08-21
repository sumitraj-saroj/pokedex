package com.dexter.app.ui.battle.statcalc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.CalculatedStats
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.battle.StatCalcUiState
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatCalculatorTab(
    uiState: StatCalcUiState,
    allPokemon: List<Pokemon>,
    onPokemonSelected: (Pokemon) -> Unit,
    onLevelChange: (Int) -> Unit,
    onNatureChange: (PokemonNature) -> Unit,
    onIvChange: (StatType, Int) -> Unit,
    onAllIvsChange: (Int) -> Unit,
    onEvChange: (StatType, Int) -> Unit,
    onEvPresetSelected: (StatType, StatType) -> Unit,
    onItemChange: (BattleItem) -> Unit,
    onTeraTypeChange: (PokemonType?) -> Unit,
    onCopySuccess: () -> Unit,
    onSendToDamageCalc: () -> Unit,
    onOpenPokemonPicker: () -> Unit,
    onClosePokemonPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pokemon = uiState.selectedPokemon ?: return
    val baseStats = pokemon.stats ?: com.dexter.app.domain.model.PokemonStats(100, 100, 100, 100, 100, 100)
    val calculated = uiState.calculatedStats

    var showNatureDropdown by remember { mutableStateOf(false) }
    var showItemDropdown by remember { mutableStateOf(false) }
    var showTeraDropdown by remember { mutableStateOf(false) }
    var hasCopiedRecently by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.copySuccessTrigger) {
        if (uiState.copySuccessTrigger > 0L) {
            hasCopiedRecently = true
            delay(2000)
            hasCopiedRecently = false
        }
    }

    val primaryColor = pokemon.primaryType.seedColor

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
        // 1. Pokémon Summary Header Card
        item(key = "pokemon_header") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.6f), primaryColor.copy(alpha = 0.15f))),
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pokemon.spriteUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = pokemon.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pokemon.capitalizedName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TypeChip(type = pokemon.primaryType)
                                pokemon.secondaryType?.let { TypeChip(type = it) }
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenPokemonPicker,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Change", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Level Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level: ${uiState.level}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = uiState.level == 50,
                                onClick = { onLevelChange(50) },
                                label = { Text("Lv. 50", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                            FilterChip(
                                selected = uiState.level == 100,
                                onClick = { onLevelChange(100) },
                                label = { Text("Lv. 100", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Slider(
                        value = uiState.level.toFloat(),
                        onValueChange = { onLevelChange(it.toInt()) },
                        valueRange = 1f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Nature, Item & Tera Dropdowns
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Nature Dropdown
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { showNatureDropdown = true },
                                label = { Text("${uiState.nature.displayName} Nature", fontSize = 11.sp) },
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

                        // Item Dropdown
                        Box {
                            FilterChip(
                                selected = uiState.heldItem != BattleItem.NONE,
                                onClick = { showItemDropdown = true },
                                label = { Text(if (uiState.heldItem == BattleItem.NONE) "No Item" else uiState.heldItem.displayName, fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                            )
                            DropdownMenu(
                                expanded = showItemDropdown,
                                onDismissRequest = { showItemDropdown = false }
                            ) {
                                BattleItem.entries.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.displayName) },
                                        onClick = {
                                            onItemChange(item)
                                            showItemDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tera Dropdown
                        Box {
                            FilterChip(
                                selected = uiState.teraType != null,
                                onClick = { showTeraDropdown = true },
                                label = { Text(uiState.teraType?.let { "Tera ${it.capitalizedName}" } ?: "Tera Type", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                            )
                            DropdownMenu(
                                expanded = showTeraDropdown,
                                onDismissRequest = { showTeraDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        onTeraTypeChange(null)
                                        showTeraDropdown = false
                                    }
                                )
                                PokemonType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.capitalizedName) },
                                        onClick = {
                                            onTeraTypeChange(type)
                                            showTeraDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. EV Budget Gauge Card
        item(key = "ev_gauge") {
            val totalEvs = uiState.evs.total
            val evFraction = (totalEvs / 508f).coerceIn(0f, 1f)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EV Investment Budget",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalEvs / 508 EVs (${508 - totalEvs} remaining)",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = if (totalEvs >= 508) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { evFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (totalEvs >= 508) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick EV Spread Presets
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { onEvPresetSelected(StatType.ATTACK, StatType.SPEED) },
                            label = { Text("Phys Sweeper (Atk/Spe)", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onEvPresetSelected(StatType.SP_ATTACK, StatType.SPEED) },
                            label = { Text("Spec Sweeper (SpA/Spe)", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onEvPresetSelected(StatType.HP, StatType.ATTACK) },
                            label = { Text("Bulky Atk (HP/Atk)", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onEvPresetSelected(StatType.HP, StatType.DEFENSE) },
                            label = { Text("Phys Tank (HP/Def)", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onEvPresetSelected(StatType.HP, StatType.SP_DEFENSE) },
                            label = { Text("Spec Tank (HP/SpD)", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }

        // 3. 6 Interactive Stat Sliders (HP, Atk, Def, SpA, SpD, Spe)
        item(key = "stat_sliders") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Individual Stats (IVs, EVs & Nature Multipliers)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val statList = listOf(
                        StatType.HP to baseStats.hp,
                        StatType.ATTACK to baseStats.attack,
                        StatType.DEFENSE to baseStats.defense,
                        StatType.SP_ATTACK to baseStats.spAttack,
                        StatType.SP_DEFENSE to baseStats.spDefense,
                        StatType.SPEED to baseStats.speed
                    )

                    statList.forEach { (statType, baseVal) ->
                        val calculatedVal = calculated?.getStat(statType) ?: baseVal
                        val evVal = uiState.evs.getStat(statType)
                        val ivVal = uiState.ivs.getStat(statType)
                        val natureMult = uiState.nature.getMultiplier(statType)

                        DetailedStatSliderRow(
                            statType = statType,
                            baseStat = baseVal,
                            calculatedStat = calculatedVal,
                            ev = evVal,
                            iv = ivVal,
                            natureMultiplier = natureMult,
                            onEvChange = { onEvChange(statType, it) },
                            onIvChange = { onIvChange(statType, it) }
                        )

                        if (statType != StatType.SPEED) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // 4. Quick IV Shortcuts
        item(key = "iv_shortcuts") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick IV Presets:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = uiState.ivs == StatSpread.ALL_31,
                        onClick = { onAllIvsChange(31) },
                        label = { Text("All 31 IVs", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = uiState.ivs.attack == 0,
                        onClick = { onIvChange(StatType.ATTACK, 0) },
                        label = { Text("0 Atk IV", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = uiState.ivs.speed == 0,
                        onClick = { onIvChange(StatType.SPEED, 0) },
                        label = { Text("0 Spe IV (Trick Room)", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }

        // 5. Actions: Copy Showdown Set & Send to Damage Calc
        item(key = "action_buttons") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Showdown Export", uiState.showdownText)
                        clipboard.setPrimaryClip(clip)
                        onCopySuccess()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasCopiedRecently) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (hasCopiedRecently) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(
                        imageVector = if (hasCopiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (hasCopiedRecently) "Copied!" else "Copy Showdown", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSendToDamageCalc,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("💥 Calc Damage", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Showdown Export Live Preview
        item(key = "showdown_preview") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Showdown Set Format:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.showdownText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Pokemon Picker
    if (uiState.isSelectingPokemon) {
        PokemonPickerBottomSheet(
            onDismissRequest = onClosePokemonPicker,
            onPokemonSelected = onPokemonSelected,
            pokemonList = allPokemon,
            title = "Choose Pokémon for Stat Calc"
        )
    }
}

@Composable
private fun DetailedStatSliderRow(
    statType: StatType,
    baseStat: Int,
    calculatedStat: Int,
    ev: Int,
    iv: Int,
    natureMultiplier: Double,
    onEvChange: (Int) -> Unit,
    onIvChange: (Int) -> Unit
) {
    val statColor = when (statType) {
        StatType.HP -> Color(0xFFFF5959)
        StatType.ATTACK -> Color(0xFFF5AC78)
        StatType.DEFENSE -> Color(0xFFFAE078)
        StatType.SP_ATTACK -> Color(0xFF9DB7F5)
        StatType.SP_DEFENSE -> Color(0xFFA7DB8D)
        StatType.SPEED -> Color(0xFFFA92B2)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Stat Label + Final In-Game Stat Big Display
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
                            .background(statColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statType.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (natureMultiplier > 1.0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("(+10%)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    } else if (natureMultiplier < 1.0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("(-10%)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Base: $baseStat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statColor)
                    ) {
                        Text(
                            text = "$calculatedStat",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // EV Slider & Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EV: $ev", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(
                        onClick = { onEvChange(252) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) { Text("252", fontSize = 10.sp) }
                    TextButton(
                        onClick = { onEvChange(4) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) { Text("4", fontSize = 10.sp) }
                    TextButton(
                        onClick = { onEvChange(0) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) { Text("0", fontSize = 10.sp) }
                }
            }
            Slider(
                value = ev.toFloat(),
                onValueChange = { onEvChange(it.toInt()) },
                valueRange = 0f..252f,
                steps = 62, // in increments of 4
                colors = SliderDefaults.colors(thumbColor = statColor, activeTrackColor = statColor),
                modifier = Modifier.fillMaxWidth().height(24.dp)
            )

            // IV Slider & Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("IV: $iv", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(
                        onClick = { onIvChange(31) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) { Text("31", fontSize = 10.sp) }
                    TextButton(
                        onClick = { onIvChange(0) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) { Text("0", fontSize = 10.sp) }
                }
            }
            Slider(
                value = iv.toFloat(),
                onValueChange = { onIvChange(it.toInt()) },
                valueRange = 0f..31f,
                colors = SliderDefaults.colors(thumbColor = Color.Gray, activeTrackColor = Color.Gray),
                modifier = Modifier.fillMaxWidth().height(24.dp)
            )
        }
    }
}
