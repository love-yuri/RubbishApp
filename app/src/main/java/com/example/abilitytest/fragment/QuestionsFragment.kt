package com.example.abilitytest.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.abilitytest.R
import com.example.abilitytest.databinding.FragmentQuestionsBinding
import com.example.abilitytest.FILEPATH
import com.example.abilitytest.utils.MessageUtil
import com.example.abilitytest.utils.Utils
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Question(
    val question: String,
    val answer: String,
    val options: List<String>
)

class QuestionsFragment : Fragment() {
    private lateinit var binding: FragmentQuestionsBinding
    private lateinit var msgUtil: MessageUtil
    private lateinit var questionList: List<Question>

    private var hasLearned = 0
    private var accuracy = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionsBinding.inflate(layoutInflater, container, false)
        msgUtil = MessageUtil(requireContext())

        hasLearned = 0
        accuracy = 0

        loadQuestions()
        Utils.runAfter(5) {
            setProcess()
        }

        questionList.forEachIndexed { index, question ->
            val context = requireContext()

            // 添加标签选项
            val radioGroup = RadioGroup(context).apply {
                setOnCheckedChangeListener { group, checkId ->
                    val userAnswer: String = group.findViewById<MaterialRadioButton>(checkId).text.toString()
                    val answer = questionList[index].answer

                    if (answer == userAnswer) {
                        msgUtil.createToast(getString(R.string.oumeideduo))
                        accuracy++
                    } else {
                        msgUtil.createToast(getString(R.string.answerError))
                    }

                    // 选择之后全部禁用
                    for (i in 0 until group.childCount) {
                        group.getChildAt(i).apply {
                            val box = this as MaterialRadioButton
                            isEnabled = false
                            // 如果选错了，则将正确答案标红
                            if (box.text.toString() == answer && answer != userAnswer) {
                                box.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                            }
                        }
                    }

                    hasLearned++
                    setProcess()
                }
            }

            // 题目
            TextView(context).apply {
                text = "${index + 1}. ${question.question}"
                textSize = 20.0f
                layoutParams = ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                )
                binding.mainLayout.addView(this)
            }

            // 选择框
            question.options.forEach {
                MaterialRadioButton(context).apply {
                    text = it
                    textSize = 17.0f
                    layoutParams = ViewGroup.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT,
                    )
                    radioGroup.addView(this)
                }
            }

            binding.mainLayout.addView(radioGroup)
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setProcess() {
        binding.indicatorText.text = "${getString(R.string.learning_progress)} $hasLearned/${questionList.size}"
        binding.indicator.progress = hasLearned * 100 /questionList.size
        if (hasLearned != 0) {
            binding.accuracyRateText.text = "${getString(R.string.accuracy)} ${accuracy * 100 / hasLearned}%"
            binding.accuracyRate.progress = accuracy * 100 / hasLearned
        } else {
            binding.accuracyRateText.text = "${getString(R.string.accuracy)} 0%"
            binding.accuracyRate.progress = 0
        }
    }

    private fun loadQuestions() {
        val json = Utils.loadFileFromAssets(requireContext(), FILEPATH.QUESTION_JSON_FILE)
        json?.also {
            try {
                questionList = Json.decodeFromString<List<Question>>(it)
            } catch (e: Exception) {
                msgUtil.createErrorDialog("${getString(R.string.jsonDecodeError)}: -> ${e.message}")
            }
        }
    }
}