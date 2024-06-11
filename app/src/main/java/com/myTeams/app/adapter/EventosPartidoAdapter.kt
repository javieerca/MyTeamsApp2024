package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.R
import com.myTeams.app.databinding.EventoEnPartidoLayoutBinding
import com.myTeams.app.model.EventoModel

class EventosPartidoAdapter(private var eventos: List<EventoModel>, val context: Context):

    RecyclerView.Adapter<EventosPartidoAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.evento_en_partido_layout, null))
    }

    override fun getItemCount(): Int {
        return eventos.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val evento = eventos[position]
        val binding = EventoEnPartidoLayoutBinding.bind(holder.itemView)

        when (evento.tipoEventoId) {
            0 -> {   //gol
                binding.imageView3.visibility = View.VISIBLE
                binding.amarillaimageView.visibility = View.INVISIBLE
                binding.rojaimageView.visibility = View.INVISIBLE
            }
            1 -> {  //amarilla
                binding.imageView3.visibility = View.INVISIBLE
                binding.amarillaimageView.visibility = View.VISIBLE
                binding.rojaimageView.visibility = View.INVISIBLE
            }
            2 -> {  //roja
                binding.imageView3.visibility = View.INVISIBLE
                binding.amarillaimageView.visibility = View.INVISIBLE
                binding.rojaimageView.visibility = View.VISIBLE
            }
        }
        binding.dorsaltextView.text = evento.jugadoresImplicados[0].numero.toString()
        binding.nombreGoleadortextView.text = evento.jugadoresImplicados[0].nombre.toString()
        binding.minutoContadortextView.text = evento.minuto.toString()
    }

}