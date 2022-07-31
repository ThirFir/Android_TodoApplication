package com.example.TodoManager

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.TodoManager.databinding.FragmentTodoAddTimeDialogBinding
import java.text.DecimalFormat

@SuppressLint("SetTextI18n")
class TodoAddTimeDialog : DialogFragment() {
    private lateinit var fragmentTodoAddTimeDialogBinding : FragmentTodoAddTimeDialogBinding
    var hours = 0
    var minutes = 0
    var isTimeSet = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val anim = WindowManager.LayoutParams()
//        anim.windowAnimations = R.style.AnimationPopupStyle
//        window.attributes = anim
        with(super.onCreateDialog(savedInstanceState)) {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.attributes?.windowAnimations = R.style.AnimationPopupStyle
            return this
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        fragmentTodoAddTimeDialogBinding = FragmentTodoAddTimeDialogBinding.inflate(inflater, container, false)
        initNumberPicker()


        with(fragmentTodoAddTimeDialogBinding) {
            btnOk.setOnClickListener {
                hours = numberPickerHour.value
                minutes = numberPickerMinute.value
                (requireActivity() as TodoAddActivity).activityTodoAddBinding.btnTime.text =
                    if (hours == 0 && minutes == 0) {
                        isTimeSet = false
                        "TIME"
                    }
                    else {
                        isTimeSet = true
                        when(hours) {
                            0 -> {
                                "AM 12 : "
                            }
                            in 1..11 -> {
                                "AM ${DecimalFormat("00").format(hours)} : "
                            }
                            12 -> {
                                "PM 12 : "
                            }
                            else -> "PM ${hours - 12} : "
                        } + DecimalFormat("00").format(minutes)
                    }
                dismiss()
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return fragmentTodoAddTimeDialogBinding.root
    }

    private fun initNumberPicker() {
        with(fragmentTodoAddTimeDialogBinding) {
            with(numberPickerHour) {
                minValue = 0
                maxValue = 23
                wrapSelectorWheel = true
                value = arguments?.getInt("hours") ?: 0
            }
            with(numberPickerMinute) {
                minValue = 0
                maxValue = 59
                wrapSelectorWheel = true
                value = arguments?.getInt("minutes") ?: 0
            }
        }
    }

}