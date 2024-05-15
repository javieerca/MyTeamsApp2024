package com.myTeams.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.databinding.ActivityAddGolBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.PlayerModel
import com.myTeams.app.model.TeamModel

class AddGolActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddGolBinding
    private var currentTeam: TeamModel = TeamModel()
    private var goleadorId: String = ""
    private var goleador: PlayerModel = PlayerModel()
    private var progresoSeekBar: Int=0

    private val db = FirebaseFirestore.getInstance()
    private var partido: PartidoModel = PartidoModel()



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddGolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textView7.text = progress.toString()
                progresoSeekBar = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seleccionarJugadorbutton.setOnClickListener {
            val seleccionarJugadorEventoActivityIntent = Intent(this, SeleccionarJugadorEventoActivity::class.java)

            seleccionarJugadorEventoActivityIntent.putExtra("idEquipo", currentTeam.id)
            startActivityForResult(seleccionarJugadorEventoActivityIntent, 333)
        }


        binding.guardarGolbutton.setOnClickListener {
            var minutoGol: Int= progresoSeekBar
            var jugadorArray: ArrayList<PlayerModel> = ArrayList()
            jugadorArray.add(goleador)
            var evento = EventoModel(
                tipoEventoId = 0,
                tipoEventoNombre = "Gol",
                jugadoresImplicados = jugadorArray,
                minuto = minutoGol
            )
            Toast.makeText(this, "Evento creado", Toast.LENGTH_SHORT).show()

            partido.goles.add(evento)

            val resultadoIntent = Intent()
            resultadoIntent.putExtra("partido", partido)

            //SUBIR A BD
            setResult(Activity.RESULT_OK, resultadoIntent)
            finish()

        }


    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
            goleador = data?.getSerializableExtra("jugador", PlayerModel::class.java)!!
            binding.nombreJugadortextView.text = goleador.name
            binding.numeroGoleadortextView.text = goleador.number.toString()

            Toast.makeText(this, "JugadorId ${goleador.id}", Toast.LENGTH_SHORT)
                .show()
        }
    }

}