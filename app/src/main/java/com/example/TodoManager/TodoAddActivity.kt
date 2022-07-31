package com.example.TodoManager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.TodoManager.databinding.ActivityTodoAddBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*

@SuppressLint("SetTextI18n")
class TodoAddActivity : AppCompatActivity() {
    lateinit var activityTodoAddBinding : ActivityTodoAddBinding
    lateinit var todoAddTimeDialog : TodoAddTimeDialog
    private var year = CalendarDay.today().year
    private var month = CalendarDay.today().month
    private var day = CalendarDay.today().day
    private var isTimeSet : Boolean = false
    private var hours : Int = 0
    private var minutes : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.activity_add_open, R.anim.hold)
        activityTodoAddBinding = ActivityTodoAddBinding.inflate(layoutInflater)
        setContentView(activityTodoAddBinding.root)


        todoAddTimeDialog = TodoAddTimeDialog()

        (intent.getSerializableExtra("date") as Date?)?.let {
            year = it.year
            month = it.month
            day = it.day
            activityTodoAddBinding.btnCalendar.text = "${year}.${month}.${day}"
        }

        with(activityTodoAddBinding) {
            btnSave.setOnClickListener{
                hours = todoAddTimeDialog.hours
                minutes = todoAddTimeDialog.minutes
                isTimeSet = todoAddTimeDialog.isTimeSet

                if(editTodoName.text.isEmpty()){
                    Toast.makeText(this@TodoAddActivity, "Please enter TODO name!!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                setResult(RESULT_OK, intent.apply {
                    val todoInfo = TodoInfo(editTodoName.text.toString(), editTodoDetail.text.toString(),
                        Date(year, month, day), Time(hours, minutes, isTimeSet))
                    putExtra("todoInfo", todoInfo)
                })
                finish()
            }
            btnCancel.setOnClickListener {
                setResult(RESULT_CANCELED, intent)
                finish()
                overridePendingTransition(R.anim.hold, R.anim.activity_add_cancel)
            }
            btnCalendar.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(this@TodoAddActivity, DatePickerDialog.OnDateSetListener {
                        view, year, month, dayOfMonth ->
                    if(CalendarDay.today().year == year &&
                            CalendarDay.today().month == month+1 &&
                            CalendarDay.today().day == dayOfMonth)
                                btnCalendar.text = "TODAY"
                    else
                        btnCalendar.text = "${year}.${month+1}.${dayOfMonth}"
                    this@TodoAddActivity.year = year
                    this@TodoAddActivity.month = month+1
                    this@TodoAddActivity.day = dayOfMonth
                },
                    year, month-1, day).show()
            }
            btnTime.setOnClickListener {
                if (!todoAddTimeDialog.isAdded)
                    todoAddTimeDialog.apply {
                        hours = arguments?.getInt("hours") ?: 0
                        minutes = arguments?.getInt("minutes") ?: 0
                        arguments = Bundle().apply {
                            putInt("hours", hours)
                            putInt("minutes", minutes)
                        }
                    }
                        .show(supportFragmentManager, "Time")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold, R.anim.activity_add_cancel)
    }
}