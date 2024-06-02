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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivityAddTeamBinding
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class EditarEquipoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTeamBinding
    private val db = FirebaseFirestore.getInstance()
    private var equipoActual: TeamModel = TeamModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        equipoActual = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!

        setup()
    }

    private fun setup(){
        binding.textView20.text = "Editar equipo"
        binding.teamNameEditText.setText(equipoActual.nombre)

        binding.savebutton.text = "Guardar"
        binding.savebutton.setOnClickListener {
            val team =TeamModel(
                nombre= binding.teamNameEditText.text.toString(),
                colorEquipacion = binding.kitSpinner.selectedItem.toString()
            )
            db.collection("teams").document(equipoActual.id)
                .update(
                    hashMapOf<String, Any>(
                        "nombre" to team.nombre,
                        "colorEquipacion" to team.colorEquipacion)
            )
                .addOnSuccessListener {

                    val listado = ArrayList<PartidoModel>()

                    db.collection("partidos").document(equipoActual.id)
                        .collection("liga")
                        .orderBy("numeroJornada", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { documents ->        //carga info partido
                            for (partido in documents) {
                                val partidoDB = partido.toObject<PartidoModel>()
                                partidoDB.id = partido.id
                                listado.add(partidoDB)
                            }

                            for(partido in listado){
                                db.collection("partidos").document(equipoActual.id).collection("liga")
                                    .document(partido.id).update(
                                        hashMapOf<String, Any>(
                                            "equipoNombre" to team.nombre
                                        )
                                    )
                            }
                        }
                    Toast.makeText(this, "Hemos guardado sus cambios.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error inesperado", Toast.LENGTH_SHORT).show()
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