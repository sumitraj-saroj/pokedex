package com.dexter.app.ui.battle.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoveSelectorCard(
    selectedMove: BattleMove,
    attackerLearnedMoves: List<PokemonMove>,
    isCritical: Boolean,
    onSelectMove: (BattleMove) -> Unit,
    onCriticalToggle: (Boolean) -> Unit,
    onCustomPowerChange: (Int) -> Unit,
    onCustomTypeChange: (PokemonType) -> Unit,
    onCustomCategoryChange: (MoveCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCustomizing by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val moveColor = selectedMove.type.seedColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(moveColor.copy(alpha = 0.6f), moveColor.copy(alpha = 0.15f))
                ),
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
                .padding(Dimens.ScreenEdgePadding)
        ) {
            // Header Row: Active Move & Critical Hit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeChip(type = selectedMove.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedMove.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilterChip(
                    selected = isCritical,
                    onClick = { onCriticalToggle(!isCritical) },
                    label = { Text("⚡ Crit", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Bolt, null, Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFFFFC107)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Move Stats: Base Power, Accuracy, Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Power: ${selectedMove.basePower}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Acc: ${selectedMove.accuracy}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (selectedMove.category) {
                        MoveCategory.PHYSICAL -> Color(0xFFEF5350).copy(alpha = 0.2f)
                        MoveCategory.SPECIAL -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                        MoveCategory.STATUS -> Color(0xFF78909C).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = selectedMove.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedMove.category) {
                            MoveCategory.PHYSICAL -> Color(0xFFEF5350)
                            MoveCategory.SPECIAL -> Color(0xFF42A5F5)
                            MoveCategory.STATUS -> Color(0xFF78909C)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (selectedMove.hitCount > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF9C27B0).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${selectedMove.hitCount} Hits",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Tight))

            // Attacker's Learned Moves (Quick Select Chips)
            if (attackerLearnedMoves.isNotEmpty()) {
                Text(
                    text = "Learned Moves (Quick Pick):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    attackerLearnedMoves.take(8).forEach { move ->
                        val isCurrent = selectedMove.name.equals(move.moveName, ignoreCase = true)
                        val detail = move.detail
                        val moveType = detail?.type ?: PokemonType.NORMAL
                        val power = detail?.power ?: 40
                        val category = when (detail?.damageClass) {
                            "special" -> MoveCategory.SPECIAL
                            "status" -> MoveCategory.STATUS
                            else -> MoveCategory.PHYSICAL
                        }

                        FilterChip(
                            selected = isCurrent,
                            onClick = {
                                onSelectMove(
                                    BattleMove(
                                        name = move.moveName,
                                        displayName = move.displayName,
                                        type = moveType,
                                        category = category,
                                        basePower = power
                                    )
                                )
                            },
                            label = { Text("${move.displayName} ($power)", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Popular Competitive Moves Quick Picks
            Text(
                text = "Popular Meta Moves:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val popularShortlist = listOf(
                    "Earthquake", "Close Combat", "Moonblast", "Shadow Ball",
                    "Draco Meteor", "Dragon Darts", "Flare Blitz", "Hydro Pump",
                    "Thunderbolt", "Ice Beam", "Brave Bird", "Knock Off"
                )

                BattleMove.POPULAR_COMPETITIVE_MOVES.filter { popularShortlist.contains(it.displayName) }.forEach { pMove ->
                    val isCurrent = selectedMove.name == pMove.name
                    FilterChip(
                        selected = isCurrent,
                        onClick = { onSelectMove(pMove) },
                        label = { Text("${pMove.displayName} (${pMove.basePower})", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Custom Move Settings Toggle
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCustomizing = !isCustomizing }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCustomizing) "Hide Custom Move Editor" else "Customize Base Power, Type & Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isCustomizing) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isCustomizing) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    // Power Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Base Power: ${selectedMove.basePower}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { onCustomPowerChange(60) }) { Text("60", fontSize = 11.sp) }
                            TextButton(onClick = { onCustomPowerChange(80) }) { Text("80", fontSize = 11.sp) }
                            TextButton(onClick = { onCustomPowerChange(100) }) { Text("100", fontSize = 11.sp) }
                            TextButton(onClick = { onCustomPowerChange(120) }) { Text("120", fontSize = 11.sp) }
                        }
                    }
                    Slider(
                        value = selectedMove.basePower.toFloat(),
                        onValueChange = { onCustomPowerChange(it.toInt()) },
                        valueRange = 1f..250f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type & Category Dropdowns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Type dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = true,
                                onClick = { showTypeDropdown = true },
                                label = { Text("Type: ${selectedMove.type.capitalizedName}", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false }
                            ) {
                                PokemonType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.capitalizedName) },
                                        onClick = {
                                            onCustomTypeChange(type)
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Category dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = true,
                                onClick = { showCategoryDropdown = true },
                                label = { Text("Class: ${selectedMove.category.displayName}", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                MoveCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName) },
                                        onClick = {
                                            onCustomCategoryChange(cat)
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
