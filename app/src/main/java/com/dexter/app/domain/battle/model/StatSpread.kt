package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable

@Immutable
data class StatSpread(
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val spAttack: Int = 0,
    val spDefense: Int = 0,
    val speed: Int = 0
) {
    val total: Int
        get() = hp + attack + defense + spAttack + spDefense + speed

    fun getStat(stat: StatType): Int = when (stat) {
        StatType.HP -> hp
        StatType.ATTACK -> attack
        StatType.DEFENSE -> defense
        StatType.SP_ATTACK -> spAttack
        StatType.SP_DEFENSE -> spDefense
        StatType.SPEED -> speed
    }

    fun updateStat(stat: StatType, value: Int): StatSpread = when (stat) {
        StatType.HP -> copy(hp = value)
        StatType.ATTACK -> copy(attack = value)
        StatType.DEFENSE -> copy(defense = value)
        StatType.SP_ATTACK -> copy(spAttack = value)
        StatType.SP_DEFENSE -> copy(spDefense = value)
        StatType.SPEED -> copy(speed = value)
    }

    companion object {
        val ALL_31 = StatSpread(31, 31, 31, 31, 31, 31)
        val ALL_0 = StatSpread(0, 0, 0, 0, 0, 0)
        val SPECIAL_ATTACKER_IVS = StatSpread(31, 0, 31, 31, 31, 31) // 0 Atk for Foul Play/Confusion
        val TRICK_ROOM_IVS = StatSpread(31, 31, 31, 31, 31, 0) // 0 Spe for Trick Room
    }
}

@Immutable
data class CalculatedStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
) {
    val total: Int
        get() = hp + attack + defense + spAttack + spDefense + speed

    fun getStat(stat: StatType): Int = when (stat) {
        StatType.HP -> hp
        StatType.ATTACK -> attack
        StatType.DEFENSE -> defense
        StatType.SP_ATTACK -> spAttack
        StatType.SP_DEFENSE -> spDefense
        StatType.SPEED -> speed
    }
}

@Immutable
data class StatStages(
    val attack: Int = 0,
    val defense: Int = 0,
    val spAttack: Int = 0,
    val spDefense: Int = 0,
    val speed: Int = 0,
    val accuracy: Int = 0,
    val evasion: Int = 0
) {
    fun getMultiplier(stat: StatType): Double {
        val stage = when (stat) {
            StatType.ATTACK -> attack
            StatType.DEFENSE -> defense
            StatType.SP_ATTACK -> spAttack
            StatType.SP_DEFENSE -> spDefense
            StatType.SPEED -> speed
            StatType.HP -> 0
        }.coerceIn(-6, 6)

        return when {
            stage >= 0 -> (2 + stage) / 2.0
            else -> 2.0 / (2 - stage)
        }
    }

    fun updateStage(stat: StatType, delta: Int): StatStages {
        return when (stat) {
            StatType.ATTACK -> copy(attack = (attack + delta).coerceIn(-6, 6))
            StatType.DEFENSE -> copy(defense = (defense + delta).coerceIn(-6, 6))
            StatType.SP_ATTACK -> copy(spAttack = (spAttack + delta).coerceIn(-6, 6))
            StatType.SP_DEFENSE -> copy(spDefense = (spDefense + delta).coerceIn(-6, 6))
            StatType.SPEED -> copy(speed = (speed + delta).coerceIn(-6, 6))
            StatType.HP -> this
        }
    }

    fun setStage(stat: StatType, value: Int): StatStages {
        val coerced = value.coerceIn(-6, 6)
        return when (stat) {
            StatType.ATTACK -> copy(attack = coerced)
            StatType.DEFENSE -> copy(defense = coerced)
            StatType.SP_ATTACK -> copy(spAttack = coerced)
            StatType.SP_DEFENSE -> copy(spDefense = coerced)
            StatType.SPEED -> copy(speed = coerced)
            StatType.HP -> this
        }
    }
}
