package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.R
import com.myTeams.app.databinding.GolLayoutBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel

class GolAdapter (private var goles: ArrayList<EventoModel>, val context: Context, private val partido: PartidoModel):

    RecyclerView.Adapter<GolAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.gol_layout, null))
    }

    override fun getItemCount(): Int {
        return goles.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val evento = goles[position]
        //holder.bind(item)
        val binding = GolLayoutBinding.bind(holder.itemView)

        holder.itemView.setOnClickListener {
            //intent de TeamActivity
        }

        binding.nameTextView.text = evento.jugadoresImplicados[0].name
        binding.numberTextView.text = evento.jugadoresImplicados[0].number.toString()
        binding.minutoTextView2.text = evento.minuto.toString()

        binding.imageView.setOnClickListener{
            Toast.makeText(context, "Vamos a borrar el gol del minuto " + evento.minuto, Toast.LENGTH_SHORT)
                .show()

            partido.goles.removeAt(position)
            notifyDataSetChanged()
            updateDataSet(goles)
        }

        binding.contenedorLayout.setOnClickListener {
           //nada
        }

    }
    private fun updateDataSet(list: ArrayList<EventoModel>) {
        goles = list
        notifyItemRangeInserted(0, itemCount)
    }

}

