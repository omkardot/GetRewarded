package com.varram.taskquest.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.varram.taskquest.fragments.DashBoardFragment
import com.varram.taskquest.fragments.StatsFragment
import com.varram.taskquest.fragments.TasksFragment
import com.varram.taskquest.ui.RewardsFragment

class MainPagerAdapter(activity: AppCompatActivity)
    : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashBoardFragment()
            1 -> RewardsFragment()
            2 -> TasksFragment()
            3 -> StatsFragment()
            else -> DashBoardFragment()
        }
    }
}