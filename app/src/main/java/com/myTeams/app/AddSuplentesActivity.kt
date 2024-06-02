package com.myTeams.app

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivityAddTitularesBinding
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddSuplentesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private lateinit var binding: ActivityAddTitularesBinding

    private var listOfItem: ArrayList<String> = ArrayList()
    private var jugadoresId: ArrayList<String> = ArrayList()

    private var jugadores: ArrayList<JugadorModel> = ArrayList()
    private var seleccionados: ArrayList<JugadorModel> = ArrayList()

    private var seleccionadosId: ArrayList<String> = ArrayList()

    private var partido: PartidoModel = PartidoModel()

    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTitularesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!
        mostrarListado(listOfItem)

        binding.myTeamText.text = "Suplentes"
        binding.guardarButton3.setOnClickListener {
                partido.suplentes = seleccionados

                val resultadoIntent = Intent()
                resultadoIntent.putExtra("sonTitulares", false)
                resultadoIntent.putExtra("partido", partido)
                resultadoIntent.putExtra("suplentesId", seleccionadosId)

                setResult(Activity.RESULT_OK, resultadoIntent)
                finish()
        }

        binding.cancelarbutton.setOnClickListener {
            finish()
        }
    }

    private fun mostrarListado(listado: ArrayList<String>){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter (applicationContext,
            android.R.layout.simple_list_item_multiple_choice,
            listOfItem)

        binding.multipleListview.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        binding.multipleListview.adapter = arrayAdapter
        binding.multipleListview.onItemClickListener = this
    }

    private fun setMultipleListView(listado: ArrayList<JugadorModel>): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()
        for (jugador in listado){
            arrayList.add("${jugador.numero}. ${jugador.nombre.uppercase()}")
            jugadoresId.add(jugador.id)
            jugadores.add(jugador)
        }

        return arrayList
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val options: String = parent?.getItemAtPosition(position) as String

        if(binding.multipleListview.isItemChecked(position)){
            seleccionadosId.add(jugadoresId[position])
            seleccionados.add(jugadores[position])

        }else{
            seleccionadosId.remove(jugadoresId[position])
            seleccionados.remove(jugadores[position])
        }

    }

    private suspend fun cargarJugadores(teamId: String?): ArrayList<JugadorModel> {
        return suspendCoroutine { continuation ->
            buscarJugadores(teamId) { jugadores ->
                continuation.resume(jugadores)
            }
        }
    }
    private fun buscarJugadores(teamId: String?, callback: (ArrayList<JugadorModel>) -> Unit) {
        //val teamDB
        val listado = ArrayList<JugadorModel>()

        val teamRef = db.collection("teams").document(currentTeam.id)

        teamRef.collection("players").orderBy("posicionId")
            .get()
            .addOnSuccessListener { documents ->
                for (jugador in documents) {
                    val playerDB = jugador.toObject<JugadorModel>()
                    playerDB.id = jugador.id
                    listado.add(playerDB)
                }

                for(titular in partido.titulares){
                    listado.remove(titular)
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
            val listado = cargarJugadores(currentTeam.id)
            listOfItem = setMultipleListView(listado)
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