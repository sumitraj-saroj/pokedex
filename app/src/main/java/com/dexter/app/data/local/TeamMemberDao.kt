package com.dexter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY slot ASC")
    fun observeTeamMembers(): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTeamMember(member: TeamMemberEntity)

    @Query("DELETE FROM team_members WHERE slot = :slot")
    suspend fun deleteTeamMember(slot: Int)

    @Query("DELETE FROM team_members")
    suspend fun clearTeam()
}
