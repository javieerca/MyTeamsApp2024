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
import com.myTeams.app.databinding.ActivityAddPartidoBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorEnPartido
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.TeamModel
import com.myTeams.app.model.minutosJugados

class AddPartidoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPartidoBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    private var jornadaActual: Int = 0
    private var jugadoresTitularesIds: ArrayList<String> = ArrayList()
    private var jugadoresSuplentesIds: ArrayList<String> = ArrayList()
    private var goleadores : ArrayList<JugadorModel> = ArrayList()
    private var tienenAmarilla: ArrayList<JugadorModel> = ArrayList()

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

    }

    private fun setup(){
        binding.jornadaNumberEdit.setText(jornadaActual.toString(),TextView.BufferType.EDITABLE)

        binding.cancelButton1.setOnClickListener {
            finish()
        }

        binding.titularesButton.setOnClickListener {
            //comprobar si hay seleccionados y seleccionarlos otra vez

            val addTitularesActivityIntent = Intent(this, AddTitularesActivity::class.java)
            addTitularesActivityIntent.putExtra("equipo", currentTeam)
            addTitularesActivityIntent.putExtra("partido", partidoActual)
            startActivityForResult(addTitularesActivityIntent, 111)

        }

        binding.Suplentesbutton.setOnClickListener {
            if(binding.contadorTitulares.text != "(11)"){
                Toast.makeText(this,"Primero debes seleccionar 11 jugadores titulares.", Toast.LENGTH_SHORT).show()
            }else{
                val addSuplentesActivityIntent = Intent(this, AddSuplentesActivity::class.java)
                addSuplentesActivityIntent.putExtra("equipo", currentTeam)
                addSuplentesActivityIntent.putExtra("partido", partidoActual)
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
                addEventoActivityIntent.putStringArrayListExtra("titulares", jugadoresTitularesIds)
                addEventoActivityIntent.putStringArrayListExtra("suplentes", jugadoresSuplentesIds)
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

                val datosPartido = PartidoModel(
                    rival = binding.rivalEditText.text.toString(),
                    equipoId = currentTeam.id,
                    local = esLocal,
                    golesEncajados = binding.encajadosTextNumber.text.toString().toInt(),
                    observaciones = binding.observacionesMultiline.text.toString(),
                    numeroJornada = binding.jornadaNumberEdit.text.toString().toInt(),
                    goles = partidoActual.goles,
                    golesMarcados = partidoActual.goles.size
                )

                val nuevoDocumento = db.collection("partidos").document(currentTeam.id).collection("liga").document()
                nuevoDocumento.set(
                    hashMapOf(
                        "numeroJornada" to datosPartido.numeroJornada,
                        "rival" to datosPartido.rival,
                        "equipoId" to datosPartido.equipoId,
                        "equipoNombre" to currentTeam.nombre,
                        "local" to datosPartido.local,
                        "golesEncajados" to datosPartido.golesEncajados,
                        "golesMarcados" to datosPartido.golesMarcados,
                        "observaciones" to datosPartido.observaciones
                    )
                ).addOnSuccessListener {
                    partidoActual.id = nuevoDocumento.id
                    Toast.makeText(this, "Id partido: ${partidoActual.id}", Toast.LENGTH_SHORT).show()
                    addEventos()
                    actualizarInfoEquipo(datosPartido)
                    //actualizarInfoJugadores()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Algo ha ido mal", Toast.LENGTH_SHORT).show()
                }

                //subir goles al partido
                for(gol in partidoActual.goles){
                    val golesRef = db.collection("partidos").document(currentTeam.id).collection("liga").document(nuevoDocumento.id).collection("goles")
                    golesRef.document().set(
                        hashMapOf(
                            "goleadorId" to gol.jugadoresImplicados[0].id,
                            "goleadorNombre" to gol.jugadoresImplicados[0].nombre,
                            "numero" to gol.jugadoresImplicados[0].numero,
                            "minuto" to gol.minuto
                        )
                    )
                }
                //subir amonestaciones al partido
                for (tarjeta in partidoActual.amonestaciones){
                    nuevoDocumento.collection("amonestaciones").document().set(
                        hashMapOf<String, Any>(
                            "amonestadoId" to tarjeta.jugadoresImplicados[0].id,
                            "amonestadoNombre" to tarjeta.jugadoresImplicados[0].nombre,
                            "amonestadoNumero" to tarjeta.jugadoresImplicados[0].numero,
                            "tipoTarjetaId" to tarjeta.tipoEventoId,
                            "tipoTarjetaNombre" to tarjeta.tipoEventoNombre,
                            "minuto" to tarjeta.minuto
                        )
                    )
                }
                //subir cambios al partido
                for (cambio in partidoActual.sustituciones){
                    nuevoDocumento.collection("sustituciones").document().set(
                        hashMapOf<String, Any>(
                            "entraId" to cambio.jugadoresImplicados[0].id,
                            "entraNombre" to cambio.jugadoresImplicados[0].nombre,
                            "entraNumero" to cambio.jugadoresImplicados[0].numero,
                            "saleId" to cambio.jugadoresImplicados[1].id,
                            "saleNombre" to cambio.jugadoresImplicados[1].nombre,
                            "saleNumero" to cambio.jugadoresImplicados[1].numero,
                            "minuto" to cambio.minuto
                        )
                    )

                    //sumar cambio a sustituido
                    val jugadoresRef =
                        db.collection("teams").document(currentTeam.id).collection("players")
                    jugadoresRef.document(cambio.jugadoresImplicados[1].id).update(
                        hashMapOf<String, Any>(
                            "vecesCambiado" to cambio.jugadoresImplicados[1].vecesCambiado + 1
                        )
                    )
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
                }

                //subir titulares
                for(jugador in partidoActual.titulares){
                    nuevoDocumento.collection("titulares").document().set(
                        hashMapOf<String, Any>(
                            "nombre" to jugador.nombre,
                            "id" to jugador.id,
                            "numero" to jugador.numero
                        )
                    )
                }
                //subir suplentes
                for(jugador in partidoActual.suplentes){
                    nuevoDocumento.collection("suplentes").document().set(
                        hashMapOf<String, Any>(
                            "nombre" to jugador.nombre,
                            "id" to jugador.id,
                            "numero" to jugador.numero
                        )
                    )
                }

                Toast.makeText(this, "Se ha añadido un nuevo partido", Toast.LENGTH_SHORT).show()


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
            }else{
                //nunca llega aqui porque el metodo haTerminado() manda el mensaje de error
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
    private fun addEventos(){
        for(gol in partidoActual.goles){
            sumarGoles(gol.jugadoresImplicados[0], gol)
        }
        for(tarjeta in partidoActual.amonestaciones){
            sumarTarjetas(tarjeta.jugadoresImplicados[0], tarjeta)
        }
        calcularMinutos(partidoActual.suplentes, partidoActual.titulares)

    }

    private fun sumarGoles(jugador: JugadorModel, gol: EventoModel){
        //sumar gol al jugador
        val yaHaMarcado = goleadores.firstOrNull{it.id == jugador.id}
        if(yaHaMarcado == null){
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            jugadoresRef.document(jugador.id).update(
                hashMapOf<String, Any>(
                    "golesMarcados" to jugador.golesMarcados + 1
                )
            )
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            gol.jugadoresImplicados[0].golesMarcados += 1
            goleadores.add(gol.jugadoresImplicados[0])
        }else{
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            jugadoresRef.document(jugador.id).update(
                hashMapOf<String, Any>(
                    "golesMarcados" to yaHaMarcado.golesMarcados + 1
                )
            )
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            goleadores.remove(yaHaMarcado)
            yaHaMarcado.golesMarcados += 1
            goleadores.add(yaHaMarcado)
        }



        Toast.makeText(this, "Se ha añadido el gol de ${gol.jugadoresImplicados[0].nombre}", Toast.LENGTH_SHORT).show()
    }
    private fun sumarTarjetas(jugador: JugadorModel, tarjeta: EventoModel){
        val tieneAmarilla = tienenAmarilla.firstOrNull { it.id == jugador.id }

        if(tarjeta.tipoEventoId == 1){      //tarjeta amarilla
            if(tieneAmarilla == null){
                val jugadoresRef =
                    db.collection("teams").document(currentTeam.id).collection("players")
                jugadoresRef.document(jugador.id).update(
                    hashMapOf<String, Any>(
                        "tarjetasAmarillas" to jugador.tarjetasAmarillas + 1
                    )
                )
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
                tarjeta.jugadoresImplicados[0].tarjetasAmarillas += 1
                tienenAmarilla.add(tarjeta.jugadoresImplicados[0])
            }else{
                val jugadoresRef =
                    db.collection("teams").document(currentTeam.id).collection("players")
                jugadoresRef.document(jugador.id).update(
                    hashMapOf<String, Any>(
                        "tarjetasAmarillas" to tieneAmarilla.tarjetasAmarillas + 1
                    )
                )
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
                tienenAmarilla.remove(tieneAmarilla)
                tieneAmarilla.tarjetasAmarillas += 1
                tienenAmarilla.add(tarjeta.jugadoresImplicados[0])
            }


        }else if(tarjeta.tipoEventoId == 2) {        //Tarjeta roja
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            jugadoresRef.document(jugador.id).update(
                hashMapOf<String, Any>(
                    "tarjetasRojas" to jugador.tarjetasRojas + 1
                )
            )
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            tarjeta.jugadoresImplicados[0].tarjetasRojas += 1
        }

        Toast.makeText(this, "Se ha añadido la tarjeta de ${tarjeta.jugadoresImplicados[0].nombre}", Toast.LENGTH_SHORT).show()
    }

    private fun calcularMinutos(suplentes: ArrayList<JugadorModel>, titulares: ArrayList<JugadorModel>){
        val jugadoresParticipantes: ArrayList<JugadorEnPartido> = ArrayList()
        val convocados: ArrayList<JugadorModel> = ArrayList()

        for (jugador in titulares){
            //Se le añade una titularidad a los 11 titulares
            convocados.add(jugador)

            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            jugadoresRef.document(jugador.id).update(

                hashMapOf<String, Any>(
                    "partidosConvocado" to jugador.partidosConvocado + 1,
                    "partidosTitular" to jugador.partidosTitular + 1
                )
            )
            .addOnFailureListener {
                Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            }

            if(haSidoCambiado(jugador)){
                for(cambio in partidoActual.sustituciones) {
                    //ver en que minuto sale
                    if(jugador.id == cambio.jugadoresImplicados[1].id){
                        val minutoSeVa = cambio.minuto
                        val jugadorCambiado = JugadorEnPartido(jugador,0, minutoSeVa)
                        jugadoresParticipantes.add(jugadorCambiado)
                    }
                }
            }else{
                val jugo90 = JugadorEnPartido(jugador,0, 90)
                jugadoresParticipantes.add(jugo90)
            }
            if(haSidoExpulsado(jugador)){
                for(expulsion in partidoActual.amonestaciones.filter { it.tipoEventoId == 2 }){ //eventoId 2  expulsion
                    if(expulsion.jugadoresImplicados[0].id == jugador.id){     //se busca cual es su expulsion
                        val jugadorExpulsado = JugadorEnPartido(jugador, 0, expulsion.minuto)
                        jugadoresParticipantes.add(jugadorExpulsado)
                    }
                }
            }
        }
        for (jugador in suplentes){
            convocados.add(jugador)

            //sumar una convocatoria
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")

            jugadoresRef.document(jugador.id).update(
                hashMapOf<String, Any>(
                    "partidosConvocado" to jugador.partidosConvocado + 1,
                )
            )
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }

            //suplente entra a jugar
            if(suplenteJuega(jugador)){
                //buscar minuto en el que entra
                val minutoEntra = minutoEntra(jugador)
                if(haSidoCambiado(jugador)){
                    //ver en que minuto sale
                    for(cambio in partidoActual.sustituciones) {
                        if(jugador.id == cambio.jugadoresImplicados[1].id){
                            val minutoSeVa = cambio.minuto
                            val jugadorCambiado = JugadorEnPartido(jugador,minutoEntra, minutoSeVa)
                            jugadoresParticipantes.add(jugadorCambiado)
                        }
                    }
                }
                else if(haSidoExpulsado(jugador)){
                    for(expulsion in partidoActual.amonestaciones.filter { it.tipoEventoId == 2 }){ //eventoId 2  expulsion
                        if(expulsion.jugadoresImplicados[0].id == jugador.id){     //se busca cual es su expulsion
                            val jugadorExpulsado = JugadorEnPartido(jugador, minutoEntra, expulsion.minuto)
                            jugadoresParticipantes.add(jugadorExpulsado)
                        }
                    }
                }
                else{
                    val jugadorCambiado = JugadorEnPartido(jugador,minutoEntra, 90)
                    jugadoresParticipantes.add(jugadorCambiado)
                }
            }
        }

        sumarMinutos(jugadoresParticipantes)
    }

    private fun sumarMinutos(participantes: ArrayList<JugadorEnPartido> ){
        for(jugadorMinutos in participantes){
            val jugador = jugadorMinutos.jugador
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            jugadoresRef.document(jugador.id).update(
                hashMapOf<String, Any>(
                    "minutosJugados" to jugador.minutosJugados + jugadorMinutos.minutosJugados(),
                    "partidosJugados" to jugador.partidosJugados + 1
                )
            )
            val partidoRef = db.collection("partidos").document(currentTeam.id).collection("liga").document(partidoActual.id)
            partidoRef.collection("minutosJugados").document(jugador.id).set(
                hashMapOf<String, Any>(
                    "jugador" to jugador.id,
                    "minutos" to jugadorMinutos.minutosJugados()
                )
            )

        }
    }


    private fun suplenteJuega(jugador: JugadorModel): Boolean{
        var respuesta = false
        for(cambio in partidoActual.sustituciones){
            if(jugador.id == cambio.jugadoresImplicados[0].id){
                respuesta = true
            }
        }
        return respuesta
    }
    private fun haSidoCambiado(jugador: JugadorModel): Boolean {
        var respuesta = false
        for(cambio in partidoActual.sustituciones) {
            if(jugador.id == cambio.jugadoresImplicados[1].id){
                respuesta = true
            }
        }
        return respuesta
    }
    private fun haSidoExpulsado(jugador: JugadorModel): Boolean {
        var respuesta = false
        val expulsiones: ArrayList<EventoModel> = ArrayList()
        for(tarjeta in partidoActual.amonestaciones){
            if(tarjeta.tipoEventoId == 2){
                expulsiones.add(tarjeta)
            }
        }
        for(expulsion in expulsiones) {
            if(jugador.id == expulsion.jugadoresImplicados[0].id){
                respuesta = true
            }
        }
        return respuesta
    }
    private fun minutoEntra(jugador: JugadorModel): Int{
        var minuto = 0
        for(cambio in partidoActual.sustituciones){
            if(jugador.id == cambio.jugadoresImplicados[0].id){
                minuto = cambio.minuto
            }
        }
        return minuto
    }


    private fun actualizarInfoEquipo(partido: PartidoModel){
        currentTeam.partidosJugados += 1

        if (partido.golesMarcados > partido.golesEncajados){
            currentTeam.victorias += 1
        }else if(partido.golesMarcados < partido.golesEncajados){
            currentTeam.derrotas += 1
        }
        if(partido.golesMarcados == partido.golesEncajados){
            currentTeam.empates += 1
        }

        currentTeam.golesMarcados += partido.golesMarcados
        currentTeam.golesEncajados += partido.golesEncajados

        db.collection("teams").document(currentTeam.id).update(
            hashMapOf<String, Any>(
                "partidosJugados" to currentTeam.partidosJugados,
                "victorias" to currentTeam.victorias,
                "derrotas" to currentTeam.derrotas,
                "empates" to currentTeam.empates,
                "golesMarcados" to currentTeam.golesMarcados,
                "golesEncajados" to currentTeam.golesEncajados
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
        jornadaActual += 1

        //actualizar numero jornada

        //comprobar que se han sumado las estadisticas al equipo y jugadores
    }

    //Confirma que se han seleccionado los jugadores titulares (codigo:111)
    //Confirma que se han seleccionado los jugadores suplentes (codigo:222)
    //Confirma que se han llegado los eventos (codigo:777)

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            partidoActual = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            jugadoresTitularesIds = data.getStringArrayListExtra("titularesId")!!
            binding.contadorTitulares.text = "("+jugadoresTitularesIds.size+")"
            for(jugador in partidoActual.titulares){
                partidoActual.convocados.add(jugador)
            }
            Toast.makeText(this, "Titulares: ${jugadoresTitularesIds.size}",Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            partidoActual = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            jugadoresSuplentesIds = data.getStringArrayListExtra("suplentesId")!!
            binding.contadorSuplentes.text = "("+jugadoresSuplentesIds.size+")"
            for(jugador in partidoActual.suplentes){
                partidoActual.convocados.add(jugador)
            }

            Toast.makeText(this, "Suplentes: ${jugadoresSuplentesIds.size}",Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            partidoActual = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            val sumatorioEventos = partidoActual.goles.size + partidoActual.sustituciones.size + partidoActual.amonestaciones.size
            binding.contadorEventostextView.text = "($sumatorioEventos)"
            binding.golesanotadostextView.text = partidoActual.goles.size.toString()

            Toast.makeText(this, "Hay $sumatorioEventos eventos",Toast.LENGTH_SHORT).show()
        }
    }
}