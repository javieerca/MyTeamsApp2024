package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.R
import com.myTeams.app.databinding.PlayerLayoutBinding
import com.myTeams.app.model.PlayerModel


class PlayerAdapter (private var players: ArrayList<PlayerModel>, val context: Context, private val db: FirebaseFirestore):

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
            //intent de TeamActivity
        }

        binding.nameTextView.text = player.name
        binding.numberTextView.text = player.number.toString()

        //Menu
        binding.menuImageView2.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.menuImageView2)
            popupMenu.inflate(R.menu.popup_team_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editarEquipoOption -> {
                        Toast.makeText(context, "Opción 1", Toast.LENGTH_SHORT).show()
                        /*
                        abrir editar Team
                         */
                        true
                    }

                    R.id.borrarEquipoOption -> {
                        Toast.makeText(context, "Vamos a borrar a " + player.name, Toast.LENGTH_SHORT)
                            .show()
                        val teamref = db.collection("teams").document(player.teamId)
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
            Toast.makeText(context, "Abrir jugador", Toast.LENGTH_SHORT).show()
            /*
            val teamActivityIntent = Intent(context, TeamActivity::class.java)

            teamActivityIntent.putExtra("idEquipo", team.id)
            context.startActivity(teamActivityIntent)
            */

        }
    }


    private fun updateDataSet(list: ArrayList<PlayerModel>) {
        players = list
        notifyItemRangeInserted(0, itemCount)
    }


}