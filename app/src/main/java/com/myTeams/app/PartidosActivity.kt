package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.myTeams.app.adapter.PartidoAdapter
import com.myTeams.app.databinding.ActivityPartidosBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PartidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartidosBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    private var numeroPartidos: Int=0
    var listado: ArrayList<PartidoModel> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPartidosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!

        setup()

    }

    private fun setup(){

        binding.addbutton.setOnClickListener {
            val addPartidoActivityIntent = Intent(this, AddPartidoActivity::class.java)

            if(currentTeam.id != ""){
                addPartidoActivityIntent.putExtra("equipoId", currentTeam.id)
                addPartidoActivityIntent.putExtra("equipo", currentTeam)
                addPartidoActivityIntent.putExtra("jornadasJugadas", numeroPartidos)
                startActivity(addPartidoActivityIntent)
            }
        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }

        binding.partidosRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }

    private fun setAdapter(listado: ArrayList<PartidoModel>) {
        binding.partidosRecyclerView.adapter = PartidoAdapter(
            listado, this, db, currentTeam
        )
        numeroPartidos = listado.size
    }

    private suspend fun cargarPartidos(teamId: String?): ArrayList<PartidoModel> {
        return suspendCoroutine { continuation ->
            buscarPartidos(teamId) { partidos ->
                continuation.resume(partidos)
            }

        }
    }
    private fun buscarPartidos(equipoId: String?, callback: (ArrayList<PartidoModel>) -> Unit) {
        //val teamDB
        //val listado = ArrayList<PartidoModel>()

        if (equipoId != null) {
            //busca todos los partidos del equipo
            db.collection("partidos").document(equipoId)
                .collection("liga")
                .orderBy("numeroJornada", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->        //carga info partido
                    for (partido in documents) {
                        val partidoDB = partido.toObject<PartidoModel>()
                        partidoDB.id = partido.id
                        listado.add(partidoDB)

                    }

                    for(partido in listado) {
                        partido.titulares = ArrayList()
                        partido.suplentes = ArrayList()
                        partido.goles = ArrayList()
                        partido.amonestaciones = ArrayList()
                        partido.sustituciones = ArrayList()

                        val partidoRef =
                            db.collection("partidos").document(equipoId).collection("liga")
                                .document(partido.id)

                        //busca titulares
                        partidoRef.collection("titulares").orderBy("numero")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (jugador in documents) {
                                    val playerDB = jugador.toObject<JugadorModel>()
                                    playerDB.id = jugador.getString("id")!!
                                    partido.titulares.add(playerDB)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }

                        //busca suplentes
                        partidoRef.collection("suplentes").orderBy("numero")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (jugador in documents) {
                                    val playerDB = jugador.toObject<JugadorModel>()
                                    playerDB.id = jugador.getString("id")!!
                                    partido.suplentes.add(playerDB)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }


                        //cargar goles
                        partidoRef.collection("goles")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (gol in documents) {
                                    //Toast.makeText(this, "Encontrado", Toast.LENGTH_SHORT).show()
                                    val goleador: ArrayList<JugadorModel> = ArrayList()
                                    goleador.add(
                                        JugadorModel(
                                            nombre = gol.get("goleadorNombre").toString(),
                                            numero = gol.get("numero").toString().toInt(),
                                            id = gol.get("goleadorId").toString()
                                        )
                                    )
                                    val golDb = EventoModel(
                                        tipoEventoId = 0,
                                        minuto = gol.get("minuto").toString().toInt(),
                                        jugadoresImplicados = goleador
                                    )
                                    golDb.id = gol.id
                                    partido.goles.add(golDb)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }

                        //cargar tarjetas
                        partidoRef.collection("amonestaciones")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (tarjeta in documents) {
                                    val tipoEventoId: Int =
                                        tarjeta.get("tipoTarjetaId").toString().toInt()
                                    val amonestado: ArrayList<JugadorModel> = ArrayList()
                                    amonestado.add(
                                        JugadorModel(
                                            nombre = tarjeta.get("amonestadoNombre").toString(),
                                            numero = tarjeta.get("amonestadoNumero").toString()
                                                .toInt(),
                                            id = tarjeta.get("amonestadoId").toString()
                                        )
                                    )
                                    val tarjetaDB = EventoModel(
                                        tipoEventoId = tipoEventoId,
                                        minuto = tarjeta.get("minuto").toString().toInt(),
                                        jugadoresImplicados = amonestado
                                    )

                                    partido.amonestaciones.add(tarjetaDB)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }

                        //cargar cambios
                        partidoRef.collection("sustituciones")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (cambio in documents) {
                                    val jugadores: ArrayList<JugadorModel> = ArrayList()
                                    jugadores.add(
                                        JugadorModel(
                                            nombre = cambio.get("entraNombre").toString(),
                                            numero = cambio.get("entraNumero").toString().toInt(),
                                            id = cambio.get("entraId").toString()
                                        )
                                    )
                                    jugadores.add(
                                        JugadorModel(
                                            nombre = cambio.get("saleNombre").toString(),
                                            numero = cambio.get("saleNumero").toString().toInt(),
                                            id = cambio.get("saleId").toString()
                                        )
                                    )

                                    val sustitucion = EventoModel(
                                        tipoEventoId = 3,
                                        minuto = cambio.get("minuto").toString().toInt(),
                                        jugadoresImplicados = jugadores
                                    )

                                    partido.sustituciones.add(sustitucion)
                                    //listadoEventos.add(sustitucion)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }
                    }


                    binding.loadingGif3.visibility = View.INVISIBLE
                    callback(listado)
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                    callback(ArrayList()) // En caso de error, devolver una lista vacía
                }


/*
                //aprovecha y carga jugadores y sus datos
                db.collection("teams").document(currentTeam.id).collection("players").get()
                    .addOnSuccessListener { documents ->
                        for (jugador in documents) {
                            val playerDB = jugador.toObject<JugadorModel>()
                            playerDB.id = jugador.id
                            currentTeam.jugadores.add(playerDB)

                        }
                        callback(listado)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                        callback(ArrayList()) // En caso de error, devolver una lista vacía
                    }

 */

            //ya ha guardado todos los datos de los partidos y los devuelve actualizados

            //callback(listado)
        } else {
            binding.loadingGif3.visibility = View.INVISIBLE
            callback(ArrayList())
        }
    }



    private fun actualizar() {
        lifecycleScope.launch {
            listado = ArrayList()
            listado = cargarPartidos(currentTeam.id)
            //una vez tiene la info de los partidos, rellena los datos (titulares,suplentes y eventos)

            setAdapter(listado)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }

}


/*
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



 */