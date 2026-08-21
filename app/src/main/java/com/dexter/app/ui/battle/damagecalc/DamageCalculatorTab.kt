package com.dexter.app.ui.battle.damagecalc

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.battle.DamageCalcUiState
import com.dexter.app.ui.battle.components.CombatantCard
import com.dexter.app.ui.battle.components.DamageResultHeroCard
import com.dexter.app.ui.battle.components.FieldConditionsCard
import com.dexter.app.ui.battle.components.MoveSelectorCard
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.theme.Dimens

@Composable
fun DamageCalculatorTab(
    uiState: DamageCalcUiState,
    allPokemon: List<Pokemon>,
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdgePadding,
            end = Dimens.ScreenEdgePadding,
            top = Dimens.Compact,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Default)
    ) {
        // 1. Live Damage Calculation Hero Card (Always pinned at the top)
        item(key = "damage_hero") {
            DamageResultHeroCard(
                result = uiState.calculationResult
            )
        }

        // 2. Move Selector Section
        item(key = "move_selector") {
            MoveSelectorCard(
                selectedMove = uiState.selectedMove,
                attackerLearnedMoves = uiState.attackerLearnedMoves,
                isCritical = uiState.isCritical,
                onSelectMove = onSelectMove,
                onCriticalToggle = onCriticalToggle,
                onCustomPowerChange = onCustomMovePowerChange,
                onCustomTypeChange = onCustomMoveTypeChange,
                onCustomCategoryChange = onCustomMoveCategoryChange
            )
        }

        // 3. Attacker Card
        item(key = "attacker_card") {
            CombatantCard(
                title = "Attacker",
                combatant = uiState.attacker,
                isAttacker = true,
                onSelectPokemonClick = onOpenAttackerPicker,
                onLevelChange = onAttackerLevelChange,
                onNatureChange = onAttackerNatureChange,
                onItemChange = onAttackerItemChange,
                onAbilityChange = onAttackerAbilityChange,
                onStageChange = onAttackerStageChange,
                onEvChange = onAttackerEvChange,
                onIvChange = onAttackerIvChange,
                onTeraTypeChange = onAttackerTeraTypeChange,
                onBurnChange = onAttackerBurnChange
            )
        }

        // Swap Button Intermediary
        item(key = "swap_button") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = onSwapAttackerDefender,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Attacker and Defender",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 4. Defender Card
        item(key = "defender_card") {
            CombatantCard(
                title = "Defender",
                combatant = uiState.defender,
                isAttacker = false,
                onSelectPokemonClick = onOpenDefenderPicker,
                onLevelChange = onDefenderLevelChange,
                onNatureChange = onDefenderNatureChange,
                onItemChange = onDefenderItemChange,
                onAbilityChange = onDefenderAbilityChange,
                onStageChange = onDefenderStageChange,
                onEvChange = onDefenderEvChange,
                onIvChange = onDefenderIvChange,
                onTeraTypeChange = onDefenderTeraTypeChange,
                onHpPercentChange = onDefenderHpPercentChange
            )
        }

        // 5. Field Conditions Card
        item(key = "field_conditions") {
            FieldConditionsCard(
                field = uiState.field,
                onWeatherChange = onWeatherChange,
                onTerrainChange = onTerrainChange,
                onDoublesToggle = onDoublesToggle,
                onReflectToggle = onReflectToggle,
                onLightScreenToggle = onLightScreenToggle,
                onAuroraVeilToggle = onAuroraVeilToggle,
                onStealthRockToggle = onStealthRockToggle,
                onSpikesLayersChange = onSpikesLayersChange,
                onHelpingHandToggle = onHelpingHandToggle
            )
        }
    }

    // Attacker Picker Bottom Sheet
    if (uiState.isSelectingAttacker) {
        PokemonPickerBottomSheet(
            onDismissRequest = onCloseAttackerPicker,
            onPokemonSelected = onAttackerSelected,
            pokemonList = allPokemon,
            title = "Choose Attacker"
        )
    }

    // Defender Picker Bottom Sheet
    if (uiState.isSelectingDefender) {
        PokemonPickerBottomSheet(
            onDismissRequest = onCloseDefenderPicker,
            onPokemonSelected = onDefenderSelected,
            pokemonList = allPokemon,
            title = "Choose Defender"
        )
    }
}
