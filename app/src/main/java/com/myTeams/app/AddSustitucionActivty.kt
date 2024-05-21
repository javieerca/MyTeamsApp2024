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
import com.myTeams.app.databinding.ActivityAddSustitucionActivtyBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.TeamModel

class AddSustitucionActivty : AppCompatActivity() {
    private lateinit var binding: ActivityAddSustitucionActivtyBinding
    private var currentTeam: TeamModel = TeamModel()
    private var sustituido: JugadorModel = JugadorModel()
    private var sustituto: JugadorModel = JugadorModel()

    private var progresoSeekBar: Int=0

    private val db = FirebaseFirestore.getInstance()
    private var partido: PartidoModel = PartidoModel()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddSustitucionActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        binding.seekBar.min = 1
        binding.seekBar.max = 90

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textView11.text = progress.toString()
                progresoSeekBar = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seleccionarsustitutobutton.setOnClickListener {
            val seleccionarJugadorEventoActivityIntent = Intent(this, SeleccionarJugadorEventoActivity::class.java)

            seleccionarJugadorEventoActivityIntent.putExtra("idEquipo", currentTeam.id)
            seleccionarJugadorEventoActivityIntent.putExtra("titularesSuplentes", 2)
            seleccionarJugadorEventoActivityIntent.putExtra("partido", partido)
            startActivityForResult(seleccionarJugadorEventoActivityIntent, 333)
        }
        binding.seleccionarsustituidobutton.setOnClickListener {
            val seleccionarJugadorEventoActivityIntent = Intent(this, SeleccionarJugadorEventoActivity::class.java)

            seleccionarJugadorEventoActivityIntent.putExtra("idEquipo", currentTeam.id)
            seleccionarJugadorEventoActivityIntent.putExtra("titularesSuplentes", 1)
            seleccionarJugadorEventoActivityIntent.putExtra("partido", partido)
            startActivityForResult(seleccionarJugadorEventoActivityIntent, 444)
        }

        binding.guardarGolbutton2.setOnClickListener {
            val minutoCambio: Int= progresoSeekBar
            val jugadorArray: ArrayList<JugadorModel> = ArrayList()
            jugadorArray.add(sustituto)
            jugadorArray.add(sustituido)
            val evento = EventoModel(
                tipoEventoId = 3,
                tipoEventoNombre = "Sustitucion",
                jugadoresImplicados = jugadorArray,
                minuto = minutoCambio
            )

            Toast.makeText(this, "Evento creado", Toast.LENGTH_SHORT).show()

            partido.sustituciones.add(evento)

            val resultadoIntent = Intent()
            resultadoIntent.putExtra("partido", partido)

            setResult(Activity.RESULT_OK, resultadoIntent)
            finish()
        }

    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
            sustituto = data?.getSerializableExtra("jugador", JugadorModel::class.java)!!
            binding.nombreEntratextView.text = sustituto.nombre
            binding.numeroEntratextView2.text = sustituto.numero.toString()

            Toast.makeText(this, "JugadorId ${sustituto.id}", Toast.LENGTH_SHORT)
                .show()
        }
        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {
            sustituido = data?.getSerializableExtra("jugador", JugadorModel::class.java)!!
            binding.nombreSaletextView3.text = sustituido.nombre
            binding.numeroSaletextView3.text = sustituido.numero.toString()

            Toast.makeText(this, "JugadorId ${sustituido.id}", Toast.LENGTH_SHORT)
                .show()
        }
    }


}
