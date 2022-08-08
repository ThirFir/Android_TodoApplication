package com.example.TodoManager

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.TodoManager.databinding.ColorSetDialogBinding

@RequiresApi(Build.VERSION_CODES.O)
class ColorSetDialog : DialogFragment() {
   lateinit var colorSetDialogBinding: ColorSetDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        with(super.onCreateDialog(savedInstanceState)) {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return this
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        colorSetDialogBinding = ColorSetDialogBinding.inflate(inflater, container, false)

        with(colorSetDialogBinding) {
            colorRed.setOnClickListener { onColorClicked(it.id) }
            colorYellow.setOnClickListener { onColorClicked(it.id) }
            colorOrange.setOnClickListener { onColorClicked(it.id) }
            colorGreen.setOnClickListener { onColorClicked(it.id) }
            colorBlue.setOnClickListener { onColorClicked(it.id) }
            colorMint.setOnClickListener { onColorClicked(it.id) }
            colorPurple.setOnClickListener { onColorClicked(it.id) }
            colorGray.setOnClickListener { onColorClicked(it.id) }
            colorBrown.setOnClickListener { onColorClicked(it.id) }
            colorNavy.setOnClickListener { onColorClicked(it.id) }
        }



        return colorSetDialogBinding.root
    }

    private fun onColorClicked(id : Int) {
        val holder = arguments?.getSerializable("holder") as TodoAdapter.TodoViewHolder?
        val position = requireArguments().getInt("position")
        var passedColor = -2
        with(colorSetDialogBinding) {
            when (id) {
                colorRed.id -> {
                    passedColor = 0
                }
                colorOrange.id -> {
                    passedColor = 1
                }
                colorYellow.id -> {
                    passedColor = 2
                }
                colorGreen.id -> {
                    passedColor = 3
                }
                colorBlue.id -> {
                    passedColor = 4
                }
                colorNavy.id -> {
                    passedColor = 5
                }
                colorPurple.id -> {
                    passedColor = 6
                }
                colorMint.id -> {
                    passedColor = 7
                }
                colorBrown.id -> {
                    passedColor = 8
                }
                colorGray.id -> {
                    passedColor = 9
                }

            }
        }
        Log.d("Dialog", passedColor.toString())
        if (holder != null) {
            (requireActivity() as MainActivity).todoFragment.todoListFragment.todoAdapter.setViewHolderBackgroundColor(
                holder, position, passedColor)
        }
        else {
            throw IllegalArgumentException("holder shouldn't be null")
        }
    }


}