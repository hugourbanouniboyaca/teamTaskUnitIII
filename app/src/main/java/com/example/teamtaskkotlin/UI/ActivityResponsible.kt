package com.example.teamtaskkotlin.UI

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
import com.example.teamtaskkotlin.Class.Responsible
import com.example.teamtaskkotlin.DB.Const
import com.example.teamtaskkotlin.DB.DbHelper
import com.example.teamtaskkotlin.MainActivity
import com.example.teamtaskkotlin.R
import com.google.android.material.appbar.MaterialToolbar

class ActivityResponsible : AppCompatActivity() {
    lateinit var listResponsible : List<Responsible>
    lateinit var arrayAdapter : ArrayAdapter<String>
    lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responsible)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Link Layout
        val txtRespName = findViewById<EditText>(R.id.txtResponsableName)
        val txtRespDocumente = findViewById<EditText>(R.id.txtResponsableDocument)
        val txtRespPhone = findViewById<EditText>(R.id.txtResponsablePhone)
        val btnCreateResp = findViewById<Button>(R.id.btnCreateResponsable)
        listView = findViewById<ListView>(R.id.lvResponsableList)
        loadResponsibleListView()//Load List

        //TopBar
        var topBar = findViewById<MaterialToolbar>(R.id.topBarResponsable)
        topBar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnCreateResp.setOnClickListener {
            if(!txtRespName.text.toString().isNullOrEmpty() && !txtRespDocumente.text.toString().isNullOrEmpty() && !txtRespPhone.text.toString().isNullOrEmpty() ){
                if(!searchSpecificResponsible(txtRespDocumente.text.toString().toInt())){
                    if(createResponsible(Responsible(txtRespDocumente.text.toString().toLong(),txtRespName.text.toString(),txtRespPhone.text.toString(),0,"A"))){
                        Toast.makeText(this,"Responsible created", Toast.LENGTH_SHORT).show()
                        loadResponsibleListView()
                        txtRespName.text.clear()
                        txtRespDocumente.text.clear()
                        txtRespPhone.text.clear()
                    }
                    else{
                        Toast.makeText(this,"Error to crete responsible", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"Document already exist", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"All fiels are required", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Create Responsable
    fun createResponsible(resp : Responsible): Boolean{
        try{
            if(resp.document<=0){
                Toast.makeText(this,"Document is not valid", Toast.LENGTH_SHORT).show()
                return false
            }
            //Data Base
            val helper = DbHelper(baseContext)
            val db = helper.writableDatabase//Write

            //Values
            val data = ContentValues().apply {
                put("DOCUMENT",resp.document)
                put("NAME",resp.name)
                put("PHONE",resp.phone)
                put("STATE","A")
            }

            //Insert
            return db.insert(Const.TABLE_RESPONSIBLE_NAME,null,data) != -1L
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }
    }

    //Search specific document
    fun searchSpecificResponsible(docSearch: Int): Boolean{
        try {
            //Data Base
            val helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Count rows
            val query = "SELECT COUNT(*) FROM responsible WHERE document = ?"
            val cursor = db.rawQuery(query, arrayOf(docSearch.toString()))

            var exist = false
            if (cursor.moveToFirst()) {
                val count = cursor.getInt(0)//First column
                exist = count > 0
            }

            cursor.close()
            return exist
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }
    }

    //Select Responsables
    fun selectAllResponsables():List<Responsible>{
        var mlResponsible = mutableListOf<Responsible>()
        try{
            //Create DB Object
            var helper = DbHelper(baseContext)
            val db = helper.readableDatabase//Read

            //Select
            val cursor = db.rawQuery("SELECT DOCUMENT,NAME,PHONE, (SELECT COUNT (*) FROM TASK_MANAGER WHERE DOCUMENT_RESPONSIBLE = DOCUMENT) AS ASIGN FROM RESPONSIBLE WHERE STATE = 'A'",null)//ToDo: Select responsables in active state
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

    //Load Responsable into ListView
    fun loadResponsibleListView(){
        listResponsible = selectAllResponsables()
        val viewTask = listResponsible.map { "${it.name} - ${it.document} [${it.totTask}]" }

        //Adapter for ListView
        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,viewTask)
        listView.adapter = arrayAdapter
    }
}