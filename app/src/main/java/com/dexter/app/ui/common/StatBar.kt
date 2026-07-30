package com.dexter.app.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@Composable
fun StatBar(
    label: String,
    value: Int,
    maxStat: Int = 255,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateStart = true }

    val progress by animateFloatAsState(
        targetValue = if (animateStart) (value.toFloat() / maxStat).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "stat_progress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Micro),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(Dimens.Major * 2.2f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toString().padStart(3, ' '),
            modifier = Modifier.width(Dimens.Major * 1.3f),
            style = StatNumberStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
        Box(
            modifier = Modifier
                .padding(start = Dimens.Tight)
                .weight(1f)
                .height(Dimens.Tight + Dimens.Micro / 2)
                .clip(RoundedCornerShape(Dimens.Micro))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(Dimens.Micro))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun PokemonStatsSection(
    stats: PokemonStats,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Micro)
    ) {
        Text(
            text = "BASE STATS",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Dimens.Tight)
        )

        StatBar(label = "HP", value = stats.hp, barColor = accentColor)
        StatBar(label = "ATK", value = stats.attack, barColor = accentColor)
        StatBar(label = "DEF", value = stats.defense, barColor = accentColor)
        StatBar(label = "SP.ATK", value = stats.spAttack, barColor = accentColor)
        StatBar(label = "SP.DEF", value = stats.spDefense, barColor = accentColor)
        StatBar(label = "SPEED", value = stats.speed, barColor = accentColor)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.Tight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stats.total.toString(),
                style = StatNumberStyle.copy(fontFamily = FontFamily.Monospace),
                color = accentColor
            )
        }
    }
}
