package com.myTeams.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.adapter.TarjetaAdapter
import com.myTeams.app.databinding.ActivityAmonestacionesPartidoBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class AmonestacionesPartidoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAmonestacionesPartidoBinding
    private var currentTeam: TeamModel = TeamModel()
    private var partido: PartidoModel = PartidoModel()

    private var jugadoresTitulares: ArrayList<String> = ArrayList()
    private var jugadoresSuplentes: ArrayList<String> = ArrayList()

    private var listado: ArrayList<EventoModel> = ArrayList()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAmonestacionesPartidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        jugadoresTitulares = intent.extras?.getStringArrayList("titulares")!!
        jugadoresSuplentes = intent.extras?.getStringArrayList("suplentes")!!

        setup()
    }

    private fun setup(){
        binding.addeventobutton.setOnClickListener {
            val addTarjetaActivty = Intent(this, AddTarjetaActivity::class.java)
            addTarjetaActivty.putExtra("equipo", currentTeam)
            addTarjetaActivty.putExtra("partido", partido)

            addTarjetaActivty.putStringArrayListExtra("titulares", jugadoresTitulares)
            addTarjetaActivty.putStringArrayListExtra("suplentes", jugadoresSuplentes)
            startActivityForResult(addTarjetaActivty, 333)
        }

        binding.guardarEventosbutton.setOnClickListener {
            guardarYSalir()
        }

        binding.atrasbutton.setOnClickListener{
            guardarYSalir()
        }

        binding.eventosRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)

    }

    private fun guardarYSalir(){
        for (evento in listado){//listado viene de la bd
            partido.amonestaciones.add(evento)
        }

        val resultadoIntent = Intent()
        resultadoIntent.putExtra("partido", partido)
        setResult(Activity.RESULT_OK, resultadoIntent)
        finish()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            guardarYSalir()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun setAdapter(listado: ArrayList<EventoModel>) {
        if(listado.isEmpty() || listado.size == 1){
            binding.eventosRecyclerView.adapter = TarjetaAdapter(
                listado, this, partido
            )
        }else{
            val listadoOrdenado = listado.sortedWith(compareBy { it.minuto })
            val arrayListOrdenado = ArrayList<EventoModel>()

            for(tarjeta in listadoOrdenado){
                arrayListOrdenado.add(tarjeta)
            }

            binding.eventosRecyclerView.adapter = TarjetaAdapter(
                arrayListOrdenado, this, partido
            )
        }
    }

    private fun encontrarSegundaAmarilla(amonestaciones: List<EventoModel>): JugadorModel?{
        val conAmarilla: ArrayList<JugadorModel> = ArrayList()
        for(tarjeta in amonestaciones){
            if(tarjeta.tipoEventoId == 1){ //es tarjeta amarilla
                conAmarilla.add(tarjeta.jugadoresImplicados[0])   //guardo los amonestados
            }
        }
        val idsEncontrados = mutableSetOf<String>()
        for (jugador in conAmarilla) {
            if (!idsEncontrados.add(jugador.id)) {
                // Si add devuelve false, significa que el id ya estaba en el array
                return jugador
            }
        }
        return null
    }

    private fun actualizar(){
        if(encontrarSegundaAmarilla(partido.amonestaciones) != null) {
            val segundaAmarilla = partido.amonestaciones.last()
            val expulsion = EventoModel(
                tipoEventoId = 2,
                tipoEventoNombre = "Tarjeta Roja",
                jugadoresImplicados = segundaAmarilla.jugadoresImplicados,
                minuto = segundaAmarilla.minuto
            )
            partido.amonestaciones.add(expulsion)
        }
        val tarjetas = partido.amonestaciones
        setAdapter(tarjetas)
    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
            //jugadoresTitulares = data?.getStringArrayListExtra("titulares")!!
            //binding.contadorTitulares.text = "(${jugadoresTitulares!!.size})"
            partido = data?.getSerializableExtra("partido", PartidoModel::class.java)!!
            //añadir evento a la bd y a listado
        }
    }
}