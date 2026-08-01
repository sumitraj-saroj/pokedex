package com.dexter.app.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.TypeMatchup
import com.dexter.app.domain.model.TypeMatchupEngine
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

private data class MatchupGroups(
    val superWeak: List<TypeMatchup>,
    val weak: List<TypeMatchup>,
    val resist: List<TypeMatchup>,
    val superResist: List<TypeMatchup>,
    val immune: List<TypeMatchup>,
    val neutral: List<TypeMatchup>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeMatchupSection(
    primaryType: PokemonType,
    secondaryType: PokemonType?,
    modifier: Modifier = Modifier
) {
    var showNeutral by remember { mutableStateOf(false) }
    val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()

    val (superWeak, weak, resist, superResist, immune, neutral) = remember(primaryType, secondaryType) {
        val all = TypeMatchupEngine.calculateDefensiveMatchups(primaryType, secondaryType)
        MatchupGroups(
            superWeak = all.filter { it.multiplier >= 4.0 },
            weak = all.filter { it.multiplier == 2.0 },
            resist = all.filter { it.multiplier == 0.5 },
            superResist = all.filter { it.multiplier <= 0.25 && it.multiplier > 0.0 },
            immune = all.filter { it.multiplier == 0.0 },
            neutral = all.filter { it.multiplier == 1.0 }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Default),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Default)
        ) {
            Text(
                text = "DEFENSIVE TYPE MATCHUPS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.Compact)
            )

            MatchupCategoryGroup(title = "4X DAMAGE (CRITICAL WEAKNESS)", matchups = superWeak, badgeColor = Color(0xFFD32F2F))
            MatchupCategoryGroup(title = "2X DAMAGE (WEAKNESS)", matchups = weak, badgeColor = Color(0xFFE53935))
            MatchupCategoryGroup(title = "0.5X DAMAGE (RESISTANT)", matchups = resist, badgeColor = Color(0xFF43A047))
            MatchupCategoryGroup(title = "0.25X DAMAGE (SUPER RESISTANT)", matchups = superResist, badgeColor = Color(0xFF2E7D32))
            MatchupCategoryGroup(title = "0X DAMAGE (IMMUNE)", matchups = immune, badgeColor = Color(0xFF1E88E5))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.Tight))
                    .clickable {
                        hapticUtils.lightTick()
                        showNeutral = !showNeutral
                    }
                    .padding(vertical = Dimens.Tight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEUTRAL DAMAGE (1x)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (showNeutral) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle neutral matchups",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showNeutral) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Tight),
                    modifier = Modifier.padding(top = Dimens.Micro)
                ) {
                    neutral.forEach { matchup ->
                        TypeMatchupChip(matchup = matchup, badgeColor = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupCategoryGroup(
    title: String,
    matchups: List<TypeMatchup>,
    badgeColor: Color
) {
    Column(modifier = Modifier.padding(bottom = Dimens.Compact)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.Micro)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Tight),
            verticalArrangement = Arrangement.spacedBy(Dimens.Tight)
        ) {
            matchups.forEach { matchup ->
                TypeMatchupChip(matchup = matchup, badgeColor = badgeColor)
            }
        }
    }
}

@Composable
private fun TypeMatchupChip(
    matchup: TypeMatchup,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.Compact))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = Dimens.Tight, vertical = Dimens.Micro),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypeChip(type = matchup.type, isCompact = true)
        Spacer(modifier = Modifier.width(Dimens.Micro))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.Micro))
                .background(badgeColor)
                .padding(horizontal = Dimens.Micro, vertical = Dimens.Micro / 2)
        ) {
            val formatStr = when (matchup.multiplier) {
                4.0 -> "4x"
                2.0 -> "2x"
                0.5 -> "½x"
                0.25 -> "¼x"
                0.0 -> "0x"
                else -> "1x"
            }
            Text(
                text = formatStr,
                style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = Color.White
            )
        }
    }
}
