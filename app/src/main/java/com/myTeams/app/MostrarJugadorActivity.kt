package com.myTeams.app

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myTeams.app.databinding.ActivityMostrarJugadorBinding
import com.myTeams.app.model.JugadorModel

class MostrarJugadorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMostrarJugadorBinding
    private var jugador: JugadorModel = JugadorModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMostrarJugadorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        jugador = intent.extras?.getSerializable("jugador", JugadorModel::class.java)!!

        setup()

    }


    private fun setup(){
        binding.nombreJugadorTextView.text = jugador.nombre
        binding.numeroJugadortextView.text = jugador.numero.toString()
        binding.valorPosiciontextView.text = jugador.posicion
        binding.valorpartidosConvocadotextView.text = jugador.partidosConvocado.toString()
        binding.valorpartidosJugadostextView.text = jugador.partidosJugados.toString()
        binding.valorminutosJugadostextView.text = jugador.minutosJugados.toString()
        binding.valorvecesCambiadotextView.text = jugador.vecesCambiado.toString()
        binding.valorgolesMarcadostextView.text = jugador.golesMarcados.toString()
        if(jugador.golesMarcados == 0){
            binding.valorGolesPartidotextView.text = 0.toString()
            binding.valorgolesMinutostextView.text = 0.toString()
        }else{
            val golesPartido = jugador.partidosJugados / jugador.golesMarcados
            val golesMinutos = jugador.minutosJugados / jugador.golesMarcados
            binding.valorGolesPartidotextView.text = golesPartido.toString()
            binding.valorgolesMinutostextView.text = golesMinutos.toString()
        }

        binding.valortarjetasAmarillastextView.text = jugador.tarjetasAmarillas.toString()
        binding.valortarjetasRojastextView.text = jugador.tarjetasRojas.toString()

        binding.atrasbutton.setOnClickListener {
            finish()
        }
    }
}