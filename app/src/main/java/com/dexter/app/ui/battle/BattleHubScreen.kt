package com.dexter.app.ui.battle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.battle.damagecalc.DamageCalculatorTab
import com.dexter.app.ui.battle.speedtier.SpeedTierTab
import com.dexter.app.ui.battle.statcalc.StatCalculatorTab
import com.dexter.app.ui.common.GlassmorphicTopAppBar
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleHubScreen(
    uiState: BattleUiState,
    onTabSelected: (BattleTab) -> Unit,
    // Damage Calc Handlers
    onAttackerSelected: (Pokemon) -> Unit,
    onDefenderSelected: (Pokemon) -> Unit,
    onSwapAttackerDefender: () -> Unit,
    onAttackerLevelChange: (Int) -> Unit,
    onAttackerNatureChange: (PokemonNature) -> Unit,
    onAttackerItemChange: (BattleItem) -> Unit,
    onAttackerAbilityChange: (BattleAbility) -> Unit,
    onAttackerStageChange: (StatType, Int) -> Unit,
    onAttackerEvChange: (StatType, Int) -> Unit,
    onAttackerIvChange: (StatType, Int) -> Unit,
    onAttackerBurnChange: (Boolean) -> Unit,
    onAttackerTeraTypeChange: (PokemonType?) -> Unit,
    onDefenderLevelChange: (Int) -> Unit,
    onDefenderNatureChange: (PokemonNature) -> Unit,
    onDefenderItemChange: (BattleItem) -> Unit,
    onDefenderAbilityChange: (BattleAbility) -> Unit,
    onDefenderStageChange: (StatType, Int) -> Unit,
    onDefenderEvChange: (StatType, Int) -> Unit,
    onDefenderIvChange: (StatType, Int) -> Unit,
    onDefenderTeraTypeChange: (PokemonType?) -> Unit,
    onDefenderHpPercentChange: (Double) -> Unit,
    onSelectMove: (BattleMove) -> Unit,
    onCriticalToggle: (Boolean) -> Unit,
    onCustomMovePowerChange: (Int) -> Unit,
    onCustomMoveTypeChange: (PokemonType) -> Unit,
    onCustomMoveCategoryChange: (MoveCategory) -> Unit,
    onWeatherChange: (BattleWeather) -> Unit,
    onTerrainChange: (BattleTerrain) -> Unit,
    onDoublesToggle: (Boolean) -> Unit,
    onReflectToggle: () -> Unit,
    onLightScreenToggle: () -> Unit,
    onAuroraVeilToggle: () -> Unit,
    onStealthRockToggle: () -> Unit,
    onSpikesLayersChange: (Int) -> Unit,
    onHelpingHandToggle: () -> Unit,
    onOpenAttackerPicker: () -> Unit,
    onCloseAttackerPicker: () -> Unit,
    onOpenDefenderPicker: () -> Unit,
    onCloseDefenderPicker: () -> Unit,
    // Stat Calc Handlers
    onStatCalcPokemonSelected: (Pokemon) -> Unit,
    onStatCalcLevelChange: (Int) -> Unit,
    onStatCalcNatureChange: (PokemonNature) -> Unit,
    onStatCalcIvChange: (StatType, Int) -> Unit,
    onStatCalcAllIvsChange: (Int) -> Unit,
    onStatCalcEvChange: (StatType, Int) -> Unit,
    onStatCalcEvPresetSelected: (StatType, StatType) -> Unit,
    onStatCalcItemChange: (BattleItem) -> Unit,
    onStatCalcTeraTypeChange: (PokemonType?) -> Unit,
    onCopyShowdownSuccess: () -> Unit,
    onSendStatCalcToDamageCalc: () -> Unit,
    onOpenStatCalcPicker: () -> Unit,
    onCloseStatCalcPicker: () -> Unit,
    // Speed Tier Handlers
    onSpeedTierLevelChange: (Int) -> Unit,
    onSpeedTierSearchQueryChange: (String) -> Unit,
    onSpeedTierCategoryFilterSelect: (SpeedTierCategory?) -> Unit,
    onSpeedTierUserPokemonSelected: (Pokemon) -> Unit,
    onUserSpeedNatureChange: (PokemonNature) -> Unit,
    onUserSpeedEvChange: (Int) -> Unit,
    onUserSpeedIvChange: (Int) -> Unit,
    onUserSpeedStatStageChange: (Int) -> Unit,
    onUserSpeedScarfToggle: (Boolean) -> Unit,
    onUserSpeedBoosterToggle: (Boolean) -> Unit,
    onUserSpeedSwiftSwimToggle: (Boolean) -> Unit,
    onUserSpeedTailwindToggle: (Boolean) -> Unit,
    onUserSpeedParalyzedToggle: (Boolean) -> Unit,
    onOpenSpeedTierPicker: () -> Unit,
    onCloseSpeedTierPicker: () -> Unit,
    // Catch Calc Handlers
    onCatchCalcPokemonSelected: (Pokemon) -> Unit,
    onCatchCalcLevelChange: (Int) -> Unit,
    onCatchCalcHpPercentChange: (Double) -> Unit,
    onCatchCalcStatusChange: (com.dexter.app.domain.battle.model.CatchStatusCondition) -> Unit,
    onCatchCalcBallSelected: (com.dexter.app.domain.battle.model.PokeBallType) -> Unit,
    onCatchCalcTurnChange: (Int) -> Unit,
    onCatchCalcNightOrCaveToggle: (Boolean) -> Unit,
    onCatchCalcWaterEncounterToggle: (Boolean) -> Unit,
    onCatchCalcAlreadyInPokedexToggle: (Boolean) -> Unit,
    onCatchCalcPlayerLevelChange: (Int) -> Unit,
    onOpenCatchCalcPicker: () -> Unit,
    onCloseCatchCalcPicker: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            GlassmorphicTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚔️ Battle & Competitive Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = if (onBackClick != null) {
                    {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else null
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Tab Row
            BattleTabRow(
                selectedTab = uiState.activeTab,
                onTabSelected = onTabSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
            )

            // Animated Tab Content Switch
            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "battle_tabs",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    BattleTab.DAMAGE_CALC -> {
                        DamageCalculatorTab(
                            uiState = uiState.damageCalc,
                            allPokemon = uiState.allPokemon,
                            onAttackerSelected = onAttackerSelected,
                            onDefenderSelected = onDefenderSelected,
                            onSwapAttackerDefender = onSwapAttackerDefender,
                            onAttackerLevelChange = onAttackerLevelChange,
                            onAttackerNatureChange = onAttackerNatureChange,
                            onAttackerItemChange = onAttackerItemChange,
                            onAttackerAbilityChange = onAttackerAbilityChange,
                            onAttackerStageChange = onAttackerStageChange,
                            onAttackerEvChange = onAttackerEvChange,
                            onAttackerIvChange = onAttackerIvChange,
                            onAttackerBurnChange = onAttackerBurnChange,
                            onAttackerTeraTypeChange = onAttackerTeraTypeChange,
                            onDefenderLevelChange = onDefenderLevelChange,
                            onDefenderNatureChange = onDefenderNatureChange,
                            onDefenderItemChange = onDefenderItemChange,
                            onDefenderAbilityChange = onDefenderAbilityChange,
                            onDefenderStageChange = onDefenderStageChange,
                            onDefenderEvChange = onDefenderEvChange,
                            onDefenderIvChange = onDefenderIvChange,
                            onDefenderTeraTypeChange = onDefenderTeraTypeChange,
                            onDefenderHpPercentChange = onDefenderHpPercentChange,
                            onSelectMove = onSelectMove,
                            onCriticalToggle = onCriticalToggle,
                            onCustomMovePowerChange = onCustomMovePowerChange,
                            onCustomMoveTypeChange = onCustomMoveTypeChange,
                            onCustomMoveCategoryChange = onCustomMoveCategoryChange,
                            onWeatherChange = onWeatherChange,
                            onTerrainChange = onTerrainChange,
                            onDoublesToggle = onDoublesToggle,
                            onReflectToggle = onReflectToggle,
                            onLightScreenToggle = onLightScreenToggle,
                            onAuroraVeilToggle = onAuroraVeilToggle,
                            onStealthRockToggle = onStealthRockToggle,
                            onSpikesLayersChange = onSpikesLayersChange,
                            onHelpingHandToggle = onHelpingHandToggle,
                            onOpenAttackerPicker = onOpenAttackerPicker,
                            onCloseAttackerPicker = onCloseAttackerPicker,
                            onOpenDefenderPicker = onOpenDefenderPicker,
                            onCloseDefenderPicker = onCloseDefenderPicker
                        )
                    }

                    BattleTab.STAT_CALC -> {
                        StatCalculatorTab(
                            uiState = uiState.statCalc,
                            allPokemon = uiState.allPokemon,
                            onPokemonSelected = onStatCalcPokemonSelected,
                            onLevelChange = onStatCalcLevelChange,
                            onNatureChange = onStatCalcNatureChange,
                            onIvChange = onStatCalcIvChange,
                            onAllIvsChange = onStatCalcAllIvsChange,
                            onEvChange = onStatCalcEvChange,
                            onEvPresetSelected = onStatCalcEvPresetSelected,
                            onItemChange = onStatCalcItemChange,
                            onTeraTypeChange = onStatCalcTeraTypeChange,
                            onCopySuccess = onCopyShowdownSuccess,
                            onSendToDamageCalc = onSendStatCalcToDamageCalc,
                            onOpenPokemonPicker = onOpenStatCalcPicker,
                            onClosePokemonPicker = onCloseStatCalcPicker
                        )
                    }

                    BattleTab.SPEED_TIERS -> {
                        SpeedTierTab(
                            uiState = uiState.speedTier,
                            allPokemon = uiState.allPokemon,
                            onLevelChange = onSpeedTierLevelChange,
                            onSearchQueryChange = onSpeedTierSearchQueryChange,
                            onCategoryFilterSelect = onSpeedTierCategoryFilterSelect,
                            onUserPokemonSelected = onSpeedTierUserPokemonSelected,
                            onUserNatureChange = onUserSpeedNatureChange,
                            onUserEvChange = onUserSpeedEvChange,
                            onUserIvChange = onUserSpeedIvChange,
                            onUserStatStageChange = onUserSpeedStatStageChange,
                            onUserScarfToggle = onUserSpeedScarfToggle,
                            onUserBoosterToggle = onUserSpeedBoosterToggle,
                            onUserSwiftSwimToggle = onUserSpeedSwiftSwimToggle,
                            onUserTailwindToggle = onUserSpeedTailwindToggle,
                            onUserParalyzedToggle = onUserSpeedParalyzedToggle,
                            onOpenUserPokemonPicker = onOpenSpeedTierPicker,
                            onCloseUserPokemonPicker = onCloseSpeedTierPicker
                        )
                    }

                    BattleTab.CATCH_CALC -> {
                        com.dexter.app.ui.battle.catchcalc.CatchCalculatorTab(
                            uiState = uiState.catchCalc,
                            allPokemon = uiState.allPokemon,
                            onPokemonSelected = onCatchCalcPokemonSelected,
                            onLevelChange = onCatchCalcLevelChange,
                            onHpPercentChange = onCatchCalcHpPercentChange,
                            onStatusChange = onCatchCalcStatusChange,
                            onBallSelected = onCatchCalcBallSelected,
                            onTurnChange = onCatchCalcTurnChange,
                            onNightOrCaveToggle = onCatchCalcNightOrCaveToggle,
                            onWaterEncounterToggle = onCatchCalcWaterEncounterToggle,
                            onAlreadyInPokedexToggle = onCatchCalcAlreadyInPokedexToggle,
                            onPlayerLevelChange = onCatchCalcPlayerLevelChange,
                            onOpenPokemonPicker = onOpenCatchCalcPicker,
                            onClosePokemonPicker = onCloseCatchCalcPicker
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleTabRow(
    selectedTab: BattleTab,
    onTabSelected: (BattleTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BattleTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab

                val tabBg = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                }

                val textColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                val elevation = if (isSelected) 3.dp else 0.dp

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tabBg,
                    shadowElevation = elevation,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.iconEmoji,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
