package com.dexter.app.ui.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dexter.app.data.local.AchievementEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    uiState: AchievementsUiState,
    onBackClick: () -> Unit,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Catching", "Quiz", "Engagement")

    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    var prevUnlockedIds by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(uiState.achievements) {
        val currentUnlockedIds = uiState.achievements.filter { it.isUnlocked }.map { it.id }.toSet()
        val prev = prevUnlockedIds
        if (prev != null) {
            val newlyUnlocked = currentUnlockedIds - prev
            if (newlyUnlocked.isNotEmpty()) {
                haptics.successPulse()
            }
        }
        prevUnlockedIds = currentUnlockedIds
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            com.dexter.app.ui.common.GlassmorphicTopAppBar(
                title = {
                    Text("Badges & Achievements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Chips in single-line horizontal scroll
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.lightTick()
                            onCategorySelect(category)
                        },
                        label = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.achievements, key = { it.id }) { achievement ->
                    AchievementBadgeCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementBadgeCard(achievement: AchievementEntity) {
    val isUnlocked = achievement.isUnlocked
    val progressRatio = (achievement.currentProgress.toFloat() / achievement.maxProgress.toFloat()).coerceIn(0f, 1f)

    val badgeIcon: ImageVector = when (achievement.category) {
        "Catching" -> Icons.Default.CatchingPokemon
        "Quiz" -> Icons.Default.Psychology
        "Engagement" -> Icons.Default.Star
        else -> Icons.Default.MilitaryTech
    }

    val iconColor = if (isUnlocked) {
        when (achievement.category) {
            "Catching" -> MaterialTheme.colorScheme.primary
            "Quiz" -> Color(0xFF9C27B0)
            "Engagement" -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp),
        modifier = Modifier.alpha(if (isUnlocked) 1.0f else 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = if (isUnlocked) iconColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = achievement.title,
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if (!isUnlocked) {
                    Surface(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar & text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isUnlocked) "UNLOCKED" else "${achievement.currentProgress}/${achievement.maxProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
