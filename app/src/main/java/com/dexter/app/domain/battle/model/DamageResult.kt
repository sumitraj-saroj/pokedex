package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable

@Immutable
data class DamageRollResult(
    val rolls: List<Int>,
    val minDamage: Int,
    val maxDamage: Int,
    val avgDamage: Double,
    val defenderMaxHp: Int,
    val minPercent: Double,
    val maxPercent: Double,
    val avgPercent: Double,
    val koChanceText: String,
    val summaryFormulaText: String,
    val typeMultiplier: Double,
    val isStab: Boolean,
    val isCritical: Boolean,
    val hitsTakenToKo: IntRange,
    val ohkoChance: Double,
    val twoHkoChance: Double,
    val threeHkoChance: Double,
    val stealthRockDamage: Int = 0,
    val spikesDamage: Int = 0,
    val leftoversHeal: Int = 0
)
