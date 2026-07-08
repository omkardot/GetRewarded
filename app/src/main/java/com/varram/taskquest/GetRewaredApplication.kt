package com.varram.taskquest

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.FirebaseApp
import com.varram.taskquest.data.local.AppDatabase

class GetRewaredApplication:Application() {
    companion object {
        lateinit var db: AppDatabase
    }
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initDataBaseChanges()

    }
    private fun initDataBaseChanges() {
        val MIGRATION_3_4 = object : Migration(6,7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'"
                )
            }
        }
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_db"
        )
            .addMigrations(MIGRATION_3_4)
            .build()
    }
}