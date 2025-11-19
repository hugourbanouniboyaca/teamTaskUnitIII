package com.example.teamtaskkotlin.DB

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context : Context) : SQLiteOpenHelper(context,Const.DATA_BASE_NAME,null,Const.DATA_BASE_VERSION) {
    override fun onCreate(p0: SQLiteDatabase?) {
        if(p0!=null){
            p0.execSQL(Const.TABLE_TASK_CREATE)
            p0.execSQL(Const.TABLE_TASK_MG_CREATE)
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if(p0!=null){
            p0.execSQL(Const.TABLE_TASK_DROP)
            p0.execSQL(Const.TABLE_TASK_MG_DROP)
        }
        onCreate(p0)
    }
}