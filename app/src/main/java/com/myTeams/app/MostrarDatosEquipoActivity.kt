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
import com.myTeams.app.databinding.ActivityMostrarDatosEquipoBinding
import com.myTeams.app.model.TeamModel

class MostrarDatosEquipoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMostrarDatosEquipoBinding
    private var equipoActual: TeamModel = TeamModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMostrarDatosEquipoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        equipoActual = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!

        window.statusBarColor = getColor(R.color.verdeTitulos)

        setup()
    }

    private fun setup(){
        binding.nombreEquipotextView.text = equipoActual.nombre
        binding.partidosJugadosTextView.text = equipoActual.partidosJugados.toString()
        binding.victoriasTextView.text = equipoActual.victorias.toString()
        binding.empatesTextView.text = equipoActual.empates.toString()
        binding.derrotasTextView.text = equipoActual.derrotas.toString()
        binding.golesfavorTextView.text = equipoActual.golesMarcados.toString()
        binding.golesContraTextView.text = equipoActual.golesEncajados.toString()
        binding.diferenciaGolesTextView.text = (equipoActual.golesMarcados - equipoActual.golesEncajados).toString()
        binding.puntosTextView.text = (equipoActual.victorias * 3 + equipoActual.empates).toString()

        binding.puntosButton.setOnClickListener {
            val popupMenu = PopupMenu(this,binding.puntosButton)
            popupMenu.inflate(R.menu.popup_3puntos_equipo)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.jugadoresPopupOption -> {
                        val teamActivityIntent = Intent(this, PlayersActivity::class.java)
                        if(equipoActual.id != ""){
                            teamActivityIntent.putExtra("equipoId", equipoActual.id)
                            teamActivityIntent.putExtra("equipo", equipoActual)
                            startActivity(teamActivityIntent)
                        }
                        true
                    }
                    R.id.partidosPopupOption -> {
                        val partidosActivityIntent = Intent(this, PartidosActivity::class.java)

                        if(equipoActual.id != ""){
                            partidosActivityIntent.putExtra("equipoId", equipoActual.id)
                            partidosActivityIntent.putExtra("equipo", equipoActual)
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

        binding.atrasbutton.setOnClickListener {
            finish()
        }
    }
}