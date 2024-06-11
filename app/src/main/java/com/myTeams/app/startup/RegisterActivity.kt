package com.myTeams.app.startup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.HomeActivity
import com.myTeams.app.ProviderType
import com.myTeams.app.R
import com.myTeams.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    private val GOOGLE_SIGN_IN = 100

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //setup
        setup()

    }
    private fun setup(){
        title = "Autenticación"

        binding.registerButton2.setOnClickListener {
            var nombreValido = true

            var nombreDeUsuario = binding.usernameEditText.text.toString().trim()
            if (nombreDeUsuario.contains("#") || nombreDeUsuario.contains("$") || nombreDeUsuario.contains(
                    "%"
                )
                || nombreDeUsuario.contains(";") || nombreDeUsuario.contains("<") || nombreDeUsuario.contains(
                    ">"
                )
                || nombreDeUsuario.contains(",") || nombreDeUsuario.contains("=") || nombreDeUsuario.contains(
                    "!"
                )
                || nombreDeUsuario.contains("¡") || nombreDeUsuario.contains("?") || nombreDeUsuario.contains(
                    "¿"
                )
                || nombreDeUsuario.contains("'") || nombreDeUsuario.contains("[") || nombreDeUsuario.contains(
                    "]"
                )
                || nombreDeUsuario.contains("^") || nombreDeUsuario.contains("¨") || nombreDeUsuario.contains(
                    "{"
                )
                || nombreDeUsuario.contains("}") || nombreDeUsuario.contains("ç") || nombreDeUsuario.contains(
                    ":"
                )
            ) {
                nombreValido = false
                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setMessage("Su nombre de usuario no puede contener caracteres especiales.")
                builder.setTitle("Importante")
                builder.setCancelable(false)

                builder.setPositiveButton("OK") { _, _ ->
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }
            if (nombreDeUsuario.count() in 1..5) {

                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setMessage("Su nombre de usuario debe contener mas de 6 caracteres.")
                builder.setTitle("Importante")
                builder.setCancelable(false)

                builder.setPositiveButton("OK") { _, _ ->
                }
                val alertDialog = builder.create()
                alertDialog.show()
                nombreValido = false
            }
            if (nombreDeUsuario.contains(" ")) {
                nombreValido = false
                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setMessage("Su nombre de usuario no puede contener espacios.")
                builder.setTitle("Importante")
                builder.setCancelable(false)

                builder.setPositiveButton("OK") { _, _ ->
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }

            if (nombreValido) {
                //comprobar que no existe
                val nuevoUsername = binding.usernameEditText.text.trim().toString()
                db.collection("users")
                    .whereEqualTo("username", nuevoUsername)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            //guardar y cambiar estado
                            if (binding.emailEdittext.text.isNotEmpty() && binding.passwordEditText2.text.isNotEmpty()) {
                                var email = binding.emailEdittext.text.toString()
                                var password = binding.passwordEditText2.text.toString()
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                                            db.collection("users").document(email).set(
                                                hashMapOf<String, Any>("username" to nuevoUsername)
                                            )
                                        } else {
                                            showAlert()
                                        }
                                    }
                            }

                        } else {
                            val builder = AlertDialog.Builder(this@RegisterActivity)
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
                        Toast.makeText(this, "Ha ocurrido un error inesperado.", Toast.LENGTH_SHORT)
                            .show()
                    }



            }
        }

        binding.googleButton2.setOnClickListener{
            //configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)


        }

        binding.atrasbutton.setOnClickListener {
            finish()
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email:String, provider: ProviderType){

        val homeActivityIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider)
        }
        startActivity(homeActivityIntent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)

                if(account!= null){
                    val credential= GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(account.email ?:"", ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }
                }
            }catch(e: ApiException){
                showAlert()
            }


        }
    }
}