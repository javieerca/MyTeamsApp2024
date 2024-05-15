package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivityTeamBinding
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TeamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeamBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val teamId = intent.extras?.getString("idEquipo")

        if (teamId != null) {
            lifecycleScope.launch {
                currentTeam  = cargarEquipo(teamId)
            }
        }

        binding.playersButton.setOnClickListener {
            Toast.makeText(this, "Cambiando a jugadores de $teamId", Toast.LENGTH_SHORT).show()
            val teamActivityIntent = Intent(this, PlayersActivity::class.java)

            if(currentTeam.id != ""){
                teamActivityIntent.putExtra("teamId", currentTeam.id)
                teamActivityIntent.putExtra("equipo", currentTeam)
                startActivity(teamActivityIntent)
            }
        }

        binding.gamesButton.setOnClickListener {
            val partidosActivityIntent = Intent(this, PartidosActivity::class.java)

            if(currentTeam.id != ""){
                partidosActivityIntent.putExtra("teamId", currentTeam.id)
                partidosActivityIntent.putExtra("equipo", currentTeam)
                startActivity(partidosActivityIntent)
            }
        }

    }

    private suspend fun cargarEquipo(teamId: String?): TeamModel {
        return suspendCoroutine { continuation ->
            buscarEquipo(teamId) { equipo ->
                continuation.resume(equipo)
            }
        }
    }

    private fun buscarEquipo(teamId: String?, callback: (TeamModel) -> Unit) {
        val teamRef = db.collection("teams").document(teamId!!)
        teamRef.get()
            .addOnSuccessListener { document->
                currentTeam = document.toObject<TeamModel>()!!
                currentTeam.id= document.id
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting teeam: ", exception)
                callback(TeamModel()) // En caso de error, devolver una lista vacía
            }
        callback(currentTeam)
        Toast.makeText(this,"Ya hemos econtrado el equipo ${currentTeam.name}",Toast.LENGTH_SHORT).show()
    }

}