package com.myTeams.app.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.EditarJugadorActivity
import com.myTeams.app.EditarPartidoActivity
import com.myTeams.app.MostrarJugadorActivity
import com.myTeams.app.R
import com.myTeams.app.databinding.PlayerLayoutBinding
import com.myTeams.app.model.JugadorModel


class PlayerAdapter (private var players: ArrayList<JugadorModel>, val context: Context, private val db: FirebaseFirestore):

RecyclerView.Adapter<PlayerAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.player_layout, null))
    }



    override fun getItemCount(): Int {
        return players.size
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val player = players[position]
        //holder.bind(item)
        val binding = PlayerLayoutBinding.bind(holder.itemView)

        holder.itemView.setOnClickListener {
        }

        binding.nameTextView.text = player.nombre
        binding.numberTextView.text = player.numero.toString()



        //Menu
        binding.menuImageView2.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.menuImageView2)
            popupMenu.inflate(R.menu.popup_team_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editarEquipoOption -> {
                        val editarJugadorActivityIntent = Intent(context, EditarJugadorActivity::class.java)
                        editarJugadorActivityIntent.putExtra("jugador", player)
                        context.startActivity(editarJugadorActivityIntent)

                        true
                    }

                    R.id.borrarEquipoOption -> {
                        Toast.makeText(context, "Has borrado a " + player.nombre, Toast.LENGTH_SHORT)
                            .show()
                        val teamref = db.collection("teams").document(player.equipoId)
                        val playerRef = teamref.collection("players").document(player.id)

                        playerRef.delete()
                            .addOnSuccessListener {
                                players.removeAt(position)
                                notifyDataSetChanged()
                                updateDataSet(players)
                            }
                            .addOnFailureListener{
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()

                            }

                        true
                    }else -> {
                        false
                    }
                }
            }
        }
        binding.contenedorLayout.setOnClickListener {
            val mostrarJugadorActivityIntent = Intent(context, MostrarJugadorActivity::class.java)
            mostrarJugadorActivityIntent.putExtra("jugador", player)
            context.startActivity(mostrarJugadorActivityIntent)
        }
    }


    private fun updateDataSet(list: ArrayList<JugadorModel>) {
        players = list
        notifyItemRangeInserted(0, itemCount)
    }


}