package com.myTeams.app.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.MostrarJugadorActivity
import com.myTeams.app.R
import com.myTeams.app.databinding.Jugadores3EstadisitcasBinding
import com.myTeams.app.model.JugadorModel

class JugadoresGolesAdapter (private var players: ArrayList<JugadorModel>, val context: Context):

    RecyclerView.Adapter<JugadoresGolesAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.jugadores_3_estadisitcas, null))
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val binding = Jugadores3EstadisitcasBinding.bind(holder.itemView)

        if(players.size == 0){
            binding.contenedorLayout.visibility = View.GONE
        }

        if(players.size > 0){
            val jugador1 = players[0]
            binding.nombre1TextView.text = jugador1.nombre
            binding.minutos1TextView.text = jugador1.golesMarcados.toString()

            binding.primeroLayout.setOnClickListener{
                val mostrarJugadorActivityIntent = Intent(context, MostrarJugadorActivity::class.java)
                mostrarJugadorActivityIntent.putExtra("jugador", jugador1)
                context.startActivity(mostrarJugadorActivityIntent)
            }
        }else{
            //Toast.makeText(context, "No hay goleadores", Toast.LENGTH_SHORT).show()
        }

        if(players.size > 1){
            val jugador2 = players[1]
            binding.nombre1TextView2.text = jugador2.nombre
            binding.minutos2TextView.text = jugador2.golesMarcados.toString()

            binding.segundoLayout.setOnClickListener{
                val mostrarJugadorActivityIntent = Intent(context, MostrarJugadorActivity::class.java)
                mostrarJugadorActivityIntent.putExtra("jugador", jugador2)
                context.startActivity(mostrarJugadorActivityIntent)
            }
        }else{
            var params = binding.posicion1TextView.layoutParams as ViewGroup.MarginLayoutParams

            (binding.posicion1TextView.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 24

            binding.dividerPrimero.visibility = View.GONE
            binding.segundoLayout.visibility = View.GONE
            binding.terceroConstraint.visibility = View.GONE
        }

        if(players.size > 2){
            val jugador3 = players[2]
            binding.nombre3TextView.text = jugador3.nombre
            binding.minutos3TextView.text = jugador3.golesMarcados.toString()

            binding.terceroConstraint.setOnClickListener{
                val mostrarJugadorActivityIntent = Intent(context, MostrarJugadorActivity::class.java)
                mostrarJugadorActivityIntent.putExtra("jugador", jugador3)
                context.startActivity(mostrarJugadorActivityIntent)
            }
        }else{
            binding.terceroConstraint.visibility = View.GONE
        }

    }

}