package com.example.teamtaskkotlin

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    //List for adapters
    lateinit var listTask:List<Task>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Fields

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBar)
        topBar.setOnMenuItemClickListener {
                MenuItem ->
            when(MenuItem.itemId){
                R.id.miAsignTask->{
                    startActivity(Intent(this,ManagedTaskActivity::class.java))
                    true
                }
                R.id.miReportTask->{
                    startActivity(Intent(this,ReportActivity::class.java))
                    true
                }
                else->{
                    false
                }
            }
        }

        //ListView
        listView = findViewById(R.id.lvTask)
        loadTask()//Load all task
        //Click list view
        // adapterView: referencia al ListView
        // view: la vista que se pulsó
        // i: índice del elemento (0, 1, 2, ...)
        // l: id interno del elemento (normalmente igual a position)
        listView.setOnItemClickListener { adapterView, view, i, l ->
            changeStatus(listTask[i].id)
        }

        //Text and Button
        var txtTaskName = findViewById<EditText>(R.id.txtTaskName)
        var btnCreateTask = findViewById<Button>(R.id.btnCreateTask)
        btnCreateTask.setOnClickListener {
            if(!txtTaskName.text.isNullOrEmpty()){
                createTask(txtTaskName.text.toString().uppercase())
                loadTask()//Update list
                txtTaskName.text.clear()
            }
            else{
                Toast.makeText(this,"task name is required",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Create task
    fun createTask(name:String):Boolean{
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("NAME",name)
                put("STATUS","CREATED")
            }

            //Insert
            return db.insert(Const.TABLE_TASK_NAME,null,data) != -1L
        }
        catch (e:Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
            return false
        }
    }

    //Select Task
    fun selectAllTask():List<Task>{
        var mlTask = mutableListOf<Task>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS,COALESCE(MG.RESPONSABLE,'N/A') AS RESPONSABLE FROM TASK T LEFT JOIN TASK_MANAGER MG ON MG.TASK_ID = T.ID ORDER BY T.NAME"
                ,null)
            cursor.use {
                while (it.moveToNext()){
                    val id = it.getLong(0)//ID
                    val name = it.getString(1)//NAME
                    val status = it.getString(2)//STATUS
                    val resp = it.getString(3)//RESPONSIBLE

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

    //Change task status
     fun changeStatus(idTask: Long) {
        val msn = AlertDialog.Builder(this)
        msn.setMessage("¿Wold you like to COMPLETE the task?")
            .setTitle("ATENTION")
            .setPositiveButton("YES") { dialog, which ->
                if (updateTaskDB(idTask)) {
                    Toast.makeText(this, "Updated status", Toast.LENGTH_SHORT).show()
                    loadTask()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.cancel() //Unload
            }
        msn.create().show() //View message
    }

    //Update
    fun updateTaskDB(idUpdate:Long):Boolean {
        try {
            //Find the object and evaluate status
            val taskFind = listTask.find { it.id == idUpdate }
            if(!taskFind?.status.equals("COMPLETED")) {
                //Create DB Object
                var helper = DbHelper(baseContext)
                val db = helper.writableDatabase//Write

                //Values
                val data = ContentValues().apply {
                    put("STATUS", "COMPLETED")
                }

                //Update
                return db.update(
                    Const.TABLE_TASK_NAME, data,
                    "ID = ?", arrayOf(idUpdate.toString())
                ) > 0
            }
            else{
                Toast.makeText(this, "Object can't update", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }
    }


}