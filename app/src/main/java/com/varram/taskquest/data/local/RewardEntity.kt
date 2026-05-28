package com.varram.taskquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val requiredPoints: Int,
    val isUnlocked: Boolean = false
)