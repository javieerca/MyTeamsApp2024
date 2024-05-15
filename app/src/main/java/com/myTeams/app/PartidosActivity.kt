package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.myTeams.app.adapter.PartidoAdapter
import com.myTeams.app.adapter.PlayerAdapter
import com.myTeams.app.databinding.ActivityPartidosBinding
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PartidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartidosBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    var numeroPartidos: Int=0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPartidosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!

        if (currentTeam.id != null) {
            lifecycleScope.launch {
                val listado = cargarPartidos(currentTeam.id)
                numeroPartidos = listado.size
                setAdapter(listado)
            }
        }

        setup()

    }

    private fun setup(){

        binding.addbutton.setOnClickListener {
            val addPartidoActivityIntent = Intent(this, AddPartidoActivity::class.java)

            if(currentTeam.id != ""){
                addPartidoActivityIntent.putExtra("teamId", currentTeam.id)
                addPartidoActivityIntent.putExtra("equipo", currentTeam)
                addPartidoActivityIntent.putExtra("jornadasJugadas", numeroPartidos)
                startActivity(addPartidoActivityIntent)
            }
        }

        binding.partidosRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }

    private fun setAdapter(listado: ArrayList<PartidoModel>) {
        binding.partidosRecyclerView.adapter = PartidoAdapter(
            listado, this, db
        )
        numeroPartidos = listado.size
    }

    private suspend fun cargarPartidos(teamId: String?): ArrayList<PartidoModel> {
        return suspendCoroutine { continuation ->
            buscarPartidos(teamId) { partidos ->
                continuation.resume(partidos)
            }
        }
    }
    private fun buscarPartidos(teamId: String?, callback: (ArrayList<PartidoModel>) -> Unit) {
        //val teamDB
        val listado = ArrayList<PartidoModel>()

        if (teamId != null) {
            db.collection("partidos").document(currentTeam.id)
                .collection("liga")
                .orderBy("numeroJornada", Query.Direction.DESCENDING)
                .get()

                .addOnSuccessListener { documents ->
                    for (partido in documents) {
                        val partidoDB = partido.toObject<PartidoModel>()
                        partidoDB.id = partido.id
                        listado.add(partidoDB)
                    }
                    binding.loadingGif3.visibility = View.INVISIBLE
                    callback(listado)
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                    callback(ArrayList()) // En caso de error, devolver una lista vacía
                }

        } else {
            binding.loadingGif3.visibility = View.INVISIBLE
            callback(ArrayList())
        }
    }

    private fun actualizar() {
        lifecycleScope.launch {
            val listado = cargarPartidos(currentTeam.id)
            setAdapter(listado)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }

}