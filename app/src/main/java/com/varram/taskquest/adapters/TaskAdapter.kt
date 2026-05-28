package com.varram.taskquest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varram.taskquest.R
import com.varram.taskquest.data.local.TaskEntity

class TaskAdapter(
    private var taskList: List<TaskEntity>,
    private val onCompleteClick: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val tvReward: TextView = view.findViewById(R.id.tvReward)

        val btnComplete: RelativeLayout = view.findViewById(R.id.btnComplete)
        val tvCompleted: TextView = view.findViewById(R.id.tvCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tasks, parent, false)

        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val task = taskList[position]

        holder.tvTaskTitle.text = task.title
        holder.tvPoints.text = "${task.points} pts"
        holder.tvReward.text = task.rewardText
        holder.tvCategory.text = task.category.uppercase()

        if (task.isCompleted) {

            holder.btnComplete.visibility = View.GONE
            holder.tvCompleted.visibility = View.VISIBLE

        } else {

            holder.btnComplete.visibility = View.VISIBLE
            holder.tvCompleted.visibility = View.GONE
        }

        holder.btnComplete.setOnClickListener {
            onCompleteClick(task)
        }
    }

    fun updateData(newList: List<TaskEntity>) {
        taskList = newList
        notifyDataSetChanged()
    }
}