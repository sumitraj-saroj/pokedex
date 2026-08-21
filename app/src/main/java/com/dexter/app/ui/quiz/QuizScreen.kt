package com.dexter.app.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.ui.common.GlassmorphicTopAppBar
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onSelectOption: (Int) -> Unit,
    onPlayCry: () -> Unit = {},
    onRestartGame: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleGeneration: (Int) -> Unit = {},
    onSelectAllGenerations: () -> Unit = {},
    onSelectGenerationPreset: (Set<Int>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticUtils()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            GlassmorphicTopAppBar(
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
                },
                actions = {
                    // Top App Bar Generation Filter Action Button
                    val isAllGens = uiState.isAllGenerationsSelected
                    val filterText = if (isAllGens) {
                        "All Gens"
                    } else if (uiState.selectedGenerations.size <= 2) {
                        uiState.selectedGenerations.sorted().joinToString(", ") { "Gen $it" }
                    } else {
                        "${uiState.selectedGenerations.size} Gens"
                    }

                    Surface(
                        onClick = {
                            haptics.selectionTick()
                            showFilterSheet = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllGens) {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAllGens) Icons.Default.Tune else Icons.Default.FilterList,
                                contentDescription = "Filter Generations",
                                modifier = Modifier.size(14.dp),
                                tint = if (isAllGens) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = filterText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllGens) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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
            if (uiState.isLoading) {
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

            if (uiState.targetPokemon == null && uiState.availablePokemonCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Pokémon In Selected Generations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Please select at least one Pokémon generation to play the quiz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                haptics.selectionTick()
                                onSelectAllGenerations()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset to All Generations")
                        }
                    }
                }
                return@Surface
            }

            val target = uiState.targetPokemon

            if (target == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Surface
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenHeight = maxHeight
                val needsScroll = screenHeight < 640.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (needsScroll) Modifier.verticalScroll(rememberScrollState())
                            else Modifier
                        )
                        .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Generation Selector Horizontal Chips Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "All" chip
                        val isAllSelected = uiState.isAllGenerationsSelected
                        val allCornerRadius by animateDpAsState(
                            targetValue = if (isAllSelected) Dimens.Major else Dimens.Compact,
                            animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                            label = "all_gen_chip_shape"
                        )
                        FilterChip(
                            selected = isAllSelected,
                            onClick = {
                                haptics.selectionTick()
                                onSelectAllGenerations()
                            },
                            shape = RoundedCornerShape(allCornerRadius),
                            label = {
                                Text(
                                    text = "All",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isAllSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        // Gen 1 to Gen 9 Chips
                        (1..9).forEach { genNumber ->
                            val isSelected = !isAllSelected && uiState.selectedGenerations.contains(genNumber)
                            val cornerRadius by animateDpAsState(
                                targetValue = if (isSelected) Dimens.Major else Dimens.Compact,
                                animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
                                label = "gen_${genNumber}_chip_shape"
                            )

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptics.selectionTick()
                                    onToggleGeneration(genNumber)
                                },
                                shape = RoundedCornerShape(cornerRadius),
                                label = {
                                    Text(
                                        text = "Gen $genNumber",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        // More filter button
                        Surface(
                            onClick = {
                                haptics.selectionTick()
                                showFilterSheet = true
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "More Filters",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Header stats bar: Lives, Score, Streak
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                        this@Column.AnimatedVisibility(
                            visible = uiState.isAnswered,
                            enter = fadeIn() + scaleIn(initialScale = 0.85f),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "It's ${target.capitalizedName}! (Gen ${target.effectiveGeneration})",
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
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
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
                                        overflow = TextOverflow.Ellipsis,
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

            // Generation Filter Bottom Sheet
            if (showFilterSheet) {
                QuizGenerationFilterBottomSheet(
                    selectedGenerations = uiState.selectedGenerations,
                    generationCounts = uiState.generationCounts,
                    totalMatchingCount = uiState.availablePokemonCount,
                    onToggleGeneration = onToggleGeneration,
                    onSelectAll = onSelectAllGenerations,
                    onSelectPreset = onSelectGenerationPreset,
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}
