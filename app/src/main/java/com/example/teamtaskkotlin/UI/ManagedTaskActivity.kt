package com.example.teamtaskkotlin.UI

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamtaskkotlin.Class.Responsible
import com.example.teamtaskkotlin.Class.Task
import com.example.teamtaskkotlin.DB.Const
import com.example.teamtaskkotlin.DB.DbHelper
import com.example.teamtaskkotlin.MainActivity
import com.example.teamtaskkotlin.R
import com.google.android.material.appbar.MaterialToolbar

class ManagedTaskActivity : AppCompatActivity() {
    //List for adapters
    lateinit var listTask:List<Task>
    lateinit var listResponsibles:List<Responsible>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView
    lateinit var spResponsibles: Spinner
    lateinit var arrayAdapterSpinner : ArrayAdapter<String>

    lateinit var txtResp : TextView
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

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBarManaged)
        topBar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        //Text and Button
        txtResp = findViewById(R.id.txtResponsable)
        btnAddResp = findViewById(R.id.btnAddtask)

        //Link spinner
        spResponsibles = findViewById(R.id.spResponsibles)
        loadResponsibles()//Load Active Responsibles

        //ListView
        listView = findViewById(R.id.lvTaskWithoutResp)
        loadTask()

        //Click list view
        listView.setOnItemClickListener { adapterView, view, i, l ->
           if(listTask.count()>0) {
               idSelection = listTask[i].id
               txtResp.text ="RESPONSIBLE FOR: ${listTask[i].name}"
               btnAddResp.isEnabled = true
           }
        }

      //Click Add Responsable
        btnAddResp.setOnClickListener {
            var selection = listResponsibles[spResponsibles.selectedItemPosition].document.toString()
            if(selection=="-1"){
                Toast.makeText(this,"Please select responsible", Toast.LENGTH_SHORT).show()
            }
            else
            {
                if(idSelection!=null) {
                    //Create responsable and update task status
                    if(createResponsible(idSelection!!,listResponsibles[spResponsibles.selectedItemPosition].document.toString()) && updateTaskStatus(idSelection!!)) {
                        idSelection = null
                        txtResp.text=""
                        btnAddResp.isEnabled=false
                        loadTask()
                        loadResponsibles()
                        Toast.makeText(this,"Task was assigned",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this,"Error, can't create a responsible",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"Please, select a task",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    //Create Responsable
    fun createResponsible(idTask:Long,respDocument:String):Boolean{
        try{

            if (idTask <= 0 || respDocument.isBlank()) {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
                return false
            }

            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("TASK_ID",idTask.toString())
                put("DOCUMENT_RESPONSIBLE",respDocument)
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
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS FROM TASK T WHERE T.ID NOT IN (SELECT TASK_ID FROM TASK_MANAGER) ORDER BY T.NAME",null)
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
            btnAddResp.isEnabled=false

            //Clear List View
            listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptyList())
        }
        else{
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

    //Select all active responsible
    fun selectActiveResponsible(): List<Responsible>{
        var mlResponsible = mutableListOf<Responsible>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT DOCUMENT,NAME,PHONE, (SELECT COUNT (*) FROM TASK_MANAGER WHERE DOCUMENT_RESPONSIBLE = DOCUMENT) AS ASIGN FROM RESPONSIBLE WHERE STATE = 'A'",null)// Select responsables in active state
            cursor.use {
                while (it.moveToNext()){
                    val document = it.getLong(0)//DOCUMENT
                    val name = it.getString(1)//NAME
                    val phone = it.getString(2)//PHONE
                    val tot = it.getInt(3)//TOT TASK

                    mlResponsible.add(Responsible(document.toLong(),name,phone,tot.toInt(),"A"))//Add new object
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        return mlResponsible
    }

    //Load Responsible into Spinner
    fun loadResponsibles() {

        val activeResponsibles = selectActiveResponsible()

        //Add first option and activeResponsibles
        listResponsibles =listOf(Responsible(-1, "Select responsible", "00", 0, "I")) + activeResponsibles

        //Validate real responsibles
        if (activeResponsibles.isEmpty()) {

            btnAddResp.isEnabled = false

            //Clear ListView
            listView.adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,emptyList<String>())

        } else {

            btnAddResp.isEnabled = true

            //Spinner Text
            val viewResponsibles = listResponsibles.map {
                if (it.document == -1L) {//First Item
                    "Select responsible"
                } else {
                    "${it.name} - ${it.document} [${it.totTask}]"
                }
            }

            //Spinner Adapter
            arrayAdapterSpinner = ArrayAdapter(this,android.R.layout.simple_spinner_item,viewResponsibles)
            arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spResponsibles.adapter = arrayAdapterSpinner
        }
    }
}