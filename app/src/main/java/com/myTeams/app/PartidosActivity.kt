package com.myTeams.app

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
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
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
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
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
                            }


                        //cargar goles
                        partidoRef.collection("goles")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (gol in documents) {
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
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
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
                                callback(ArrayList()) // En caso de error, devolver una lista vacía
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
            if(listado.isEmpty()){
                binding.sinPartidostextView.visibility = View.VISIBLE
            }else{
                binding.sinPartidostextView.visibility = View.INVISIBLE
            }
            setAdapter(listado)
        }
    }

    override fun onResume() {
        actualizar()
        super.onResume()
    }

}
