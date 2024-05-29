package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.myTeams.app.databinding.ActivityTeamBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TeamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeamBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentTeam: TeamModel = TeamModel()
    private var ultimoPartido: PartidoModel = PartidoModel()
    private var listaPartidos: ArrayList<PartidoModel> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        val teamId = intent.extras?.getString("idEquipo")

        if (teamId != null) {
            lifecycleScope.launch {
                currentTeam  = cargarEquipo(teamId)
                ultimoPartido = cargarPartido(teamId)
                listaPartidos.add(ultimoPartido)
            }
        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }

        binding.puntosButton.setOnClickListener {
            val popupMenu = PopupMenu(this,binding.puntosButton)
            popupMenu.inflate(R.menu.popup_3puntos_equipo)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.jugadoresPopupOption -> {
                        val teamActivityIntent = Intent(this, PlayersActivity::class.java)
                        if(currentTeam.id != ""){
                            teamActivityIntent.putExtra("equipoId", currentTeam.id)
                            teamActivityIntent.putExtra("equipo", currentTeam)
                            startActivity(teamActivityIntent)
                        }
                        true
                    }
                    R.id.partidosPopupOption -> {
                        val partidosActivityIntent = Intent(this, PartidosActivity::class.java)

                        if(currentTeam.id != ""){
                            partidosActivityIntent.putExtra("equipoId", currentTeam.id)
                            partidosActivityIntent.putExtra("equipo", currentTeam)
                            startActivity(partidosActivityIntent)
                        }
                        true
                    }

                    R.id.miCuentaPopupOption -> {
                        val miCuentaActivityIntent = Intent(this, MiCuentaActivity::class.java)
                        startActivity(miCuentaActivityIntent)

                        true

                    }

                    else -> {
                        false
                    }
                }
            }
        }

        binding.verTodostextView.setOnClickListener{
            val partidosActivityIntent = Intent(this, PartidosActivity::class.java)

            if(currentTeam.id != ""){
                partidosActivityIntent.putExtra("equipoId", currentTeam.id)
                partidosActivityIntent.putExtra("equipo", currentTeam)
                startActivity(partidosActivityIntent)
            }

        }


        binding.partidoRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }


    private fun setPartidoAdapter(listado: ArrayList<PartidoModel>) {
        binding.partidoRecyclerView.adapter = PartidoAdapter(
            listado, this, db, currentTeam
        )
    }

    private suspend fun cargarEquipo(teamId: String?): TeamModel {
        return suspendCoroutine { continuation ->
            buscarEquipo(teamId) { equipo ->
                continuation.resume(equipo)
            }
        }
    }

    private fun buscarEquipo(teamId: String?, callback: (TeamModel) -> Unit) {
        val teamRef = db.collection("teams").document(teamId!!)
        teamRef.get()
            .addOnSuccessListener { document->
                currentTeam = document.toObject<TeamModel>()!!
                currentTeam.id= document.id

                binding.myTeamText3.text = currentTeam.nombre
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting team: ", exception)
                callback(TeamModel()) // En caso de error, devolver una lista vacía
            }
        callback(currentTeam)
        Toast.makeText(this,"Ya hemos econtrado el equipo ${currentTeam.nombre}",Toast.LENGTH_SHORT).show()
    }


    private suspend fun cargarPartido(teamId: String?): PartidoModel{
        return suspendCoroutine { continuation ->
            buscarPartido(teamId) { equipo ->
                continuation.resume(equipo)
            }
        }
    }

    private fun buscarPartido(equipoId: String?, callback: (PartidoModel) -> Unit) {
        val ligaRef = db.collection("partidos").document(equipoId!!).collection("liga")
        var partido = PartidoModel()

        ligaRef
            .orderBy("numeroJornada", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents->
                partido = documents.documents[0].toObject<PartidoModel>()!!
                partido.id= documents.documents[0].id

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
                        callback(PartidoModel()) // En caso de error, devolver una lista vacía
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
                        callback(PartidoModel()) // En caso de error, devolver una lista vacía
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
                        callback(PartidoModel()) // En caso de error, devolver una lista vacía
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
                        callback(PartidoModel()) // En caso de error, devolver una lista vacía
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
                        callback(PartidoModel()) // En caso de error, devolver una lista vacía
                    }
                binding.loadingGif.visibility = View.INVISIBLE
                callback(partido)
            }

    }


    private fun actualizar() {
        lifecycleScope.launch {
            val partido = cargarPartido(intent.extras?.getString("idEquipo"))
            listaPartidos = ArrayList()
            listaPartidos.add(partido)
            setPartidoAdapter(listaPartidos)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }
}