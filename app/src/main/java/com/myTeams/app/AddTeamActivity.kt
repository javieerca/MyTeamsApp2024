package com.myTeams.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        binding.savebutton.setOnClickListener {
            val team =TeamModel(
                nombre= binding.teamNameEditText.text.toString(),
                colorEquipacion = binding.kitSpinner.selectedItem.toString()
            )

            if (email != null) {
                db.collection("teams").document().set(
                    hashMapOf("nombre" to team.nombre,
                        "colorEquipacion" to team.colorEquipacion,
                        "usuario" to email,
                        "partidosJugados" to team.partidosJugados,
                        "victorias" to team.victorias,
                        "golesMarcados" to team.golesMarcados
                    )
                )
                    .addOnSuccessListener {
                        Toast.makeText(this, "Hemos añadido el equipo", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha ocurrido un error inesperado", Toast.LENGTH_SHORT).show()
                    }
            }

            finish()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.atrasbutton.setOnClickListener{
            finish()
        }
    }
}