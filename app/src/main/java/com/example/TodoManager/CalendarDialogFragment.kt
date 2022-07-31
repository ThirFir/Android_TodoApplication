package com.example.TodoManager

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.example.TodoManager.R
import com.example.TodoManager.databinding.FragmentCalendarDialogBinding


@RequiresApi(Build.VERSION_CODES.O)
class CalendarDialogFragment : DialogFragment() {
    lateinit var fragmentCalendarDialogBinding : FragmentCalendarDialogBinding
    private lateinit var todoDB : SQLiteDatabase
    private lateinit var date : Date
    private var day : Int = 0
    private lateinit var dayOfWeek: String

    private val todoList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentCalendarDialogBinding = FragmentCalendarDialogBinding.inflate(layoutInflater)
        date = arguments?.getSerializable("date") as Date
        day = date.day
        dayOfWeek = arguments?.getString("day_of_week").toString()

        todoDB = requireActivity().openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
        todoDB.execSQL(resources.getString(R.string.get_todo_tb))

        val cursor = todoDB.rawQuery("select * from TODO_TB", null)
        with(cursor){
            while(moveToNext()) {
                if(date.isSameDate(Date(DatabaseConstants.YEAR.ordinal, DatabaseConstants.MONTH.ordinal, DatabaseConstants.DAY.ordinal)))
                    todoList.add(getString(DatabaseConstants.NAME.ordinal))
            }
        }

        with(fragmentCalendarDialogBinding) {
            dialogDateDay.text = day.toString()
            dialogDateDayOfWeek.text = dayOfWeek

            for(name in todoList)
                cardGroupLayout.addView(generateCardView(name))

            floatingButton.setOnClickListener {
                val todoAddIntent = Intent(activity, TodoAddActivity::class.java)
                todoAddIntent.putExtra("date", date)
                //(requireActivity() as MainActivity).requestNewTodoInfoLauncher.launch(todoAddIntent)
            }
            return root
        }
        //dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun generateCardView(name : String) : CardView {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(10, 50, 10, 0)

        return CardView(requireActivity()).apply {
            this.layoutParams = layoutParams
            radius = 24F
            setCardBackgroundColor(Color.LTGRAY)
            cardElevation = 8F
            maxCardElevation = 12F
            addView(generateLayoutInsideCardView(name))
        }
    }

    private fun generateLayoutInsideCardView(name : String) : LinearLayout {
        val layout = LinearLayout(requireActivity())
        layout.setPadding(5, 10, 10, 5)
        layout.gravity = Gravity.CENTER_VERTICAL

        val circle = ImageView(requireActivity())
        circle.setBackgroundResource(R.drawable.list_circle)

        val circleLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        circleLayoutParams.setMargins(30, 0, 30, 0)
        circle.layoutParams = circleLayoutParams

        val todoName = TextView(requireActivity())
        todoName.gravity = Gravity.CENTER_VERTICAL
        todoName.typeface = ResourcesCompat.getFont(requireActivity(), R.font.dalseo_healing_bold)
        todoName.text = name
        todoName.textSize = 20f
        todoName.setTextColor(Color.WHITE)

        layout.addView(circle)
        layout.addView(todoName)

        return layout
    }
//    private fun refreshList(date : String) {
//        todoList.clear()
//        fragmentCalendarDialogBinding.cardGroupLayout.removeAllViewsInLayout()
//        todoDB = requireActivity().openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
//        val cursor = todoDB.rawQuery("select * from TODO_TB", null)
//        with(cursor){
//            while(moveToNext()) {
//                if (getString(DatabaseConstants.DATE.ordinal) == date)
//                    todoList.add(getString(DatabaseConstants.NAME.ordinal))
//            }
//        }
//        for(name in todoList)
//            fragmentCalendarDialogBinding.cardGroupLayout.addView(generateCardView(name))
//    }
}