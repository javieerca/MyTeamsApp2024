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
import com.google.firebase.firestore.toObject
import com.myTeams.app.adapter.PlayerAdapter
import com.myTeams.app.databinding.ActivityPlayersBinding
import com.myTeams.app.model.PlayerModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlayersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayersBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!

        setup()

    }

    private fun setup(){
        binding.myTeamText2.text = "MyPlayers"

        binding.addButton2.setOnClickListener {
            val addPlayerActivityIntent = Intent(this, AddPlayerActivity::class.java)
            addPlayerActivityIntent.putExtra("equipo", currentTeam)
            startActivity(addPlayerActivityIntent)
        }


        binding.playersRecyclerView.layoutManager =
               GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }

    private fun setAdapter(listado: ArrayList<PlayerModel>) {
        binding.playersRecyclerView.adapter = PlayerAdapter(
            listado, this, db
        )
    }

    private suspend fun cargarJugadores(email: String?): ArrayList<PlayerModel> {
        return suspendCoroutine { continuation ->
            buscarJugadores(email) { jugadores ->
                continuation.resume(jugadores)
            }
        }
    }
    private fun buscarJugadores(teamId: String?, callback: (ArrayList<PlayerModel>) -> Unit) {
        //val teamDB
        val listado = ArrayList<PlayerModel>()

        val teamRef = db.collection("teams").document(teamId!!)


        if (teamId != null) {
            teamRef.collection("players")
                .get()
                .addOnSuccessListener { documents ->
                    for (jugador in documents) {
                        val playerDB = jugador.toObject<PlayerModel>()
                        playerDB.id = jugador.id
                        listado.add(playerDB)

                    }
                    binding.loadingGif2.visibility = View.INVISIBLE
                    callback(listado)
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                    callback(ArrayList()) // En caso de error, devolver una lista vacía
                }

        } else {
            binding.loadingGif2.visibility = View.INVISIBLE
            callback(ArrayList())
        }
    }

    private fun actualizar() {
        lifecycleScope.launch {
            val listado = cargarJugadores(intent.extras?.getString("teamId"))
            setAdapter(listado)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }


}