package com.varram.taskquest.ui.screens

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.data.local.TaskEntity
import kotlinx.coroutines.launch

class AddTaskScreen : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etPoints: EditText
    private lateinit var etReward: EditText
    private lateinit var cbDaily: CheckBox
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task_screen)

        etTitle = findViewById(R.id.etTitle)
        etPoints = findViewById(R.id.etPoints)
        etReward = findViewById(R.id.etReward)
        cbDaily = findViewById(R.id.cbDaily)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = etTitle.text.toString()
        val points = etPoints.text.toString().toIntOrNull() ?: 0
        val reward = etReward.text.toString()
        val isDaily = cbDaily.isChecked

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter task title", Toast.LENGTH_SHORT).show()
            return
        }

        val task = TaskEntity(
            title = title,
            points = points,
            rewardText = reward,
            category = "",
            isDaily = isDaily
        )

        // Insert into DB
        lifecycleScope.launch {
            MainActivity.db.taskDao().insertTask(task)

            runOnUiThread {
                Toast.makeText(this@AddTaskScreen, "Task Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}