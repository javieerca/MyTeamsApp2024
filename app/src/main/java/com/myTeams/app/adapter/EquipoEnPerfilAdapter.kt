package com.myTeams.app.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myTeams.app.R
import com.myTeams.app.TeamActivity
import com.myTeams.app.databinding.EquipoEnPerfilLayoutBinding
import com.myTeams.app.model.TeamModel

class EquipoEnPerfilAdapter(private var teams: ArrayList<TeamModel>, val context: Context, private val db: FirebaseFirestore):

    RecyclerView.Adapter<EquipoEnPerfilAdapter.ItemViewHolder>(){
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder{
        return ItemViewHolder(layoutInflater.inflate(R.layout.equipo_en_perfil_layout, null))
    }

    override fun getItemCount(): Int {
        return teams.size
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val equipo = teams[position]
        //holder.bind(item)
        val binding= EquipoEnPerfilLayoutBinding.bind(holder.itemView)

        binding.nombreEquipotextView.text = equipo.nombre
        binding.valorVictorias.text = equipo.victorias.toString()
        binding.valorEmpates.text = equipo.empates.toString()
        binding.valorDerrotas.text = equipo.derrotas.toString()
        binding.valorGoles.text = equipo.golesMarcados.toString()
        val puntos = (equipo.victorias * 3) + (equipo.empates)
        binding.valorPuntos.text = puntos.toString()

        binding.contenedorLayout.setOnClickListener{
            val teamActivityIntent = Intent(context, TeamActivity::class.java)

            teamActivityIntent.putExtra("idEquipo", equipo.id)
            context.startActivity(teamActivityIntent)
        }

    }

    private fun updateDataSet(list: ArrayList<TeamModel>){
        teams = list
        notifyItemRangeInserted(0, itemCount)
    }


}