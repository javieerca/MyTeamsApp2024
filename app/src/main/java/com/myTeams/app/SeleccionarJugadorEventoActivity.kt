package com.myTeams.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.databinding.ActivitySeleccionarJugadorEventoBinding
import com.myTeams.app.model.PartidoModel

import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch

class SeleccionarJugadorEventoActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private lateinit var binding: ActivitySeleccionarJugadorEventoBinding
    private var equipoId: String = ""

    private var listOfItem: ArrayList<String> = ArrayList()
    private var jugadoresId: ArrayList<String> = ArrayList()

    private var listado: ArrayList<JugadorModel> = ArrayList()

    private var playerSelected: JugadorModel = JugadorModel()

    private var seleccionadoId: String? = null
    private var jugadorSeleccionado: JugadorModel = JugadorModel()
    private var titularesSuplentesTodos: Int = 0
    private var partido: PartidoModel = PartidoModel()

    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySeleccionarJugadorEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        equipoId = intent.extras?.getString("idEquipo")!!
        titularesSuplentesTodos = intent.extras?.getInt("titularesSuplentes")!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        if(titularesSuplentesTodos == 1){
            listado = partido.titulares
        }else if (titularesSuplentesTodos == 2){
            listado = partido.titulares
        }
        else if(titularesSuplentesTodos == 0){
            listado = partido.convocados
        }
        mostrarListado(listOfItem)


        binding.guardarButton3.setOnClickListener {
            if(playerSelected.id == ""){
                Toast.makeText(this, "Debes seleccionar 1 jugador.", Toast.LENGTH_SHORT).show()
            }else{
                  enviarJugadorAtras()
            }
        }

        binding.cancelarbutton.setOnClickListener {
            finish()
        }
    }
    private fun enviarJugadorAtras(){
        val resultadoIntent = Intent()
        resultadoIntent.putExtra("jugador", playerSelected)
        setResult(Activity.RESULT_OK, resultadoIntent)
        finish()
    }

    private fun mostrarListado(listado: ArrayList<String>){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter (applicationContext,
            android.R.layout.simple_list_item_multiple_choice,
            listado)

        binding.multipleListview.choiceMode = ListView.CHOICE_MODE_SINGLE
        binding.multipleListview.adapter = arrayAdapter
        binding.multipleListview.onItemClickListener = this
    }

    private fun setMultipleListView(listado: ArrayList<JugadorModel>): ArrayList<String> {
        /*
        val arrayList: ArrayList<String> = ArrayList()
        if (sonSuplentes == true) {
            for (jugador in partido.suplentes) {
                arrayList.add("${jugador.numero}. ${jugador.nombre.uppercase()}")
            }

        }else if(sonSuplentes == false || sonSuplentes == null){
            for (jugador in partido.suplentes) {
                arrayList.add("${jugador.numero}. ${jugador.nombre.uppercase()}")
            }
        }
        return arrayList

         */
        val arrayList: ArrayList<String> = ArrayList()
        for (jugador in listado){
            arrayList.add("${jugador.numero}. ${jugador.nombre.uppercase()}")
        }
        return arrayList

    }



    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val options: String = parent?.getItemAtPosition(position) as String
        if(binding.multipleListview.isItemChecked(position)){
            playerSelected = listado[position]
        }else{
            playerSelected = JugadorModel()
        }

    }


    private fun actualizar() {
        lifecycleScope.launch {
            if(titularesSuplentesTodos == 1){
                listado = partido.titulares
                listOfItem = setMultipleListView(partido.titulares)
                mostrarListado(listOfItem)
            }else if(titularesSuplentesTodos == 2){
                listado = partido.suplentes
                listOfItem = setMultipleListView(partido.suplentes)
                mostrarListado(listOfItem)
            }else if(titularesSuplentesTodos == 0){
                listado = partido.convocados
                listOfItem = setMultipleListView(partido.convocados)
                mostrarListado(listOfItem)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter (applicationContext,
            android.R.layout.simple_list_item_multiple_choice,
            listOfItem)
        binding.multipleListview.adapter = arrayAdapter
    }

}