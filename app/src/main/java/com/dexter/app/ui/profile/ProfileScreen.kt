package com.dexter.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.data.repository.TrainerData

import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Switch

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onAvatarSelect: (Int) -> Unit = {},
    onHapticToggle: (Boolean) -> Unit = {},
    onThemeSelect: (com.dexter.app.data.repository.AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAvatarPicker by remember { mutableStateOf(false) }
    val haptics = com.dexter.app.ui.common.rememberHapticUtils(isEnabled = uiState.trainerData.isHapticEnabled)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            com.dexter.app.ui.common.GlassmorphicTopAppBar(
                title = {
                    Text("Trainer Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val trainer = uiState.trainerData
        val currentLevelXp = (trainer.totalXp - uiState.currentLevelBaseXp).coerceAtLeast(0)
        val levelXpSpan = (uiState.nextLevelXp - uiState.currentLevelBaseXp).coerceAtLeast(1)
        val progress = (currentLevelXp.toFloat() / levelXpSpan.toFloat()).coerceIn(0f, 1f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clickable Avatar with Edit Icon Badge (Unclipped)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            val avatarUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${trainer.avatarPokemonId}.png"
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Trainer Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }

                        // Edit badge overlay
                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Level ${trainer.level} Trainer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // XP Progress bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "XP Progress",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${trainer.totalXp} / ${uiState.nextLevelXp} XP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }

            // Daily Login Streak Card
            val isDark = com.dexter.app.ui.theme.LocalDarkTheme.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Login Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
                        )
                        Text(
                            text = "${trainer.loginStreak} Day${if (trainer.loginStreak != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFFBF360C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Trainer Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        icon = Icons.Default.CatchingPokemon,
                        title = "Pokémon Caught",
                        value = "${uiState.caughtCount}",
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        icon = Icons.Default.Psychology,
                        title = "Quiz Correct",
                        value = "${uiState.totalQuizCorrect}",
                        accentColor = Color(0xFF9C27B0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        icon = Icons.Default.Star,
                        title = "Best Quiz Streak",
                        value = "${uiState.bestQuizStreak}",
                        accentColor = Color(0xFFFF9800)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        icon = Icons.Default.EmojiEvents,
                        title = "Badges Unlocked",
                        value = "${uiState.unlockedAchievementsCount}",
                        accentColor = Color(0xFF4CAF50)
                    )
                }
            }

            // Achievements Button
            Button(
                onClick = onAchievementsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = "Badges")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Badges & Achievements", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Haptic Feedback Settings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptic Feedback",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Haptic Feedback",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tactile response on taps, cards & achievements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Switch(
                        checked = trainer.isHapticEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                haptics.mediumImpact()
                            }
                            onHapticToggle(checked)
                        }
                    )
                }
            }

            // App Theme Preference Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "App Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Choose Light, Dark, or System theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themeOptions = listOf(
                            com.dexter.app.data.repository.AppThemeMode.LIGHT to "Light",
                            com.dexter.app.data.repository.AppThemeMode.DARK to "Dark",
                            com.dexter.app.data.repository.AppThemeMode.SYSTEM to "System"
                        )
                        themeOptions.forEach { (mode, label) ->
                            val isSelected = uiState.themeMode == mode
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptics.lightClick()
                                    onThemeSelect(mode)
                                },
                                label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            com.dexter.app.data.repository.AppThemeMode.LIGHT -> Icons.Default.LightMode
                                            com.dexter.app.data.repository.AppThemeMode.DARK -> Icons.Default.DarkMode
                                            com.dexter.app.data.repository.AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Avatar Selection Dialog Modal
        if (showAvatarPicker) {
            AlertDialog(
                onDismissRequest = { showAvatarPicker = false },
                title = {
                    Text(
                        text = "Choose Trainer Avatar",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Select a Gen 1 Starter, Pikachu, or Legendary Pokémon:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(280.dp)
                        ) {
                            items(TrainerData.AVATAR_OPTIONS) { pokeId ->
                                val isSelected = pokeId == trainer.avatarPokemonId
                                Surface(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            haptics.successPulse()
                                            onAvatarSelect(pokeId)
                                            showAvatarPicker = false
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png"
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Avatar $pokeId",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAvatarPicker = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
