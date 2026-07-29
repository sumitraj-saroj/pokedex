package com.dexter.app.data.repository

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.dexter.app.data.local.EvolutionDao
import com.dexter.app.data.local.EvolutionEntity
import com.dexter.app.data.local.MoveDetailEntity
import com.dexter.app.data.local.PokemonAbilityDao
import com.dexter.app.data.local.PokemonAbilityEntity
import com.dexter.app.data.local.PokemonDao
import com.dexter.app.data.local.PokemonEntity
import com.dexter.app.data.local.PokemonFormDao
import com.dexter.app.data.local.PokemonFormEntity
import com.dexter.app.data.local.PokemonMoveDao
import com.dexter.app.data.local.PokemonMoveEntity
import com.dexter.app.data.local.PokemonStatsEntity
import com.dexter.app.data.local.TeamMemberDao
import com.dexter.app.data.local.TeamMemberEntity
import com.dexter.app.data.local.UserCollectionDao
import com.dexter.app.data.local.UserCollectionEntity
import com.dexter.app.data.remote.PokeApiService
import com.dexter.app.domain.mapper.mapRemoteToEntities
import com.dexter.app.domain.mapper.parseEvolutionChain
import com.dexter.app.domain.mapper.parseIdFromUrl
import com.dexter.app.domain.mapper.toDomain
import com.dexter.app.domain.model.EvolutionNode
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonAbility
import com.dexter.app.domain.model.PokemonForm
import com.dexter.app.domain.model.PokemonMove
import com.dexter.app.domain.model.SyncState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "pokemon_sync_preferences")
private val INITIAL_SYNC_COMPLETED_KEY = booleanPreferencesKey("initial_sync_completed")

interface PokemonRepository {
    val syncState: StateFlow<SyncState>
    fun observeAllPokemon(): Flow<List<Pokemon>>
    fun observePokemonById(id: Int): Flow<Pokemon?>
    fun observeEvolutionChain(chainId: Int): Flow<List<EvolutionNode>>
    fun observeMovesForPokemon(pokemonId: Int): Flow<List<PokemonMove>>
    fun observeAbilitiesForPokemon(pokemonId: Int): Flow<List<PokemonAbility>>
    fun observeFormsForPokemon(basePokemonId: Int): Flow<List<PokemonForm>>
    fun observeTeamMembers(): Flow<Map<Int, Pokemon>>
    suspend fun setTeamMember(slot: Int, pokemonId: Int)
    suspend fun removeTeamMember(slot: Int)
    suspend fun clearTeam()
    suspend fun ensurePokemonDetailExtras(pokemonId: Int)
    suspend fun syncPokemonData(forceResync: Boolean = false)
    suspend fun toggleCaught(pokemonId: Int, isCaught: Boolean)
    suspend fun toggleFavorite(pokemonId: Int, isFavorite: Boolean)
}

private data class PokemonSyncResult(
    val pokemon: PokemonEntity,
    val stats: PokemonStatsEntity,
    val evolutions: List<EvolutionEntity>,
    val abilities: List<PokemonAbilityEntity>,
    val moveEntities: List<PokemonMoveEntity>,
    val moveDetails: List<MoveDetailEntity>,
    val forms: List<PokemonFormEntity>
)

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pokemonDao: PokemonDao,
    private val userCollectionDao: UserCollectionDao,
    private val evolutionDao: EvolutionDao,
    private val pokemonMoveDao: PokemonMoveDao,
    private val pokemonAbilityDao: PokemonAbilityDao,
    private val pokemonFormDao: PokemonFormDao,
    private val teamMemberDao: TeamMemberDao,
    private val pokeApiService: PokeApiService
) : PokemonRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    override fun observeAllPokemon(): Flow<List<Pokemon>> {
        return pokemonDao.observeAllPokemon().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observePokemonById(id: Int): Flow<Pokemon?> {
        return pokemonDao.observePokemonById(id).map { it?.toDomain() }
    }

    override fun observeEvolutionChain(chainId: Int): Flow<List<EvolutionNode>> {
        return evolutionDao.observeEvolutionChain(chainId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeMovesForPokemon(pokemonId: Int): Flow<List<PokemonMove>> {
        return pokemonMoveDao.observeMovesForPokemon(pokemonId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeAbilitiesForPokemon(pokemonId: Int): Flow<List<PokemonAbility>> {
        return pokemonAbilityDao.observeAbilitiesForPokemon(pokemonId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeFormsForPokemon(basePokemonId: Int): Flow<List<PokemonForm>> {
        return pokemonFormDao.observeFormsForPokemon(basePokemonId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun ensurePokemonDetailExtras(pokemonId: Int) {
        withContext(Dispatchers.IO) {
            val movesCount = pokemonMoveDao.getMoveCountForPokemon(pokemonId)
            val abilitiesCount = pokemonAbilityDao.getAbilityCountForPokemon(pokemonId)
            if (movesCount > 0 && abilitiesCount > 0) {
                return@withContext
            }

            try {
                val detail = pokeApiService.getPokemonDetail(pokemonId)
                val species = try { pokeApiService.getPokemonSpecies(pokemonId) } catch (e: Exception) { null }

                // 1. Process evolution chain if available
                val chainId = parseIdFromUrl(species?.evolutionChain?.url)
                if (evolutionDao.getEvolutionChain(chainId).isEmpty()) {
                    try {
                        val evoResponse = pokeApiService.getEvolutionChain(chainId)
                        val evoEntities = parseEvolutionChain(evoResponse)
                        evolutionDao.insertEvolutions(evoEntities)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 2. Process Abilities
                val abilitiesList = detail.abilities.map { slotDto ->
                    val abilityName = slotDto.ability.name
                    val effectText = try {
                        val abilityRes = pokeApiService.getAbilityDetail(abilityName)
                        abilityRes.effectEntries.firstOrNull { it.language.name == "en" }?.shortEffect
                            ?: abilityRes.flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText
                            ?: "No description available."
                    } catch (e: Exception) {
                        "No description available."
                    }
                    PokemonAbilityEntity(
                        id = "${pokemonId}_$abilityName",
                        pokemonId = pokemonId,
                        abilityName = abilityName,
                        displayName = abilityName.replace("-", " ").titlecaseWords(),
                        isHidden = slotDto.isHidden,
                        effectText = effectText.replace('\n', ' ').replace('\u000c', ' ').trim()
                    )
                }
                pokemonAbilityDao.insertAbilities(abilitiesList)

                // 3. Process Moves (Limit to first 30 level-up / TM moves for fast loading)
                val moveEntities = mutableListOf<PokemonMoveEntity>()
                val moveDetailEntities = mutableListOf<MoveDetailEntity>()

                detail.moves.take(30).forEach { moveSlot ->
                    val moveName = moveSlot.move.name
                    val firstVersion = moveSlot.versionGroupDetails.firstOrNull()
                    val learnMethod = firstVersion?.moveLearnMethod?.name ?: "level-up"
                    val levelLearned = firstVersion?.levelLearnedAt ?: 0

                    moveEntities.add(
                        PokemonMoveEntity(
                            id = "${pokemonId}_$moveName",
                            pokemonId = pokemonId,
                            moveName = moveName,
                            learnMethod = learnMethod,
                            levelLearnedAt = levelLearned
                        )
                    )

                    try {
                        val moveRes = pokeApiService.getMoveDetail(moveName)
                        val effect = moveRes.effectEntries.firstOrNull { it.language.name == "en" }?.shortEffect
                            ?: moveRes.flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText
                            ?: "No effect description."

                        moveDetailEntities.add(
                            MoveDetailEntity(
                                moveName = moveName,
                                displayName = moveName.replace("-", " ").titlecaseWords(),
                                type = moveRes.type.name,
                                power = moveRes.power,
                                accuracy = moveRes.accuracy,
                                damageClass = moveRes.damageClass?.name ?: "physical",
                                effectText = effect.replace('\n', ' ').replace('\u000c', ' ').trim()
                            )
                        )
                    } catch (e: Exception) {
                        // fallback if move detail endpoint fails
                        moveDetailEntities.add(
                            MoveDetailEntity(
                                moveName = moveName,
                                displayName = moveName.replace("-", " ").titlecaseWords(),
                                type = "normal",
                                power = null,
                                accuracy = null,
                                damageClass = "physical",
                                effectText = "No details available."
                            )
                        )
                    }
                }
                pokemonMoveDao.insertMoveDetails(moveDetailEntities)
                pokemonMoveDao.insertPokemonMoves(moveEntities)

                // 4. Process Form Varieties (Alolan, Galarian, Mega, etc.)
                if (species != null && species.varieties.isNotEmpty()) {
                    val formEntities = mutableListOf<PokemonFormEntity>()
                    species.varieties.forEach { variety ->
                        if (!variety.isDefault) {
                            try {
                                val formDetail = pokeApiService.getPokemonDetailByName(variety.pokemon.name)
                                val (formPEntity, formSEntity) = mapRemoteToEntities(formDetail, species)
                                formEntities.add(
                                    PokemonFormEntity(
                                        id = formPEntity.id,
                                        basePokemonId = pokemonId,
                                        formName = variety.pokemon.name,
                                        displayName = variety.pokemon.name.replace("-", " ").titlecaseWords(),
                                        primaryType = formPEntity.primaryType,
                                        secondaryType = formPEntity.secondaryType,
                                        spriteUrl = formPEntity.spriteUrl,
                                        shinySpriteUrl = formPEntity.shinySpriteUrl,
                                        officialArtworkUrl = formPEntity.officialArtworkUrl,
                                        shinyArtworkUrl = formPEntity.shinyArtworkUrl,
                                        homeArtworkUrl = formPEntity.homeArtworkUrl,
                                        animatedSpriteUrl = formPEntity.animatedSpriteUrl,
                                        cryAudioUrl = formPEntity.cryAudioUrl,
                                        heightM = formPEntity.heightM,
                                        weightKg = formPEntity.weightKg,
                                        hp = formSEntity.hp,
                                        attack = formSEntity.attack,
                                        defense = formSEntity.defense,
                                        spAttack = formSEntity.spAttack,
                                        spDefense = formSEntity.spDefense,
                                        speed = formSEntity.speed
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (formEntities.isNotEmpty()) {
                        pokemonFormDao.insertForms(formEntities)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun syncPokemonData(forceResync: Boolean) {
        withContext(Dispatchers.IO) {
            if (!forceResync) {
                val prefs = context.syncDataStore.data.first()
                if (prefs[INITIAL_SYNC_COMPLETED_KEY] == true) {
                    _syncState.value = SyncState.Completed
                    return@withContext
                }

                val existingCount = pokemonDao.getPokemonCount()
                val totalMovesCount = pokemonMoveDao.getTotalMoveCount()
                if (existingCount >= 1025 && totalMovesCount > 0) {
                    context.syncDataStore.edit { it[INITIAL_SYNC_COMPLETED_KEY] = true }
                    _syncState.value = SyncState.Completed
                    return@withContext
                }
            }

            _syncState.value = SyncState.Syncing(0, 1025)

            try {
                val targetIds = (1..1025).toList()
                val total = targetIds.size
                var completedCount = 0

                val semaphore = Semaphore(15)
                val fetchedChainIds = ConcurrentHashMap.newKeySet<Int>()
                val abilityEffectCache = ConcurrentHashMap<String, String>()
                val moveDetailCache = ConcurrentHashMap<String, MoveDetailEntity>()

                val imageLoader = context.imageLoader
                fun preCacheImage(url: String?) {
                    if (!url.isNullOrBlank()) {
                        val req = ImageRequest.Builder(context).data(url).build()
                        imageLoader.enqueue(req)
                    }
                }

                suspend fun getAbilityEffect(abilityName: String): String {
                    abilityEffectCache[abilityName]?.let { return it }
                    val effectText = try {
                        val abilityRes = pokeApiService.getAbilityDetail(abilityName)
                        abilityRes.effectEntries.firstOrNull { it.language.name == "en" }?.shortEffect
                            ?: abilityRes.flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText
                            ?: "No description available."
                    } catch (e: Exception) {
                        "No description available."
                    }.replace('\n', ' ').replace('\u000c', ' ').trim()
                    abilityEffectCache[abilityName] = effectText
                    return effectText
                }

                suspend fun getMoveDetail(moveName: String): MoveDetailEntity {
                    moveDetailCache[moveName]?.let { return it }
                    val detailEntity = try {
                        val moveRes = pokeApiService.getMoveDetail(moveName)
                        val effect = moveRes.effectEntries.firstOrNull { it.language.name == "en" }?.shortEffect
                            ?: moveRes.flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText
                            ?: "No effect description."
                        MoveDetailEntity(
                            moveName = moveName,
                            displayName = moveName.replace("-", " ").titlecaseWords(),
                            type = moveRes.type.name,
                            power = moveRes.power,
                            accuracy = moveRes.accuracy,
                            damageClass = moveRes.damageClass?.name ?: "physical",
                            effectText = effect.replace('\n', ' ').replace('\u000c', ' ').trim()
                        )
                    } catch (e: Exception) {
                        MoveDetailEntity(
                            moveName = moveName,
                            displayName = moveName.replace("-", " ").titlecaseWords(),
                            type = "normal",
                            power = null,
                            accuracy = null,
                            damageClass = "physical",
                            effectText = "No details available."
                        )
                    }
                    moveDetailCache[moveName] = detailEntity
                    return detailEntity
                }

                targetIds.chunked(25).forEach { chunk ->
                    coroutineScope {
                        val pokemonBuffer = mutableListOf<PokemonEntity>()
                        val statsBuffer = mutableListOf<PokemonStatsEntity>()
                        val evolutionBuffer = mutableListOf<EvolutionEntity>()
                        val abilityBuffer = mutableListOf<PokemonAbilityEntity>()
                        val moveEntityBuffer = mutableListOf<PokemonMoveEntity>()
                        val moveDetailBuffer = mutableListOf<MoveDetailEntity>()
                        val formBuffer = mutableListOf<PokemonFormEntity>()

                        val deferreds = chunk.map { id ->
                            async {
                                semaphore.withPermit {
                                    try {
                                        val detail = pokeApiService.getPokemonDetail(id)
                                        val species = try {
                                            pokeApiService.getPokemonSpecies(id)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        val (pEntity, sEntity) = mapRemoteToEntities(detail, species)

                                        // Image pre-caching for base Pokemon (Official, Shiny, HOME, Animated, Sprites)
                                        preCacheImage(pEntity.officialArtworkUrl)
                                        preCacheImage(pEntity.shinyArtworkUrl)
                                        preCacheImage(pEntity.homeArtworkUrl)
                                        preCacheImage(pEntity.animatedSpriteUrl)
                                        preCacheImage(pEntity.spriteUrl)
                                        preCacheImage(pEntity.shinySpriteUrl)

                                        // Evolution chain
                                        val chainId = pEntity.evolutionChainId
                                        var evos: List<EvolutionEntity> = emptyList()
                                        if (chainId != null && fetchedChainIds.add(chainId)) {
                                            try {
                                                val evoResponse = pokeApiService.getEvolutionChain(chainId)
                                                evos = parseEvolutionChain(evoResponse)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        }

                                        // Abilities
                                        val abs = detail.abilities.map { slotDto ->
                                            val abilityName = slotDto.ability.name
                                            val effect = getAbilityEffect(abilityName)
                                            PokemonAbilityEntity(
                                                id = "${id}_$abilityName",
                                                pokemonId = id,
                                                abilityName = abilityName,
                                                displayName = abilityName.replace("-", " ").titlecaseWords(),
                                                isHidden = slotDto.isHidden,
                                                effectText = effect
                                            )
                                        }

                                        // Moves (take top 30)
                                        val mEntities = mutableListOf<PokemonMoveEntity>()
                                        val mDetails = mutableListOf<MoveDetailEntity>()
                                        detail.moves.take(30).forEach { moveSlot ->
                                            val moveName = moveSlot.move.name
                                            val firstVersion = moveSlot.versionGroupDetails.firstOrNull()
                                            val learnMethod = firstVersion?.moveLearnMethod?.name ?: "level-up"
                                            val levelLearned = firstVersion?.levelLearnedAt ?: 0

                                            mEntities.add(
                                                PokemonMoveEntity(
                                                    id = "${id}_$moveName",
                                                    pokemonId = id,
                                                    moveName = moveName,
                                                    learnMethod = learnMethod,
                                                    levelLearnedAt = levelLearned
                                                )
                                            )
                                            mDetails.add(getMoveDetail(moveName))
                                        }

                                        // Forms
                                        val fEntities = mutableListOf<PokemonFormEntity>()
                                        if (species != null && species.varieties.isNotEmpty()) {
                                            species.varieties.forEach { variety ->
                                                if (!variety.isDefault) {
                                                    try {
                                                        val formDetail = pokeApiService.getPokemonDetailByName(variety.pokemon.name)
                                                        val (formPEntity, formSEntity) = mapRemoteToEntities(formDetail, species)
                                                        preCacheImage(formPEntity.officialArtworkUrl)
                                                        preCacheImage(formPEntity.shinyArtworkUrl)
                                                        preCacheImage(formPEntity.homeArtworkUrl)
                                                        preCacheImage(formPEntity.animatedSpriteUrl)
                                                        preCacheImage(formPEntity.spriteUrl)

                                                        fEntities.add(
                                                            PokemonFormEntity(
                                                                id = formPEntity.id,
                                                                basePokemonId = id,
                                                                formName = variety.pokemon.name,
                                                                displayName = variety.pokemon.name.replace("-", " ").titlecaseWords(),
                                                                primaryType = formPEntity.primaryType,
                                                                secondaryType = formPEntity.secondaryType,
                                                                spriteUrl = formPEntity.spriteUrl,
                                                                shinySpriteUrl = formPEntity.shinySpriteUrl,
                                                                officialArtworkUrl = formPEntity.officialArtworkUrl,
                                                                shinyArtworkUrl = formPEntity.shinyArtworkUrl,
                                                                homeArtworkUrl = formPEntity.homeArtworkUrl,
                                                                animatedSpriteUrl = formPEntity.animatedSpriteUrl,
                                                                cryAudioUrl = formPEntity.cryAudioUrl,
                                                                heightM = formPEntity.heightM,
                                                                weightKg = formPEntity.weightKg,
                                                                hp = formSEntity.hp,
                                                                attack = formSEntity.attack,
                                                                defense = formSEntity.defense,
                                                                spAttack = formSEntity.spAttack,
                                                                spDefense = formSEntity.spDefense,
                                                                speed = formSEntity.speed
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        // ignore
                                                    }
                                                }
                                            }
                                        }

                                        PokemonSyncResult(pEntity, sEntity, evos, abs, mEntities, mDetails, fEntities)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        }
                        val results = deferreds.awaitAll().filterNotNull()

                        results.forEach { res ->
                            pokemonBuffer.add(res.pokemon)
                            statsBuffer.add(res.stats)
                            evolutionBuffer.addAll(res.evolutions)
                            abilityBuffer.addAll(res.abilities)
                            moveEntityBuffer.addAll(res.moveEntities)
                            moveDetailBuffer.addAll(res.moveDetails)
                            formBuffer.addAll(res.forms)
                        }

                        if (pokemonBuffer.isNotEmpty()) {
                            pokemonDao.insertPokemonList(pokemonBuffer)
                            pokemonDao.insertStatsList(statsBuffer)
                        }
                        if (evolutionBuffer.isNotEmpty()) {
                            evolutionDao.insertEvolutions(evolutionBuffer)
                        }
                        if (abilityBuffer.isNotEmpty()) {
                            pokemonAbilityDao.insertAbilities(abilityBuffer)
                        }
                        if (moveDetailBuffer.isNotEmpty()) {
                            pokemonMoveDao.insertMoveDetails(moveDetailBuffer)
                        }
                        if (moveEntityBuffer.isNotEmpty()) {
                            pokemonMoveDao.insertPokemonMoves(moveEntityBuffer)
                        }
                        if (formBuffer.isNotEmpty()) {
                            pokemonFormDao.insertForms(formBuffer)
                        }

                        synchronized(this@PokemonRepositoryImpl) {
                            completedCount += chunk.size
                            _syncState.value = SyncState.Syncing(completedCount.coerceAtMost(total), total)
                        }
                    }
                }

                context.syncDataStore.edit { it[INITIAL_SYNC_COMPLETED_KEY] = true }
                _syncState.value = SyncState.Completed
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.localizedMessage ?: "Failed to sync Pokémon data")
            }
        }
    }

    override suspend fun toggleCaught(pokemonId: Int, isCaught: Boolean) {
        withContext(Dispatchers.IO) {
            val updated = UserCollectionEntity(
                pokemonId = pokemonId,
                isCaught = isCaught
            )
            userCollectionDao.upsertUserCollection(updated)
        }
    }

    override suspend fun toggleFavorite(pokemonId: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            val updated = UserCollectionEntity(
                pokemonId = pokemonId,
                isFavorite = isFavorite
            )
            userCollectionDao.upsertUserCollection(updated)
        }
    }

    override fun observeTeamMembers(): Flow<Map<Int, Pokemon>> {
        return combine(
            teamMemberDao.observeTeamMembers(),
            observeAllPokemon()
        ) { members, pokemonList ->
            val pokemonMap = pokemonList.associateBy { it.id }
            members.mapNotNull { member ->
                val pokemon = pokemonMap[member.pokemonId]
                if (pokemon != null) member.slot to pokemon else null
            }.toMap()
        }
    }

    override suspend fun setTeamMember(slot: Int, pokemonId: Int) {
        withContext(Dispatchers.IO) {
            teamMemberDao.insertOrUpdateTeamMember(TeamMemberEntity(slot = slot, pokemonId = pokemonId))
        }
    }

    override suspend fun removeTeamMember(slot: Int) {
        withContext(Dispatchers.IO) {
            teamMemberDao.deleteTeamMember(slot)
        }
    }

    override suspend fun clearTeam() {
        withContext(Dispatchers.IO) {
            teamMemberDao.clearTeam()
        }
    }

    private fun String.titlecaseWords(): String {
        return this.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
