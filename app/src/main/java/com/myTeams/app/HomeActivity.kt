package com.myTeams.app

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.adapter.TeamAdapter
import com.myTeams.app.databinding.ActivityHomeBinding
import com.myTeams.app.model.TeamModel
import com.myTeams.app.startup.MainActivity
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


enum class ProviderType{
    BASIC,
    GOOGLE
}
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        val emailIntent = intent.extras?.getString("email")

        if (emailIntent != null) {
            email = emailIntent
            lifecycleScope.launch {
                val listado = cargarEquipos(email)
                setup()

                setAdapter(listado)
            }
        }

        //guardado de datos de sesion
        val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()

    }

    private fun setup() {

        binding.logoutbutton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            //borrado de datos
            val prefs =
                getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }

        binding.saveButton.setOnClickListener {
            val addTeamActivityIntent = Intent(this, AddTeamActivity::class.java)
            startActivity(addTeamActivityIntent)
        }

        binding.userbutton.setOnClickListener {
            val miCuentaActivityIntent = Intent(this, MiCuentaActivity::class.java)
            miCuentaActivityIntent.putExtra("email", email)
            startActivity(miCuentaActivityIntent)
        }

        binding.playersRecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)
    }

    private fun setAdapter(listado: ArrayList<TeamModel>) {
        binding.playersRecyclerView.adapter = TeamAdapter(
            listado, this, db
        )

    }

    private suspend fun cargarEquipos(email: String?): ArrayList<TeamModel> {
        return suspendCoroutine { continuation ->
            buscarEquipos(email) { equipos ->
                continuation.resume(equipos)
            }
        }
    }

    private fun buscarEquipos(email: String?, callback: (ArrayList<TeamModel>) -> Unit) {
        val listado = ArrayList<TeamModel>()

        if (email != null) {
            db.collection("teams")
                .whereEqualTo("usuario", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val teamDB = document.toObject<TeamModel>()
                        teamDB.id = document.id
                        listado.add(teamDB)
                    }
                    binding.loadingGif.visibility = View.INVISIBLE
                    callback(listado)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                    callback(ArrayList()) // En caso de error, devolver una lista vacía
                }

        } else {
            binding.loadingGif.visibility = View.INVISIBLE
            callback(ArrayList())
        }
    }

    private fun actualizar() {
        lifecycleScope.launch {
            val listado = cargarEquipos(intent.extras?.getString("email"))
            setAdapter(listado)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }


    //controlar la pulsacion del boton atras
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            confirmCerrar()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun confirmCerrar(){
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setMessage("¿Desea salir de la app?")
        builder.setTitle("Importante")
        builder.setCancelable(false)

        builder.setPositiveButton("yes") { _, _ ->
            finishAffinity()
        }
        builder.setNegativeButton("no"){_,_ ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}



