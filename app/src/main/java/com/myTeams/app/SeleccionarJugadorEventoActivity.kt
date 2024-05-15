package com.myTeams.app

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivitySeleccionarJugadorEventoBinding

import com.myTeams.app.model.PlayerModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SeleccionarJugadorEventoActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private lateinit var binding: ActivitySeleccionarJugadorEventoBinding
    private var equipoId: String = ""

    private var listOfItem: ArrayList<String> = ArrayList()
    private var jugadoresId: ArrayList<String> = ArrayList()

    private var listado: ArrayList<PlayerModel> = ArrayList()

    private var playerSelected: PlayerModel = PlayerModel()

    private var seleccionadoId: String? = null
    private var jugadorSeleccionado: PlayerModel = PlayerModel()

    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()

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

        equipoId = intent.extras?.getString("idEquipo")!!
        mostrarListado(listOfItem)


        binding.guardarButton3.setOnClickListener {
            if(playerSelected.id == ""){
                Toast.makeText(this, "Debes seleccionar 1 jugador.", Toast.LENGTH_SHORT).show()
            }else{
                  enviarJugadorAtras()
            }
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
            listOfItem)

        binding.multipleListview.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        binding.multipleListview.adapter = arrayAdapter
        binding.multipleListview.onItemClickListener = this
    }

    private fun setMultipleListView(listado: ArrayList<PlayerModel>): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()
        for (jugador in listado){
            arrayList.add("${jugador.number}. ${jugador.name.uppercase()}")
        }
        return arrayList
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val options: String = parent?.getItemAtPosition(position) as String
        if(binding.multipleListview.isItemChecked(position)){
            playerSelected = listado[position]
        }else{
            playerSelected = PlayerModel()
        }

    }

    private suspend fun cargarJugadores(teamId: String?): ArrayList<PlayerModel> {
        return suspendCoroutine { continuation ->
            buscarJugadores(teamId) { jugadores ->
                continuation.resume(jugadores)
            }
        }
    }
    private fun buscarJugadores(teamId: String?, callback: (ArrayList<PlayerModel>) -> Unit) {
        //val teamDB
        val teamRef = db.collection("teams").document(teamId!!)
        teamRef.collection("players").orderBy("positionId")
            .get()
            .addOnSuccessListener { documents ->
                for (jugador in documents) {
                    val playerDB = jugador.toObject<PlayerModel>()
                    playerDB.id = jugador.id
                    listado.add(playerDB)
                }
                callback(listado)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                callback(ArrayList()) // En caso de error, devolver una lista vacía
            }
    }

    private fun actualizar() {
        lifecycleScope.launch {
            listOfItem = setMultipleListView(cargarJugadores(equipoId))
            mostrarListado(listOfItem)
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