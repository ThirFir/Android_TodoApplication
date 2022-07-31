package com.example.TodoManager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.TodoManager.databinding.ActivityTodoTimerBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.timer

class TodoTimerActivity : AppCompatActivity() {
    lateinit var activityTodoTimerBinding: ActivityTodoTimerBinding
    private var totalTime = 0L
    private var elapsedTime = 0L
    private var ID = -1
    private var seconds = 0
    private var minutes = 0
    private var hours = 0
    private var timerTask : Timer? = null
    lateinit var todoDB : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityTodoTimerBinding = ActivityTodoTimerBinding.inflate(layoutInflater)
        setContentView(activityTodoTimerBinding.root)


        initializeVariables()
        initializeActivityTexts()

        setButtonState(3)
        if(elapsedTime != 0L)
            activityTodoTimerBinding.btnReset.isEnabled = true

        activityTodoTimerBinding.btnStart.setOnClickListener{
            startTimer()
            setButtonState(1)
        }
        activityTodoTimerBinding.btnStop.setOnClickListener{
            stopTimer()
            updateElapsedTimeOfDB()
            setButtonState(2)
        }
        activityTodoTimerBinding.btnReset.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("타이머를 초기화합니다.")
                .setPositiveButton("OK") { _, _ ->
                    run {
                        elapsedTime = 0L
                        seconds = 0
                        minutes = 0
                        hours = 0

                        stopTimer()
                        timerHandler()

                        updateElapsedTimeOfDB()
                        setButtonState(3)
                    }
                }
                .setNegativeButton("CANCEL") { _, _ ->
                    run {
                        // cancel button
                    }
                }.show()
        }
    }

    private fun setButtonState(button : Int) = when(button) {
        1 -> {
            activityTodoTimerBinding.btnStop.isEnabled = true
            activityTodoTimerBinding.btnReset.isEnabled = true
            activityTodoTimerBinding.btnStart.isEnabled = false
        }
        2 -> {
            activityTodoTimerBinding.btnStop.isEnabled = false
            activityTodoTimerBinding.btnReset.isEnabled = true
            activityTodoTimerBinding.btnStart.isEnabled = true
        }
        3 -> {
            activityTodoTimerBinding.btnStop.isEnabled = false
            activityTodoTimerBinding.btnReset.isEnabled = false
            activityTodoTimerBinding.btnStart.isEnabled = true
        }
        else -> {
            throw Exception("Illegal button state index.")
        }
    }
    private fun initializeVariables() {
//        todoDB = openOrCreateDatabase("todo_db", Context.MODE_PRIVATE, null)
//        ID = intent.getIntExtra("todoID", -1)
//
//        val cursor = todoDB.rawQuery("select * from TODO_TB where ID = $ID", null)
//        cursor.moveToFirst()
//
//        if(cursor != null) {
//            totalTime = cursor.getLong(DatabaseConstants.TOTAL_TIME.ordinal)
//            elapsedTime = totalTime - cursor.getLong(DatabaseConstants.REST_TIME.ordinal)
//        }
//        seconds = (elapsedTime % 60).toInt()
//        minutes = (elapsedTime / 60 % 60).toInt()
//        hours = (elapsedTime / 3600 % 24).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeActivityTexts(){
        val cursor = todoDB.rawQuery("select * from TODO_TB where ID = $ID", null)
        cursor.moveToFirst()

        activityTodoTimerBinding.textTodoName.text = cursor.getString(DatabaseConstants.NAME.ordinal)
        activityTodoTimerBinding.textTotal.text = "목표 시간  ${DecimalFormat("00").format(totalTime / 3600 % 24)}시간 " +
                "${DecimalFormat("00").format(totalTime / 60 % 60)}분"

        activityTodoTimerBinding.textHours.text = DecimalFormat("00").format(hours)
        activityTodoTimerBinding.textMinutes.text = DecimalFormat("00").format(minutes)
        activityTodoTimerBinding.textSeconds.text = DecimalFormat("00").format(seconds)
    }

    private fun startTimer() {
        timerTask = timer(period = 1000, initialDelay = 1000) {
            ++elapsedTime
            seconds = (elapsedTime % 60).toInt()
            minutes = (elapsedTime / 60 % 60).toInt()
            hours = (elapsedTime / 3600 % 24).toInt()

            timerHandler()
            if(elapsedTime == totalTime){
                // finish
            }
        }
    }

    // Main Thread UI Handler.
    private fun timerHandler() {
        object : Handler(Looper.getMainLooper()){
            override fun handleMessage(inputMessage: Message){
                activityTodoTimerBinding.textSeconds.text = DecimalFormat("00").format(seconds)
                activityTodoTimerBinding.textMinutes.text = DecimalFormat("00").format(minutes)
                activityTodoTimerBinding.textHours.text = DecimalFormat("00").format(hours)
            }
        }.obtainMessage().sendToTarget()
    }

    private fun stopTimer() {
        timerTask?.cancel()
    }
    private fun updateElapsedTimeOfDB() {
        todoDB.execSQL("update TODO_TB set TODO_REST_TIME = ${totalTime - elapsedTime} WHERE ID = $ID")
    }

    override fun onPause() {
        stopTimer()
        intent.putExtra("elapsedTime", elapsedTime)

        updateElapsedTimeOfDB()
        super.onPause()
    }

    override fun onStop() {
        stopTimer()
        intent.putExtra("elapsedTime", elapsedTime)

        updateElapsedTimeOfDB()
        super.onStop()
    }

    override fun onDestroy() {
        stopTimer()
        intent.putExtra("elapsedTime", elapsedTime)

        updateElapsedTimeOfDB()
        super.onDestroy()
    }
}