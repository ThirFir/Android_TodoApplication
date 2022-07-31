package com.example.TodoManager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object FragmentLifeCycle{
    fun replace(activity: FragmentActivity, location: Int, fragment: Fragment) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(location, fragment).commit()
    }

    fun add(activity: FragmentActivity, location: Int, fragment: Fragment) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.add(location, fragment).commit()
    }

}