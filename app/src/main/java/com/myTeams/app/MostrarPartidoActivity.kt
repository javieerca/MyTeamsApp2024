package com.myTeams.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.adapter.EventosPartidoAdapter
import com.myTeams.app.adapter.SustitucionEnPartidoAdapter
import com.myTeams.app.databinding.ActivityMostrarPartidoBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class MostrarPartidoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMostrarPartidoBinding
    private var equipoActual: TeamModel = TeamModel()
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

        equipoActual = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partidoActual = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!
        setup()
    }

    private fun setup(){
        binding.puntosButton.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.puntosButton)
            popupMenu.inflate(R.menu.solo_editar_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editarOpcion -> {
                        val editarPartidoActivityIntent = Intent(this, EditarPartidoActivity::class.java)

                        editarPartidoActivityIntent.putExtra("equipo", equipoActual)
                        editarPartidoActivityIntent.putExtra("jornadaActual", partidoActual.numeroJornada)
                        editarPartidoActivityIntent.putExtra("partido", partidoActual)
                        this.startActivity(editarPartidoActivityIntent)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }
        binding.eventosRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)

        binding.sustitucionesrecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)

        binding.observacionesDbTextView.text = partidoActual.observaciones

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
        binding.jornadaTextView3.text = "Jornada ${partidoActual.numeroJornada}"
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
        actualizar()
        super.onResume()
    }

}
