package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable

@Immutable
data class BallCatchSummary(
    val ballType: PokeBallType,
    val catchChancePercent: Double,
    val expectedBalls: Double,
    val multiplierUsed: Double
)

@Immutable
data class CatchRateResult(
    val catchChancePercent: Double,
    val modifiedCatchValue: Int,
    val shakeProbability: Double,
    val expectedBalls: Double,
    val ballsFor50Percent: Int,
    val ballsFor90Percent: Int,
    val ballsFor95Percent: Int,
    val ballsFor99Percent: Int,
    val catchChanceWithinThrows: List<Pair<Int, Double>>, // e.g. [(1, 42.8%), (3, 81.3%), (5, 93.7%), (10, 99.6%)]
    val shake0Chance: Double,
    val shake1Chance: Double,
    val shake2Chance: Double,
    val shake3Chance: Double,
    val ballComparisonList: List<BallCatchSummary>
)
