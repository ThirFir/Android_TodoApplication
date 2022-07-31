package com.example.TodoManager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.TodoManager.databinding.ActivityMainBinding


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding : ActivityMainBinding
    lateinit var requestTodoTimeLauncher : ActivityResultLauncher<Intent>
    lateinit var requestNewTodoInfoLauncher: ActivityResultLauncher<Intent>
    lateinit var todoDB : SQLiteDatabase
    lateinit var cursor : Cursor

    private val todoFragment = TodoFragment()
    //private val calendarFragment = CalendarFragment()
    //val tabs = listOf(todoFragment, calendarFragment)

    // View Pager2 Class.
//    inner class MainFragmentPagerAdapter(val fm : FragmentManager, val lc : Lifecycle) : FragmentStateAdapter(fm, lc) {
//        override fun getItemCount(): Int = tabs.size
//        override fun createFragment(position: Int): Fragment = tabs[position]
//    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setSupportActionBar(activityMainBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.drawer_icon)

        todoDB = openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
        //todoDB.execSQL("DROP TABLE TODO_TB")
        todoDB.execSQL(resources.getString(R.string.get_todo_tb))
        cursor = todoDB.rawQuery("select * from TODO_TB order by POS_FOR_DATE asc", null)

        //supportFragmentManager.beginTransaction().add(R.id.tab_layout, TodoFragment()).commit()
//        activityMainBinding.viewPager.adapter = MainFragmentPagerAdapter(supportFragmentManager, lifecycle)      // view pager
//
//        TabLayoutMediator(activityMainBinding.tabLayout, activityMainBinding.viewPager) { tab, position ->
//            tab.icon = when(position) {
//                0 -> getDrawable(R.drawable.list_icon)
//                1 -> getDrawable(R.drawable.calendar_icon)
//                else -> null
//            }
//        }.attach()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.view_fragment_todo, todoFragment).commit()


        requestTodoTimeLauncher = requestTodoTime()
        requestNewTodoInfoLauncher = requestNewTodoInfo()
    }


    private fun requestTodoTime() =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun requestNewTodoInfo() =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_CANCELED) return@registerForActivityResult

            /* Add new Todo
             * to list - TodoListFragment
             */
            val todoInfo = it.data?.getSerializableExtra("todoInfo") as TodoInfo? ?: return@registerForActivityResult

            val todoDB = openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
            todoDB.execSQL(resources.getString(R.string.get_todo_tb))
            val cursor = todoDB
                .rawQuery("select * from TODO_TB where YEAR=${todoInfo.date.year} AND MONTH=${todoInfo.date.month} AND DAY=${todoInfo.date.day} order by POS_FOR_DATE asc", null)


            var lastPos = -1

            with(cursor) {
                while (moveToNext()) {
                    if (todoInfo.name == getString(DatabaseConstants.NAME.ordinal)) {
                        Toast.makeText(this@MainActivity, "같은 이름의 TODO가 존재합니다.", Toast.LENGTH_SHORT).show()
                        return@registerForActivityResult
                    }
                    lastPos = getInt(DatabaseConstants.POS_FOR_DATE.ordinal)

                }
            }
            Log.d("Log", "$lastPos")

            with(todoInfo) {
                todoDB.execSQL("insert into TODO_TB (POS_FOR_DATE, NAME, DETAILS, YEAR, MONTH, DAY, HOURS, MINUTES, IS_TIME_SET) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(lastPos + 1, name, details, date.year, date.month, date.day, time.hours, time.minutes, time.isTimeSet))
            }

            if(todoInfo.date.isSameDate(todoFragment.selectedDate)) {
                todoFragment.todoListFragment.todoAdapter.todoList.add(todoInfo)
                todoFragment.todoListFragment.todoAdapter.notifyItemInserted(todoFragment.todoListFragment.todoAdapter.todoList.size)
                todoFragment.setTextNumberOfTasks(todoFragment.todoListFragment.todoAdapter.todoList.size)
            }
//            calendarFragment.refresh(todoInfo.date)

            Toast.makeText(this, "Success!!", Toast.LENGTH_SHORT).show()
        }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.toolbar_main, menu)
//        super.onCreateOptionsMenu(menu)
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_delete_all -> {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setTitle("Alert")
                    .setMessage("Reset the list.")
                    .setPositiveButton("OK") { _, _ ->
                        todoDB.execSQL("drop table if exists TODO_TB")
                        todoFragment.todoListFragment.clear()
                        todoFragment.todoListFragment.refresh()
                    }
                    .setNegativeButton("CANCEL") { _, _ -> }.show()
            }
            android.R.id.home -> {      // drawer icon
                activityMainBinding.drawerLayout.open()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}