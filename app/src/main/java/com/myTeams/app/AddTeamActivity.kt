package com.myTeams.app

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.databinding.ActivityAddTeamBinding
import com.myTeams.app.model.TeamModel

class AddTeamActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var binding: ActivityAddTeamBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)


        binding.saveButton.setOnClickListener {
            val team =TeamModel(
                name= binding.teamNameEditText.text.toString(),
                kitColor = binding.kitSpinner.selectedItem.toString()
            )

            if (email != null) {
                db.collection("teams").document().set(
                    hashMapOf("name" to team.name,
                        "kitColor" to team.kitColor,
                        "user" to email,
                        "gamesPlayed" to team.gamesPlayed,
                        "gamesWon" to team.gamesWon,
                        "goalsScored" to team.goalsScored
                    )
                )
            }

            finish()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

    }
}