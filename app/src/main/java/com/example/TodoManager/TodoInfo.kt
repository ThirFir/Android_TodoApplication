package com.example.TodoManager

import com.prolificinteractive.materialcalendarview.CalendarDay
import java.io.Serializable

data class TodoInfo (var name : String, var details : String, val date : Date, val time : Time) : Serializable
data class Date(var year : Int, var month : Int, var day : Int) : Serializable {

    fun isSameDate(other : Date) : Boolean =
        year == other.year && month == other.month && day == other.day
    fun isSameDate(other : CalendarDay) : Boolean =
        year == other.year && month == other.month && day == other.day
    fun isToday() : Boolean =
        year == CalendarDay.today().year && month == CalendarDay.today().month && day == CalendarDay.today().day
}
data class Time(var hours : Int, var minutes : Int, var isTimeSet : Boolean = false) : Serializable

fun CalendarDay.toDate() : Date = Date(this.year, this.month, this.day)