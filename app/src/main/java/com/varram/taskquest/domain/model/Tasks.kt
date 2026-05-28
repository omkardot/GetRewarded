package com.varram.taskquest.domain.model

data class Task(
    val id: Int,
    val title: String,
    val points: Int,
    val rewardText: String,
    val isCompleted: Boolean,
    val isDaily: Boolean
)