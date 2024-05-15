package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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

        binding.button2.setOnClickListener {
            crear11()
        }
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

    private suspend fun cargarJugadores(teamId: String?): ArrayList<PlayerModel> {
        return suspendCoroutine { continuation ->
            buscarJugadores(teamId) { jugadores ->
                continuation.resume(jugadores)
            }
        }
    }
    private fun buscarJugadores(teamId: String?, callback: (ArrayList<PlayerModel>) -> Unit) {
        //val teamDB
        val listado = ArrayList<PlayerModel>()

        val teamRef = db.collection("teams").document(teamId!!)


        if (teamId != null) {
            teamRef.collection("players").orderBy("number")
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

    private fun crear11(){
        var jugadores: ArrayList<PlayerModel>  = ArrayList()
        val por = PlayerModel(
            name = "por",
            number = 1,
            position = "Portero",
            positionId = 0,
            teamId = currentTeam.id,
        )
        jugadores.add(por)

        val ld = PlayerModel(
            name = "Ld",
            number = 2,
            position = "Defensa",
            positionId = 1,
            teamId = currentTeam.id,
        )
        jugadores.add(ld)

        val li = PlayerModel(
            name = "Li",
            number = 4,
            position = "Defensa",
            positionId = 1,
            teamId = currentTeam.id,
        )
        jugadores.add(li)

        val ctd = PlayerModel(
            name = "Ct",
            number = 3,
            position = "Defensa",
            positionId = 1,
            teamId = currentTeam.id,
        )
        jugadores.add(ctd)

        val cti = PlayerModel(
            name = "Ct",
            number = 5,
            position = "Defensa",
            positionId = 1,
            teamId = currentTeam.id,
        )
        jugadores.add(cti)

        val mcd = PlayerModel(
            name = "Mcd",
            number = 14,
            position = "Mediocentro",
            positionId = 2,
            teamId = currentTeam.id,
        )
        jugadores.add(mcd)

        val mci = PlayerModel(
            name = "Mc",
            number = 8,
            position = "Mediocentro",
            positionId = 2,
            teamId = currentTeam.id,
        )
        jugadores.add(mci)

        val mc = PlayerModel(
            name = "Mc",
            number = 6,
            position = "Mediocentro",
            positionId = 2,
            teamId = currentTeam.id,
        )
        jugadores.add(mc)

        val ei = PlayerModel(
            name = "Ei",
            number = 7,
            position = "Delantero",
            positionId = 3,
            teamId = currentTeam.id,
        )
        jugadores.add(ei)

        val ed = PlayerModel(
            name = "Ed",
            number = 10,
            position = "Delantero",
            positionId = 3,
            teamId = currentTeam.id,
        )
        jugadores.add(ed)

        val dc = PlayerModel(
            name = "Dc",
            number = 9,
            position = "Delantero",
            positionId = 3,
            teamId = currentTeam.id,
        )
        jugadores.add(dc)

        val teamref = db.collection("teams").document(currentTeam.id)
        for (i in 0 until jugadores.size) {
            teamref.collection("players").document().set(
                hashMapOf(
                    "name" to jugadores[i].name,
                    "teamId" to jugadores[i].teamId,
                    "position" to jugadores[i].position,
                    "number" to jugadores[i].number,
                    "gamesPlayed" to jugadores[i].gamesPlayed,
                    "goalsScored" to jugadores[i].goalsScored,
                    "positionId" to jugadores[i].positionId
                )
            )
        }
        Toast.makeText(this, "Se han añadido ${jugadores.size} jugadores", Toast.LENGTH_SHORT).show()
    }


}