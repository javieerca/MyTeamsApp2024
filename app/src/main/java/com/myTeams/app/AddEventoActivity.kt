package com.myTeams.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myTeams.app.databinding.ActivityAddEventoBinding
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class AddEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventoBinding
    private var currentTeam: TeamModel = TeamModel()
    private var partido: PartidoModel = PartidoModel()
    private var jugadoresTitulares: ArrayList<String> = ArrayList()
    private var jugadoresSuplentes: ArrayList<String> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        currentTeam = intent.extras?.getSerializable("equipo", TeamModel::class.java)!!
        partido = intent.extras?.getSerializable("partido", PartidoModel::class.java)!!

        //TODO cargar jugadores desde partido
        //jugadoresTitulares = intent.extras?.getStringArrayList("titulares")!!
        //jugadoresSuplentes = intent.extras?.getStringArrayList("suplentes")!!


        binding.golesbutton.setOnClickListener {
            val golesPartidoActivityIntent = Intent(this, GolesPartidoActivity::class.java)
            golesPartidoActivityIntent.putExtra("equipo", currentTeam)
            golesPartidoActivityIntent.putExtra("partido", partido)

            golesPartidoActivityIntent.putStringArrayListExtra("titulares", jugadoresTitulares)
            golesPartidoActivityIntent.putStringArrayListExtra("suplentes", jugadoresSuplentes)
            startActivityForResult(golesPartidoActivityIntent, 333)

            Toast.makeText(this, "Seleccionado goles", Toast.LENGTH_SHORT).show()
        }

        binding.amonestacionesbutton.setOnClickListener {
            val amonestacionesPartidoActivityIntent = Intent(this, AmonestacionesPartidoActivity::class.java)
            amonestacionesPartidoActivityIntent.putExtra("equipo", currentTeam)
            amonestacionesPartidoActivityIntent.putExtra("partido", partido)

            amonestacionesPartidoActivityIntent.putStringArrayListExtra("titulares", jugadoresTitulares)
            amonestacionesPartidoActivityIntent.putStringArrayListExtra("suplentes", jugadoresSuplentes)
            startActivityForResult(amonestacionesPartidoActivityIntent, 444)

            Toast.makeText(this, "Seleccionado goles", Toast.LENGTH_SHORT).show()
        }

        binding.cambiosbutton.setOnClickListener {
            val sustitucionesPartidoIntent = Intent(this, SustitucionesPartido::class.java)
            sustitucionesPartidoIntent.putExtra("equipo", currentTeam)
            sustitucionesPartidoIntent.putExtra("partido", partido)

            sustitucionesPartidoIntent.putStringArrayListExtra("titulares", jugadoresTitulares)
            sustitucionesPartidoIntent.putStringArrayListExtra("suplentes", jugadoresSuplentes)
            startActivityForResult(sustitucionesPartidoIntent, 555)

            Toast.makeText(this, "Seleccionado goles", Toast.LENGTH_SHORT).show()

        }


        binding.atrasbutton.setOnClickListener {
            guardarYSalir()
        }



    }
    private fun guardarYSalir(){
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 333 && resultCode == Activity.RESULT_OK) {
            partido = data?.getSerializableExtra("partido", PartidoModel::class.java)!!
            //recibe el partido actualizado con los goles dentro del listado eventos

        }
        //hacer 444 con amonestaciones
        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {
            partido = data?.getSerializableExtra("partido", PartidoModel::class.java)!!
            //recibe el partido actualizado con los goles dentro del listado eventos
        }
        if (requestCode == 555 && resultCode == Activity.RESULT_OK) {
            partido = data?.getSerializableExtra("partido", PartidoModel::class.java)!!
            //recibe el partido actualizado con los goles dentro del listado eventos
        }

        //hacer 555 con sustituciones
    }

}