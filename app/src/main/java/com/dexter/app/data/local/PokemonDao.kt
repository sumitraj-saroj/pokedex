package com.dexter.app.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class PokemonWithDetails(
    @Embedded val pokemon: PokemonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemonId"
    )
    val stats: PokemonStatsEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "pokemonId"
    )
    val collection: UserCollectionEntity?
)

data class PokemonMoveWithDetail(
    @Embedded val move: PokemonMoveEntity,
    @Relation(
        parentColumn = "moveName",
        entityColumn = "moveName"
    )
    val detail: MoveDetailEntity?
)

@Dao
interface PokemonDao {

    @Transaction
    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun observeAllPokemon(): Flow<List<PokemonWithDetails>>

    @Transaction
    @Query("SELECT * FROM pokemon WHERE id = :id LIMIT 1")
    fun observePokemonById(id: Int): Flow<PokemonWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemonList: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatsList(statsList: List<PokemonStatsEntity>)

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getPokemonCount(): Int

    @Query("DELETE FROM pokemon")
    suspend fun clearPokemon()

    @Query("DELETE FROM pokemon_stats")
    suspend fun clearStats()
}

@Dao
interface UserCollectionDao {

    @Query("SELECT * FROM user_collection WHERE pokemonId = :pokemonId LIMIT 1")
    fun observeUserCollection(pokemonId: Int): Flow<UserCollectionEntity?>

    @Query("SELECT COUNT(*) FROM user_collection WHERE isCaught = 1")
    fun observeCaughtCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserCollection(collection: UserCollectionEntity)
}

@Dao
interface EvolutionDao {

    @Query("SELECT * FROM evolutions WHERE chainId = :chainId")
    fun observeEvolutionChain(chainId: Int): Flow<List<EvolutionEntity>>

    @Query("SELECT * FROM evolutions WHERE chainId = :chainId")
    suspend fun getEvolutionChain(chainId: Int): List<EvolutionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvolutions(evolutions: List<EvolutionEntity>)
}

@Dao
interface PokemonMoveDao {

    @Transaction
    @Query("SELECT * FROM pokemon_moves WHERE pokemonId = :pokemonId")
    fun observeMovesForPokemon(pokemonId: Int): Flow<List<PokemonMoveWithDetail>>

    @Query("SELECT COUNT(*) FROM pokemon_moves WHERE pokemonId = :pokemonId")
    suspend fun getMoveCountForPokemon(pokemonId: Int): Int

    @Query("SELECT COUNT(*) FROM pokemon_moves")
    suspend fun getTotalMoveCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonMoves(moves: List<PokemonMoveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoveDetails(details: List<MoveDetailEntity>)
}

@Dao
interface PokemonAbilityDao {

    @Query("SELECT * FROM pokemon_abilities WHERE pokemonId = :pokemonId")
    fun observeAbilitiesForPokemon(pokemonId: Int): Flow<List<PokemonAbilityEntity>>

    @Query("SELECT COUNT(*) FROM pokemon_abilities WHERE pokemonId = :pokemonId")
    suspend fun getAbilityCountForPokemon(pokemonId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilities(abilities: List<PokemonAbilityEntity>)
}

@Dao
interface PokemonFormDao {

    @Query("SELECT * FROM pokemon_forms WHERE basePokemonId = :basePokemonId")
    fun observeFormsForPokemon(basePokemonId: Int): Flow<List<PokemonFormEntity>>

    @Query("SELECT * FROM pokemon_forms WHERE basePokemonId = :basePokemonId")
    suspend fun getFormsForPokemon(basePokemonId: Int): List<PokemonFormEntity>

    @Query("SELECT COUNT(*) FROM pokemon_forms WHERE basePokemonId = :basePokemonId")
    suspend fun getFormCountForPokemon(basePokemonId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForms(forms: List<PokemonFormEntity>)
}
