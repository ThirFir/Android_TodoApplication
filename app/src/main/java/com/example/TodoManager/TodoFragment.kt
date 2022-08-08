package com.example.TodoManager

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.TodoManager.databinding.FragmentTodoBinding
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils.getDay3LettersName
import com.michalsvec.singlerowcalendar.utils.DateUtils.getDayName
import com.michalsvec.singlerowcalendar.utils.DateUtils.getDayNumber
import com.michalsvec.singlerowcalendar.utils.DateUtils.getMonthNumber
import com.michalsvec.singlerowcalendar.utils.DateUtils.getYear
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import java.util.Date


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
class TodoFragment : Fragment() {
    private lateinit var fragmentTodoBinding : FragmentTodoBinding
    private var dateToday = LocalDate.now()
    private var dayOfWeek = dateToday.dayOfWeek
    private val daysHavingTodo = mutableListOf<Int>()
    val todoListFragment = TodoListFragment()

    private val calendar = Calendar.getInstance()
    private var currentMonth = 0
    var selectedDate = CalendarDay.today().toDate()

    lateinit var singleRowCalendar : SingleRowCalendar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentTodoBinding = FragmentTodoBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        (requireActivity() as MainActivity).todoDB.execSQL(resources.getString(R.string.get_todo_tb))
        (requireActivity() as MainActivity).cursor = (requireActivity() as MainActivity).todoDB.rawQuery("select * from TODO_TB", null)
        with((requireActivity() as MainActivity).cursor){
            while(moveToNext()) {
                val date = com.example.TodoManager.Date(getInt(DatabaseConstants.YEAR.ordinal), getInt(
                    DatabaseConstants.MONTH.ordinal),
                    getInt(DatabaseConstants.DAY.ordinal))
                if(!daysHavingTodo.contains(date.day))
                    daysHavingTodo.add(date.day)
            }
        }

        calendar.time = Date()
        currentMonth = calendar[Calendar.MONTH]

        setTodoDateView(calendar.time)
        val myCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                return if (isSelected) {
                    if (daysHavingTodo.contains(getDayNumber(date).toInt()))
                        R.layout.selected_calendar_item
                    else
                        R.layout.selected_calendar_item
                } else {
                    if (daysHavingTodo.contains(getDayNumber(date).toInt()))
                        R.layout.calendar_item
                    else
                        R.layout.calendar_item
                }

                // NOTE: if we don't want to do it this way, we can simply change color of background
                // in bindDataToCalendarView method
            }

            @SuppressLint("ResourceType")
            override fun bindDataToCalendarView(
                holder: SingleRowCalendarAdapter.CalendarViewHolder,
                date: Date,
                position: Int,
                isSelected: Boolean
            ) {
                // using this method we can bind data to calendar view
                // good practice is if all views in layout have same IDs in all item views
                with(holder.itemView) {
                    findViewById<TextView>(R.id.tv_date_calendar_item).text =
                        getDayNumber(date)
                    findViewById<TextView>(R.id.tv_day_calendar_item).text =
                        getDay3LettersName(date)
                    if (isSelected) {
                        findViewById<LinearLayout>(R.id.cl_calendar_item)
                            .setBackgroundResource(R.drawable.selected_calendar_item_background)
                        if (daysHavingTodo.contains(getDayNumber(date).toInt()))
                            findViewById<View>(R.id.green_dot).visibility = View.VISIBLE
                        else
                            findViewById<View>(R.id.green_dot).visibility = View.INVISIBLE
                    } else {
                        findViewById<LinearLayout>(R.id.cl_calendar_item)
                            .setBackgroundResource(R.drawable.calendar_item_background)
                        if (daysHavingTodo.contains(getDayNumber(date).toInt()))
                            findViewById<View>(R.id.green_dot).visibility = View.VISIBLE
                        else
                            findViewById<View>(R.id.green_dot).visibility = View.INVISIBLE

                    }
                }
            }
        }
        val mySelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                // set date to calendar according to position
                val cal = Calendar.getInstance()
                cal.time = date
                return when (cal[Calendar.DAY_OF_WEEK]) {
                    else -> true
                }
            }
        }

        val myCalendarChangesObserver = object : CalendarChangesObserver {
            override fun whenWeekMonthYearChanged(
                weekNumber: String,
                monthNumber: String,
                monthName: String,
                year: String,
                date: Date
            ) {
                super.whenWeekMonthYearChanged(weekNumber, monthNumber, monthName, year, date)
            }

            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                super.whenSelectionChanged(isSelected, position, date)
                Log.d("Log", "whenSelectionChanged : " + getDayNumber(date))        // 이전거 나오고 누른거 나옴
                if(selectedDate.day != getDayNumber(date).toInt()) {
                    selectedDate.day = getDayNumber(date).toInt()
                    setTodoDateView(date)
                    todoListFragment.onDateChanged(selectedDate)
                }

            }

            override fun whenCalendarScrolled(dx: Int, dy: Int) {
                super.whenCalendarScrolled(dx, dy)
            }

            override fun whenSelectionRestored() {
                super.whenSelectionRestored()
            }

            override fun whenSelectionRefreshed() {
                super.whenSelectionRefreshed()
            }
        }

        singleRowCalendar = fragmentTodoBinding.rowCalendar.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            setDates(getFutureDatesOfCurrentMonth())
            initialPositionIndex = CalendarDay.today().day - 3

            init()

            select(CalendarDay.today().day - 1)
        }

        fragmentTodoBinding.rowCalendar
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_todo_list, todoListFragment).addToBackStack(null).commit()

        return fragmentTodoBinding.root
    }



    fun setRowCalendarDayView(day : Int, add : Boolean) {
        if(todoListFragment.todoAdapter.todoList.size == 0)
            daysHavingTodo.remove(day)
        if(add && !daysHavingTodo.contains(day))
            daysHavingTodo.add(day)
        singleRowCalendar.adapter?.notifyDataSetChanged()
    }

    fun setTextNumberOfTasks(size: Int) {
        fragmentTodoBinding.textNumberOfTasks.text = "$size tasks"
    }

    private fun setTodoDateView(date : Date) {
        fragmentTodoBinding.dateToday.text = "$dateToday " + dayOfWeek.getDisplayName(
            TextStyle.FULL, Locale.KOREAN)
        fragmentTodoBinding.dateToday.text = getYear(date) + "-" + getMonthNumber(date) + "-" + getDayNumber(date) + " " + getDayName(date)
    }

    private fun getDatesOfNextMonth(): List<Date> {
        currentMonth++ // + because we want next month
        if (currentMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] + 1)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentMonth = calendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }


    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        list.add(calendar.time)
        while (currentMonth == calendar[Calendar.MONTH]) {
            calendar.add(Calendar.DATE, +1)
            if (calendar[Calendar.MONTH] == currentMonth)
                list.add(calendar.time)
        }
        calendar.add(Calendar.DATE, -1)
        return list
    }
}