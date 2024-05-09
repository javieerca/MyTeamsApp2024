package com.myTeams.app

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.databinding.ActivityAddPlayerBinding
import com.myTeams.app.model.PlayerModel
import com.myTeams.app.model.TeamModel

class AddPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlayerBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPlayerBinding.inflate(layoutInflater)
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
        binding.saveButton.setOnClickListener {
            var valido = false
            if (!binding.nameEditText.text.isNullOrEmpty() && !binding.numberEditText.text.isNullOrEmpty()) {
                valido = true
            }


            if (valido) {
                val player = PlayerModel(
                    name = binding.nameEditText.text.toString(),
                    number = binding.numberEditText.text.toString().toInt(),
                    position = binding.positionSpinner.selectedItem.toString(),
                    teamId = currentTeam.id
                )

                val teamref = db.collection("teams").document(currentTeam.id)

                teamref.collection("players").document().set(
                    hashMapOf(
                        "name" to player.name,
                        "teamId" to player.teamId,
                        "position" to player.position,
                        "number" to player.number,
                        "gamesPlayed" to player.gamesPlayed,
                        "goalsScored" to player.goalsScored
                    )
                )
                finish()
                Toast.makeText(this, "Vamos a añadir a " + player.name, Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            finish()
            //añadir confimarSalir
        }

    }
}