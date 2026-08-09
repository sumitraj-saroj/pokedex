package com.dexter.app.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import com.dexter.app.ui.common.GlassmorphicTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onSelectOption: (Int) -> Unit,
    onPlayCry: () -> Unit = {},
    onRestartGame: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            com.dexter.app.ui.common.GlassmorphicTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Who's That Pokémon?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading || uiState.targetPokemon == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparing Pokémon Quiz...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                return@Surface
            }

            val target = uiState.targetPokemon

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenHeight = maxHeight
                val needsScroll = screenHeight < 620.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (needsScroll) Modifier.verticalScroll(rememberScrollState())
                            else Modifier
                        )
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Header stats bar: Lives, Score, Streak
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lives
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { index ->
                            val hasLife = index < uiState.lives
                            Icon(
                                imageVector = if (hasLife) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Life",
                                tint = if (hasLife) Color(0xFFE53935) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                    }

                    // Streak pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 Streak: ${uiState.currentStreak}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${uiState.sessionScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Reserved Fixed-Height Feedback Banner (32.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.isAnswered,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "It's ${target.capitalizedName}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Pokémon Image Container Card (Expands dynamically to fill available middle space)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUri = target.officialArtworkUrl ?: target.spriteUrl

                        androidx.compose.runtime.key(target.id) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .crossfade(false)
                                    .build(),
                                contentDescription = "Quiz Sprite",
                                colorFilter = if (uiState.isAnswered) null else ColorFilter.tint(Color.Black, BlendMode.SrcIn),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                            )
                        }

                        // Top-Right Play Cry Button
                        IconButton(
                            onClick = onPlayCry,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Cry Audio",
                                tint = if (uiState.isPlayingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bottom Live Soundwave Visualizer Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            AudioWaveformVisualizer(
                                isPlaying = uiState.isPlayingAudio,
                                height = 32.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Multiple Choice Options (Positioned cleanly above bottom bar)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.options.forEach { option ->
                        val isSelected = uiState.selectedOptionId == option.id
                        val isCorrectTarget = option.id == target.id

                        val backgroundColor by animateColorAsState(
                            targetValue = when {
                                uiState.isAnswered && isCorrectTarget -> Color(0xFF2E7D32) // Green
                                uiState.isAnswered && isSelected -> Color(0xFFC62828) // Red
                                else -> MaterialTheme.colorScheme.surfaceContainer
                            },
                            animationSpec = tween(durationMillis = 300),
                            label = "OptionBg"
                        )

                        val textColor = if (uiState.isAnswered && (isCorrectTarget || isSelected)) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable(enabled = !uiState.isAnswered) {
                                    onSelectOption(option.id)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = backgroundColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.capitalizedName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Game Over Dialog
        if (uiState.isGameOver) {
            val haptics = com.dexter.app.ui.common.rememberHapticUtils()
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Game Over!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("You ran out of lives!")
                        Text("Final Score: ${uiState.sessionScore}", fontWeight = FontWeight.Bold)
                        Text("Best Streak: ${uiState.bestStreak}")
                        Text("Correct Answers: ${uiState.correctCount}")
                        Text("XP Earned: +${uiState.totalXpEarned} XP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptics.lightTick()
                            onRestartGame()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Play Again")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Again")
                    }
                }
            )
        }
    }
}
}

