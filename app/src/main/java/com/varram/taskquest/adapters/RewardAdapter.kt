package com.varram.taskquest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.R
import com.varram.taskquest.data.local.RewardEntity

class RewardAdapter(
    private var rewardList: List<RewardEntity>,
    private val onUnlockClick: (RewardEntity) -> Unit
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvRewardTitle: TextView = view.findViewById(R.id.tvRewardTitle)
        val tvRewardDesc: TextView = view.findViewById(R.id.tvRewardDesc)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPointsNeed: TextView = view.findViewById(R.id.tvPointsNeed)
        val btnAction: Button = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)

        return RewardViewHolder(view)
    }

    override fun getItemCount(): Int = rewardList.size

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {

        val reward = rewardList[position]

        holder.tvRewardTitle.text = reward.title

        holder.tvRewardDesc.text =
            "Complete tasks and use points to unlock this reward."

        holder.tvPointsNeed.text = "✪ ${reward.requiredPoints}"

        if (reward.isUnlocked) {

            holder.tvStatus.text = "UNLOCKED"
            holder.btnAction.text = "Redeem"
            holder.btnAction.isEnabled = true
            holder.btnAction.alpha = 1f

        } else {

            holder.tvStatus.text = "LOCKED"
            holder.btnAction.text = "Unlock"
            holder.btnAction.isEnabled = true
            holder.btnAction.alpha = 1f
        }

        holder.btnAction.setOnClickListener {
            onUnlockClick(reward)
        }
    }

    fun updateData(newList: List<RewardEntity>) {
        rewardList = newList
        notifyDataSetChanged()
    }
}