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
import com.myTeams.app.model.JugadorModel
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
        window.statusBarColor = getColor(R.color.verdeTitulos)



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

        binding.atrasbutton.setOnClickListener {
            finish()
        }

        binding.playersRecyclerView.layoutManager =
               GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }

    private fun setAdapter(listado: ArrayList<JugadorModel>) {
        binding.playersRecyclerView.adapter = PlayerAdapter(
            listado, this, db
        )
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

        val teamRef = db.collection("teams").document(teamId!!)


        if (teamId != null) {
            teamRef.collection("players").orderBy("numero")
                .get()
                .addOnSuccessListener { documents ->
                    for (jugador in documents) {
                        val playerDB = jugador.toObject<JugadorModel>()
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
            val listado = cargarJugadores(intent.extras?.getString("equipoId"))
            setAdapter(listado)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }

    private fun crear11(){
        val jugadores: ArrayList<JugadorModel>  = ArrayList()
        val por = JugadorModel(
            nombre = "por",
            numero = 1,
            posicion = "Portero",
            posicionId = 0,
            equipoId = currentTeam.id,
        )
        jugadores.add(por)

        val ld = JugadorModel(
            nombre = "Ld",
            numero = 2,
            posicion = "Defensa",
            posicionId = 1,
            equipoId = currentTeam.id,
        )
        jugadores.add(ld)

        val li = JugadorModel(
            nombre = "Li",
            numero = 4,
            posicion = "Defensa",
            posicionId = 1,
            equipoId = currentTeam.id,
        )
        jugadores.add(li)

        val ctd = JugadorModel(
            nombre = "Ct",
            numero = 3,
            posicion = "Defensa",
            posicionId = 1,
            equipoId = currentTeam.id,
        )
        jugadores.add(ctd)

        val cti = JugadorModel(
            nombre = "Ct",
            numero = 5,
            posicion = "Defensa",
            posicionId = 1,
            equipoId = currentTeam.id,
        )
        jugadores.add(cti)

        val mcd = JugadorModel(
            nombre = "Mcd",
            numero = 14,
            posicion = "Mediocentro",
            posicionId = 2,
            equipoId = currentTeam.id,
        )
        jugadores.add(mcd)

        val mci = JugadorModel(
            nombre = "Mc",
            numero = 8,
            posicion = "Mediocentro",
            posicionId = 2,
            equipoId = currentTeam.id,
        )
        jugadores.add(mci)

        val mc = JugadorModel(
            nombre = "Mc",
            numero = 6,
            posicion = "Mediocentro",
            posicionId = 2,
            equipoId = currentTeam.id,
        )
        jugadores.add(mc)

        val ei = JugadorModel(
            nombre = "Ei",
            numero = 7,
            posicion = "Delantero",
            posicionId = 3,
            equipoId = currentTeam.id,
        )
        jugadores.add(ei)

        val ed = JugadorModel(
            nombre = "Ed",
            numero = 10,
            posicion = "Delantero",
            posicionId = 3,
            equipoId = currentTeam.id,
        )
        jugadores.add(ed)

        val dc = JugadorModel(
            nombre = "Dc",
            numero = 9,
            posicion = "Delantero",
            posicionId = 3,
            equipoId = currentTeam.id,
        )
        jugadores.add(dc)

        val teamref = db.collection("teams").document(currentTeam.id)
        for (i in 0 until jugadores.size) {
            teamref.collection("players").document().set(
                hashMapOf(
                    "nombre" to jugadores[i].nombre,
                    "equipoId" to jugadores[i].equipoId,
                    "posicion" to jugadores[i].posicion,
                    "numero" to jugadores[i].numero,
                    "partidosJugados" to jugadores[i].partidosJugados,
                    "golesMarcados" to jugadores[i].golesMarcados,
                    "posicionId" to jugadores[i].posicionId
                )
            )
        }
        Toast.makeText(this, "Se han añadido ${jugadores.size} jugadores", Toast.LENGTH_SHORT).show()
    }


}