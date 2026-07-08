package com.varram.taskquest.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.varram.taskquest.GetRewaredApplication
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.data.local.TaskEntity
import kotlinx.coroutines.launch

class AddTaskFragment : Fragment(R.layout.fragment_add_task) {

    private lateinit var etTitle: EditText
    private lateinit var etPoints: EditText
    private lateinit var etReward: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchDaily: SwitchCompat

    private lateinit var txtPreviewCategory: TextView
    private lateinit var txtPreviewTitle: TextView
    private lateinit var txtPreviewPoints: TextView
    private lateinit var txtPreviewReward: TextView
    private lateinit var btnSave: RelativeLayout

    private val categories = arrayOf(
        "Fitness",
        "Study",
        "Work",
        "Health",
        "Mindset",
        "Other"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSpinner()
        setupPreview()
        setupSave()
    }

    private fun initViews(view: View) {

        etTitle = view.findViewById(R.id.etTitle)
        etPoints = view.findViewById(R.id.etPoints)
        etReward = view.findViewById(R.id.etReward)
        spinnerCategory = view.findViewById(R.id.spinnerTaskType)
        switchDaily = view.findViewById(R.id.switchDailyTask)

        txtPreviewCategory = view.findViewById(R.id.txt_category)
        txtPreviewTitle = view.findViewById(R.id.title)
        txtPreviewPoints = view.findViewById(R.id.points)
        txtPreviewReward = view.findViewById(R.id.rewardtext)

        btnSave = view.findViewById(R.id.btnSaveTask)
    }

    private fun setupSpinner() {

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinnerCategory.adapter = adapter
    }

    private fun setupPreview() {

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etTitle.addTextChangedListener(watcher)
        etPoints.addTextChangedListener(watcher)
        etReward.addTextChangedListener(watcher)

        spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updatePreview()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        updatePreview()
    }

    private fun updatePreview() {

        val title = etTitle.text.toString().ifEmpty { "Live Card" }
        val points = etPoints.text.toString().ifEmpty { "30" }
        val reward = etReward.text.toString().ifEmpty { "Relax in sauna" }
        val category = spinnerCategory.selectedItem.toString()

        txtPreviewTitle.text = title
        txtPreviewPoints.text = points
        txtPreviewReward.text = " Reward: $reward"
        txtPreviewCategory.text = category
    }

    private fun setupSave() {

        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val pointsText = etPoints.text.toString().trim()
            val reward = etReward.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val isDaily = switchDaily.isChecked

            if (title.isEmpty()) {
                toast("Enter title")
                return@setOnClickListener
            }

            if (pointsText.isEmpty()) {
                toast("Enter points")
                return@setOnClickListener
            }

            val points = pointsText.toInt()

            val task = TaskEntity(
                title = title,
                points = points,
                rewardText = reward,
                category = category,
                isDaily = isDaily,
                isCompleted = false
            )

            lifecycleScope.launch {

                GetRewaredApplication.db.taskDao().insertTask(task)

                toast("Task Saved")

                clearForm()
            }
        }
    }

    private fun clearForm() {

        etTitle.text.clear()
        etPoints.text.clear()
        etReward.text.clear()
        switchDaily.isChecked = false
        spinnerCategory.setSelection(0)

        updatePreview()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}