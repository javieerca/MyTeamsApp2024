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
import com.myTeams.app.adapter.JugadoresGolesAdapter
import com.myTeams.app.adapter.JugadoresMinutosAdapter
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

        if(teamId != null){
            lifecycleScope.launch {
                currentTeam  = cargarEquipo(teamId)
                ultimoPartido = cargarPartido(intent.extras?.getString("idEquipo"))
                listaPartidos = ArrayList()
                if(ultimoPartido.id != ""){
                    listaPartidos.add(ultimoPartido)
                    binding.sinPartidostextView.visibility = View.INVISIBLE

                }else{
                    binding.loadingGif.visibility = View.INVISIBLE
                    binding.sinPartidostextView.visibility = View.VISIBLE
                }
                setPartidoAdapter(listaPartidos)
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
                    R.id.equipoPopupOption ->{
                        val miEquipoActivityIntent = Intent(this, MostrarDatosEquipoActivity::class.java)
                        miEquipoActivityIntent.putExtra("equipo", currentTeam)
                        startActivity(miEquipoActivityIntent)

                        true
                    }

                    R.id.miCuentaPopupOption -> {
                        val miCuentaActivityIntent = Intent(this, MiCuentaActivity::class.java)
                        miCuentaActivityIntent.putExtra("email", currentTeam.usuario)

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

        binding.verJugadorestextView.setOnClickListener {
            val teamActivityIntent = Intent(this, PlayersActivity::class.java)
            if(currentTeam.id != ""){
                teamActivityIntent.putExtra("equipoId", currentTeam.id)
                teamActivityIntent.putExtra("equipo", currentTeam)
                startActivity(teamActivityIntent)
            }
        }

        binding.partidoRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
        binding.jugadoresMinutosrecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
        binding.jugadoresGolesrecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }


    private fun setPartidoAdapter(listado: ArrayList<PartidoModel>) {
        binding.partidoRecyclerView.adapter = PartidoAdapter(
            listado, this, db, currentTeam
        )
    }

    private fun setJugadoresMasMinutosAdapter(listado: ArrayList<JugadorModel>) {
        binding.jugadoresMinutosrecyclerView.adapter = JugadoresMinutosAdapter(
            listado, this
        )
    }

    private fun setJugadoresGoleadores(listado: ArrayList<JugadorModel>) {
        binding.jugadoresGolesrecyclerView.adapter = JugadoresGolesAdapter(
            listado, this
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

                binding.nombreEquipotextView.text = currentTeam.nombre
                callback(currentTeam)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting team: ", exception)
                callback(TeamModel()) // En caso de error, devolver una lista vacía
            }
        //callback(currentTeam)
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
                if(documents.documents.isEmpty()){
                    callback(partido)
                }else{
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
                    partidoRef.collection("titulares")
                        .get()
                        .addOnSuccessListener { documents ->
                            val titularesPartidoId = ArrayList<String>()

                            for (jugador in documents) {
                                titularesPartidoId.add(jugador.getString("id")!!)
                            }

                            for(id in titularesPartidoId){
                                db.collection("teams").document(equipoId).collection("players")
                                    .document(id)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id
                                        partido.titulares.add(playerDB)
                                        partido.titulares.sortBy { it.numero }
                                    }
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                            callback(PartidoModel()) // En caso de error, devolver una lista vacía
                        }

                    //busca suplentes
                    partidoRef.collection("suplentes")
                        .get()
                        .addOnSuccessListener { documents ->
                            val suplentesPartidoId = ArrayList<String>()

                            for (jugador in documents) {
                                suplentesPartidoId.add(jugador.getString("id")!!)
                            }

                            for(id in suplentesPartidoId){
                                db.collection("teams").document(equipoId).collection("players")
                                    .document(id)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id
                                        partido.suplentes.add(playerDB)
                                        partido.suplentes.sortBy { it.numero }
                                    }
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

                                db.collection("teams").document(equipoId).collection("players")
                                    .document(gol.getString("goleadorId")!!)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id

                                        goleador.add(
                                            JugadorModel(
                                                nombre = playerDB.nombre,
                                                numero = playerDB.numero,
                                                id = playerDB.id
                                            )
                                        )
                                    }

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
                                val idAmonestado = tarjeta.getString("amonestadoId")
                                val amonestado: ArrayList<JugadorModel> = ArrayList()

                                db.collection("teams").document(equipoId).collection("players")
                                    .document(idAmonestado!!)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id
                                        amonestado.add(
                                            JugadorModel(
                                                nombre = playerDB.nombre,
                                                numero = playerDB.numero,
                                                id = tarjeta.get("amonestadoId").toString()
                                            )
                                        )
                                    }

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
                                val entraId = cambio.getString("entraId")

                                db.collection("teams").document(equipoId).collection("players")
                                    .document(entraId!!)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id
                                        jugadores.add(
                                            JugadorModel(
                                                nombre = playerDB.nombre,
                                                numero = playerDB.numero,
                                                id = playerDB.id
                                            )
                                        )
                                    }

                                val saleId = cambio.getString("saleId")
                                db.collection("teams").document(equipoId).collection("players")
                                    .document(saleId!!)
                                    .get()
                                    .addOnSuccessListener { document->
                                        val playerDB = document.toObject<JugadorModel>()
                                        playerDB!!.id = document.id
                                        jugadores.add(
                                            JugadorModel(
                                                nombre = playerDB.nombre,
                                                numero = playerDB.numero,
                                                id = playerDB.id
                                            )
                                        )
                                    }

                                val sustitucion = EventoModel(
                                    tipoEventoId = 3,
                                    minuto = cambio.get("minuto").toString().toInt(),
                                    jugadoresImplicados = jugadores
                                )

                                partido.sustituciones.add(sustitucion)
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
            .addOnFailureListener {
                callback(PartidoModel())
            }

    }

    private suspend fun cargarJugadoresMinutos(teamId: String?): ArrayList<JugadorModel>{
        return suspendCoroutine { continuation ->
            buscarJugadoresMinutos(teamId) { equipo ->
                continuation.resume(equipo)
            }
        }
    }
    private fun buscarJugadoresMinutos(equipoId: String?, callback: (ArrayList<JugadorModel>) -> Unit) {
        val listado = ArrayList<JugadorModel>()
        val jugadoresRef = db.collection("teams").document(equipoId!!).collection("players")

        jugadoresRef.orderBy("minutosJugados",Query.Direction.DESCENDING).limit(3)
            .get()
            .addOnSuccessListener { documents ->
                for (jugador in documents) {
                    val playerDB = jugador.toObject<JugadorModel>()
                    playerDB.id = jugador.id
                    listado.add(playerDB)
                }
                callback(listado)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                callback(ArrayList()) // En caso de error, devolver una lista vacía
            }

    }

    private suspend fun cargarJugadoresGoles(teamId: String?): ArrayList<JugadorModel>{
        return suspendCoroutine { continuation ->
            buscarJugadoresGoles(teamId) { equipo ->
                continuation.resume(equipo)
            }
        }
    }
    private fun buscarJugadoresGoles(equipoId: String?, callback: (ArrayList<JugadorModel>) -> Unit) {
        val listado = ArrayList<JugadorModel>()
        val jugadoresRef = db.collection("teams").document(equipoId!!).collection("players")

        jugadoresRef.orderBy("golesMarcados",Query.Direction.DESCENDING).limit(3)
            .get()
            .addOnSuccessListener { documents ->
                for (jugador in documents) {
                    val playerDB = jugador.toObject<JugadorModel>()
                    playerDB.id = jugador.id
                    if(playerDB.golesMarcados != 0){
                        listado.add(playerDB)
                    }
                }
                callback(listado)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                callback(ArrayList()) // En caso de error, devolver una lista vacía
            }

    }

    private fun actualizar() {
        lifecycleScope.launch {
            val partido = cargarPartido(intent.extras?.getString("idEquipo"))
            listaPartidos = ArrayList()
            if(partido.id != ""){
                listaPartidos.add(ultimoPartido)
                binding.sinPartidostextView.visibility = View.INVISIBLE
            }else{
                binding.loadingGif.visibility = View.INVISIBLE
                binding.sinPartidostextView.visibility = View.VISIBLE
            }
            setPartidoAdapter(listaPartidos)

        }
        lifecycleScope.launch {
            val jugadores = cargarJugadoresMinutos(intent.extras?.getString("idEquipo"))

            setJugadoresMasMinutosAdapter(jugadores)
            binding.loadingGifMinutos.visibility = View.INVISIBLE
        }

        lifecycleScope.launch {
            val jugadores = cargarJugadoresGoles(intent.extras?.getString("idEquipo"))

            setJugadoresGoleadores(jugadores)
            binding.loadingGifGoles.visibility = View.INVISIBLE
        }

    }

    override fun onResume() {
        actualizar()
        super.onResume()
    }
}