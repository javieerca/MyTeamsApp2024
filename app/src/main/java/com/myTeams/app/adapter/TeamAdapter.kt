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
import com.myTeams.app.databinding.TeamLayoutBinding
import com.myTeams.app.R
import com.myTeams.app.TeamActivity
import com.myTeams.app.model.TeamModel

class TeamAdapter(private var teams: ArrayList<TeamModel>, val context:Context, private val db: FirebaseFirestore):

    RecyclerView.Adapter<TeamAdapter.ItemViewHolder>(){
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder{
        return ItemViewHolder(layoutInflater.inflate(R.layout.team_layout, null))
    }

    override fun getItemCount(): Int {
        return teams.size
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val team = teams[position]
        //holder.bind(item)
        val binding= TeamLayoutBinding.bind(holder.itemView)

        holder.itemView.setOnClickListener{
            //intent de TeamActivity
        }

        binding.nombreTextView.text = team.name
        binding.numberGamesTextView.text = team.gamesPlayed.toString()
        binding.numberWinstextView.text = team.gamesWon.toString()
        binding.numberGoalstextView.text = team.goalsScored.toString()

        //Menu
        binding.menuImageView.setOnClickListener{
            val popupMenu = PopupMenu(context,binding.menuImageView)
            popupMenu.inflate(R.menu.popup_team_menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId){
                    R.id.editarEquipoOption -> {
                        Toast.makeText(context,"Opción 1", Toast.LENGTH_SHORT).show()

                        /*
                        abrir editar Team
                         */

                        true
                    }
                    R.id.borrarEquipoOption ->{
                        Toast.makeText(context, "Vamos a borrar a "+team.name, Toast.LENGTH_SHORT).show()
                        db.collection("teams").document(team.id).delete()
                        db.collection("partidos").document(team.id).delete()

                        teams.removeAt(position)
                        notifyDataSetChanged()
                        updateDataSet(teams)


                        true
                    }
                    else ->{
                        false
                    }
                }
            }
        }
        binding.contenedorLayout.setOnClickListener{
            val teamActivityIntent = Intent(context, TeamActivity::class.java)

            teamActivityIntent.putExtra("idEquipo", team.id)
            context.startActivity(teamActivityIntent)
        }
    }



    private fun updateDataSet(list: ArrayList<TeamModel>){
        teams = list
        notifyItemRangeInserted(0, itemCount)
    }


}