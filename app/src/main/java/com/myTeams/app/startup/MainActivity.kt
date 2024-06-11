package com.myTeams.app.startup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.myTeams.app.HomeActivity
import com.myTeams.app.R
import com.myTeams.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_Theme_MyTeamsApp2024)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.verdeTitulos)

        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        binding.logInButton.setOnClickListener {
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
        }
        binding.registerButton.setOnClickListener {
            val registerActivityIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerActivityIntent)
        }

        session()
    }


    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        if(email != null){
            //R.layout.activity_home = View.INVISIBLE        //hace invisible el layout
            //showHome(email, ProviderType.BASIC.toString())

            showHome(email)
            finish()
        }
    }

    private fun showHome(email:String){
        val homeActivityIntent = Intent(this, HomeActivity::class.java)
            homeActivityIntent.putExtra("email", email)
        startActivity(homeActivityIntent)
    }
}