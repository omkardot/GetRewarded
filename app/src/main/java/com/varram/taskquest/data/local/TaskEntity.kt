package com.varram.taskquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val points: Int,
    val rewardText: String,
    val category: String,
    val isCompleted: Boolean = false,
    val isDaily: Boolean = false
)

