package com.varram.taskquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "userdetails")
data class UserDetails(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,
    val useremail: String,
    val userUID: String,
    val isLogedin: String,
    val lastLoginDate: String,
    val loginProvider: String,
    val profileUrl: String,
    val createdAt:String
)
