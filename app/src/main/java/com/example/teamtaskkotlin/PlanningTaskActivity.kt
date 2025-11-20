package com.example.teamtaskkotlin

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamtaskkotlin.Class.Task
import com.example.teamtaskkotlin.Class.Task_Planning
import com.example.teamtaskkotlin.DB.Const
import com.example.teamtaskkotlin.DB.DbHelper
import com.google.android.material.appbar.MaterialToolbar

class PlanningTaskActivity : AppCompatActivity() {

    //List for adapters
    lateinit var listPlanningTask:List<Task_Planning>
    lateinit var listTask:List<Task>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView
    lateinit var spTaskPlanning : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_planning_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Fields

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBarPlanning)
        topBar.setNavigationOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }

        //Spiner
        spTaskPlanning = findViewById(R.id.spTaskPlanning)
        loadSpinnerTask()

        //ListView
        listView = findViewById(R.id.lvPlanningTask)
        loadPlanningTask()

        //Text
        var txtYear = findViewById<EditText>(R.id.txtYearPlanning)
        var txtMonth = findViewById<EditText>(R.id.txtMonthPlanning)
        var txtDay = findViewById<EditText>(R.id.txtDayPlanning)
        var txtHrs = findViewById<EditText>(R.id.txtHrsPlanning)

        //Button
        var btnSavePlanning = findViewById<Button>(R.id.btnSavePlanning)
        btnSavePlanning.setOnClickListener {
            if (!txtYear.text.isNullOrEmpty() && !txtMonth.text.isNullOrEmpty() && !txtDay.text.isNullOrEmpty() && !txtHrs.text.isNullOrEmpty()){
                var selection = spTaskPlanning.selectedItem.toString()
                if(selection=="-- Select Task --"){
                    Toast.makeText(this,"Please select task", Toast.LENGTH_SHORT).show()
                }
                else{
                    //Validate fields
                    if(validateDateHour(txtYear.text.toString().toInt(),txtMonth.text.toString().toInt(),txtDay.text.toString().toInt(),txtHrs.text.toString().toInt())) {
                        //Insert
                        if (createPlanningTask(
                                listTask[spTaskPlanning.selectedItemPosition].id,
                                txtYear.text.toString(),
                                txtMonth.text.toString(),
                                txtDay.text.toString(),
                                txtHrs.text.toString()
                            )
                        ) {
                            Toast.makeText(this, "Planning created", Toast.LENGTH_SHORT).show()
                            loadPlanningTask()

                            //Clena fields
                            txtYear.text.clear()
                            txtMonth.text.clear()
                            txtDay.text.clear()
                            txtHrs.text.clear()
                        } else {
                            Toast.makeText(
                                this,
                                "Error, can't create planning task",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(this,"Please, enter year, month, day and hours", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Validate date and Hour
    fun validateDateHour(year : Int,month : Int,day : Int, hrs: Int): Boolean {
        if (year < 2025) {
            Toast.makeText(this, " The year must be greater than 2025", Toast.LENGTH_SHORT).show()
            return false
        }

        if (month !in 1..12) {
            Toast.makeText(this, "The month must be between 1 and 12", Toast.LENGTH_SHORT).show()
            return false
        }

        if (month == 2 && day > 28) {
            Toast.makeText(this, "February cannot have more than 28 days", Toast.LENGTH_SHORT).show()
            return false
        }

        if (day !in 1..31) {
            Toast.makeText(this, "The day must be between 1 and 31", Toast.LENGTH_SHORT).show()
            return false
        }

        if (hrs <= 0) {
            Toast.makeText(this, "Hours must be greater than zero", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    //Create Planning task
    fun createPlanningTask(taskId:Long,year:String,month:String,day:String,Hrs:String):Boolean{
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("TASK_ID",taskId.toString())
                put("YEAR",year)
                put("MONTH",month)
                put("DAY",day)
                put("HOURS",Hrs)
            }

            //Insert
            return db.insert(Const.TABLE_TASK_PLANNING_NAME,null,data) != -1L
        }
        catch (e:Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
            return false
        }
    }

    //Select Task whit responsible
    fun selectAllPlanningTask():List<Task_Planning>{
        var mlTask = mutableListOf<Task_Planning>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS,MG.RESPONSABLE,TP.YEAR,TP.MONTH,TP.DAY,TP.HOURS FROM TASK T JOIN TASK_MANAGER MG ON MG.TASK_ID = T.ID AND T.STATUS NOT LIKE 'COMPLETED' JOIN TASK_PLANNING TP ON TP.TASK_ID = T.ID ORDER BY T.NAME"
                ,null)
            cursor.use {
                while (it.moveToNext()){
                    val id = it.getLong(0)//ID
                    val name = it.getString(1)//NAME
                    val status = it.getString(2)//STATUS
                    val resp = it.getString(3)//RESPONSABLE
                    val year = it.getString(4)//YEAR
                    val month = it.getString(5)//MONTH
                    val day = it.getString(6)//DAY
                    val hours = it.getString(7)//HOURS

                    mlTask.add(Task_Planning(id,name,status,resp,year,month,day,hours))//Add new object
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        return mlTask
    }

    //Load Task into ListView
    fun loadPlanningTask(){
        listPlanningTask = selectAllPlanningTask()
        val viewTask = listPlanningTask.map { "${it.name} [${it.status}] - ${it.responsible} -> ${it.day}/${it.month}/${it.year} - ${it.hours}Hrs" }

        //Adapter for ListView
        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
            viewTask)
        listView.adapter = arrayAdapter
    }

    //Select all task
    fun selectAllTask():List<Task>{
        var mlTask = mutableListOf<Task>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT T.ID,T.NAME,T.STATUS,MG.RESPONSABLE FROM TASK T JOIN TASK_MANAGER MG ON MG.TASK_ID = T.ID AND T.STATUS NOT LIKE 'COMPLETED' ORDER BY T.NAME"
                ,null)
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

    //Load Task into Spinner
    fun loadSpinnerTask(){
        listTask = listOf(Task(-1,"-- Select Task --","INACTIVE","N/A")) + selectAllTask()
        val viewTask = listTask.map { "${it.name} [${it.status}] - ${it.responsible}" }

        //Adapter for ListView
        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,
            viewTask)
        spTaskPlanning.adapter = arrayAdapter
    }
}