package com.varram.taskquest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.varram.taskquest.R
import com.varram.taskquest.adapters.TaskPagerAdapter

// TasksFragment.kt
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        viewPager.adapter = TaskPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

            when (position) {
                0 -> tab.text = "All"
                1 -> tab.text = "Daily"
                2 -> tab.text = "Completed"
            }

        }.attach()
    }
}