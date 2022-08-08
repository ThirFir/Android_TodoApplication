package com.example.TodoManager

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TodoManager.databinding.FragmentTodoListBinding
import com.example.TodoManager.databinding.ItemTodoBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.io.Serializable
import java.text.DecimalFormat


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class TodoListFragment : Fragment() {
    lateinit var fragmentTodoListBinding: FragmentTodoListBinding
    lateinit var todoAdapter: TodoAdapter
    lateinit var helper : ItemTouchHelper
    lateinit var helperCallback : ItemTouchHelperCallback
    lateinit var todoRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentTodoListBinding = FragmentTodoListBinding.inflate(inflater, container, false)
        todoRecyclerView = fragmentTodoListBinding.TodoList
        todoAdapter = TodoAdapter(mutableListOf(), requireActivity())
        todoRecyclerView.adapter = todoAdapter

//        HoldableSwipeHandler.Builder(requireActivity())
//            .setOnRecyclerView(todoRecyclerView)
//            .setSwipeButtonAction(object : SwipeButtonAction {
//                override fun onClickFirstButton(absoluteAdapterPosition: Int) {
//                    val item = todoAdapter.todoList[absoluteAdapterPosition]
//                }
//            })
//            .setDismissOnClickFirstItem(false)
//            .build()


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
                helperCallback = ItemTouchHelperCallback(todoAdapter)
                helper = ItemTouchHelper(ItemTouchHelperCallback(todoAdapter))
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
        with(requireActivity() as MainActivity) {
            todoDB.execSQL(resources.getString(R.string.get_todo_tb))
            cursor = todoDB.rawQuery("select * from TODO_TB where YEAR=${selectedDate.year} AND MONTH=${selectedDate.month} AND DAY=${selectedDate.day} order by POS_FOR_DATE asc", null)
            with(cursor) {
                while (moveToNext()) {
                    val date = Date(getInt(DatabaseConstants.YEAR.ordinal),
                        getInt(DatabaseConstants.MONTH.ordinal),
                        getInt(DatabaseConstants.DAY.ordinal))
                    val time = Time(getInt(DatabaseConstants.HOURS.ordinal),
                        getInt(DatabaseConstants.MINUTES.ordinal),
                        getInt(DatabaseConstants.IS_TIME_SET.ordinal) > 0)
                    todoAdapter.todoList.add(TodoInfo(
                        name = getString(DatabaseConstants.NAME.ordinal),
                        details = getString(DatabaseConstants.DETAILS.ordinal),
                        date = date,
                        time = time))
                }
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

@SuppressLint("ClickableViewAccessibility")
class ItemTouchHelperCallback(val listener: ItemTouchHelperListener) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        return makeMovementFlags(UP or DOWN, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return listener.onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {        // Swipe해서 밖으로 내보내면 호출(1번만 호출됨)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n", "ClickableViewAccessibility", "CutPasteId", "ResourceAsColor",
    "NotifyDataSetChanged", "Recycle")
class TodoAdapter(
    val todoList: MutableList<TodoInfo>,
    private val activity: FragmentActivity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperListener {
    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root), Serializable
    lateinit var itemTodoBinding : ItemTodoBinding

    private val backgroundColorList = listOf(
        listOf(R.color.cardRed, R.color.cardLessRed),
        listOf(R.color.cardOrange, R.color.cardLessOrange),
        listOf(R.color.cardYellow, R.color.cardLessYellow),
        listOf(R.color.cardGreen, R.color.cardLessGreen),
        listOf(R.color.cardBlue, R.color.cardLessBlue),
        listOf(R.color.cardNavy, R.color.cardLessNavy),
        listOf(R.color.cardPurple, R.color.cardLessPurple),
        listOf(R.color.cardMint, R.color.cardLessMint),
        listOf(R.color.cardBrown, R.color.cardLessBrown),
        listOf(R.color.cardGray, R.color.cardLessGray)
    )
    private var backgroundColorCursor : Int = 0



    override fun getItemCount(): Int = todoList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TodoViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        setAnimation(holder.itemView)
        setViewHolderBackgroundColor(holder, position)

        with(itemTodoBinding) {
            with(expandable) {
                if(isExpanded)
                    toggleLayout()

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
                        secondLayout.findViewById<TextView>(R.id.text_details).visibility = View.VISIBLE
                    toggleLayout()
                }


                secondLayout.findViewById<ImageView>(R.id.icon_item_option).setOnClickListener {
                    activity as MainActivity
                    val mainConstraintLayout = activity.activityMainBinding.mainActivityLayout

                    val touchedAbsolutePosition = intArrayOf(0, 0)
                    it.getLocationOnScreen(touchedAbsolutePosition)
                    val touchedAbsoluteX = touchedAbsolutePosition[0]
                    val touchedAbsoluteY = touchedAbsolutePosition[1]

                    val layoutInflater : LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val horizontalMenu : ConstraintLayout =
                        layoutInflater.inflate(R.layout.menu_speech_bubble, mainConstraintLayout, false) as ConstraintLayout
                    val param = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

                    // param.setMargins(touchedAbsoluteX, touchedAbsoluteY, 0, 0)
                    if(horizontalMenu.parent != null)
                        (horizontalMenu.parent as ViewGroup).removeView(horizontalMenu)
                    mainConstraintLayout.addView(horizontalMenu, param)

                    // MainActivity의 전체 화면 담당하는 ConstraintLayout에 부착
                    with(ConstraintSet()) {
                        clone(activity.activityMainBinding.mainActivityLayout)
                        connect(
                            R.id.menu_bubble_layout, ConstraintSet.START, mainConstraintLayout.id, ConstraintSet.START,
                            touchedAbsoluteX
                        )
                        connect(
                            R.id.menu_bubble_layout, ConstraintSet.TOP, mainConstraintLayout.id, ConstraintSet.TOP,
                            touchedAbsoluteY
                        )
                        applyTo(mainConstraintLayout)
                    }

                    // cardview 제거
                    horizontalMenu.findViewById<ImageView>(R.id.bubble_item_remove).setOnClickListener {
                        (horizontalMenu.parent as ViewGroup).removeView(horizontalMenu)     // 메뉴 닫음

                        /**
                         * OnItemMove 실행 후 notifyDataSetChanged() 호출 전까진
                         * 실제 Position이 변경되지 않아서 item 제거 시 문제가 발생하는 것 같음
                         * 따라서 클릭 parentLayout의 text를 따와서 제거하는 방식 채택
                         */
                        val name = parentLayout.findViewById<TextView>(R.id.text_todo).text
                        val date = Date(todoList[position].date.year, todoList[position].date.month, todoList[position].date.day)
                        activity.todoDB.execSQL("delete from TODO_TB where YEAR=? AND MONTH=? AND DAY=? AND NAME=?",
                            arrayOf(date.year, date.month, date.day, name))
                        var realPosition : Int = -1
                        for (t in todoList) {
                            ++realPosition
                            if(t.name == name) {
                                todoList.remove(t)
                                break
                            }
                        }
                        notifyItemRemoved(realPosition)

                        //clearAnimation(this@with)
                        notifyItemRangeChanged(realPosition, todoList.size)

                        //setAnimation(holder.itemView)
                        activity.todoFragment.setTextNumberOfTasks(todoList.size)
                        activity.todoFragment.setRowCalendarDayView(date.day, false)

                    }

                    horizontalMenu.findViewById<ImageView>(R.id.bubble_item_color).setOnClickListener {
                        ColorSetDialog().apply {
                            arguments = Bundle().apply {
                                putSerializable("holder", holder as TodoViewHolder)
                                putInt("position", position)
                            }
                        }.show(activity.supportFragmentManager, "color")
                    }
                }


            }

        }
    }

    fun setViewHolderBackgroundColor(holder: RecyclerView.ViewHolder, position: Int, passedColor : Int = -2) {
        Log.d("colorChangeMethod", "Invoked $position")
        itemTodoBinding = (holder as TodoAdapter.TodoViewHolder).binding
        val cursor = (activity as MainActivity).todoDB.rawQuery("select * from todo_tb " +
                "where YEAR=${todoList[0].date.year} and MONTH=${todoList[0].date.month} and day=${todoList[0].date.day}", null)

        with(cursor) {
            moveToNext()
            while (getString(DatabaseConstants.NAME.ordinal) != todoList[position].name)
                moveToNext()
            var color = getInt(DatabaseConstants.COLOR.ordinal)
            if(color == -1) {
                val setColor: Int = if (passedColor == -2)
                    backgroundColorCursor
                else
                    passedColor

                activity.todoDB.execSQL("update todo_tb set COLOR=? " +
                        "where YEAR=? and MONTH=? and day=? and NAME=?",
                    arrayOf(setColor,
                        todoList[0].date.year,
                        todoList[0].date.month,
                        todoList[0].date.day,
                        todoList[position].name))
                color = setColor
            }
            else if(passedColor != -2 && color != passedColor) {
                activity.todoDB.execSQL("update todo_tb set COLOR=? " +
                        "where YEAR=? and MONTH=? and day=? and NAME=?",
                    arrayOf(passedColor,
                        todoList[0].date.year,
                        todoList[0].date.month,
                        todoList[0].date.day,
                        todoList[position].name))
                color = passedColor
            }
            when(color) {
                -1 -> throw IllegalArgumentException("Color set -1")
                else -> {
                    itemTodoBinding.expandable.parentLayout.setBackgroundResource(backgroundColorList[color][0])
                    itemTodoBinding.expandable.secondLayout.setBackgroundResource(backgroundColorList[color][1])
                }
            }
            ++backgroundColorCursor
            if (backgroundColorCursor == backgroundColorList.size)
                backgroundColorCursor = 0
        }
    }

    private fun setAnimation(viewToAnimate : View) {
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



