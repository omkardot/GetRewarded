package com.varram.taskquest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.GetRewaredApplication
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.adapters.TaskAdapter
import com.varram.taskquest.data.local.TaskEntity
import kotlinx.coroutines.launch

// DailyTasksFragment.kt
class DailyTasksFragment : Fragment(R.layout.fragment_daily_tasks) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TaskAdapter(emptyList()) { task ->
            completeTask(task)
        }

        recyclerView.adapter = adapter

        loadTasks()
    }

    private fun loadTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            GetRewaredApplication.db.taskDao().getAllTasks().collect { list ->
                adapter.updateData(list.filter { it.isDaily && !it.isCompleted })
            }
        }
    }

    private fun completeTask(task: TaskEntity) {
        viewLifecycleOwner.lifecycleScope.launch {

            GetRewaredApplication.db.taskDao().updateTask(
                task.copy(isCompleted = true)
            )
        }
    }
}