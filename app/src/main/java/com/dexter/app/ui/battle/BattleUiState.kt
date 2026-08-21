package com.dexter.app.ui.battle

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.battle.engine.Combatant
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleField
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.CalculatedStats
import com.dexter.app.domain.battle.model.DamageRollResult
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.SpeedTierEntry
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatStages
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.domain.model.PokemonType

enum class BattleTab(val title: String, val iconEmoji: String) {
    DAMAGE_CALC("Damage Calc", "💥"),
    STAT_CALC("Stat & Nature", "📊"),
    SPEED_TIERS("Speed Tiers", "⚡"),
    CATCH_CALC("Catch Rate", "🎯")
}

@Immutable
data class CatchCalcUiState(
    val targetPokemon: Pokemon? = null,
    val level: Int = 50,
    val currentHpPercent: Double = 100.0,
    val statusCondition: com.dexter.app.domain.battle.model.CatchStatusCondition = com.dexter.app.domain.battle.model.CatchStatusCondition.NONE,
    val selectedBall: com.dexter.app.domain.battle.model.PokeBallType = com.dexter.app.domain.battle.model.PokeBallType.ULTRA_BALL,
    val turnNumber: Int = 1,
    val isNightOrCave: Boolean = true,
    val isWaterEncounter: Boolean = false,
    val isAlreadyInPokedex: Boolean = true,
    val playerPokemonLevel: Int = 60,
    val isOppositeGenderSameSpecies: Boolean = false,
    val customCatchRate: Int? = null,
    val calculationResult: com.dexter.app.domain.battle.model.CatchRateResult? = null,
    val isSelectingPokemon: Boolean = false
)

@Immutable
data class DamageCalcUiState(
    val attacker: Combatant,
    val defender: Combatant,
    val selectedMove: BattleMove,
    val attackerLearnedMoves: List<PokemonMove> = emptyList(),
    val defenderLearnedMoves: List<PokemonMove> = emptyList(),
    val field: BattleField = BattleField(),
    val isCritical: Boolean = false,
    val calculationResult: DamageRollResult? = null,
    val isSelectingAttacker: Boolean = false,
    val isSelectingDefender: Boolean = false,
    val isSelectingMove: Boolean = false
)

@Immutable
data class StatCalcUiState(
    val selectedPokemon: Pokemon? = null,
    val level: Int = 50,
    val nature: PokemonNature = PokemonNature.JOLLY,
    val ivs: StatSpread = StatSpread.ALL_31,
    val evs: StatSpread = StatSpread.ALL_0,
    val calculatedStats: CalculatedStats? = null,
    val heldItem: BattleItem = BattleItem.NONE,
    val ability: String? = null,
    val teraType: PokemonType? = null,
    val showdownText: String = "",
    val copySuccessTrigger: Long = 0L,
    val isSelectingPokemon: Boolean = false
)

@Immutable
data class SpeedTierUiState(
    val level: Int = 50,
    val ladder: List<SpeedTierEntry> = emptyList(),
    val userPokemon: Pokemon? = null,
    val userLevel: Int = 50,
    val userNature: PokemonNature = PokemonNature.JOLLY,
    val userIv: Int = 31,
    val userEv: Int = 252,
    val userStatStage: Int = 0,
    val userHasScarf: Boolean = false,
    val userHasBooster: Boolean = false,
    val userHasSwiftSwim: Boolean = false,
    val userHasTailwind: Boolean = false,
    val userIsParalyzed: Boolean = false,
    val selectedCategoryFilter: SpeedTierCategory? = null,
    val searchQuery: String = "",
    val isSelectingUserPokemon: Boolean = false
)

@Immutable
data class BattleUiState(
    val activeTab: BattleTab = BattleTab.DAMAGE_CALC,
    val allPokemon: List<Pokemon> = emptyList(),
    val damageCalc: DamageCalcUiState,
    val statCalc: StatCalcUiState,
    val speedTier: SpeedTierUiState,
    val catchCalc: CatchCalcUiState = CatchCalcUiState(),
    val isLoading: Boolean = false
)
