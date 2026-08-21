package com.dexter.app.domain.battle.model

enum class StatType(val displayName: String, val shortName: String) {
    HP("HP", "HP"),
    ATTACK("Attack", "Atk"),
    DEFENSE("Defense", "Def"),
    SP_ATTACK("Sp. Attack", "SpA"),
    SP_DEFENSE("Sp. Defense", "SpD"),
    SPEED("Speed", "Spe")
}

enum class PokemonNature(
    val displayName: String,
    val increasedStat: StatType?,
    val decreasedStat: StatType?
) {
    HARDY("Hardy", null, null),
    LONELY("Lonely", StatType.ATTACK, StatType.DEFENSE),
    BRAVE("Brave", StatType.ATTACK, StatType.SPEED),
    ADAMANT("Adamant", StatType.ATTACK, StatType.SP_ATTACK),
    NAUGHTY("Naughty", StatType.ATTACK, StatType.SP_DEFENSE),

    BOLD("Bold", StatType.DEFENSE, StatType.ATTACK),
    DOCILE("Docile", null, null),
    RELAXED("Relaxed", StatType.DEFENSE, StatType.SPEED),
    IMPISH("Impish", StatType.DEFENSE, StatType.SP_ATTACK),
    LAX("Lax", StatType.DEFENSE, StatType.SP_DEFENSE),

    TIMID("Timid", StatType.SPEED, StatType.ATTACK),
    HASTY("Hasty", StatType.SPEED, StatType.DEFENSE),
    SERIOUS("Serious", null, null),
    JOLLY("Jolly", StatType.SPEED, StatType.SP_ATTACK),
    NAIVE("Naive", StatType.SPEED, StatType.SP_DEFENSE),

    MODEST("Modest", StatType.SP_ATTACK, StatType.ATTACK),
    MILD("Mild", StatType.SP_ATTACK, StatType.DEFENSE),
    QUIET("Quiet", StatType.SP_ATTACK, StatType.SPEED),
    BASHFUL("Bashful", null, null),
    RASH("Rash", StatType.SP_ATTACK, StatType.SP_DEFENSE),

    CALM("Calm", StatType.SP_DEFENSE, StatType.ATTACK),
    GENTLE("Gentle", StatType.SP_DEFENSE, StatType.DEFENSE),
    SASSY("Sassy", StatType.SP_DEFENSE, StatType.SPEED),
    CAREFUL("Careful", StatType.SP_DEFENSE, StatType.SP_ATTACK),
    QUIRKY("Quirky", null, null);

    val isNeutral: Boolean
        get() = increasedStat == null || increasedStat == decreasedStat

    fun getMultiplier(stat: StatType): Double {
        if (stat == StatType.HP) return 1.0
        return when {
            stat == increasedStat && stat != decreasedStat -> 1.1
            stat == decreasedStat && stat != increasedStat -> 0.9
            else -> 1.0
        }
    }

    val description: String
        get() = when {
            isNeutral -> "Neutral (No stat changes)"
            else -> "+10% ${increasedStat?.shortName}, -10% ${decreasedStat?.shortName}"
        }

    companion object {
        fun fromString(name: String): PokemonNature {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: HARDY
        }
    }
}
