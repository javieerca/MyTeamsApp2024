package com.myTeams.app

import android.app.AlertDialog
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

class EditarJugadorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlayerBinding
    private val db = FirebaseFirestore.getInstance()
    private var jugador: JugadorModel = JugadorModel()

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
        window.statusBarColor = getColor(R.color.verdeTitulos)

        jugador = intent.extras?.getSerializable("jugador", JugadorModel::class.java)!!


        setup()
    }

    private fun setup(){
        binding.myTeamText5.text = "Editar jugador"
        binding.nameEditText.setText(jugador.nombre)
        binding.numberEditText.setText(jugador.numero.toString())
        binding.positionSpinner.setSelection(jugador.posicionId)

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
                    )
                val teamref = db.collection("teams").document(jugador.equipoId)

                teamref.collection("players").document(jugador.id).update(
                    hashMapOf<String,Any>(
                        "nombre" to player.nombre,
                        "posicion" to player.posicion,
                        "numero" to player.numero,
                        "posicionId" to player.posicionId
                    )
                )

                val builder = AlertDialog.Builder(this@EditarJugadorActivity)
                builder.setMessage("Se están guardando sus cambios.")
                builder.setTitle("Importante")
                builder.setCancelable(false)

                builder.setPositiveButton("OK") { _, _ ->
                    finish()
                }
                val alertDialog = builder.create()
                alertDialog.show()

            } else {
                Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.atrasbutton.setOnClickListener {
            confirmCerrar()
        }
        binding.cancelButton.setOnClickListener {
            finish()
            //añadir confimarSalir
        }
    }

    private fun confirmCerrar(){
        val builder = AlertDialog.Builder(this@EditarJugadorActivity)
        builder.setMessage("¿Desea salir sin guardar?")
        builder.setTitle("Importante")
        builder.setCancelable(false)

        builder.setPositiveButton("si") { _, _ ->
            finish()
        }
        builder.setNegativeButton("no"){_,_ ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}