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
import com.myTeams.app.model.JugadorModel
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
            var posicionId=0
            if (binding.positionSpinner.selectedItem.toString() == "Defensa"){
                posicionId=1
            }else if(binding.positionSpinner.selectedItem.toString() == "Centrocampista"){
                posicionId=2
            }
            else if(binding.positionSpinner.selectedItem.toString() == "Delantero"){
                posicionId=3
            }


            if (valido) {
                val player = JugadorModel(
                    nombre = binding.nameEditText.text.toString(),
                    numero = binding.numberEditText.text.toString().toInt(),
                    posicion = binding.positionSpinner.selectedItem.toString(),
                    posicionId = posicionId,
                    equipoId = currentTeam.id,

                )

                val teamref = db.collection("teams").document(currentTeam.id)

                teamref.collection("players").document().set(
                    hashMapOf(
                        "nombre" to player.nombre,
                        "equipoId" to player.equipoId,
                        "posicion" to player.posicion,
                        "numero" to player.numero,
                        "partidosJugados" to player.partidosJugados,
                        "golesMarcados" to player.golesMarcados,
                        "posicionId" to player.posicionId,
                        "tarjetasAmarillas" to player.tarjetasAmarillas,
                        "tarjetasRojas" to player.tarjetasRojas
                    )
                )
                finish()
                Toast.makeText(this, "Vamos a añadir a " + player.nombre, Toast.LENGTH_SHORT).show()

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