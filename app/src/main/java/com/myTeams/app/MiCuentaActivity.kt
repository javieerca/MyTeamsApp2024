package com.myTeams.app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.adapter.EquipoEnPerfilAdapter
import com.myTeams.app.databinding.ActivityMiCuentaBinding
import com.myTeams.app.model.TeamModel
import com.myTeams.app.startup.MainActivity
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("NAME_SHADOWING")
class MiCuentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMiCuentaBinding
    private val db = FirebaseFirestore.getInstance()
    private var email: String = ""
    private var nombreDeUsuario : String =""
    private var estaEditando: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMiCuentaBinding.inflate(layoutInflater)
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

        setup()
    }

    private fun setup(){

        val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val emailRecuperado = prefs.getString("email","")

        binding.correoTextView.text = emailRecuperado

        binding.atrasbutton.setOnClickListener {
            finish()
        }

        binding.editarbutton.setOnClickListener {
            if(estaEditando){
                if(binding.usernameEditText.text.isEmpty()){
                    val builder = AlertDialog.Builder(this@MiCuentaActivity)
                    builder.setMessage("Debe introducir un nombre de usuario.")
                    builder.setTitle("Importante")
                    builder.setCancelable(false)

                    builder.setPositiveButton("OK") { _, _ ->
                    }
                    val alertDialog = builder.create()
                    alertDialog.show()
                }
                else{
                    var nombreValido = true

                    nombreDeUsuario = binding.usernameEditText.text.toString().trim()
                    if(nombreDeUsuario.contains("#") || nombreDeUsuario.contains("$") || nombreDeUsuario.contains("%")
                        || nombreDeUsuario.contains(";") || nombreDeUsuario.contains("<") || nombreDeUsuario.contains(">")
                        || nombreDeUsuario.contains(",") || nombreDeUsuario.contains("=") ||nombreDeUsuario.contains("!")
                        ||nombreDeUsuario.contains("¡") || nombreDeUsuario.contains("?") || nombreDeUsuario.contains("¿")
                        || nombreDeUsuario.contains("'") || nombreDeUsuario.contains("[") || nombreDeUsuario.contains("]")
                        || nombreDeUsuario.contains("^") || nombreDeUsuario.contains("¨") || nombreDeUsuario.contains("{")
                        || nombreDeUsuario.contains("}") || nombreDeUsuario.contains("ç") || nombreDeUsuario.contains(":"))
                    {
                        nombreValido = false
                        val builder = AlertDialog.Builder(this@MiCuentaActivity)
                        builder.setMessage("Su nombre de usuario no puede contener caracteres especiales.")
                        builder.setTitle("Importante")
                        builder.setCancelable(false)

                        builder.setPositiveButton("OK") { _, _ ->
                        }
                        val alertDialog = builder.create()
                        alertDialog.show()
                    }
                    if(nombreDeUsuario.count() in 1..5){

                        val builder = AlertDialog.Builder(this@MiCuentaActivity)
                        builder.setMessage("Su nombre de usuario debe contener mas de 6 caracteres.")
                        builder.setTitle("Importante")
                        builder.setCancelable(false)

                        builder.setPositiveButton("OK") { _, _ ->
                        }
                        val alertDialog = builder.create()
                        alertDialog.show()
                        nombreValido = false
                    }
                    if(nombreDeUsuario.contains(" ")){
                        nombreValido = false
                        val builder = AlertDialog.Builder(this@MiCuentaActivity)
                        builder.setMessage("Su nombre de usuario no puede contener espacios.")
                        builder.setTitle("Importante")
                        builder.setCancelable(false)

                        builder.setPositiveButton("OK") { _, _ ->
                        }
                        val alertDialog = builder.create()
                        alertDialog.show()
                    }


                    if(nombreValido){

                        //comprobar que no existe
                        val nuevoUsername = binding.usernameEditText.text.trim().toString()
                        db.collection("users")
                            .whereEqualTo("username", nuevoUsername)
                            .get()
                            .addOnSuccessListener { documents ->
                                if(documents.isEmpty){
                                    //guardar y cambiar estado
                                    db.collection("users").document(email)
                                        .update(
                                            hashMapOf<String, Any>("username" to nuevoUsername)
                                        )

                                    val builder = AlertDialog.Builder(this@MiCuentaActivity)
                                    builder.setMessage("Se ha actualizado su nombre de usuario.")
                                    builder.setTitle("Importante")
                                    builder.setCancelable(false)

                                    builder.setPositiveButton("OK") { _, _ ->
                                    }
                                    val alertDialog = builder.create()
                                    alertDialog.show()

                                    binding.editarbutton.background.setTint(getColor(R.color.grisClaro))
                                    binding.editarbutton.setTextColor(Color.BLACK)

                                    binding.usernameEditText.isEnabled = false
                                    binding.editarbutton.text = "Editar"
                                    estaEditando = false
                                }else{
                                    val builder = AlertDialog.Builder(this@MiCuentaActivity)
                                    builder.setMessage("Ese nombre ya está en uso.")
                                    builder.setTitle("Importante")
                                    builder.setCancelable(false)

                                    builder.setPositiveButton("OK") { _, _ ->
                                    }
                                    val alertDialog = builder.create()
                                    alertDialog.show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,"Ha ocurrido un error inesperado.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            else{
                binding.usernameEditText.isEnabled = true
                binding.editarbutton.background.setTint(getColor(R.color.azulBoton))
                binding.editarbutton.setTextColor(Color.WHITE)
                binding.editarbutton.text = "Guardar"

                estaEditando = true
            }
        }

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

        binding.equiposrecyclerView.layoutManager =
            GridLayoutManager(this, 1, RecyclerView.VERTICAL, false)

    }

    private fun setAdapter(listado: ArrayList<TeamModel>) {
        binding.equiposrecyclerView.adapter = EquipoEnPerfilAdapter(
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

                    db.collection("users").document(email)
                        .get()
                        .addOnSuccessListener {documents ->
                            nombreDeUsuario = documents.getString("username")!!
                            if(nombreDeUsuario.isNotEmpty()){
                                binding.usernameEditText.setText(nombreDeUsuario)
                            }
                        }
                        .addOnFailureListener {
                            binding.usernameEditText.setText("")
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                    callback(ArrayList()) // En caso de error, devolver una lista vacía
                }

        } else {
            binding.loadingGif.visibility = View.INVISIBLE
            callback(ArrayList())
        }
    }

    private fun actualizar() {
        lifecycleScope.launch {
            val listado = cargarEquipos(email)
            setAdapter(listado)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizar()
    }
}