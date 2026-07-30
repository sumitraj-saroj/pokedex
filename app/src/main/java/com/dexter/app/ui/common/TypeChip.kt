package com.dexter.app.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.theme.Dimens

/**
 * Returns clean high-contrast text color (White or Black) based on luminance of background seed color.
 */
fun Color.contentColorForSeed(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.65f) Color(0xFF1C1B1F) else Color.White
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TypeChip(
    type: PokemonType,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isSelected: Boolean = false
) {
    val textColor = type.seedColor.contentColorForSeed()
    
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) Dimens.Major else Dimens.Compact,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "type_chip_shape"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = if (isCompact) Dimens.Micro * 6 else Dimens.MinTouchTarget / 1.7f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(type.seedColor)
            .padding(
                horizontal = if (isCompact) Dimens.Tight else Dimens.Compact,
                vertical = if (isCompact) Dimens.Micro else Dimens.Tight / 2
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.typeName.uppercase(),
            color = textColor,
            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
