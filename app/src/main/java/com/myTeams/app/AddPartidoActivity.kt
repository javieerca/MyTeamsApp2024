package com.myTeams.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivityAddPartidoBinding
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.PlayerModel
import com.myTeams.app.model.TeamModel

class AddPartidoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPartidoBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    private var jornadaActual: Int = 0
    private var jugadoresTitularesIds: ArrayList<String> = ArrayList()
    private var jugadoresSuplentesIds: ArrayList<String> = ArrayList()

    private var partidoActual: PartidoModel = PartidoModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPartidoBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        jornadaActual = intent.extras?.getInt("jornadasJugadas")!! + 1
        partidoActual.equipoId = currentTeam.id

        setup()
        binding.contadorTitulares.text= "(11)"

    }

    private fun setup(){
        binding.jornadaNumberEdit.setText(jornadaActual.toString(),TextView.BufferType.EDITABLE)

        binding.cancelButton1.setOnClickListener {
            finish()
        }

        binding.titularesButton.setOnClickListener {
            //comprobar si hay seleccionados y seleccionarlos otra vez

            val teamActivityIntent = Intent(this, AddTitularesActivity::class.java)
            teamActivityIntent.putExtra("idEquipo", currentTeam.id)
            startActivityForResult(teamActivityIntent, 111)

        }

        binding.Suplentesbutton.setOnClickListener {
            if(binding.contadorTitulares.text != "(11)"){
                Toast.makeText(this,"Primero debes seleccionar 11 jugadores titulares.", Toast.LENGTH_SHORT).show()
            }else{
                val addSuplentesActivityIntent = Intent(this, AddSuplentesActivity::class.java)
                addSuplentesActivityIntent.putExtra("idEquipo", currentTeam.id)
                addSuplentesActivityIntent.putStringArrayListExtra("titularesId", jugadoresTitularesIds)
                startActivityForResult(addSuplentesActivityIntent, 222)
            }
        }

        binding.eventosbutton.setOnClickListener {
            if(binding.contadorTitulares.text != "(11)"){
                Toast.makeText(this, "Debes seleccionar a los jugadores titulares", Toast.LENGTH_SHORT).show()
            }else{
                /*
                val builder = AlertDialog.Builder(this@AddPartidoActivity)
                val view = layoutInflater.inflate(R.layout.activity_add_evento, null)
                builder.setView(view)
                val alertDialog = builder.create()
                bindingEventos = ActivityAddEventoBinding.inflate(alertDialog.layoutInflater)
                alertDialog.show()
                */

                val addEventoActivityIntent = Intent(this, AddEventoActivity::class.java)
                addEventoActivityIntent.putExtra("equipo", currentTeam)
                addEventoActivityIntent.putExtra("partido", partidoActual)
                addEventoActivityIntent.putStringArrayListExtra("titularesId", jugadoresTitularesIds)
                addEventoActivityIntent.putStringArrayListExtra("suplentesId", jugadoresSuplentesIds)
                startActivityForResult(addEventoActivityIntent, 777)

            }
        }

        binding.guardarButton.setOnClickListener {

            if(haTerminado()){
                var esLocal = true
                //comprobar spinner
                if(binding.estadioSpinner.selectedItem.toString() == "Visitante"){
                    esLocal = false
                }
                //crear variable resultado: String que escriba el resultado despues de ver si es local

                val partidoAguardar = PartidoModel(
                    rival = binding.rivalEditText.text.toString(),
                    equipoId = currentTeam.id,
                    local = esLocal,
                    golesEncajados = binding.encajadosTextNumber.text.toString().toInt(),
                    observaciones = binding.observacionesMultiline.text.toString(),
                    numeroJornada = binding.jornadaNumberEdit.text.toString().toInt(),
                    goles = partidoActual.goles,
                    golesMarcados = partidoActual.goles.size
                )

                db.collection("partidos").document(currentTeam.id).collection("liga").document().set(
                    hashMapOf(
                        "numeroJornada" to partidoAguardar.numeroJornada,
                        "rival" to partidoAguardar.rival,
                        "equipoId" to currentTeam.id,
                        "equipoNombre" to currentTeam.name,
                        "local" to partidoAguardar.local,
                        "golesEncajados" to partidoAguardar.golesEncajados,
                        "golesMarcados" to partidoAguardar.golesMarcados,
                        "observaciones" to partidoAguardar.observaciones
                    )
                )
                addEventos(partidoAguardar)
                actualizarInfoEquipo(partidoAguardar)
                actualizarInfoJugadores()

                Toast.makeText(this, "Se ha añadido un partido mas", Toast.LENGTH_SHORT).show()

                val builder = AlertDialog.Builder(this@AddPartidoActivity)
                builder.setMessage("¿Desea seguir añadiendo jornadas?")
                builder.setTitle("Importante")
                builder.setCancelable(false)

                builder.setPositiveButton("Sí") { _, _ ->
                    limpiar()
                }
                builder.setNegativeButton("No"){_,_ ->
                    finish()
                }

                val alertDialog = builder.create()
                alertDialog.show()
            }
            }

    }

    private fun haTerminado(): Boolean{
        if(binding.jornadaNumberEdit.text.isNullOrEmpty()){
            Toast.makeText(this, "Debe introducir el número de la jornada.", Toast.LENGTH_SHORT).show()
            return false
        }
        if(binding.rivalEditText.text.isNullOrEmpty()){
            Toast.makeText(this, "Debe introducir el nombre del equipo rival.", Toast.LENGTH_SHORT).show()
            return false
        }
        if(binding.encajadosTextNumber.text.isNullOrEmpty()){
            Toast.makeText(this, "Si no recibió goles excriba 0.", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!binding.contadorTitulares.text.equals("(11)")){
            Toast.makeText(this, "Debe seleccionar 11 jugadores titulares.", Toast.LENGTH_SHORT).show()
            return false
        }


        return true
    }


    private fun addEventos(partido: PartidoModel){
        //recorrer el arraylist de eventos
    }

    private fun actualizarInfoJugadores(){
        actualizarTitulares(jugadoresTitularesIds)
        actualizarSuplentes(jugadoresSuplentesIds)
        sumarGoles()
    }

    private fun actualizarTitulares(titulares: ArrayList<String>) {
        if (binding.contadorEventostextView.text == "(0)") {        //No hay eventos
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")

            for (jugadorId in titulares) {
                jugadoresRef.document(jugadorId).get()
                    .addOnSuccessListener { jugador ->
                        val playerDB = jugador.toObject<PlayerModel>()
                        playerDB!!.id = jugador.id

                        if(jugador != null){
                            jugadoresRef.document(playerDB.id).update(
                                hashMapOf<String, Any>(
                                    "partidosConvocado" to playerDB.partidosConvocado + 1,
                                    "gamesPlayed" to playerDB.gamesPlayed + 1,
                                    "gamesStarted" to playerDB.gamesStarted + 1,
                                    "minutesPlayed" to playerDB.minutesPlayed + 90
                                )
                            )
                        }
                    }
            }
        }
    } //terminar con cambios

    private fun actualizarSuplentes(suplentes: ArrayList<String>) {
        if (binding.contadorEventostextView.text == "(0)") {        //No hay eventos
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")

            for (jugadorId in suplentes) {
                jugadoresRef.document(jugadorId).get()
                    .addOnSuccessListener { jugador ->
                        val playerDB = jugador.toObject<PlayerModel>()
                        playerDB!!.id = jugador.id

                        if(jugador != null){
                            jugadoresRef.document(playerDB.id).update(
                                hashMapOf<String, Any>(
                                    "partidosConvocado" to playerDB.partidosConvocado + 1,
                                    "gamesStarted" to playerDB.gamesStarted + 0,
                                    "minutesPlayed" to playerDB.minutesPlayed + 0
                                )
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    } //terminar con cambios

    private fun sumarGoles(){
        if(partidoActual.goles.size == 0){
            //no hay goles añadidos
        }else{
            for (gol in partidoActual.goles){
                val idGoleador = gol.jugadoresImplicados[0].id

                val jugadoresRef =
                    db.collection("teams").document(currentTeam.id).collection("players")


                jugadoresRef.document(idGoleador).update(
                    hashMapOf<String, Any>(
                        "goalsScored" to gol.jugadoresImplicados[0].goalsScored + 1
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Se ha añadido el gol de ${gol.jugadoresImplicados[0].name}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun actualizarInfoEquipo(partido: PartidoModel){

        currentTeam.gamesPlayed += 1

        if (partido.golesMarcados > partido.golesEncajados){
            currentTeam.gamesWon += 1
        }else if(partido.golesMarcados < partido.golesEncajados){
            currentTeam.derrotas += 1
        }
        if(partido.golesMarcados == partido.golesEncajados){
            currentTeam.empates += 1
        }

        currentTeam.goalsScored += partido.golesMarcados
        currentTeam.golesEncajados += partido.golesEncajados

        db.collection("teams").document(currentTeam.id).update(
            hashMapOf<String, Any>(
                "gamesPlayed" to currentTeam.gamesPlayed,
                "gamesWon" to currentTeam.gamesWon,
                "gamesLost" to currentTeam.derrotas,
                "gamesDraw" to currentTeam.empates,
                "goalsScored" to currentTeam.goalsScored,
                "goalsConceded" to currentTeam.golesEncajados
            )
        )
    }

    private fun limpiar(){
        binding.rivalEditText.text = null
        binding.encajadosTextNumber.setText("0")
        binding.observacionesMultiline.text = null
        binding.estadioSpinner.setSelection(0)

        binding.contadorTitulares.text = "(0)"
        binding.contadorSuplentes.text = "(0)"
        binding.contadorEventostextView.text = "(0)"

        //actualizar numero jornada

        //comprobar que se han sumado las estadisticas al equipo y jugadores
    }


    //Confirma que se han seleccionado los jugadores titulares (codigo:111)
    //Confirma que se han seleccionado los jugadores suplentes (codigo:222)

    //Confirma que se han llegado los eventos (codigo:222)

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            jugadoresTitularesIds = data?.getStringArrayListExtra("titulares")!!
            binding.contadorTitulares.text = "("+jugadoresTitularesIds.size+")"
            Toast.makeText(this, "Titulares: ${jugadoresTitularesIds.size}",Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            jugadoresSuplentesIds = data?.getStringArrayListExtra("suplentes")!!
            binding.contadorSuplentes.text = "(${jugadoresSuplentesIds.size})"
            Toast.makeText(this, "Titulares: ${jugadoresSuplentesIds.size}",Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            partidoActual = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            Toast.makeText(this, "Hay ${partidoActual.goles.size} eventos",Toast.LENGTH_SHORT).show()
            val cantidadEventos = partidoActual.goles.size + partidoActual.amonestaciones.size + partidoActual.sustituciones.size
            binding.golesanotadostextView.text = partidoActual.goles.size.toString()
            binding.contadorEventostextView.text = "($cantidadEventos)"

        }
    }

}