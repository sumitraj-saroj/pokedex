package com.dexter.app.ui.battle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.domain.battle.engine.Combatant
import com.dexter.app.domain.battle.engine.DamageCalculatorEngine
import com.dexter.app.domain.battle.engine.SpeedTierEngine
import com.dexter.app.domain.battle.engine.StatCalculatorEngine
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleField
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatStages
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.dexter.app.domain.battle.engine.CatchContext
import com.dexter.app.domain.battle.engine.CatchRateEngine
import com.dexter.app.domain.battle.model.CatchStatusCondition
import com.dexter.app.domain.battle.model.PokeBallType

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val repository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialTabArg: String? = savedStateHandle.get<String>("tab")
    private val initialPokemonIdArg: Int? = savedStateHandle.get<Int>("pokemonId")

    private val defaultAttacker = createDefaultPokemon(
        id = 887, name = "dragapult", number = 887,
        primaryType = PokemonType.DRAGON, secondaryType = PokemonType.GHOST,
        hp = 88, atk = 120, def = 75, spa = 100, spd = 75, spe = 142
    )

    private val defaultDefender = createDefaultPokemon(
        id = 748, name = "toxapex", number = 748,
        primaryType = PokemonType.POISON, secondaryType = PokemonType.WATER,
        hp = 50, atk = 63, def = 152, spa = 53, spd = 142, spe = 35
    )

    private val defaultMove = BattleMove.POPULAR_COMPETITIVE_MOVES.firstOrNull { it.name == "dragon-darts" }
        ?: BattleMove("dragon-darts", "Dragon Darts", PokemonType.DRAGON, MoveCategory.PHYSICAL, 50, hitCount = 2)

    private val _uiState = MutableStateFlow(
        BattleUiState(
            activeTab = when (initialTabArg) {
                "stat", "stat_calc" -> BattleTab.STAT_CALC
                "speed", "speed_tiers" -> BattleTab.SPEED_TIERS
                "catch", "catch_calc" -> BattleTab.CATCH_CALC
                else -> BattleTab.DAMAGE_CALC
            },
            damageCalc = DamageCalcUiState(
                attacker = Combatant(
                    pokemon = defaultAttacker,
                    nature = PokemonNature.JOLLY,
                    evs = StatSpread(attack = 252, speed = 252),
                    item = BattleItem.CHOICE_BAND
                ),
                defender = Combatant(
                    pokemon = defaultDefender,
                    nature = PokemonNature.BOLD,
                    evs = StatSpread(hp = 252, defense = 252),
                    item = BattleItem.LEFTOVERS
                ),
                selectedMove = defaultMove
            ),
            statCalc = StatCalcUiState(
                selectedPokemon = defaultAttacker,
                level = 50,
                nature = PokemonNature.JOLLY,
                ivs = StatSpread.ALL_31,
                evs = StatSpread(attack = 252, speed = 252)
            ),
            speedTier = SpeedTierUiState(
                level = 50,
                ladder = SpeedTierEngine.getCompetitiveBenchmarks(50),
                userPokemon = defaultAttacker
            ),
            catchCalc = CatchCalcUiState(
                targetPokemon = defaultDefender,
                level = 50,
                currentHpPercent = 100.0,
                selectedBall = PokeBallType.ULTRA_BALL
            )
        )
    )
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    init {
        // Initial Calculations
        recalculateDamage()
        recalculateStatCalc()
        recalculateSpeedLadder()
        recalculateCatchRate()

        // Observe All Pokemon from Repository
        viewModelScope.launch {
            repository.observeAllPokemon().collect { pokemonList ->
                if (pokemonList.isNotEmpty()) {
                    _uiState.update { current ->
                        val targetPokemon = if (initialPokemonIdArg != null && initialPokemonIdArg > 0) {
                            pokemonList.firstOrNull { it.id == initialPokemonIdArg }
                        } else null

                        val updatedAttacker = if (targetPokemon != null) {
                            current.damageCalc.attacker.copy(pokemon = targetPokemon)
                        } else {
                            pokemonList.firstOrNull { it.id == current.damageCalc.attacker.pokemon.id }?.let {
                                current.damageCalc.attacker.copy(pokemon = it)
                            } ?: current.damageCalc.attacker
                        }

                        val updatedDefender = pokemonList.firstOrNull { it.id == current.damageCalc.defender.pokemon.id }?.let {
                            current.damageCalc.defender.copy(pokemon = it)
                        } ?: current.damageCalc.defender

                        val updatedStatCalcPokemon = if (targetPokemon != null) {
                            targetPokemon
                        } else {
                            pokemonList.firstOrNull { it.id == current.statCalc.selectedPokemon?.id } ?: current.statCalc.selectedPokemon
                        }

                        val updatedCatchTarget = if (targetPokemon != null) {
                            targetPokemon
                        } else {
                            pokemonList.firstOrNull { it.id == current.catchCalc.targetPokemon?.id } ?: current.catchCalc.targetPokemon
                        }

                        val updatedUserPokemon = targetPokemon
                            ?: pokemonList.firstOrNull { it.id == current.speedTier.userPokemon?.id }
                            ?: current.speedTier.userPokemon

                        current.copy(
                            allPokemon = pokemonList,
                            damageCalc = current.damageCalc.copy(
                                attacker = updatedAttacker,
                                defender = updatedDefender
                            ),
                            statCalc = current.statCalc.copy(selectedPokemon = updatedStatCalcPokemon),
                            catchCalc = current.catchCalc.copy(targetPokemon = updatedCatchTarget),
                            speedTier = current.speedTier.copy(userPokemon = updatedUserPokemon)
                        )
                    }

                    // Load learned moves for attacker and defender
                    loadLearnedMoves(_uiState.value.damageCalc.attacker.pokemon.id, isAttacker = true)
                    loadLearnedMoves(_uiState.value.damageCalc.defender.pokemon.id, isAttacker = false)

                    recalculateDamage()
                    recalculateStatCalc()
                    recalculateSpeedLadder()
                }
            }
        }
    }

    fun selectTab(tab: BattleTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // =========================================================================
    // DAMAGE CALCULATOR HANDLERS
    // =========================================================================

    fun setAttackerPokemon(pokemon: Pokemon) {
        viewModelScope.launch {
            repository.ensurePokemonDetailExtras(pokemon.id)
        }
        _uiState.update { state ->
            val updatedAttacker = state.damageCalc.attacker.copy(
                pokemon = pokemon,
                teraType = null
            )
            state.copy(
                damageCalc = state.damageCalc.copy(
                    attacker = updatedAttacker,
                    isSelectingAttacker = false
                )
            )
        }
        loadLearnedMoves(pokemon.id, isAttacker = true)
        recalculateDamage()
    }

    fun setDefenderPokemon(pokemon: Pokemon) {
        viewModelScope.launch {
            repository.ensurePokemonDetailExtras(pokemon.id)
        }
        _uiState.update { state ->
            val updatedDefender = state.damageCalc.defender.copy(
                pokemon = pokemon,
                teraType = null
            )
            state.copy(
                damageCalc = state.damageCalc.copy(
                    defender = updatedDefender,
                    isSelectingDefender = false
                )
            )
        }
        loadLearnedMoves(pokemon.id, isAttacker = false)
        recalculateDamage()
    }

    fun swapAttackerAndDefender() {
        _uiState.update { state ->
            val prevAttacker = state.damageCalc.attacker
            val prevDefender = state.damageCalc.defender
            val prevAttackerMoves = state.damageCalc.attackerLearnedMoves
            val prevDefenderMoves = state.damageCalc.defenderLearnedMoves

            state.copy(
                damageCalc = state.damageCalc.copy(
                    attacker = prevDefender,
                    defender = prevAttacker,
                    attackerLearnedMoves = prevDefenderMoves,
                    defenderLearnedMoves = prevAttackerMoves
                )
            )
        }
        recalculateDamage()
    }

    fun setAttackerLevel(level: Int) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(level = level.coerceIn(1, 100)))) }
        recalculateDamage()
    }

    fun setAttackerNature(nature: PokemonNature) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(nature = nature))) }
        recalculateDamage()
    }

    fun setAttackerItem(item: BattleItem) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(item = item))) }
        recalculateDamage()
    }

    fun setAttackerAbility(ability: BattleAbility) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(ability = ability))) }
        recalculateDamage()
    }

    fun setAttackerStatStage(stat: StatType, delta: Int) {
        _uiState.update { state ->
            val stages = state.damageCalc.attacker.statStages.updateStage(stat, delta)
            state.copy(damageCalc = state.damageCalc.copy(attacker = state.damageCalc.attacker.copy(statStages = stages)))
        }
        recalculateDamage()
    }

    fun setAttackerEv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val evs = state.damageCalc.attacker.evs.updateStat(stat, value.coerceIn(0, 252))
            state.copy(damageCalc = state.damageCalc.copy(attacker = state.damageCalc.attacker.copy(evs = evs)))
        }
        recalculateDamage()
    }

    fun setAttackerIv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val ivs = state.damageCalc.attacker.ivs.updateStat(stat, value.coerceIn(0, 31))
            state.copy(damageCalc = state.damageCalc.copy(attacker = state.damageCalc.attacker.copy(ivs = ivs)))
        }
        recalculateDamage()
    }

    fun setAttackerBurned(isBurned: Boolean) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(isBurned = isBurned))) }
        recalculateDamage()
    }

    fun setAttackerTeraType(teraType: PokemonType?) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(attacker = it.damageCalc.attacker.copy(teraType = teraType))) }
        recalculateDamage()
    }

    fun setDefenderLevel(level: Int) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(level = level.coerceIn(1, 100)))) }
        recalculateDamage()
    }

    fun setDefenderNature(nature: PokemonNature) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(nature = nature))) }
        recalculateDamage()
    }

    fun setDefenderItem(item: BattleItem) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(item = item))) }
        recalculateDamage()
    }

    fun setDefenderAbility(ability: BattleAbility) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(ability = ability))) }
        recalculateDamage()
    }

    fun setDefenderStatStage(stat: StatType, delta: Int) {
        _uiState.update { state ->
            val stages = state.damageCalc.defender.statStages.updateStage(stat, delta)
            state.copy(damageCalc = state.damageCalc.copy(defender = state.damageCalc.defender.copy(statStages = stages)))
        }
        recalculateDamage()
    }

    fun setDefenderEv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val evs = state.damageCalc.defender.evs.updateStat(stat, value.coerceIn(0, 252))
            state.copy(damageCalc = state.damageCalc.copy(defender = state.damageCalc.defender.copy(evs = evs)))
        }
        recalculateDamage()
    }

    fun setDefenderIv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val ivs = state.damageCalc.defender.ivs.updateStat(stat, value.coerceIn(0, 31))
            state.copy(damageCalc = state.damageCalc.copy(defender = state.damageCalc.defender.copy(ivs = ivs)))
        }
        recalculateDamage()
    }

    fun setDefenderTeraType(teraType: PokemonType?) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(teraType = teraType))) }
        recalculateDamage()
    }

    fun setDefenderCurrentHpPercent(percent: Double) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(defender = it.damageCalc.defender.copy(currentHpPercent = percent.coerceIn(1.0, 100.0)))) }
        recalculateDamage()
    }

    fun selectMove(move: BattleMove) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(selectedMove = move, isSelectingMove = false)) }
        recalculateDamage()
    }

    fun setCustomMovePower(power: Int) {
        _uiState.update { state ->
            val updated = state.damageCalc.selectedMove.copy(basePower = power.coerceIn(1, 300))
            state.copy(damageCalc = state.damageCalc.copy(selectedMove = updated))
        }
        recalculateDamage()
    }

    fun setCustomMoveType(type: PokemonType) {
        _uiState.update { state ->
            val updated = state.damageCalc.selectedMove.copy(type = type)
            state.copy(damageCalc = state.damageCalc.copy(selectedMove = updated))
        }
        recalculateDamage()
    }

    fun setCustomMoveCategory(category: MoveCategory) {
        _uiState.update { state ->
            val updated = state.damageCalc.selectedMove.copy(category = category)
            state.copy(damageCalc = state.damageCalc.copy(selectedMove = updated))
        }
        recalculateDamage()
    }

    fun setCritical(isCritical: Boolean) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isCritical = isCritical)) }
        recalculateDamage()
    }

    fun setWeather(weather: BattleWeather) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(field = it.damageCalc.field.copy(weather = weather))) }
        recalculateDamage()
    }

    fun setTerrain(terrain: BattleTerrain) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(field = it.damageCalc.field.copy(terrain = terrain))) }
        recalculateDamage()
    }

    fun setDoubles(isDoubles: Boolean) {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(field = it.damageCalc.field.copy(isDoubles = isDoubles))) }
        recalculateDamage()
    }

    fun toggleReflect() {
        _uiState.update { state ->
            val current = state.damageCalc.field.defenderReflect
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(defenderReflect = !current)))
        }
        recalculateDamage()
    }

    fun toggleLightScreen() {
        _uiState.update { state ->
            val current = state.damageCalc.field.defenderLightScreen
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(defenderLightScreen = !current)))
        }
        recalculateDamage()
    }

    fun toggleAuroraVeil() {
        _uiState.update { state ->
            val current = state.damageCalc.field.defenderAuroraVeil
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(defenderAuroraVeil = !current)))
        }
        recalculateDamage()
    }

    fun toggleStealthRock() {
        _uiState.update { state ->
            val current = state.damageCalc.field.defenderStealthRock
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(defenderStealthRock = !current)))
        }
        recalculateDamage()
    }

    fun setSpikesLayers(layers: Int) {
        _uiState.update { state ->
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(defenderSpikesLayers = layers.coerceIn(0, 3))))
        }
        recalculateDamage()
    }

    fun toggleHelpingHand() {
        _uiState.update { state ->
            val current = state.damageCalc.field.attackerHelpingHand
            state.copy(damageCalc = state.damageCalc.copy(field = state.damageCalc.field.copy(attackerHelpingHand = !current)))
        }
        recalculateDamage()
    }

    fun openAttackerPicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingAttacker = true)) }
    }

    fun closeAttackerPicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingAttacker = false)) }
    }

    fun openDefenderPicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingDefender = true)) }
    }

    fun closeDefenderPicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingDefender = false)) }
    }

    fun openMovePicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingMove = true)) }
    }

    fun closeMovePicker() {
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(isSelectingMove = false)) }
    }

    private fun recalculateDamage() {
        val calcState = _uiState.value.damageCalc
        val result = DamageCalculatorEngine.calculateDamage(
            attacker = calcState.attacker,
            defender = calcState.defender,
            move = calcState.selectedMove,
            field = calcState.field,
            isCritical = calcState.isCritical
        )
        _uiState.update { it.copy(damageCalc = it.damageCalc.copy(calculationResult = result)) }
    }

    private fun loadLearnedMoves(pokemonId: Int, isAttacker: Boolean) {
        viewModelScope.launch {
            repository.observeMovesForPokemon(pokemonId).collect { moves ->
                if (moves.isNotEmpty()) {
                    _uiState.update { state ->
                        if (isAttacker) {
                            state.copy(damageCalc = state.damageCalc.copy(attackerLearnedMoves = moves))
                        } else {
                            state.copy(damageCalc = state.damageCalc.copy(defenderLearnedMoves = moves))
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // STAT & NATURE CALCULATOR HANDLERS
    // =========================================================================

    fun setStatCalcPokemon(pokemon: Pokemon) {
        _uiState.update { state ->
            state.copy(
                statCalc = state.statCalc.copy(
                    selectedPokemon = pokemon,
                    isSelectingPokemon = false
                )
            )
        }
        recalculateStatCalc()
    }

    fun setStatCalcLevel(level: Int) {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(level = level.coerceIn(1, 100))) }
        recalculateStatCalc()
    }

    fun setStatCalcNature(nature: PokemonNature) {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(nature = nature)) }
        recalculateStatCalc()
    }

    fun setStatCalcIv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val ivs = state.statCalc.ivs.updateStat(stat, value.coerceIn(0, 31))
            state.copy(statCalc = state.statCalc.copy(ivs = ivs))
        }
        recalculateStatCalc()
    }

    fun setStatCalcAllIvs(value: Int) {
        _uiState.update { state ->
            val coerced = value.coerceIn(0, 31)
            val ivs = StatSpread(coerced, coerced, coerced, coerced, coerced, coerced)
            state.copy(statCalc = state.statCalc.copy(ivs = ivs))
        }
        recalculateStatCalc()
    }

    fun setStatCalcEv(stat: StatType, value: Int) {
        _uiState.update { state ->
            val currentEvs = state.statCalc.evs
            val otherEvsTotal = currentEvs.total - currentEvs.getStat(stat)
            val maxAllowedForThisStat = (508 - otherEvsTotal).coerceIn(0, 252)
            val clampedValue = value.coerceIn(0, maxAllowedForThisStat)
            val updated = currentEvs.updateStat(stat, clampedValue)
            state.copy(statCalc = state.statCalc.copy(evs = updated))
        }
        recalculateStatCalc()
    }

    fun setStatCalcEvPreset(stat1: StatType, stat2: StatType) {
        _uiState.update { state ->
            val updated = StatSpread().updateStat(stat1, 252).updateStat(stat2, 252).updateStat(StatType.HP, 4)
            state.copy(statCalc = state.statCalc.copy(evs = updated))
        }
        recalculateStatCalc()
    }

    fun setStatCalcItem(item: BattleItem) {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(heldItem = item)) }
        recalculateStatCalc()
    }

    fun setStatCalcAbility(ability: String?) {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(ability = ability)) }
        recalculateStatCalc()
    }

    fun setStatCalcTeraType(teraType: PokemonType?) {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(teraType = teraType)) }
        recalculateStatCalc()
    }

    fun triggerCopySuccess() {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(copySuccessTrigger = System.currentTimeMillis())) }
    }

    fun sendStatCalcToDamageCalc() {
        val statState = _uiState.value.statCalc
        val pokemon = statState.selectedPokemon ?: return
        _uiState.update { state ->
            val updatedAttacker = state.damageCalc.attacker.copy(
                pokemon = pokemon,
                level = statState.level,
                nature = statState.nature,
                ivs = statState.ivs,
                evs = statState.evs,
                item = statState.heldItem,
                teraType = statState.teraType
            )
            state.copy(
                activeTab = BattleTab.DAMAGE_CALC,
                damageCalc = state.damageCalc.copy(attacker = updatedAttacker)
            )
        }
        recalculateDamage()
    }

    fun openStatCalcPokemonPicker() {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(isSelectingPokemon = true)) }
    }

    fun closeStatCalcPokemonPicker() {
        _uiState.update { it.copy(statCalc = it.statCalc.copy(isSelectingPokemon = false)) }
    }

    private fun recalculateStatCalc() {
        val state = _uiState.value.statCalc
        val pokemon = state.selectedPokemon ?: return
        val baseStats = pokemon.stats ?: PokemonStats(100, 100, 100, 100, 100, 100)
        val isShedinja = pokemon.id == 292

        val calculated = StatCalculatorEngine.calculateAllStats(
            baseStats = baseStats,
            ivs = state.ivs,
            evs = state.evs,
            level = state.level,
            nature = state.nature,
            isShedinja = isShedinja
        )

        val showdownExport = StatCalculatorEngine.exportToShowdown(
            pokemon = pokemon,
            level = state.level,
            nature = state.nature,
            item = if (state.heldItem != BattleItem.NONE) state.heldItem.displayName else null,
            ability = state.ability,
            teraType = state.teraType?.capitalizedName,
            evs = state.evs,
            ivs = state.ivs
        )

        _uiState.update {
            it.copy(
                statCalc = it.statCalc.copy(
                    calculatedStats = calculated,
                    showdownText = showdownExport
                )
            )
        }
    }

    // =========================================================================
    // SPEED TIER CHECKER HANDLERS
    // =========================================================================

    fun setSpeedTierLevel(level: Int) {
        val clampedLevel = if (level == 100) 100 else 50
        _uiState.update { it.copy(speedTier = it.speedTier.copy(level = clampedLevel, userLevel = clampedLevel)) }
        recalculateSpeedLadder()
    }

    fun setSpeedTierSearchQuery(query: String) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(searchQuery = query)) }
    }

    fun setSpeedTierCategoryFilter(category: SpeedTierCategory?) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(selectedCategoryFilter = category)) }
    }

    fun setSpeedTierUserPokemon(pokemon: Pokemon) {
        _uiState.update { state ->
            state.copy(
                speedTier = state.speedTier.copy(
                    userPokemon = pokemon,
                    isSelectingUserPokemon = false
                )
            )
        }
        recalculateSpeedLadder()
    }

    fun setUserSpeedNature(nature: PokemonNature) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userNature = nature)) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedEv(ev: Int) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userEv = ev.coerceIn(0, 252))) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedIv(iv: Int) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userIv = iv.coerceIn(0, 31))) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedStatStage(stage: Int) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userStatStage = stage.coerceIn(-6, 6))) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedScarf(hasScarf: Boolean) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userHasScarf = hasScarf)) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedBooster(hasBooster: Boolean) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userHasBooster = hasBooster)) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedSwiftSwim(hasSwiftSwim: Boolean) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userHasSwiftSwim = hasSwiftSwim)) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedTailwind(hasTailwind: Boolean) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userHasTailwind = hasTailwind)) }
        recalculateSpeedLadder()
    }

    fun setUserSpeedParalyzed(isParalyzed: Boolean) {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(userIsParalyzed = isParalyzed)) }
        recalculateSpeedLadder()
    }

    fun openSpeedTierUserPokemonPicker() {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(isSelectingUserPokemon = true)) }
    }

    fun closeSpeedTierUserPokemonPicker() {
        _uiState.update { it.copy(speedTier = it.speedTier.copy(isSelectingUserPokemon = false)) }
    }

    private fun recalculateSpeedLadder() {
        val speedState = _uiState.value.speedTier
        val benchmarks = SpeedTierEngine.getCompetitiveBenchmarks(speedState.level)

        val ladder = if (speedState.userPokemon != null) {
            SpeedTierEngine.createLadderWithUserPokemon(
                benchmarks = benchmarks,
                userPokemon = speedState.userPokemon,
                level = speedState.userLevel,
                nature = speedState.userNature,
                iv = speedState.userIv,
                ev = speedState.userEv,
                statStage = speedState.userStatStage,
                hasScarf = speedState.userHasScarf,
                hasBoosterEnergy = speedState.userHasBooster,
                hasSwiftSwim = speedState.userHasSwiftSwim,
                hasTailwind = speedState.userHasTailwind,
                isParalyzed = speedState.userIsParalyzed
            )
        } else {
            benchmarks
        }

        _uiState.update { it.copy(speedTier = it.speedTier.copy(ladder = ladder)) }
    }

    // --- Catch Rate Calculator Actions ---

    fun setCatchCalcPokemon(pokemon: Pokemon) {
        _uiState.update {
            it.copy(
                catchCalc = it.catchCalc.copy(
                    targetPokemon = pokemon,
                    customCatchRate = null,
                    isSelectingPokemon = false
                )
            )
        }
        recalculateCatchRate()
    }

    fun setCatchCalcLevel(level: Int) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(level = level.coerceIn(1, 100))) }
        recalculateCatchRate()
    }

    fun setCatchCalcHpPercent(percent: Double) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(currentHpPercent = percent.coerceIn(1.0, 100.0))) }
        recalculateCatchRate()
    }

    fun setCatchCalcStatus(status: CatchStatusCondition) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(statusCondition = status)) }
        recalculateCatchRate()
    }

    fun setCatchCalcBall(ball: PokeBallType) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(selectedBall = ball)) }
        recalculateCatchRate()
    }

    fun setCatchCalcTurn(turn: Int) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(turnNumber = turn.coerceIn(1, 99))) }
        recalculateCatchRate()
    }

    fun setCatchCalcNightOrCave(isNight: Boolean) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isNightOrCave = isNight)) }
        recalculateCatchRate()
    }

    fun setCatchCalcWaterEncounter(isWater: Boolean) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isWaterEncounter = isWater)) }
        recalculateCatchRate()
    }

    fun setCatchCalcAlreadyInPokedex(isInDex: Boolean) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isAlreadyInPokedex = isInDex)) }
        recalculateCatchRate()
    }

    fun setCatchCalcPlayerLevel(level: Int) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(playerPokemonLevel = level.coerceIn(1, 100))) }
        recalculateCatchRate()
    }

    fun setCatchCalcOppositeGender(isOpposite: Boolean) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isOppositeGenderSameSpecies = isOpposite)) }
        recalculateCatchRate()
    }

    fun setCatchCalcCustomRate(rate: Int?) {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(customCatchRate = rate?.coerceIn(1, 255))) }
        recalculateCatchRate()
    }

    fun openCatchCalcPokemonPicker() {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isSelectingPokemon = true)) }
    }

    fun closeCatchCalcPokemonPicker() {
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(isSelectingPokemon = false)) }
    }

    private fun recalculateCatchRate() {
        val catchState = _uiState.value.catchCalc
        val pokemon = catchState.targetPokemon ?: return

        val context = CatchContext(
            pokemon = pokemon,
            targetLevel = catchState.level,
            currentHpPercent = catchState.currentHpPercent,
            statusCondition = catchState.statusCondition,
            activeBall = catchState.selectedBall,
            customCatchRate = catchState.customCatchRate,
            turnNumber = catchState.turnNumber,
            isNightOrCave = catchState.isNightOrCave,
            isWaterEncounter = catchState.isWaterEncounter,
            isAlreadyInPokedex = catchState.isAlreadyInPokedex,
            playerPokemonLevel = catchState.playerPokemonLevel,
            isOppositeGenderSameSpecies = catchState.isOppositeGenderSameSpecies
        )

        val result = CatchRateEngine.calculateCatchRate(context)
        _uiState.update { it.copy(catchCalc = it.catchCalc.copy(calculationResult = result)) }
    }

    private fun createDefaultPokemon(
        id: Int,
        name: String,
        number: Int,
        primaryType: PokemonType,
        secondaryType: PokemonType?,
        hp: Int,
        atk: Int,
        def: Int,
        spa: Int,
        spd: Int,
        spe: Int
    ): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            number = number,
            heightM = 1.0,
            weightKg = 30.0,
            primaryType = primaryType,
            secondaryType = secondaryType,
            spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
            stats = PokemonStats(hp, atk, def, spa, spd, spe)
        )
    }
}
