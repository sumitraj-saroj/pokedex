package com.dexter.app.ui.battle.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.battle.model.BattleField
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FieldConditionsCard(
    field: BattleField,
    onWeatherChange: (BattleWeather) -> Unit,
    onTerrainChange: (BattleTerrain) -> Unit,
    onDoublesToggle: (Boolean) -> Unit,
    onReflectToggle: () -> Unit,
    onLightScreenToggle: () -> Unit,
    onAuroraVeilToggle: () -> Unit,
    onStealthRockToggle: () -> Unit,
    onSpikesLayersChange: (Int) -> Unit,
    onHelpingHandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showWeatherDropdown by remember { mutableStateOf(false) }
    var showTerrainDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Field, Weather & Hazards",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick indicator of active conditions
                    val activeCount = listOf(
                        field.weather != BattleWeather.CLEAR,
                        field.terrain != BattleTerrain.NONE,
                        field.isDoubles,
                        field.defenderReflect,
                        field.defenderLightScreen,
                        field.defenderAuroraVeil,
                        field.defenderStealthRock,
                        field.defenderSpikesLayers > 0
                    ).count { it }

                    if (activeCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "$activeCount active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    // Weather & Terrain Dropdowns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Weather Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = field.weather != BattleWeather.CLEAR,
                                onClick = { showWeatherDropdown = true },
                                label = { Text("🌦️ ${field.weather.displayName}", fontSize = 11.sp, maxLines = 1) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showWeatherDropdown,
                                onDismissRequest = { showWeatherDropdown = false }
                            ) {
                                BattleWeather.entries.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text(w.displayName) },
                                        onClick = {
                                            onWeatherChange(w)
                                            showWeatherDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Terrain Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = field.terrain != BattleTerrain.NONE,
                                onClick = { showTerrainDropdown = true },
                                label = { Text("⚡ ${field.terrain.displayName}", fontSize = 11.sp, maxLines = 1) },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showTerrainDropdown,
                                onDismissRequest = { showTerrainDropdown = false }
                            ) {
                                BattleTerrain.entries.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.displayName) },
                                        onClick = {
                                            onTerrainChange(t)
                                            showTerrainDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battle Format & Defender Screens
                    Text(
                        text = "Screens & Battle Format:",
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
                        FilterChip(
                            selected = field.isDoubles,
                            onClick = { onDoublesToggle(!field.isDoubles) },
                            label = { Text(if (field.isDoubles) "⚔️ Doubles (0.75x Spread)" else "🗡️ Singles", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderReflect,
                            onClick = onReflectToggle,
                            label = { Text("🛡️ Reflect (-50% Phys)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderLightScreen,
                            onClick = onLightScreenToggle,
                            label = { Text("✨ Light Screen (-50% Spec)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderAuroraVeil,
                            onClick = onAuroraVeilToggle,
                            label = { Text("❄️ Aurora Veil (-50% All)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.attackerHelpingHand,
                            onClick = onHelpingHandToggle,
                            label = { Text("🤝 Helping Hand (+50%)", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Entry Hazards on Defender
                    Text(
                        text = "Entry Hazards on Defender:",
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
                        FilterChip(
                            selected = field.defenderStealthRock,
                            onClick = onStealthRockToggle,
                            label = { Text("🪨 Stealth Rock", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderSpikesLayers == 1,
                            onClick = { onSpikesLayersChange(if (field.defenderSpikesLayers == 1) 0 else 1) },
                            label = { Text("📍 1 Spikes (12.5%)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderSpikesLayers == 2,
                            onClick = { onSpikesLayersChange(if (field.defenderSpikesLayers == 2) 0 else 2) },
                            label = { Text("📍 2 Spikes (16.7%)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = field.defenderSpikesLayers == 3,
                            onClick = { onSpikesLayersChange(if (field.defenderSpikesLayers == 3) 0 else 3) },
                            label = { Text("📍 3 Spikes (25%)", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}
