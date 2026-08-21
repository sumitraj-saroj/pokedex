package com.dexter.app.ui.battle.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.battle.model.PokeBallType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BallSelectorCard(
    selectedBall: PokeBallType,
    turnNumber: Int,
    isNightOrCave: Boolean,
    isWaterEncounter: Boolean,
    isAlreadyInPokedex: Boolean,
    playerPokemonLevel: Int,
    onBallSelected: (PokeBallType) -> Unit,
    onTurnChange: (Int) -> Unit,
    onNightOrCaveToggle: (Boolean) -> Unit,
    onWaterEncounterToggle: (Boolean) -> Unit,
    onAlreadyInPokedexToggle: (Boolean) -> Unit,
    onPlayerLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    val quickBalls = listOf(
        PokeBallType.QUICK_BALL,
        PokeBallType.ULTRA_BALL,
        PokeBallType.DUSK_BALL,
        PokeBallType.NET_BALL,
        PokeBallType.TIMER_BALL,
        PokeBallType.REPEAT_BALL,
        PokeBallType.GREAT_BALL,
        PokeBallType.POKE_BALL,
        PokeBallType.MASTER_BALL
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Poké Ball & Battle Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // More Balls Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { showDropdown = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("All Balls (${PokeBallType.entries.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        PokeBallType.entries.forEach { ball ->
                            DropdownMenuItem(
                                text = { Text("${ball.emoji} ${ball.displayName} (${ball.description})") },
                                onClick = {
                                    onBallSelected(ball)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Ball Selection Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickBalls.forEach { ball ->
                    val isSelected = selectedBall == ball
                    FilterChip(
                        selected = isSelected,
                        onClick = { onBallSelected(ball) },
                        label = {
                            Text(
                                text = "${ball.emoji} ${ball.displayName}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ball description banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${selectedBall.emoji} ${selectedBall.displayName}: ${selectedBall.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Situational Conditions
            Text(
                text = "Environmental & Battle Factors:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Night / Cave Toggle
                FilterChip(
                    selected = isNightOrCave,
                    onClick = { onNightOrCaveToggle(!isNightOrCave) },
                    label = { Text("🌑 Night / Cave (Dusk Ball 3x)", fontSize = 11.sp) }
                )

                // Water / Surfing Toggle
                FilterChip(
                    selected = isWaterEncounter,
                    onClick = { onWaterEncounterToggle(!isWaterEncounter) },
                    label = { Text("🌊 Water / Surfing (Dive 3.5x)", fontSize = 11.sp) }
                )

                // Already Caught in Dex Toggle
                FilterChip(
                    selected = isAlreadyInPokedex,
                    onClick = { onAlreadyInPokedexToggle(!isAlreadyInPokedex) },
                    label = { Text("🔁 Already Caught (Repeat 3.5x)", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Turn Count Stepper (Quick Ball & Timer Ball)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Battle Turn: $turnNumber ${if (turnNumber == 1) "(Quick Ball Active ⚡)" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (turnNumber == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (turnNumber > 1) onTurnChange(turnNumber - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, Modifier.size(16.dp))
                    }
                    Text(
                        text = "$turnNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onTurnChange(turnNumber + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    }
                }
            }

            // Player Pokemon Level (for Level Ball)
            if (selectedBall == PokeBallType.LEVEL_BALL) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Your Active Pokémon Level: $playerPokemonLevel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = playerPokemonLevel.toFloat(),
                    onValueChange = { onPlayerLevelChange(it.toInt()) },
                    valueRange = 1f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
