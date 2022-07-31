package com.example.TodoManager

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TodoManager.databinding.FragmentTodoListBinding
import com.example.TodoManager.databinding.ItemTodoBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.DecimalFormat


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class TodoListFragment : Fragment() {
    lateinit var fragmentTodoListBinding: FragmentTodoListBinding
    lateinit var todoAdapter: TodoAdapter
    lateinit var itemTouchHelperCallback : ItemTouchHelperCallback
    lateinit var helper : ItemTouchHelper
    lateinit var todoRecyclerView: RecyclerView
    private var lastPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentTodoListBinding = FragmentTodoListBinding.inflate(inflater, container, false)
        todoRecyclerView = fragmentTodoListBinding.TodoList
        todoAdapter = TodoAdapter(mutableListOf(), requireActivity())
        todoRecyclerView.adapter = todoAdapter
        onDateChanged(CalendarDay.today().toDate())

        with(fragmentTodoListBinding) {
            with(floatingButton) {
                setOnClickListener {
                    val todoAddIntent = Intent(activity, TodoAddActivity::class.java)
                    (requireActivity() as MainActivity).requestNewTodoInfoLauncher.launch(todoAddIntent)
                }
            }
            with(TodoList) {
                layoutManager = LinearLayoutManager(activity)
                itemTouchHelperCallback = ItemTouchHelperCallback(todoAdapter)
                helper = ItemTouchHelper(itemTouchHelperCallback)
                helper.attachToRecyclerView(todoRecyclerView)
            }
        }

        return fragmentTodoListBinding.root
    }

    fun refresh() {
        todoRecyclerView.adapter?.notifyDataSetChanged()
    }

    fun clear() {
        todoAdapter.todoList.clear()
    }

    /**
     * Called when row-calendar selection changed
     *
     * row-calendar 에서 날짜 선택 변경 시 호출
     */
    fun onDateChanged(selectedDate : Date) {
        clear()
        (requireActivity() as MainActivity).cursor =
            (requireActivity() as MainActivity).todoDB
                .rawQuery("select * from TODO_TB where YEAR=${selectedDate.year} AND MONTH=${selectedDate.month} AND DAY=${selectedDate.day} order by POS_FOR_DATE asc", null)
        with((requireActivity() as MainActivity).cursor){
            while(moveToNext()) {
                val date = Date(getInt(DatabaseConstants.YEAR.ordinal), getInt(DatabaseConstants.MONTH.ordinal), getInt(DatabaseConstants.DAY.ordinal))
                val time = Time(getInt(DatabaseConstants.HOURS.ordinal), getInt(DatabaseConstants.MINUTES.ordinal), getInt(DatabaseConstants.IS_TIME_SET.ordinal) > 0)
                todoAdapter.todoList.add(TodoInfo(
                    name = getString(DatabaseConstants.NAME.ordinal),
                    details = getString(DatabaseConstants.DETAILS.ordinal),
                    date = date,
                    time = time))
            }
        }
        refresh()
        (parentFragment as TodoFragment).setTextNumberOfTasks(todoAdapter.todoList.size)
    }
}

interface ItemTouchHelperListener {
    fun onItemMove(from_position : Int, to_position : Int) : Boolean
    fun onItemSwipe(position: Int)
}

class ItemTouchHelperCallback(val listener: ItemTouchHelperListener) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        // 드래그 방향
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // 스와이프 방향
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        // 이동을 만드는 메소드
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onItemSwipe(viewHolder.adapterPosition)
    }

}
@RequiresApi(Build.VERSION_CODES.O)
class TodoAdapter(
    val todoList: MutableList<TodoInfo>,
    private val activity: FragmentActivity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperListener {
    // var lastPosition = -1
    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = todoList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TodoViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "CutPasteId", "ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        setAnimation(holder.itemView, position)

        val itemTodoBinding = (holder as TodoViewHolder).binding
        with(itemTodoBinding) {
            with(expandable) {
                when(position % 4) {
                    0 -> {
                        parentLayout.setBackgroundResource(R.color.cardRed)
                        secondLayout.setBackgroundResource(R.color.cardLessRed)
                    }
                    1 -> {
                        parentLayout.setBackgroundResource(R.color.cardPurple)
                        secondLayout.setBackgroundResource(R.color.cardLessPurple)
                    }
                    2 -> {
                        parentLayout.setBackgroundResource(R.color.cardBlue)
                        secondLayout.setBackgroundResource(R.color.cardLessBlue)
                    }
                    3 -> {
                        parentLayout.setBackgroundResource(R.color.cardGreen)
                        secondLayout.setBackgroundResource(R.color.cardLessGreen)
                    }
                }       // set Card's background.
                if(todoList[position].time.isTimeSet) {
                    parentLayout.findViewById<ImageView>(R.id.icon_time).visibility = View.VISIBLE
                    with(parentLayout.findViewById<TextView>(R.id.text_time)) {
                        visibility = View.VISIBLE
                        text = when(todoList[position].time.hours) {
                                0 -> {
                                    "AM 12 : "
                                }
                                in 1..11 -> {
                                    "AM ${DecimalFormat("00").format(todoList[position].time.hours)} : "
                                }
                                12 -> {
                                    "PM 12 : "
                                }
                                else -> "PM ${todoList[position].time.hours - 12} : "
                            } + DecimalFormat("00").format(todoList[position].time.minutes)
                    }
                    parentLayout.findViewById<TextView>(R.id.text_time).visibility = View.VISIBLE

                }       // set Card's time text and icon.

                parentLayout.findViewById<CheckBox>(R.id.check_box).setOnClickListener {
                    if(parentLayout.findViewById<CheckBox>(R.id.check_box).isChecked) {
                        parentLayout.findViewById<TextView>(R.id.text_todo).paintFlags =
                            parentLayout.findViewById<TextView>(R.id.text_todo).paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    else {
                        parentLayout.findViewById<TextView>(R.id.text_todo).paintFlags = 0
                    }
                }       // about check_box
                parentLayout.findViewById<TextView>(R.id.text_todo).text = todoList[position].name
                secondLayout.findViewById<TextView>(R.id.text_details).text = todoList[position].details

                parentLayout.setOnClickListener {
                    if(todoList[position].details.isNotEmpty())
                        toggleLayout()
                }
            }

        }
    }
    private fun setAnimation(viewToAnimate : View, position : Int) {
        val animation: Animation =
            AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as TodoViewHolder).binding.root.clearAnimation()
    }

    override fun onItemMove(from_position: Int, to_position: Int): Boolean {
        Log.d("Log", "OnItemMove $from_position $to_position")
        val fromTodo = todoList[from_position]
        val toTodo = todoList[to_position]
        val fromName = fromTodo.name
        val toName = toTodo.name
        (activity as MainActivity).todoDB.execSQL("update todo_tb set POS_FOR_DATE=? where NAME=? and YEAR=? and MONTH=? and DAY=?",
            arrayOf(from_position, toName, toTodo.date.year, toTodo.date.month, toTodo.date.day))
        activity.todoDB.execSQL("update todo_tb set POS_FOR_DATE=? where NAME=? and YEAR=? and MONTH=? and DAY=?",
            arrayOf(to_position, fromName, fromTodo.date.year, fromTodo.date.month, fromTodo.date.day))

        todoList.removeAt(from_position)
        todoList.add(to_position, fromTodo)

        notifyItemMoved(from_position, to_position)
        return true
    }

    override fun onItemSwipe(position: Int) {

    }
}



