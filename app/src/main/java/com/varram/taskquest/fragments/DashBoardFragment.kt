package com.varram.taskquest.fragments


import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.GetRewaredApplication
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.adapters.TaskAdapter
import com.varram.taskquest.data.local.TaskEntity
import kotlinx.coroutines.launch

class DashBoardFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var tvPoints: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvXp: TextView
    private lateinit var tvTaskLeft: TextView

    private lateinit var progressLevel: ProgressBar
    private lateinit var recyclerTodayTasks: RecyclerView

    private lateinit var btnRewards: LinearLayout
    private lateinit var btnAddTask: LinearLayout

    private lateinit var adapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecycler()
        loadDashboardData()
        loadTodayTasks()
        setupClicks()
    }

    private fun initViews(view: View) {

        tvPoints = view.findViewById(R.id.tvPoints)
        tvStreak = view.findViewById(R.id.tvStreak)
        tvLevel = view.findViewById(R.id.tvLevel)
        tvXp = view.findViewById(R.id.tvXp)
        tvTaskLeft = view.findViewById(R.id.tvTaskLeft)

        progressLevel = view.findViewById(R.id.progressLevel)
        recyclerTodayTasks = view.findViewById(R.id.recyclerTodayTasks)

        btnRewards = view.findViewById(R.id.btnRewards)
        btnAddTask = view.findViewById(R.id.btnAddTask)
    }

    private fun setupRecycler() {

        adapter = TaskAdapter(emptyList()) { task ->
            completeTask(task)
        }

        recyclerTodayTasks.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerTodayTasks.adapter = adapter
    }

    private fun loadDashboardData() {

        viewLifecycleOwner.lifecycleScope.launch {

            val stats = GetRewaredApplication.db.userStatsDao().getStats()

            if (stats != null) {

                val points = stats.totalPoints
                val streak = stats.streak

                tvPoints.text = "$points Points 💰"
                tvStreak.text = "🔥 $streak Day Streak"

                val level = (points / 500) + 1
                val xp = points % 500
                val xpNeed = 500

                tvLevel.text = "LEVEL $level"
                tvXp.text = "$xp/$xpNeed XP"

                val progress = (xp * 100) / xpNeed
                progressLevel.progress = progress
            }
        }
    }

    private fun loadTodayTasks() {

        viewLifecycleOwner.lifecycleScope.launch {

            GetRewaredApplication.db.taskDao().getAllTasks().collect { list ->

                val todayList =
                    list.filter {
                        !it.isCompleted &&
                                (it.isDaily || true)
                    }.take(3)

                adapter.updateData(todayList)

                val total = todayList.size
                val left = todayList.count { !it.isCompleted }

                tvTaskLeft.text = "$left/$total LEFT"
            }
        }
    }

    private fun completeTask(task: TaskEntity) {

        viewLifecycleOwner.lifecycleScope.launch {

            GetRewaredApplication.db.taskDao().updateTask(
                task.copy(isCompleted = true)
            )

            val stats = GetRewaredApplication.db.userStatsDao().getStats()

            stats?.let {

//                MainActivity.db.userStatsDao().updateStats(
//                    it.copy(
//                        totalPoints = it.totalPoints + task.points,
//                        tasksDone = it.tasksDone + 1,
//                        todayCompleted = it.todayCompleted + 1
//                    )
//                )
            }

            loadDashboardData()
        }
    }

    private fun setupClicks() {

        btnRewards.setOnClickListener {

            // Navigate to RewardFragment
            // findNavController().navigate(...)
        }

        btnAddTask.setOnClickListener {

            // Navigate to AddTaskFragment
            // findNavController().navigate(...)
        }
    }
}