// RewardFragment.kt
package com.varram.taskquest.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.R
import com.varram.taskquest.MainActivity
import com.varram.taskquest.adapters.RewardAdapter
import com.varram.taskquest.data.local.RewardEntity
import kotlinx.coroutines.launch

class RewardsFragment : Fragment(R.layout.fragment_rewards) {

    private lateinit var tvBalance: TextView
    private lateinit var tvTier: TextView
    private lateinit var recyclerRewards: RecyclerView
    private lateinit var rewardAdapter: RewardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvBalance = view.findViewById(R.id.tvBalance)
        tvTier = view.findViewById(R.id.tvTier)
        recyclerRewards = view.findViewById(R.id.recyclerRewards)

        recyclerRewards.layoutManager = LinearLayoutManager(requireContext())

        rewardAdapter = RewardAdapter(emptyList()) { reward ->
            redeemReward(reward)
        }

        recyclerRewards.adapter = rewardAdapter

        loadBalance()
        loadRewards()
    }

    private fun loadBalance() {

        viewLifecycleOwner.lifecycleScope.launch {

            val stats = MainActivity.db.userStatsDao().getStats()

            val points = stats?.totalPoints ?: 0

            tvBalance.text = points.toString()

            tvTier.text = when {
                points >= 2000 -> "👑 Elite Tier"
                points >= 1000 -> "⚡ High-Achiever Tier"
                points >= 500 -> "🔥 Rising Tier"
                else -> "🌱 Beginner Tier"
            }
        }
    }

    private fun loadRewards() {

        viewLifecycleOwner.lifecycleScope.launch {

            MainActivity.db.rewardDao().getAllRewards().collect { list ->
                rewardAdapter.updateData(list)
            }
        }
    }

    private fun redeemReward(reward: RewardEntity) {

        viewLifecycleOwner.lifecycleScope.launch {

            val stats = MainActivity.db.userStatsDao().getStats() ?: return@launch

            if (stats.totalPoints >= reward.requiredPoints) {

                MainActivity.db.userStatsDao().updateStats(
                    stats.copy(
                        totalPoints = stats.totalPoints - reward.requiredPoints
                    )
                )

                MainActivity.db.rewardDao().updateReward(
                    reward.copy(isUnlocked = true)
                )

                loadBalance()
            }
        }
    }
}