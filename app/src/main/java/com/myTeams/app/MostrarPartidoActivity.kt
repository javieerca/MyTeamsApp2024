package com.myTeams.app

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.adapter.EventosPartidoAdapter
import com.myTeams.app.adapter.SustitucionEnPartidoAdapter
import com.myTeams.app.databinding.ActivityMostrarPartidoBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class MostrarPartidoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMostrarPartidoBinding
    private var partidoActual: PartidoModel = PartidoModel()
    private var listadoEventos: ArrayList<EventoModel> = ArrayList()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMostrarPartidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        //currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partidoActual = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!
        setup()
    }

    private fun setup(){
        binding.eventosRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)

        binding.sustitucionesrecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)


        if(partidoActual.local){
            binding.localTextView2.text = partidoActual.equipoNombre
            binding.golesLocaltextView.text = partidoActual.golesMarcados.toString()

            binding.visitantetextView2.text = partidoActual.rival
            binding.golesVisitantetextView.text = partidoActual.golesEncajados.toString()
        }else{
            binding.localTextView2.text = partidoActual.rival
            binding.golesLocaltextView.text = partidoActual.golesEncajados.toString()

            binding.visitantetextView2.text = partidoActual.equipoNombre
            binding.golesVisitantetextView.text = partidoActual.golesMarcados.toString()
        }

        for(gol in partidoActual.goles){
            listadoEventos.add(gol)
        }
        for(tarjeta in partidoActual.amonestaciones){
            listadoEventos.add(tarjeta)
        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }
    }
    private fun setAdapterEventos(listadoDeEventos: List<EventoModel>) {
        binding.eventosRecyclerView.adapter = EventosPartidoAdapter(
            listadoDeEventos, this
        )
    }
    private fun setAdapterCambios(listadoDeCambios: List<EventoModel>) {
        binding.sustitucionesrecyclerView.adapter = SustitucionEnPartidoAdapter(
            listadoDeCambios, this
        )
    }

    private fun actualizar() {
        var cadenaTitulares = ""
        for (jugador in partidoActual.titulares){
            cadenaTitulares = "$cadenaTitulares ${jugador.numero}. ${jugador.nombre}. \n"
        }
        binding.listadoTitularestextView.text = cadenaTitulares
        var cadenaSuplentes =""
        for(jugador in partidoActual.suplentes){
            cadenaSuplentes = "$cadenaSuplentes ${jugador.numero}. ${jugador.nombre}. \n"
        }
        binding.listadoSuplentestextView.text= cadenaSuplentes

        val eventosOrdenados = listadoEventos.sortedBy { it.minuto }
        setAdapterEventos(eventosOrdenados)
        val sustitucionesOrdenado = partidoActual.sustituciones.sortedBy { it.minuto }
        setAdapterCambios(sustitucionesOrdenado)
    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }

}










/*
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

            val nuevoDocumento = db.collection("partidos").document(currentTeam.id).collection("liga").document(partidoActualizado.id)
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
                partidoActualizado.id = nuevoDocumento.id
                Toast.makeText(this, "Id partido: ${partidoActualizado.id}", Toast.LENGTH_SHORT).show()
                addEventos()
                actualizarInfoEquipo(datosPartido)
                //actualizarInfoJugadores()
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Algo ha ido mal", Toast.LENGTH_SHORT).show()
                }

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
            //subir cambios al partido
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

                //buscar jugador actualizado en la bd
                var jugadorBd = JugadorModel()
                jugadoresRef.document(cambio.jugadoresImplicados[1].id)
                    .get()
                    .addOnSuccessListener { document ->
                        jugadorBd = document.toObject<JugadorModel>()!!

                        jugadoresRef.document(cambio.jugadoresImplicados[1].id).update(
                            hashMapOf<String, Any>(
                                "vecesCambiado" to jugadorBd.vecesCambiado + 1
                            )
                        )
                    }

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

            Toast.makeText(this, "Se ha añadido un nuevo partido", Toast.LENGTH_SHORT).show()


            val builder = AlertDialog.Builder(this@EditarPartidoActivity)
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

    private fun insertarDatos(){

        binding.golesanotadostextView.text = partidoActualizado.golesMarcados.toString()
        binding.jornadaNumberEdit.setText(partidoActualizado.numeroJornada.toString())
        binding.rivalEditText.setText(partidoActualizado.rival)
        binding.encajadosTextNumber.setText(partidoActualizado.golesEncajados.toString())
        if(!partidoActualizado.local){
            binding.estadioSpinner.setSelection(1)
        }
        binding.contadorTitulares.text = "(${partidoActualizado.titulares.size})"
        binding.contadorSuplentes.text = "(${partidoActualizado.suplentes.size})"
        binding.contadorEventostextView.text = "(${partidoActualizado.goles.size + partidoActualizado.sustituciones.size + partidoActualizado.amonestaciones.size})"

        binding.observacionesMultiline.setText(partidoActualizado.observaciones)

        for(jugador in partidoActualizado.titulares){
            partidoActualizado.convocados.add(jugador)
            jugadoresTitularesIds.add(jugador.id)
        }

        for(jugador in partidoActualizado.suplentes){
            partidoActualizado.convocados.add(jugador)
            jugadoresSuplentesIds.add(jugador.id)
        }





 */