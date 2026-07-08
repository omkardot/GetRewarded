package com.varram.taskquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.varram.taskquest.data.dao.RewardDao
import com.varram.taskquest.data.dao.TaskDao
import com.varram.taskquest.data.dao.UserDetailsDao
import com.varram.taskquest.data.dao.UserStatsDao

@Database(
    entities = [TaskEntity::class, UserStatsEntity::class, RewardEntity::class,UserDetails::class],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun rewardDao(): RewardDao
    abstract fun userdetailsDao(): UserDetailsDao
}