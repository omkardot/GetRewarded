package com.varram.taskquest.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.adapters.RewardAdapter
import com.varram.taskquest.data.local.RewardEntity
import kotlinx.coroutines.launch

class RewardActivity : AppCompatActivity() {

    private lateinit var adapter: RewardAdapter
    private lateinit var tvPoints: TextView
    private lateinit var btnAddReward: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        val recyclerView = findViewById<RecyclerView>(R.id.rvRewards)
        tvPoints = findViewById(R.id.tvPoints)
        btnAddReward = findViewById(R.id.btnAddReward)

        adapter = RewardAdapter(emptyList()) { reward ->
            unlockReward(reward)
        }
        btnAddReward.setOnClickListener {
            startActivity(Intent(this,AddRewardActivity::class.java))
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        observeRewards()
        loadPoints()
    }

    private fun observeRewards() {
        lifecycleScope.launch {
            MainActivity.db.rewardDao().getAllRewards().collect { rewards ->
                adapter.updateData(rewards)
            }
        }
    }

    private fun loadPoints() {
        lifecycleScope.launch {
            val stats = MainActivity.db.userStatsDao().getStats()
            runOnUiThread {
                tvPoints.text = "Points: ${stats?.totalPoints ?: 0}"
            }
        }
    }

    private fun unlockReward(reward: RewardEntity) {
        lifecycleScope.launch {

            val stats = MainActivity.db.userStatsDao().getStats() ?: return@launch

            if (stats.totalPoints < reward.requiredPoints) {
                runOnUiThread {
                    Toast.makeText(this@RewardActivity, "Not enough points!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val newPoints = stats.totalPoints - reward.requiredPoints
            MainActivity.db.userStatsDao().updateStats(stats.copy(totalPoints = newPoints))

            MainActivity.db.rewardDao().updateReward(reward.copy(isUnlocked = true))

            loadPoints()

            runOnUiThread {
                Toast.makeText(this@RewardActivity, "Reward Unlocked!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}