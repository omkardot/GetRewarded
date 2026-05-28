package com.varram.taskquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(

    @PrimaryKey
    val id: Int = 1,

    val totalPoints: Int = 0,
    val lastCheckedDate: String = "",
    val streak: Int = 0
)