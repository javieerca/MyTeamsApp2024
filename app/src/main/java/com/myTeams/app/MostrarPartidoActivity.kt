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