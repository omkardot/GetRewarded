package com.varram.taskquest.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.varram.taskquest.fragments.AllTasksFragment
import com.varram.taskquest.fragments.CompletedTasksFragment
import com.varram.taskquest.fragments.DailyTasksFragment

// TaskPagerAdapter.kt
class TaskPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> AllTasksFragment()
            1 -> DailyTasksFragment()
            else -> CompletedTasksFragment()
        }
    }
}