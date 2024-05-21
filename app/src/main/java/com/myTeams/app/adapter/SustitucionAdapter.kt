package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.R
import com.myTeams.app.databinding.SustitucionLayoutBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel

class SustitucionAdapter (private var sustituciones: ArrayList<EventoModel>, val context: Context, private val partido: PartidoModel):
    RecyclerView.Adapter<SustitucionAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.sustitucion_layout, null))
    }

    override fun getItemCount(): Int {
        return sustituciones.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val evento = sustituciones[position]
        //holder.bind(item)
        val binding = SustitucionLayoutBinding.bind(holder.itemView)

        holder.itemView.setOnClickListener {
            //intent de TeamActivity
        }

        binding.sustitutoTextView.text = evento.jugadoresImplicados[0].nombre
        binding.dorsalentraTextView.text = evento.jugadoresImplicados[0].numero.toString()

        binding.sustituidoTextView.text = evento.jugadoresImplicados[1].nombre
        binding.dorsalsaleTextView.text = evento.jugadoresImplicados[1].numero.toString()

        binding.minutoTextView3.text = evento.minuto.toString()


        binding.imageView2.setOnClickListener{
            Toast.makeText(context, "Vamos a borrar el cambio del minuto " + evento.minuto, Toast.LENGTH_SHORT)
                .show()

            partido.sustituciones.removeAt(position)
            notifyDataSetChanged()
            updateDataSet(sustituciones)
        }

    }

    private fun updateDataSet(list: ArrayList<EventoModel>) {
        sustituciones = list
        notifyItemRangeInserted(0, itemCount)
    }
}