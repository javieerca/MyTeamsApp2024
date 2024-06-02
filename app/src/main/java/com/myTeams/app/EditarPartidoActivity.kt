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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.databinding.ActivityEditarPartidoBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorEnPartido
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel
import com.myTeams.app.model.minutosJugados
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EditarPartidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPartidoBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    private var jornadaActual: Int = 0
    private var jugadoresTitularesIds: ArrayList<String> = ArrayList()
    private var jugadoresSuplentesIds: ArrayList<String> = ArrayList()
    private var jugadoresParticipantes: ArrayList<JugadorEnPartido> = ArrayList()
    private val convocados: ArrayList<JugadorModel> = ArrayList()


    private var goleadores : ArrayList<JugadorModel> = ArrayList()
    private var tienenAmarilla: ArrayList<JugadorModel> = ArrayList()

    private var partidoDb: PartidoModel = PartidoModel()
    private var partidoActualizado: PartidoModel = PartidoModel()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditarPartidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        //partidoDb.equipoId = currentTeam.id
        partidoDb = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!
        partidoActualizado = partidoDb
        jornadaActual = intent.extras?.getInt("jornadaActual")!!


        setup()

    }

    private fun setup(){
        binding.jornadaNumberEdit.setText(jornadaActual.toString(), TextView.BufferType.EDITABLE)

        binding.cancelButton1.setOnClickListener {
            finish()
        }

        insertarDatos()

        binding.titularesButton.setOnClickListener {
            //comprobar si hay seleccionados y seleccionarlos otra vez

            val addTitularesActivityIntent = Intent(this, AddTitularesActivity::class.java)
            addTitularesActivityIntent.putExtra("equipo", currentTeam)
            addTitularesActivityIntent.putExtra("partido", partidoActualizado)
            startActivityForResult(addTitularesActivityIntent, 111)

        }

        binding.Suplentesbutton.setOnClickListener {
            if(binding.contadorTitulares.text != "(11)"){
                Toast.makeText(this,"Primero debes seleccionar 11 jugadores titulares.", Toast.LENGTH_SHORT).show()
            }else{
                val addSuplentesActivityIntent = Intent(this, AddSuplentesActivity::class.java)
                addSuplentesActivityIntent.putExtra("equipo", currentTeam)
                addSuplentesActivityIntent.putExtra("partido", partidoActualizado)
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
                addEventoActivityIntent.putExtra("partido", partidoActualizado)
                addEventoActivityIntent.putStringArrayListExtra("titulares", jugadoresTitularesIds)
                addEventoActivityIntent.putStringArrayListExtra("suplentes", jugadoresSuplentesIds)
                startActivityForResult(addEventoActivityIntent, 777)

            }
        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }

        binding.guardarButton.text = "Guardar"
        binding.guardarButton.setOnClickListener {
            if(haTerminado()){
                var borrado = false
                lifecycleScope.launch {
                    borrado = borrarPartido()
                    if(borrado){
                        lifecycleScope.launch {
                            var partidoSubido = solicitudSubirPartido()
                            if(partidoSubido){
                                addEventos()
                            }
                        }

                    }
                }
            }else{
                //nunca llega aqui porque el metodo haTerminado() manda el mensaje de error
            }
        }
    }
    private suspend fun solicitudSubirPartido():  Boolean{
        return suspendCoroutine { continuation ->
            subirPartido { terminado->
                continuation.resume(terminado)
            }
        }
    }

    private suspend fun borrarPartido(): Boolean {
        return suspendCoroutine { continuation ->
            borradoPartido { terminado ->
                continuation.resume(terminado)
            }
        }
    }

    private fun borradoPartido(callback: (Boolean) -> Unit) {
        val jugadoresRef =
            db.collection("teams").document(partidoDb.equipoId).collection("players")

        //buscar equipoDB
        var equipoDb = TeamModel()
        db.collection("teams").document(partidoDb.equipoId)
            .get()
            .addOnSuccessListener {document ->
                equipoDb = document.toObject<TeamModel>()!!
                equipoDb.id = document.id

                currentTeam = equipoDb

                db.collection("teams").document(partidoDb.equipoId).update(
                    hashMapOf<String, Any>(
                        "golesMarcados" to equipoDb.golesMarcados - partidoDb.golesMarcados,
                        "golesEncajados" to equipoDb.golesEncajados - partidoDb.golesEncajados,
                        "partidosJugados" to equipoDb.partidosJugados - 1
                    )
                )
                    .addOnSuccessListener {
                        //restar victoria, derrota o empate
                        if(partidoDb.golesMarcados > partidoDb.golesEncajados){
                            db.collection("teams").document(partidoDb.equipoId).update(
                                hashMapOf<String, Any>(
                                    "victorias" to equipoDb.victorias - 1
                                )
                            )
                        }else if(partidoDb.golesMarcados < partidoDb.golesEncajados){
                            db.collection("teams").document(partidoDb.equipoId).update(
                                hashMapOf<String, Any>(
                                    "derrotas" to equipoDb.derrotas - 1
                                )
                            )
                        }else{
                            db.collection("teams").document(partidoDb.equipoId).update(
                                hashMapOf<String, Any>(
                                    "empates" to equipoDb.empates - 1
                                )
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error" , Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error" , Toast.LENGTH_SHORT).show()
            }

        //borrar goles a jugador
        for(gol in partidoDb.goles){
            //buscar jugador
            var goleador= JugadorModel()
            jugadoresRef.document(gol.jugadoresImplicados[0].id)
                .get()
                .addOnSuccessListener { document ->
                    if(document != null){
                        goleador = document.toObject(JugadorModel::class.java)!!
                        goleador.id = document.id

                        //quitar gol al goleador
                        jugadoresRef.document(goleador.id).update(
                            hashMapOf<String,Any>(
                                "golesMarcados" to goleador.golesMarcados - 1
                            )
                        )
                    }
                }
        }


        //quitar veces cambiado a jugador
        for(cambio in partidoDb.sustituciones){
            //buscar jugador
            var cambiado= JugadorModel()
            jugadoresRef.document(cambio.jugadoresImplicados[1].id)
                .get()
                .addOnSuccessListener { document ->
                    cambiado = document.toObject<JugadorModel>()!!
                    cambiado.id = document.id

                    //quitar cambio al cambiado
                    jugadoresRef.document(cambiado.id).update(
                        hashMapOf<String,Any>(
                            "vecesCambiado" to cambiado.vecesCambiado - 1
                        )
                    )
                }

        }

        //quitar amonestaciones
        for(tarjeta in partidoDb.amonestaciones) {
            //buscar jugador
            var amonestado: JugadorModel
            jugadoresRef.document(tarjeta.jugadoresImplicados[0].id)
                .get()
                .addOnSuccessListener { document ->
                    amonestado = document.toObject<JugadorModel>()!!
                    amonestado.id = document.id

                    if (tarjeta.tipoEventoId == 1) { //tarjeta amarilla
                        //quitar amarilla al jugador
                        jugadoresRef.document(amonestado.id).update(
                            hashMapOf<String, Any>(
                                "tarjetasAmarillas" to amonestado.tarjetasAmarillas - 1
                            )
                        )

                    } else {
                        //quitar roja al jugador
                        jugadoresRef.document(amonestado.id).update(
                            hashMapOf<String, Any>(
                                "tarjetasRojas" to amonestado.tarjetasRojas - 1
                            )
                        )
                    }
                }
        }

        var jugadoresMinutosJugados: ArrayList<JugadorEnPartido> = ArrayList()
        //quitar minutos
        //primero busca minutos jugados
        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("minutosJugados")
            .get()
            .addOnSuccessListener { documents->
                for (documento in documents){
                    val jugadorMinutos = JugadorEnPartido(jugador= JugadorModel(id= documento.getString("jugador")!!),
                        jugoMinutos = documento.get("minutos")!!.toString().toInt())
                    jugadoresMinutosJugados.add(jugadorMinutos)
                    documento.reference.delete()
                }

                //restar minutos
                for(jugadorMinutos in jugadoresMinutosJugados){
                    //busca jugador
                    var jugador= JugadorModel()
                    jugadoresRef.document(jugadorMinutos.jugador.id)
                        .get()
                        .addOnSuccessListener { document ->
                            jugador = document.toObject<JugadorModel>()!!
                            jugador.id = document.id

                            //resta minutos
                            jugadoresRef.document(jugador.id).update(
                                hashMapOf<String, Any>(
                                    "minutosJugados" to jugador.minutosJugados - jugadorMinutos.jugoMinutos,
                                    "partidosJugados" to jugador.partidosJugados - 1
                                )
                            )
                        }
                }
                //titularidades
            }


        var titularesId: ArrayList<String> = ArrayList()
        //borrar titularidades
        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("titulares")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    val titularId = documento.getString("id")!!
                    titularesId.add(titularId)
                }

                for (id in titularesId){
                    var jugador = JugadorModel()
                    jugadoresRef.document(id)
                        .get()
                        .addOnSuccessListener { document ->
                            jugador = document.toObject<JugadorModel>()!!
                            jugador.id = document.id

                            //resta convocatorias
                            jugadoresRef.document(jugador.id).update(
                                hashMapOf<String, Any>(
                                    "partidosTitular" to jugador.partidosTitular - 1,
                                    "partidosConvocado" to jugador.partidosConvocado - 1
                                )
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                        }
                }

            }

        var suplentesId: ArrayList<String> = ArrayList()
        //suplentes
        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("suplentes")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    val suplenteId = documento.getString("id")!!
                    suplentesId.add(suplenteId)
                    documento.reference.delete()
                }

                for (id in suplentesId){
                    var jugador = JugadorModel()
                    jugadoresRef.document(id)
                        .get()
                        .addOnSuccessListener { document ->
                            jugador = document.toObject<JugadorModel>()!!
                            jugador.id = document.id
                            partidoDb.suplentes.add(jugador)

                            //resta convocatorias
                            jugadoresRef.document(jugador.id).update(
                                hashMapOf<String, Any>(
                                    "partidosConvocado" to jugador.partidosConvocado - 1
                                )
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                }

            }
            .addOnFailureListener {
                Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
            }

        //borar subcolecciones

        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("goles")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    documento.reference.delete()
                }
            }

        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("sustituciones")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    documento.reference.delete()
                }
            }

        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("amonestaciones")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    documento.reference.delete()
                }
            }

        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("titulares")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    documento.reference.delete()
                }
            }
        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .collection("suplentes")
            .get()
            .addOnSuccessListener { documents ->
                for (documento in documents) {
                    documento.reference.delete()
                }
            }

        db.collection("partidos").document(partidoDb.equipoId).collection("liga").document(partidoDb.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Vamos a borrar a la jornada ${partidoDb.numeroJornada}", Toast.LENGTH_SHORT).show()
                db.collection("partidos").document(partidoDb.equipoId)
                    .collection("liga").document(partidoDb.id).delete()

                callback(true)
            }

    }

    private fun subirPartido(callback: (Boolean) -> Unit){
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
                goles = partidoActualizado.goles,
                golesMarcados = partidoActualizado.goles.size
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
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Id partido: ${partidoActualizado.id}", Toast.LENGTH_SHORT).show()
                partidoActualizado.id = nuevoDocumento.id

                actualizarInfoEquipo(datosPartido)

                //subir goles al partido
                for(gol in partidoActualizado.goles){
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
                for (tarjeta in partidoActualizado.amonestaciones){
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

                for (cambio in partidoActualizado.sustituciones){
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
                for(jugador in partidoActualizado.titulares){
                    nuevoDocumento.collection("titulares").document().set(
                        hashMapOf<String, Any>(
                            "nombre" to jugador.nombre,
                            "id" to jugador.id,
                            "numero" to jugador.numero
                        )
                    )
                }
                //subir suplentes
                for(jugador in partidoActualizado.suplentes){
                    nuevoDocumento.collection("suplentes").document().set(
                        hashMapOf<String, Any>(
                            "nombre" to jugador.nombre,
                            "id" to jugador.id,
                            "numero" to jugador.numero
                        )
                    )
                }

                callback(true)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Algo ha ido mal al crear el partido", Toast.LENGTH_SHORT).show()
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
        for(gol in partidoActualizado.goles){
            sumarGoles(gol.jugadoresImplicados[0], gol)
        }
        for(tarjeta in partidoActualizado.amonestaciones){
            sumarTarjetas(tarjeta.jugadoresImplicados[0], tarjeta)
        }

        lifecycleScope.launch {
            var calculadoTitulares = solicitudCalcularMinutosTitulares()
            var calculadoSuplentes = false
            if(calculadoTitulares){
                lifecycleScope.launch {
                    calculadoSuplentes  = solicitudCalcularMinutosSuplentes()
                    println("Terminado suplentes")

                    if(calculadoSuplentes){
                        sumarMinutos(jugadoresParticipantes)
                    }

                }
                mostrarGuardando()
            }


        }

    }
    private fun mostrarGuardando(){
        val builder = AlertDialog.Builder(this@EditarPartidoActivity)
        builder.setMessage("Se están guardando sus cambios.")
        builder.setTitle("Importante")
        builder.setCancelable(false)

        builder.setPositiveButton("OK") { _, _ ->
            finish()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private suspend fun solicitudCalcularMinutosTitulares():  Boolean{
        return suspendCoroutine { continuation ->
            calcularMinutosTitulares { terminado->
                continuation.resume(terminado)
            }
        }
    }
    private suspend fun solicitudCalcularMinutosSuplentes():  Boolean{
        return suspendCoroutine { continuation ->
            calcularMinutosSuplentes { terminado->
                continuation.resume(terminado)
            }
        }
    }
    private fun calcularMinutosSuplentes(callback: (Boolean) -> Unit){
        var suplentes: ArrayList<JugadorModel> = partidoActualizado.suplentes
        var realizados = 0
        for (jugador in suplentes){
            convocados.add(jugador)

            //sumar una convocatoria
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")

            //buscar jugador actualizado en la bd
            var jugadorBd = JugadorModel()
            jugadoresRef.document(jugador.id)
                .get()
                .addOnSuccessListener { document ->
                    jugadorBd = document.toObject<JugadorModel>()!!
                    jugadorBd.id = document.id
                    jugadoresRef.document(jugadorBd.id).update(
                        hashMapOf<String, Any>(
                            "partidosConvocado" to jugadorBd.partidosConvocado + 1,
                        )
                    )
                        .addOnFailureListener {
                            Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                        }


                    //suplente entra a jugar
                    if(suplenteJuega(jugadorBd)){
                        //buscar minuto en el que entra
                        val minutoEntra = minutoEntra(jugadorBd)
                        if(haSidoCambiado(jugadorBd)){
                            //ver en que minuto sale
                            for(cambio in partidoActualizado.sustituciones) {
                                if(jugadorBd.id == cambio.jugadoresImplicados[1].id){
                                    val minutoSeVa = cambio.minuto
                                    val jugadorCambiado = JugadorEnPartido(jugador = jugadorBd,minutoEntra, minutoSeVa)
                                    jugadoresParticipantes.add(jugadorCambiado)
                                    realizados += 1
                                    if(realizados == partidoActualizado.sustituciones.size){
                                        callback(true)
                                    }
                                }

                            }
                        }
                        else if(haSidoExpulsado(jugadorBd)){
                            for(expulsion in partidoActualizado.amonestaciones.filter { it.tipoEventoId == 2 }){ //eventoId 2  expulsion
                                if(expulsion.jugadoresImplicados[0].id == jugador.id){     //se busca cual es su expulsion
                                    val jugadorExpulsado = JugadorEnPartido(jugador = jugadorBd, minutoEntra, expulsion.minuto)
                                    jugadoresParticipantes.add(jugadorExpulsado)
                                    realizados += 1
                                    if(realizados == partidoActualizado.sustituciones.size){
                                        callback(true)
                                    }
                                }
                            }
                        }
                        else{
                            val jugadorCambiado = JugadorEnPartido(jugadorBd,minutoEntra, 90)
                            jugadoresParticipantes.add(jugadorCambiado)
                            realizados += 1
                            if(realizados == partidoActualizado.sustituciones.size){
                                callback(true)
                            }
                        }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                }

        }
        if(realizados == partidoActualizado.sustituciones.size){
            callback(true)
        }
    }


    private fun calcularMinutosTitulares(callback: (Boolean) -> Unit){
        var titulares: ArrayList<JugadorModel> = partidoActualizado.titulares

        for (jugador in titulares){
            //Se le añade una titularidad a los 11 titulares
            convocados.add(jugador)

            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")

            //buscar jugador actualizado en la bd
            var jugadorBd = JugadorModel()
            jugadoresRef.document(jugador.id)
                .get()
                .addOnSuccessListener { document ->
                    jugadorBd = document.toObject<JugadorModel>()!!

                    jugadoresRef.document(jugador.id).update(
                        hashMapOf<String, Any>(
                            "partidosConvocado" to jugadorBd.partidosConvocado + 1,
                            "partidosTitular" to jugadorBd.partidosTitular + 1
                        )
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }

            if(haSidoCambiado(jugador)){
                for(cambio in partidoActualizado.sustituciones) {
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
                for(expulsion in partidoActualizado.amonestaciones.filter { it.tipoEventoId == 2 }){ //eventoId 2  expulsion
                    if(expulsion.jugadoresImplicados[0].id == jugador.id){     //se busca cual es su expulsion
                        val jugadorExpulsado = JugadorEnPartido(jugador, 0, expulsion.minuto)
                        jugadoresParticipantes.add(jugadorExpulsado)
                    }
                }
            }
        }
            ////////////////////////////////////////
            //sumarMinutos(jugadoresParticipantes)
            callback(true)

    }


    private fun sumarMinutos(participantes: ArrayList<JugadorEnPartido> ){
        for(jugadorMinutos in participantes){
            val jugador = jugadorMinutos.jugador
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            //buscar jugador actualizado en la bd
            var jugadorBd = JugadorModel()
            jugadoresRef.document(jugador.id)
                .get()
                .addOnSuccessListener { document ->
                    jugadorBd = document.toObject<JugadorModel>()!!
                    jugadoresRef.document(jugador.id).update(
                        hashMapOf<String, Any>(
                            "minutosJugados" to jugadorBd.minutosJugados + jugadorMinutos.minutosJugados(),
                            "partidosJugados" to jugadorBd.partidosJugados + 1
                        )
                    ).addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
                }


            val partidoRef = db.collection("partidos").document(currentTeam.id).collection("liga").document(partidoActualizado.id)
            partidoRef.collection("minutosJugados").document(jugador.id).set(
                hashMapOf<String, Any>(
                    "jugador" to jugador.id,
                    "minutos" to jugadorMinutos.minutosJugados()
                )
            )

        }
    }

    private fun sumarGoles(jugador: JugadorModel, gol: EventoModel){
        //sumar gol al jugador
        val jugadoresRef =
            db.collection("teams").document(currentTeam.id).collection("players")

        //buscar jugador actualizado en la bd
        var jugadorBd = JugadorModel()
        jugadoresRef.document(jugador.id)
            .get()
            .addOnSuccessListener { document ->
                jugadorBd = document.toObject<JugadorModel>()!!

                jugadoresRef.document(jugador.id).update(
                    hashMapOf<String, Any>(
                        "golesMarcados" to jugadorBd.golesMarcados + 1
                    )
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            }
        Toast.makeText(this, "Se ha añadido el gol de ${gol.jugadoresImplicados[0].nombre}", Toast.LENGTH_SHORT).show()

    }

    private fun sumarTarjetas(jugador: JugadorModel, tarjeta: EventoModel){

        if(tarjeta.tipoEventoId == 1){      //tarjeta amarilla
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            //buscar jugador actualizado en la bd
            var jugadorBd = JugadorModel()
            jugadoresRef.document(jugador.id)
                .get()
                .addOnSuccessListener { document ->
                    jugadorBd = document.toObject<JugadorModel>()!!
                    jugadoresRef.document(jugador.id).update(
                        hashMapOf<String, Any>(
                            "tarjetasAmarillas" to jugadorBd.tarjetasAmarillas + 1
                        )
                    )
                }

                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }else if(tarjeta.tipoEventoId == 2) {        //Tarjeta roja
            val jugadoresRef =
                db.collection("teams").document(currentTeam.id).collection("players")
            //buscar jugador actualizado en la bd
            var jugadorBd = JugadorModel()
            jugadoresRef.document(jugador.id)
                .get()
                .addOnSuccessListener { document ->
                    jugadorBd = document.toObject<JugadorModel>()!!
                    jugadoresRef.document(jugador.id).update(
                        hashMapOf<String, Any>(
                            "tarjetasRojas" to jugadorBd.tarjetasRojas + 1
                        )
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            tarjeta.jugadoresImplicados[0].tarjetasRojas += 1
        }

        Toast.makeText(this, "Se ha añadido la tarjeta de ${tarjeta.jugadoresImplicados[0].nombre}", Toast.LENGTH_SHORT).show()
    }

    private fun suplenteJuega(jugador: JugadorModel): Boolean{
        var respuesta = false
        for(cambio in partidoActualizado.sustituciones){
            if(jugador.id == cambio.jugadoresImplicados[0].id){
                respuesta = true
            }
        }
        return respuesta
    }
    private fun haSidoCambiado(jugador: JugadorModel): Boolean {
        var respuesta = false
        for(cambio in partidoActualizado.sustituciones) {
            if(jugador.id == cambio.jugadoresImplicados[1].id){
                respuesta = true
            }
        }
        return respuesta
    }
    private fun haSidoExpulsado(jugador: JugadorModel): Boolean {
        var respuesta = false
        val expulsiones: ArrayList<EventoModel> = ArrayList()
        for(tarjeta in partidoActualizado.amonestaciones){
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
        for(cambio in partidoActualizado.sustituciones){
            if(jugador.id == cambio.jugadoresImplicados[0].id){
                minuto = cambio.minuto
            }
        }
        return minuto
    }

    private fun actualizarInfoEquipo(partido: PartidoModel){
        db.collection("teams").document(currentTeam.id)
            .get()
            .addOnSuccessListener {document ->
                currentTeam = document.toObject<TeamModel>()!!
                currentTeam.id = document.id

                currentTeam.partidosJugados += 1

                if (partido.golesMarcados > partido.golesEncajados){
                    currentTeam.victorias += 1
                }
                if(partido.golesMarcados < partido.golesEncajados){
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

    }

    private fun insertarDatos() {

        binding.golesanotadostextView.text = partidoActualizado.golesMarcados.toString()
        binding.jornadaNumberEdit.setText(partidoActualizado.numeroJornada.toString())
        binding.rivalEditText.setText(partidoActualizado.rival)
        binding.encajadosTextNumber.setText(partidoActualizado.golesEncajados.toString())
        if (!partidoActualizado.local) {
            binding.estadioSpinner.setSelection(1)
        }
        binding.contadorTitulares.text = "(${partidoActualizado.titulares.size})"
        binding.contadorSuplentes.text = "(${partidoActualizado.suplentes.size})"
        binding.contadorEventostextView.text =
            "(${partidoActualizado.goles.size + partidoActualizado.sustituciones.size + partidoActualizado.amonestaciones.size})"

        binding.observacionesMultiline.setText(partidoActualizado.observaciones)

        for (jugador in partidoActualizado.titulares) {
            partidoActualizado.convocados.add(jugador)
            jugadoresTitularesIds.add(jugador.id)
        }

        for (jugador in partidoActualizado.suplentes) {
            partidoActualizado.convocados.add(jugador)
            jugadoresSuplentesIds.add(jugador.id)
        }
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
            partidoActualizado = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            jugadoresTitularesIds = ArrayList()
            jugadoresTitularesIds = data.getStringArrayListExtra("titularesId")!!
            binding.contadorTitulares.text = "("+jugadoresTitularesIds.size+")"
            for(jugador in partidoActualizado.titulares){
                partidoActualizado.convocados.add(jugador)
            }
            Toast.makeText(this, "Titulares: ${jugadoresTitularesIds.size}", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 222 && resultCode == Activity.RESULT_OK) {
            partidoActualizado = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            jugadoresSuplentesIds = ArrayList()
            jugadoresSuplentesIds = data.getStringArrayListExtra("suplentesId")!!
            binding.contadorSuplentes.text = "("+jugadoresSuplentesIds.size+")"
            for(jugador in partidoActualizado.suplentes){
                val suplente = partidoActualizado.convocados.find { it.id == jugador.id }
                if (suplente == null) {
                    partidoActualizado.convocados.add(jugador)
                }else{
                    println("Ya estaba convocado")
                }
            }

            Toast.makeText(this, "Suplentes: ${jugadoresSuplentesIds.size}", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            partidoActualizado = data?.getSerializableExtra("partido", PartidoModel::class.java)!!

            val sumatorioEventos = partidoActualizado.goles.size + partidoActualizado.sustituciones.size + partidoActualizado.amonestaciones.size
            binding.contadorEventostextView.text = "($sumatorioEventos)"
            binding.golesanotadostextView.text = partidoActualizado.goles.size.toString()

            Toast.makeText(this, "Hay $sumatorioEventos eventos", Toast.LENGTH_SHORT).show()
        }
    }
}