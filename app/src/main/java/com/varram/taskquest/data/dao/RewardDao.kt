package com.varram.taskquest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.varram.taskquest.data.local.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Insert
    suspend fun insertReward(reward: RewardEntity)

    @Update
    suspend fun updateReward(reward: RewardEntity)

    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<RewardEntity>>
}