package com.varram.taskquest.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.varram.taskquest.MainActivity
import com.varram.taskquest.R
import com.varram.taskquest.data.local.RewardEntity
import kotlinx.coroutines.launch

class AddRewardFragment : Fragment(R.layout.fragment_add_rewards) {

    private lateinit var etRewardName: EditText
    private lateinit var etPoints: EditText
    private lateinit var imgBanner: ImageView
    private lateinit var btnSaveReward: RelativeLayout

    private lateinit var txtPreviewName: TextView
    private lateinit var txtPreviewPoints: TextView
    private lateinit var txtPreviewStatus: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupLivePreview()
        setupSaveButton()
    }

    private fun initViews(view: View) {

        etRewardName = view.findViewById(R.id.etRewardName)
        etPoints = view.findViewById(R.id.etPoints)
        imgBanner = view.findViewById(R.id.imgBanner)
        btnSaveReward = view.findViewById(R.id.btnSaveReward)

        txtPreviewName = view.findViewById(R.id.tvPreviewName)
        txtPreviewPoints = view.findViewById(R.id.tvPreviewPoints)
        txtPreviewStatus = view.findViewById(R.id.tvPreviewStatus)
    }

    private fun setupLivePreview() {

        val watcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        }

        etRewardName.addTextChangedListener(watcher)
        etPoints.addTextChangedListener(watcher)

        updatePreview()
    }

    private fun updatePreview() {

        val rewardName =
            if (etRewardName.text.toString().trim().isEmpty())
                "Preview Item"
            else
                etRewardName.text.toString()

        val points =
            if (etPoints.text.toString().trim().isEmpty())
                "0"
            else
                etPoints.text.toString()

        txtPreviewName.text = rewardName
        txtPreviewPoints.text = points
        txtPreviewStatus.text = "LOCKED"
    }

    private fun setupSaveButton() {

        btnSaveReward.setOnClickListener {

            val name = etRewardName.text.toString().trim()
            val pointsText = etPoints.text.toString().trim()

            if (name.isEmpty()) {
                toast("Enter reward name")
                return@setOnClickListener
            }

            if (pointsText.isEmpty()) {
                toast("Enter required points")
                return@setOnClickListener
            }

            val points = pointsText.toInt()

            val reward = RewardEntity(
                title = name,
                requiredPoints = points,
                isUnlocked = false
            )

            lifecycleScope.launch {

                MainActivity.db.rewardDao().insertReward(reward)

                toast("Reward Saved")

                clearFields()
            }
        }
    }

    private fun clearFields() {
        etRewardName.text.clear()
        etPoints.text.clear()
        updatePreview()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}