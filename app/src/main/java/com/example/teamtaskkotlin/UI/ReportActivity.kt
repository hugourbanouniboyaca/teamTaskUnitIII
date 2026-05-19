package com.example.teamtaskkotlin.UI

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamtaskkotlin.Class.Task
import com.example.teamtaskkotlin.DB.Const
import com.example.teamtaskkotlin.DB.DbHelper
import com.example.teamtaskkotlin.MainActivity
import com.example.teamtaskkotlin.R
import com.google.android.material.appbar.MaterialToolbar

class ReportActivity : AppCompatActivity() {
    //List for adapters
    lateinit var listTask:List<Task>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBarReport)
        topBar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        //lISTvIEW
        listView = findViewById(R.id.lvTaskReport)
        loadTask()
        listView.setOnItemClickListener { adapterView, view, i, l ->
            deleteTask(listTask[i].id)
        }

    }

    fun deleteTask(idTask:Long){
        val msn = AlertDialog.Builder(this)
        msn.setMessage("¿Wold you like to DELETE the task?")
            .setTitle("ATENTION")
            .setPositiveButton("YES") { dialog, which ->
                if (deleteTaskId(idTask)) {
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                    loadTask()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.cancel() //Unload
            }
        msn.create().show() //View message
    }

    //Select Task
    fun selectAllTask():List<Task>{
        var mlTask = mutableListOf<Task>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS,COALESCE(RE.NAME,'N/A') AS RESPONSIBLE FROM TASK T LEFT JOIN TASK_MANAGER MG ON MG.TASK_ID = T.ID LEFT JOIN RESPONSIBLE RE ON RE.DOCUMENT = MG.DOCUMENT_RESPONSIBLE ORDER BY T.NAME",null)
            cursor.use {
                while (it.moveToNext()){
                    val id = it.getLong(0)//ID
                    val name = it.getString(1)//NAME
                    val status = it.getString(2)//STATUS
                    val resp = it.getString(3)//RESPONSABLE

                    mlTask.add(Task(id,name,status,resp))//Add new object
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        return mlTask
    }

    //Load Task into ListView
    fun loadTask(){
        listTask = selectAllTask()
        val viewTask = listTask.map { "${it.name} [${it.status}] - ${it.responsible}" }

        //Adapter for ListView
        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
            viewTask)
        listView.adapter = arrayAdapter
    }

    //Delete Task
    fun deleteTaskId(idTask:Long): Boolean{
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Read

            //DELETE
            var delTask = db.delete(Const.TABLE_TASK_NAME,"ID = ?", arrayOf(idTask.toString()))
            return delTask>0

        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }
    }
}