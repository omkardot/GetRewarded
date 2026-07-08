package com.varram.taskquest.ui.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varram.taskquest.GetRewaredApplication
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.data.local.RewardEntity
import kotlinx.coroutines.launch

class AddRewardActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etPoints: EditText
    private lateinit var btnSave: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reward_screen)

        etTitle = findViewById(R.id.etRewardTitle)
        etPoints = findViewById(R.id.etRewardPoints)
        btnSave = findViewById(R.id.btnSaveReward)

        btnSave.setOnClickListener {
            saveReward()
        }
    }

    private fun saveReward() {
        val title = etTitle.text.toString()
        val points = etPoints.text.toString().toIntOrNull() ?: 0

        if (title.isEmpty() || points <= 0) {
            Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
            return
        }

        val reward = RewardEntity(
            title = title,
            requiredPoints = points
        )

        lifecycleScope.launch {
            GetRewaredApplication.db.rewardDao().insertReward(reward)

            runOnUiThread {
                Toast.makeText(this@AddRewardActivity, "Reward Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}