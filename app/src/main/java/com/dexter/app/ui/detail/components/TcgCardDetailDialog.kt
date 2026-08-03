package com.dexter.app.ui.detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.TcgCard
import com.dexter.app.ui.common.interactive3DCardEffect
import com.dexter.app.ui.common.rememberTiltSensorState
import com.dexter.app.ui.common.shimmerLoadingAnimation
import com.dexter.app.ui.theme.Dimens

@Composable
fun TcgCardDetailDialog(
    card: TcgCard,
    onDismissRequest: () -> Unit
) {
    val tiltState = rememberTiltSensorState()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(Dimens.Default),
            shape = RoundedCornerShape(Dimens.Default),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel3)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Default),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Gyro Interactive Preview",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.size(Dimens.Micro))

                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(Dimens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Card Preview",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                // Interactive 3D perspective container with sensor/touch-driven holographic foil sheen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.727f) // TCG Card Aspect Ratio ~ 600x825
                        .clip(RoundedCornerShape(Dimens.Default))
                        .interactive3DCardEffect(tiltState = tiltState, enableTouchDrag = true)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(card.highResImageUrl.ifBlank { card.lowResImageUrl })
                            .placeholderMemoryCacheKey(card.id)
                            .crossfade(300)
                            .diskCacheKey("${card.id}_high")
                            .build(),
                        contentDescription = "${card.name} High Resolution TCG Card",
                        contentScale = ContentScale.Fit,
                        loading = {
                            // Instant low-res image preview from memory cache while high-res PNG downloads
                            if (card.lowResImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(card.lowResImageUrl)
                                        .memoryCacheKey(card.id)
                                        .build(),
                                    contentDescription = "${card.name} Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmerLoadingAnimation()
                                )
                            }
                        },
                        error = {
                            // Fallback to low-res if high-res image fails
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.lowResImageUrl)
                                    .memoryCacheKey(card.id)
                                    .build(),
                                contentDescription = "${card.name} Fallback TCG Card",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(Dimens.Default))
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Default))

                // Details Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CARD ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = card.id,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (card.localId.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "LOCAL ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "#${card.localId}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
