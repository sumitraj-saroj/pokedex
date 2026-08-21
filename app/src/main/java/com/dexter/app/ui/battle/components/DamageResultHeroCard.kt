package com.dexter.app.ui.battle.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.battle.model.DamageRollResult
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DamageResultHeroCard(
    result: DamageRollResult?,
    onCopyResult: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (result == null) return

    val context = LocalContext.current
    var showAllRolls by remember { mutableStateOf(false) }

    // Color gradient based on KO certainty
    val koGradient = when {
        result.ohkoChance >= 100.0 -> listOf(Color(0xFF00C853), Color(0xFF64DD17)) // Emerald
        result.ohkoChance > 0.0 -> listOf(Color(0xFF00B0FF), Color(0xFF00E5FF)) // Cyan
        result.twoHkoChance >= 100.0 -> listOf(Color(0xFFFF9100), Color(0xFFFFD600)) // Amber/Gold
        result.twoHkoChance > 0.0 -> listOf(Color(0xFFFF6D00), Color(0xFFFFAB00)) // Orange
        else -> listOf(Color(0xFF78909C), Color(0xFFB0BEC5)) // Slate
    }

    val animatedMinPercent by animateFloatAsState(
        targetValue = (result.minPercent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "minPct"
    )

    val animatedMaxPercent by animateFloatAsState(
        targetValue = (result.maxPercent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "maxPct"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.linearGradient(koGradient),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: KO Probability Badge + Copy Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = koGradient.first().copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, koGradient.first())
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "💥",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = result.koChanceText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = koGradient.first()
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Pokemon Damage Calc", result.summaryFormulaText)
                        clipboard.setPrimaryClip(clip)
                        onCopyResult()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy result",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Damage Percentage & Raw Damage Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${"%.1f".format(result.minPercent)}% - ${"%.1f".format(result.maxPercent)}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Defender HP: ${result.minDamage} - ${result.maxDamage} / ${result.defenderMaxHp} HP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Avg: ${"%.1f".format(result.avgPercent)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Health Bar (Defender HP Bar with Damage Chunk Overlay)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Defender Base HP Background (Green to Red gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                            )
                        )
                )

                // Damage Chunk Overlay (Flashing Amber / Red from right side)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedMaxPercent)
                        .height(18.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE53935).copy(alpha = 0.85f), Color(0xFFFF5722))
                            )
                        )
                )

                // Guaranteed Min Damage Chunk Overlay (Solid Red)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedMinPercent)
                        .height(18.dp)
                        .align(Alignment.CenterStart)
                        .background(Color(0xFFD32F2F))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multipliers & Factor Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type Effectiveness Badge
                if (result.typeMultiplier != 1.0) {
                    val (typeText, typeColor) = when {
                        result.typeMultiplier >= 4.0 -> "4x Super Effective" to Color(0xFFD32F2F)
                        result.typeMultiplier >= 2.0 -> "2x Super Effective" to Color(0xFFE64A19)
                        result.typeMultiplier == 0.5 -> "0.5x Resisted" to Color(0xFF1976D2)
                        result.typeMultiplier == 0.25 -> "0.25x Strongly Resisted" to Color(0xFF0D47A1)
                        result.typeMultiplier == 0.0 -> "0x Immune" to Color(0xFF757575)
                        else -> "${result.typeMultiplier}x" to MaterialTheme.colorScheme.primary
                    }
                    FactorPill(text = typeText, color = typeColor)
                }

                // STAB Badge
                if (result.isStab) {
                    FactorPill(text = "STAB (1.5x)", color = Color(0xFF9C27B0))
                }

                // Critical Hit Badge
                if (result.isCritical) {
                    FactorPill(text = "⚡ Critical Hit (1.5x)", color = Color(0xFFFFC107))
                }

                // Hazard Badges
                if (result.stealthRockDamage > 0) {
                    FactorPill(text = "Stealth Rock (-${result.stealthRockDamage} HP)", color = Color(0xFF795548))
                }
                if (result.spikesDamage > 0) {
                    FactorPill(text = "Spikes (-${result.spikesDamage} HP)", color = Color(0xFF607D8B))
                }

                // Leftovers Healing
                if (result.leftoversHeal > 0) {
                    FactorPill(text = "Leftovers (+${result.leftoversHeal} HP)", color = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle 16 Rolls Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showAllRolls = !showAllRolls }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllRolls) "Hide 16 Damage Rolls" else "Show All 16 Rolls (85% to 100%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (showAllRolls) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showAllRolls) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            result.rolls.forEachIndexed { index, roll ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (roll >= result.defenderMaxHp) Color(0xFFE53935).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (roll >= result.defenderMaxHp) Color(0xFFE53935) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = "$roll",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = if (roll >= result.defenderMaxHp) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Full Showdown-Style Formula String
            Text(
                text = result.summaryFormulaText,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FactorPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
