package com.dexter.app.domain.mapper

import com.dexter.app.data.local.EvolutionEntity
import com.dexter.app.data.local.PokemonAbilityEntity
import com.dexter.app.data.local.PokemonEntity
import com.dexter.app.data.local.PokemonFormEntity
import com.dexter.app.data.local.PokemonMoveWithDetail
import com.dexter.app.data.local.PokemonStatsEntity
import com.dexter.app.data.local.PokemonWithDetails
import com.dexter.app.data.remote.ChainLinkDto
import com.dexter.app.data.remote.EvolutionChainResponseDto
import com.dexter.app.data.remote.PokemonDetailDto
import com.dexter.app.data.remote.PokemonSpeciesDto
import com.dexter.app.domain.model.EvolutionNode
import com.dexter.app.domain.model.MoveDetail
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonAbility
import com.dexter.app.domain.model.PokemonForm
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.UserCollection

fun PokemonWithDetails.toDomain(): Pokemon {
    val primType = PokemonType.fromString(pokemon.primaryType)
    val secType = pokemon.secondaryType?.let { PokemonType.fromString(it) }

    val domainStats = stats?.let {
        PokemonStats(
            hp = it.hp,
            attack = it.attack,
            defense = it.defense,
            spAttack = it.spAttack,
            spDefense = it.spDefense,
            speed = it.speed
        )
    }

    val domainCollection = collection?.let {
        UserCollection(
            pokemonId = it.pokemonId,
            isCaught = it.isCaught,
            isFavorite = it.isFavorite,
            shinyOwned = it.shinyOwned,
            isAlpha = it.isAlpha,
            ashOwned = it.ashOwned
        )
    }

    return Pokemon(
        id = pokemon.id,
        name = pokemon.name,
        number = pokemon.number,
        heightM = pokemon.heightM,
        weightKg = pokemon.weightKg,
        primaryType = primType,
        secondaryType = secType,
        isLegendary = pokemon.isLegendary,
        isMythical = pokemon.isMythical,
        category = pokemon.category,
        flavorText = pokemon.flavorText,
        spriteUrl = pokemon.spriteUrl,
        shinySpriteUrl = pokemon.shinySpriteUrl,
        officialArtworkUrl = pokemon.officialArtworkUrl,
        shinyArtworkUrl = pokemon.shinyArtworkUrl,
        homeArtworkUrl = pokemon.homeArtworkUrl,
        animatedSpriteUrl = pokemon.animatedSpriteUrl,
        pixelSpriteUrl = pokemon.pixelSpriteUrl,
        cryAudioUrl = pokemon.cryAudioUrl,
        generation = pokemon.generation,
        evolutionChainId = pokemon.evolutionChainId,
        stats = domainStats,
        collection = domainCollection
    )
}

fun EvolutionEntity.toDomain(): EvolutionNode {
    return EvolutionNode(
        id = id,
        chainId = chainId,
        speciesId = speciesId,
        speciesName = speciesName,
        evolvesFromSpeciesId = evolvesFromSpeciesId,
        trigger = trigger,
        conditionText = conditionText,
        spriteUrl = spriteUrl
    )
}

fun PokemonMoveWithDetail.toDomain(): PokemonMove {
    val domainDetail = detail?.let {
        MoveDetail(
            moveName = it.moveName,
            displayName = it.displayName,
            type = PokemonType.fromString(it.type),
            power = it.power,
            accuracy = it.accuracy,
            damageClass = it.damageClass,
            effectText = it.effectText
        )
    }
    return PokemonMove(
        id = move.id,
        pokemonId = move.pokemonId,
        moveName = move.moveName,
        learnMethod = move.learnMethod,
        levelLearnedAt = move.levelLearnedAt,
        detail = domainDetail
    )
}

fun PokemonAbilityEntity.toDomain(): PokemonAbility {
    return PokemonAbility(
        id = id,
        pokemonId = pokemonId,
        abilityName = abilityName,
        displayName = displayName,
        isHidden = isHidden,
        effectText = effectText
    )
}

fun PokemonFormEntity.toDomain(): PokemonForm {
    return PokemonForm(
        id = id,
        basePokemonId = basePokemonId,
        formName = formName,
        displayName = displayName,
        primaryType = PokemonType.fromString(primaryType),
        secondaryType = secondaryType?.let { PokemonType.fromString(it) },
        spriteUrl = spriteUrl,
        shinySpriteUrl = shinySpriteUrl,
        officialArtworkUrl = officialArtworkUrl,
        shinyArtworkUrl = shinyArtworkUrl,
        homeArtworkUrl = homeArtworkUrl,
        animatedSpriteUrl = animatedSpriteUrl,
        cryAudioUrl = cryAudioUrl,
        heightM = heightM,
        weightKg = weightKg,
        stats = PokemonStats(
            hp = hp,
            attack = attack,
            defense = defense,
            spAttack = spAttack,
            spDefense = spDefense,
            speed = speed
        )
    )
}

fun parseGenerationNumber(genName: String?): Int {
    if (genName == null) return 1
    return when {
        genName.contains("generation-i", ignoreCase = true) && !genName.contains("generation-iv", ignoreCase = true) && !genName.contains("generation-ix", ignoreCase = true) -> 1
        genName.contains("generation-ii", ignoreCase = true) && !genName.contains("generation-iii", ignoreCase = true) -> 2
        genName.contains("generation-iii", ignoreCase = true) -> 3
        genName.contains("generation-iv", ignoreCase = true) -> 4
        genName.contains("generation-v", ignoreCase = true) -> 5
        genName.contains("generation-vi", ignoreCase = true) -> 6
        genName.contains("generation-vii", ignoreCase = true) -> 7
        genName.contains("generation-viii", ignoreCase = true) -> 8
        genName.contains("generation-ix", ignoreCase = true) -> 9
        else -> 1
    }
}

fun parseIdFromUrl(url: String?): Int {
    if (url == null) return 1
    val trimmed = url.trimEnd('/')
    return trimmed.substringAfterLast('/').toIntOrNull() ?: 1
}

fun mapRemoteToEntities(
    detail: PokemonDetailDto,
    species: PokemonSpeciesDto?
): Pair<PokemonEntity, PokemonStatsEntity> {
    val sortedTypes = detail.types.sortedBy { it.slot }
    val primaryTypeStr = sortedTypes.firstOrNull()?.type?.name ?: "normal"
    val secondaryTypeStr = sortedTypes.getOrNull(1)?.type?.name

    val englishFlavorText = species?.flavorTextEntries
        ?.firstOrNull { it.language.name == "en" }
        ?.flavorText
        ?.replace('\n', ' ')
        ?.replace('\u000c', ' ')
        ?.replace('\r', ' ')
        ?.trim() ?: ""

    val englishCategory = species?.genera
        ?.firstOrNull { it.language.name == "en" }
        ?.genus
        ?.replace("Pokémon", "")
        ?.trim() ?: "Pokémon"

    val genNumber = parseGenerationNumber(species?.generation?.name)
    val chainId = parseIdFromUrl(species?.evolutionChain?.url)

    val officialArtwork = detail.sprites?.other?.officialArtwork?.frontDefault
    val officialShinyArtwork = detail.sprites?.other?.officialArtwork?.frontShiny
    val homeArtwork = detail.sprites?.other?.home?.frontDefault
    val animatedSprite = detail.sprites?.other?.showdown?.frontDefault
    val sprite = detail.sprites?.frontDefault ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${detail.id}.png"
    val shinySprite = detail.sprites?.frontShiny
    val cryUrl = detail.cries?.latest ?: detail.cries?.legacy

    val pokemonEntity = PokemonEntity(
        id = detail.id,
        name = detail.name,
        number = detail.id,
        heightM = detail.height / 10.0,
        weightKg = detail.weight / 10.0,
        primaryType = primaryTypeStr,
        secondaryType = secondaryTypeStr,
        isLegendary = species?.isLegendary ?: false,
        isMythical = species?.isMythical ?: false,
        category = englishCategory,
        flavorText = englishFlavorText,
        spriteUrl = sprite,
        shinySpriteUrl = shinySprite,
        officialArtworkUrl = officialArtwork ?: sprite,
        shinyArtworkUrl = officialShinyArtwork ?: shinySprite,
        homeArtworkUrl = homeArtwork ?: officialArtwork ?: sprite,
        animatedSpriteUrl = animatedSprite ?: sprite,
        pixelSpriteUrl = sprite,
        cryAudioUrl = cryUrl,
        generation = genNumber,
        evolutionChainId = chainId
    )

    fun getStatValue(statName: String): Int {
        return detail.stats.firstOrNull { it.stat.name.equals(statName, ignoreCase = true) }?.baseStat ?: 0
    }

    val statsEntity = PokemonStatsEntity(
        pokemonId = detail.id,
        hp = getStatValue("hp"),
        attack = getStatValue("attack"),
        defense = getStatValue("defense"),
        spAttack = getStatValue("special-attack"),
        spDefense = getStatValue("special-defense"),
        speed = getStatValue("speed")
    )

    return Pair(pokemonEntity, statsEntity)
}

fun parseEvolutionChain(response: EvolutionChainResponseDto): List<EvolutionEntity> {
    val result = mutableListOf<EvolutionEntity>()
    val chainId = response.id

    fun traverse(node: ChainLinkDto, parentId: Int?) {
        val speciesId = parseIdFromUrl(node.species.url)
        val speciesName = node.species.name
        val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$speciesId.png"

        val firstDetail = node.evolutionDetails.firstOrNull()
        val trigger = firstDetail?.trigger?.name ?: if (parentId == null) "Base" else "Level Up"

        val conditionParts = mutableListOf<String>()
        firstDetail?.minLevel?.let { conditionParts.add("Lv. $it") }
        firstDetail?.item?.let { conditionParts.add(it.name.replace("-", " ").titlecaseWords()) }
        firstDetail?.knownMove?.let { conditionParts.add("Move: ${it.name.replace("-", " ").titlecaseWords()}") }
        firstDetail?.minHappiness?.let { conditionParts.add("Friendship") }
        firstDetail?.timeOfDay?.takeIf { it.isNotBlank() }?.let { conditionParts.add(it.titlecaseWords()) }

        val conditionText = if (conditionParts.isEmpty()) {
            if (parentId == null) "Base Form" else trigger.replace("-", " ").titlecaseWords()
        } else {
            conditionParts.joinToString(", ")
        }

        result.add(
            EvolutionEntity(
                id = "${chainId}_$speciesId",
                chainId = chainId,
                speciesId = speciesId,
                speciesName = speciesName,
                evolvesFromSpeciesId = parentId,
                trigger = trigger,
                conditionText = conditionText,
                spriteUrl = spriteUrl
            )
        )

        for (child in node.evolvesTo) {
            traverse(child, speciesId)
        }
    }

    traverse(response.chain, null)
    return result
}

private fun String.titlecaseWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
