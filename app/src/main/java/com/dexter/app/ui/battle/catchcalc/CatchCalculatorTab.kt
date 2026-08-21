package com.dexter.app.ui.battle.catchcalc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.battle.model.CatchStatusCondition
import com.dexter.app.domain.battle.model.PokeBallType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.ui.battle.CatchCalcUiState
import com.dexter.app.ui.battle.components.AllBallsComparisonLadderCard
import com.dexter.app.ui.battle.components.BallSelectorCard
import com.dexter.app.ui.battle.components.CatchResultHeroCard
import com.dexter.app.ui.battle.components.TargetPokemonCatchCard
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.theme.Dimens

@Composable
fun CatchCalculatorTab(
    uiState: CatchCalcUiState,
    allPokemon: List<Pokemon>,
    onPokemonSelected: (Pokemon) -> Unit,
    onLevelChange: (Int) -> Unit,
    onHpPercentChange: (Double) -> Unit,
    onStatusChange: (CatchStatusCondition) -> Unit,
    onBallSelected: (PokeBallType) -> Unit,
    onTurnChange: (Int) -> Unit,
    onNightOrCaveToggle: (Boolean) -> Unit,
    onWaterEncounterToggle: (Boolean) -> Unit,
    onAlreadyInPokedexToggle: (Boolean) -> Unit,
    onPlayerLevelChange: (Int) -> Unit,
    onOpenPokemonPicker: () -> Unit,
    onClosePokemonPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetPokemon = uiState.targetPokemon ?: return

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
        // 1. Live Catch Rate Hero Card (Pinned Result)
        item(key = "catch_hero") {
            CatchResultHeroCard(
                result = uiState.calculationResult,
                selectedBall = uiState.selectedBall
            )
        }

        // 2. Target Pokémon HP & Status Controls
        item(key = "target_pokemon") {
            TargetPokemonCatchCard(
                pokemon = targetPokemon,
                level = uiState.level,
                currentHpPercent = uiState.currentHpPercent,
                statusCondition = uiState.statusCondition,
                customCatchRate = uiState.customCatchRate,
                onOpenPicker = onOpenPokemonPicker,
                onLevelChange = onLevelChange,
                onHpPercentChange = onHpPercentChange,
                onStatusChange = onStatusChange
            )
        }

        // 3. Poké Ball & Environment Selector
        item(key = "ball_selector") {
            BallSelectorCard(
                selectedBall = uiState.selectedBall,
                turnNumber = uiState.turnNumber,
                isNightOrCave = uiState.isNightOrCave,
                isWaterEncounter = uiState.isWaterEncounter,
                isAlreadyInPokedex = uiState.isAlreadyInPokedex,
                playerPokemonLevel = uiState.playerPokemonLevel,
                onBallSelected = onBallSelected,
                onTurnChange = onTurnChange,
                onNightOrCaveToggle = onNightOrCaveToggle,
                onWaterEncounterToggle = onWaterEncounterToggle,
                onAlreadyInPokedexToggle = onAlreadyInPokedexToggle,
                onPlayerLevelChange = onPlayerLevelChange
            )
        }

        // 4. All Poké Balls Effectiveness Comparison Ladder
        uiState.calculationResult?.let { result ->
            item(key = "all_balls_ladder") {
                AllBallsComparisonLadderCard(
                    comparisons = result.ballComparisonList,
                    selectedBall = uiState.selectedBall,
                    onSelectBall = onBallSelected
                )
            }
        }
    }

    // Pokémon Picker Bottom Sheet
    if (uiState.isSelectingPokemon) {
        PokemonPickerBottomSheet(
            onDismissRequest = onClosePokemonPicker,
            onPokemonSelected = onPokemonSelected,
            pokemonList = allPokemon,
            title = "Select Wild Pokémon to Catch"
        )
    }
}
