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
import com.myTeams.app.databinding.PartidoLayoutBinding
import com.myTeams.app.model.PartidoModel

class PartidoAdapter(private var partidos: ArrayList<PartidoModel>, val context: Context, private val db: FirebaseFirestore):

    RecyclerView.Adapter<PartidoAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.partido_layout, null))
    }

    override fun getItemCount(): Int {
        return partidos.size
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val partido = partidos[position]

        val binding = PartidoLayoutBinding.bind(holder.itemView)
        binding.jornadaTextView.text = "Jornada ${partido.numeroJornada}"

        if (partido.local) {
            binding.localTextView.text = partido.equipoNombre
            binding.golesLocalTextView.text = partido.golesMarcados.toString()

            binding.visitanteTextView.text = partido.rival
            binding.golesvisitanteTextView2.text = partido.golesEncajados.toString()
        }
        if(!partido.local){
            binding.localTextView.text = partido.rival
            binding.golesLocalTextView.text = partido.golesEncajados.toString()

            binding.visitanteTextView.text = partido.equipoNombre
            binding.golesvisitanteTextView2.text = partido.golesMarcados.toString()
        }

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
                        Toast.makeText(context, "Vamos a borrar a la jornada ${partido.numeroJornada}", Toast.LENGTH_SHORT).show()
                        db.collection("partidos").document(partido.equipoId)
                            .collection("liga").document(partido.id).delete()

                        //Editar equipos y jugadores


                        partidos.removeAt(position)
                        notifyDataSetChanged()
                        updateDataSet(partidos)
                        true
                    }
                    else ->{
                        false
                    }
                }
            }
        }
    }

    private fun updateDataSet(list: ArrayList<PartidoModel>){
        partidos = list
        notifyItemRangeInserted(0, itemCount)
    }

}


