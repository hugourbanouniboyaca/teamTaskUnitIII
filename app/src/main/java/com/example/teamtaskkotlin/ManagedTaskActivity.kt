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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamtaskkotlin.Class.Task
import com.example.teamtaskkotlin.DB.Const
import com.example.teamtaskkotlin.DB.DbHelper
import com.google.android.material.appbar.MaterialToolbar

class ManagedTaskActivity : AppCompatActivity() {
    //List for adapters
    lateinit var listTask:List<Task>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView

    lateinit var txtResp : EditText
    lateinit var btnAddResp : Button

    //Selection Task
    var idSelection : Long? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_managed_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Link fields

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBarManaged)
        topBar.setNavigationOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
        //Text and Button
        txtResp = findViewById(R.id.txtResponsable)
        btnAddResp = findViewById(R.id.btnAddtask)

        //ListView
        listView = findViewById(R.id.lvTaskWithoutResp)
        loadTask()
        //Click list view
        listView.setOnItemClickListener { adapterView, view, i, l ->
           if(listTask.count()>0) {
               idSelection = listTask[i].id
               txtResp.hint = "RESPONSIBLE FOR: ${listTask[i].name}"
               txtResp.isEnabled = true
               btnAddResp.isEnabled = true
           }
        }

      //Click Add Responsable
        btnAddResp.setOnClickListener {
            if(!txtResp.text.isNullOrEmpty())
            {
                if(idSelection!=null) {
                    //Create responsable and update task status
                    if(createResponsable(idSelection!!, txtResp.text.toString().uppercase()) && updateTaskStatus(idSelection!!)) {
                        idSelection = null
                        txtResp.text.clear()
                        txtResp.isEnabled=false
                        btnAddResp.isEnabled=false
                        loadTask()
                        Toast.makeText(this,"Task was assigned",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this,"Error, can't create a responsable",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"Please, select a task",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Responsable is required",Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Create Responsable
    fun createResponsable(idTask:Long,respName:String):Boolean{
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("TASK_ID",idTask.toString())
                put("RESPONSABLE",respName)
            }

            //Insert
            return db.insert(Const.TABLE_TASK_MG_NAME,null,data) != -1L
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
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS FROM TASK T WHERE T.ID NOT IN (SELECT TASK_ID FROM TASK_MANAGER) ORDER BY T.NAME"
                ,null)
            cursor.use {
                while (it.moveToNext()){
                    val id = it.getLong(0)//ID
                    val name = it.getString(1)//NAME
                    val status = it.getString(2)//STATUS

                    mlTask.add(Task(id,name,status,"N/A"))//Add new object
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

        //Without task, text and button are unabled
        if(listTask.count()==0){
            txtResp.isEnabled=false
            btnAddResp.isEnabled=false

            //Clear List View
            listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptyList())
        }
        else{
            txtResp.isEnabled=true
            btnAddResp.isEnabled=true

            //Load ListView
            val viewTask = listTask.map { "${it.name} [${it.status}]" }

            //Adapter for ListView
            arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
                viewTask)
            listView.adapter = arrayAdapter
        }
    }

    //Updated task status to assigned
    fun updateTaskStatus(idTask:Long):Boolean{
        try {
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("STATUS", "ASSIGNED")
            }

            //Update
            return db.update(
                Const.TABLE_TASK_NAME, data,
                "ID = ?", arrayOf(idTask.toString())
            ) > 0

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }
    }
}