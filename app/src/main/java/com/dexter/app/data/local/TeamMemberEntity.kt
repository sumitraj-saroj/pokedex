package com.dexter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val slot: Int, // 1..6
    val pokemonId: Int
)
