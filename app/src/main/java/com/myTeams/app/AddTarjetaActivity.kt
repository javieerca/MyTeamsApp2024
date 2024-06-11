package com.myTeams.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myTeams.app.databinding.ActivityAddTarjetaBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class AddTarjetaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTarjetaBinding
    private var currentTeam: TeamModel = TeamModel()
    private var amonestado: JugadorModel = JugadorModel()
    private var progresoSeekBar: Int=0
    private var esAmarilla: Boolean = true

    private var partido: PartidoModel = PartidoModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTarjetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        binding.seekBar.min = 1

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.minutotextView.text = progress.toString()
                progresoSeekBar = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.switch1.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                esAmarilla = false
                binding.switch1.trackTintList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                binding.switch1.text = "Roja"
            } else {
                esAmarilla = true
                binding.switch1.trackTintList = ColorStateList.valueOf(Color.parseColor("#EDED09"))
                binding.switch1.text = "Amarilla"
            }
        }

        binding.seleccionarJugadorbutton.setOnClickListener {
            val seleccionarJugadorEventoActivityIntent = Intent(this, SeleccionarJugadorEventoActivity::class.java)

            seleccionarJugadorEventoActivityIntent.putExtra("idEquipo", currentTeam.id)
            seleccionarJugadorEventoActivityIntent.putExtra("titularesSuplentes", 0)
            seleccionarJugadorEventoActivityIntent.putExtra("partido", partido)
            startActivityForResult(seleccionarJugadorEventoActivityIntent, 333)
        }

        binding.masbutton.setOnClickListener {
            binding.seekBar.progress += 1
        }

        binding.menosbutton.setOnClickListener {
            binding.seekBar.progress -= 1
        }

        binding.guardarGolbutton.setOnClickListener {
            val minutoTarjeta: Int= progresoSeekBar
            if(amonestado.id != ""){
                val jugadorArray: ArrayList<JugadorModel> = ArrayList()
                jugadorArray.add(amonestado)
                var tipoEventoId = 1
                var tipoEventoNombre = "Tarjeta Amarilla"
                if(!esAmarilla){
                    tipoEventoId = 2
                    tipoEventoNombre = "Tarjeta roja"
                }
                val evento = EventoModel(
                    tipoEventoId = tipoEventoId,
                    tipoEventoNombre = tipoEventoNombre,
                    jugadoresImplicados = jugadorArray,
                    minuto = minutoTarjeta
                )
                partido.amonestaciones.add(evento)

                val resultadoIntent = Intent()
                resultadoIntent.putExtra("partido", partido)

                setResult(Activity.RESULT_OK, resultadoIntent)
                finish()
            }else{
                Toast.makeText(this, "Deber seleccionar un jugador", Toast.LENGTH_SHORT).show()
            }
        }

        binding.atrasbutton.setOnClickListener {
            confirmCerrar()
        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }
    }

    private fun confirmCerrar(){
        val builder = AlertDialog.Builder(this@AddTarjetaActivity)
        builder.setMessage("¿Desea salir sin guardar?")
        builder.setTitle("Importante")
        builder.setCancelable(false)

        builder.setPositiveButton("yes") { _, _ ->
            finish()
        }
        builder.setNegativeButton("no"){_,_ ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
            amonestado = data?.getSerializableExtra("jugador", JugadorModel::class.java)!!
            binding.nombreJugadortextView.text = amonestado.nombre
            binding.numeroGoleadortextView.text = amonestado.numero.toString()
        }
    }
}