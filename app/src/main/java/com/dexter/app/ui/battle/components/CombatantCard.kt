package com.dexter.app.ui.battle.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.battle.engine.Combatant
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombatantCard(
    title: String,
    combatant: Combatant,
    isAttacker: Boolean,
    onSelectPokemonClick: () -> Unit,
    onLevelChange: (Int) -> Unit,
    onNatureChange: (PokemonNature) -> Unit,
    onItemChange: (BattleItem) -> Unit,
    onAbilityChange: (BattleAbility) -> Unit,
    onStageChange: (StatType, Int) -> Unit,
    onEvChange: (StatType, Int) -> Unit,
    onIvChange: (StatType, Int) -> Unit,
    onTeraTypeChange: (PokemonType?) -> Unit,
    onBurnChange: ((Boolean) -> Unit)? = null,
    onHpPercentChange: ((Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showNatureDropdown by remember { mutableStateOf(false) }
    var showItemDropdown by remember { mutableStateOf(false) }
    var showAbilityDropdown by remember { mutableStateOf(false) }
    var showTeraDropdown by remember { mutableStateOf(false) }

    val pokemon = combatant.pokemon
    val primaryColor = pokemon.primaryType.seedColor
    val cardBorderBrush = Brush.horizontalGradient(
        listOf(
            primaryColor.copy(alpha = 0.6f),
            primaryColor.copy(alpha = 0.15f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenEdgePadding)
        ) {
            // Header Row: Role Title, Level Chips, and Expand Toggle
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
                            .background(if (isAttacker) Color(0xFFEF5350) else Color(0xFF42A5F5))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Level 50 / 100 selector
                    FilterChip(
                        selected = combatant.level == 50,
                        onClick = { onLevelChange(50) },
                        label = { Text("Lv. 50", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = combatant.level == 100,
                        onClick = { onLevelChange(100) },
                        label = { Text("Lv. 100", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Tight))

            // Main Info Row: Sprite + Name + Type Chips + Change Button
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
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TypeChip(type = pokemon.primaryType)
                        pokemon.secondaryType?.let { TypeChip(type = it) }
                        combatant.teraType?.let { tera ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tera.seedColor.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tera.seedColor)
                            ) {
                                Text(
                                    text = "Tera ${tera.capitalizedName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tera.seedColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSelectPokemonClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Change", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Tight))

            // Quick Setup Chips: Nature, Item, Ability, Tera
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nature Chip
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { showNatureDropdown = true },
                        label = { Text("${combatant.nature.displayName} Nature", fontSize = 11.sp) },
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

                // Item Chip
                Box {
                    FilterChip(
                        selected = combatant.item != BattleItem.NONE,
                        onClick = { showItemDropdown = true },
                        label = { Text(if (combatant.item == BattleItem.NONE) "No Item" else combatant.item.displayName, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                    )
                    DropdownMenu(
                        expanded = showItemDropdown,
                        onDismissRequest = { showItemDropdown = false }
                    ) {
                        BattleItem.entries.forEach { itm ->
                            DropdownMenuItem(
                                text = { Text(itm.displayName) },
                                onClick = {
                                    onItemChange(itm)
                                    showItemDropdown = false
                                }
                            )
                        }
                    }
                }

                // Ability Chip
                Box {
                    FilterChip(
                        selected = combatant.ability != BattleAbility.NONE,
                        onClick = { showAbilityDropdown = true },
                        label = { Text(if (combatant.ability == BattleAbility.NONE) "No Ability" else combatant.ability.displayName, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                    )
                    DropdownMenu(
                        expanded = showAbilityDropdown,
                        onDismissRequest = { showAbilityDropdown = false }
                    ) {
                        BattleAbility.entries.filter { if (isAttacker) it.isAttackerAbility else it.isDefenderAbility }.forEach { ab ->
                            DropdownMenuItem(
                                text = { Text(ab.displayName) },
                                onClick = {
                                    onAbilityChange(ab)
                                    showAbilityDropdown = false
                                }
                            )
                        }
                    }
                }

                // Tera Type Chip
                Box {
                    FilterChip(
                        selected = combatant.teraType != null,
                        onClick = { showTeraDropdown = true },
                        label = { Text(combatant.teraType?.let { "Tera ${it.capitalizedName}" } ?: "Tera: None", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) }
                    )
                    DropdownMenu(
                        expanded = showTeraDropdown,
                        onDismissRequest = { showTeraDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Original Types)") },
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

                // Burn Toggle for Attacker
                if (isAttacker && onBurnChange != null) {
                    FilterChip(
                        selected = combatant.isBurned,
                        onClick = { onBurnChange(!combatant.isBurned) },
                        label = { Text("🔥 Burned", fontSize = 11.sp) }
                    )
                }
            }

            // Stat Boost Stages & Advanced Sliders Toggle
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
                    text = if (isExpanded) "Hide Advanced Stats & Boosts" else "Show Stat Boosts & EVs/IVs",
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
                    // Stat Stage Boosters (-6 to +6)
                    Text(
                        text = "Stat Stage Boosts (-6 to +6):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val relevantStats = if (isAttacker) {
                            listOf(StatType.ATTACK, StatType.SP_ATTACK, StatType.SPEED)
                        } else {
                            listOf(StatType.DEFENSE, StatType.SP_DEFENSE, StatType.HP)
                        }

                        relevantStats.forEach { stat ->
                            val stage = when (stat) {
                                StatType.ATTACK -> combatant.statStages.attack
                                StatType.DEFENSE -> combatant.statStages.defense
                                StatType.SP_ATTACK -> combatant.statStages.spAttack
                                StatType.SP_DEFENSE -> combatant.statStages.spDefense
                                StatType.SPEED -> combatant.statStages.speed
                                StatType.HP -> 0
                            }

                            if (stat != StatType.HP) {
                                StagePill(
                                    stat = stat,
                                    stage = stage,
                                    onIncrement = { onStageChange(stat, 1) },
                                    onDecrement = { onStageChange(stat, -1) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // EV & IV Sliders for key offensive/defensive stats
                    val statToEdit = if (isAttacker) StatType.ATTACK else StatType.DEFENSE
                    val spStatToEdit = if (isAttacker) StatType.SP_ATTACK else StatType.SP_DEFENSE

                    CompactEvIvRow(
                        statType = statToEdit,
                        ev = combatant.evs.getStat(statToEdit),
                        iv = combatant.ivs.getStat(statToEdit),
                        onEvChange = { onEvChange(statToEdit, it) },
                        onIvChange = { onIvChange(statToEdit, it) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    CompactEvIvRow(
                        statType = spStatToEdit,
                        ev = combatant.evs.getStat(spStatToEdit),
                        iv = combatant.ivs.getStat(spStatToEdit),
                        onEvChange = { onEvChange(spStatToEdit, it) },
                        onIvChange = { onIvChange(spStatToEdit, it) }
                    )

                    if (!isAttacker) {
                        Spacer(modifier = Modifier.height(6.dp))
                        CompactEvIvRow(
                            statType = StatType.HP,
                            ev = combatant.evs.hp,
                            iv = combatant.ivs.hp,
                            onEvChange = { onEvChange(StatType.HP, it) },
                            onIvChange = { onIvChange(StatType.HP, it) }
                        )

                        // Defender HP % slider
                        if (onHpPercentChange != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current HP: ${combatant.currentHpPercent.toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    TextButton(onClick = { onHpPercentChange(100.0) }) { Text("100%", fontSize = 10.sp) }
                                    TextButton(onClick = { onHpPercentChange(50.0) }) { Text("50%", fontSize = 10.sp) }
                                    TextButton(onClick = { onHpPercentChange(25.0) }) { Text("25%", fontSize = 10.sp) }
                                }
                            }
                            Slider(
                                value = combatant.currentHpPercent.toFloat(),
                                onValueChange = { onHpPercentChange(it.toDouble()) },
                                valueRange = 1f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StagePill(
    stat: StatType,
    stage: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Remove, null, Modifier.size(14.dp))
            }
            Text(
                text = "${stat.shortName} ${if (stage > 0) "+$stage" else "$stage"}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    stage > 0 -> Color(0xFF4CAF50)
                    stage < 0 -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun CompactEvIvRow(
    statType: StatType,
    ev: Int,
    iv: Int,
    onEvChange: (Int) -> Unit,
    onIvChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statType.shortName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )

        // EV controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("EV:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$ev",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            FilterChip(
                selected = ev == 252,
                onClick = { onEvChange(if (ev == 252) 0 else 252) },
                label = { Text("252", fontSize = 10.sp) },
                modifier = Modifier.height(24.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            FilterChip(
                selected = ev == 0,
                onClick = { onEvChange(0) },
                label = { Text("0", fontSize = 10.sp) },
                modifier = Modifier.height(24.dp)
            )
        }

        // IV controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("IV:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$iv",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(20.dp)
            )
            FilterChip(
                selected = iv == 31,
                onClick = { onIvChange(if (iv == 31) 0 else 31) },
                label = { Text("31", fontSize = 10.sp) },
                modifier = Modifier.height(24.dp)
            )
        }
    }
}
