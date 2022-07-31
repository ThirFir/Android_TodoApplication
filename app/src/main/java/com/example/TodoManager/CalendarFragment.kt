//package com.example.StudyManager
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.graphics.Color
//import android.graphics.Typeface
//import android.os.Build
//import android.os.Bundle
//import android.text.style.ForegroundColorSpan
//import android.text.style.RelativeSizeSpan
//import android.text.style.StyleSpan
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.RequiresApi
//import androidx.fragment.app.FragmentActivity
//import androidx.recyclerview.widget.DividerItemDecoration
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.StudyManager.databinding.FragmentCalendarBinding
//import com.prolificinteractive.materialcalendarview.*
//import com.prolificinteractive.materialcalendarview.spans.DotSpan
//import java.time.LocalDate
//import java.time.format.TextStyle
//import java.util.*
//
//
//@RequiresApi(Build.VERSION_CODES.O)
//class CalendarFragment : Fragment() {
//    lateinit var fragmentCalenderBinding : FragmentCalendarBinding
//    private val todoList = mutableListOf<TodoInfo>()
//    private val datesHavingTODO = mutableListOf<Date>()
//    lateinit var todoDB : SQLiteDatabase
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View? {
//        setHasOptionsMenu(false)
//        todoDB = requireActivity().openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
//        todoDB.execSQL(resources.getString(R.string.get_todo_tb))
//
//        val cursor = todoDB.rawQuery("select * from TODO_TB", null)
//        with(cursor){
//            while(moveToNext()) {
//                val date = Date(getInt(DatabaseConstants.YEAR.ordinal), getInt(DatabaseConstants.MONTH.ordinal),
//                    getInt(DatabaseConstants.DAY.ordinal))
//                val time = Time(getInt(DatabaseConstants.HOURS.ordinal), getInt(DatabaseConstants.MINUTES.ordinal))
//                if(!datesHavingTODO.contains(date))
//                    datesHavingTODO.add(date)
//                if(date.isToday())
//                todoList.add(TodoInfo(
//                    name = getString(DatabaseConstants.NAME.ordinal),
//                    details = getString(DatabaseConstants.DETAILS.ordinal),
//                    date,
//                    time
//                ))
//            }
//        }
//
//        fragmentCalenderBinding = FragmentCalendarBinding.inflate(layoutInflater)
//
//        with(fragmentCalenderBinding) {
//            var selectedDay = 0
//
//            with(calendar) {
//                setHeaderTextAppearance(R.font.dalseo_healing_medium)
//                selectedDate = CalendarDay.today()
//                addDecorator(TodayDecorator())
//
//                setOnDateChangedListener {
//                        widget,
//                        date,
//                        selected ->
//                    run {
//                        refreshList(date.toDate())
//                        if (selectedDay == date.day) {
//                            val dayOfWeek = LocalDate.of(date.year, date.month, date.day).dayOfWeek
//                            val dialog = CalendarDialogFragment()
//                            with(Bundle()) {
//                                putSerializable("date", date.toDate())
//                                putString("day_of_week", dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN))
//                                dialog.arguments = this
//                            }
//                            dialog.show(parentFragmentManager, "Custom")
//                        } else
//                            selectedDay = date.day
//                    }
//                }
//
//                for (date in datesHavingTODO)
//                    addDecorator(HasTodoDecorator(date))
//            }
//            with(fragmentTodoListCalendar) {
//                layoutManager = LinearLayoutManager(activity)
//                adapter = TodoAdapter(this@CalendarFragment.todoList, requireActivity())
//                addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
//            }
//            return root
//        }
//    }
//    @SuppressLint("NotifyDataSetChanged")
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun refresh(showingDate : Date) {
//        refreshList(showingDate)
//        for (dates in datesHavingTODO)
//            fragmentCalenderBinding.calendar.addDecorator(HasTodoDecorator(dates))
//
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun refreshList(showingDate : Date) {
//        todoList.clear()
//        todoDB = requireActivity().openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
//        val cursor = todoDB.rawQuery("select * from TODO_TB", null)
//        with(cursor){
//            while(moveToNext()) {
//                val date = Date(getInt(DatabaseConstants.YEAR.ordinal), getInt(DatabaseConstants.MONTH.ordinal),
//                    getInt(DatabaseConstants.DAY.ordinal))
//                val time = Time(getInt(DatabaseConstants.HOURS.ordinal), getInt(DatabaseConstants.MINUTES.ordinal))
//                if(!datesHavingTODO.contains(date))
//                    datesHavingTODO.add(date)
//                if (date == showingDate)
//                    todoList.add(TodoInfo(
//                        name = getString(DatabaseConstants.NAME.ordinal),
//                        details = getString(DatabaseConstants.DETAILS.ordinal),
//                        date,
//                        time
//                    ))
//            }
//        }
//        fragmentCalenderBinding.fragmentTodoListCalendar.adapter = TodoAdapter(todoList, activity ?: return)
//        (fragmentCalenderBinding.fragmentTodoListCalendar.adapter as TodoAdapter).notifyDataSetChanged()
//    }
//}
//
//class TodoAdapter(
//    private val todoList: MutableList<TodoInfo>,
//    private val activity: FragmentActivity,
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)
//
//    override fun getItemCount(): Int = todoList.size
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
//        TodoViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//
//    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//        val itemTodoBinding = (holder as TodoViewHolder).binding
//        with(itemTodoBinding) {
//            textTodo.text = todoList[position].name
//        }
//    }
//
//}
//
//class TodayDecorator : DayViewDecorator {
//    private val date = CalendarDay.today()
//
//    override fun decorate(view: DayViewFacade?) {
//        view?.addSpan(StyleSpan(Typeface.BOLD))
//        view?.addSpan(RelativeSizeSpan(1.4f))
//        view?.addSpan(ForegroundColorSpan(Color.parseColor("#1D872A")))
//    }
//
//    override fun shouldDecorate(day: CalendarDay?): Boolean {
//        return day?.equals(date)!!
//    }
//}
//
//class HasTodoDecorator(private val date: Date): DayViewDecorator {
//
//    override fun shouldDecorate(day: CalendarDay?): Boolean {
//        return day?.let { date.isSameDate(it) } ?: false
//    }
//
//    override fun decorate(view: DayViewFacade?) {
//        view?.addSpan(DotSpan(10F, Color.parseColor("#1D872A")))
//    }
//}
//
//
