package com.varram.taskquest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.varram.taskquest.data.local.TaskEntity
import com.varram.taskquest.data.local.UserDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDetailsDao {

    @Insert
    suspend fun insertUserDetails(user: UserDetails): Long

    @Update
    suspend fun updateUserDetials(task: UserDetails)
}