package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.R
import com.myTeams.app.databinding.SustitucionesEnPartidoLayoutBinding
import com.myTeams.app.model.EventoModel

class SustitucionEnPartidoAdapter (private var sustituciones: List<EventoModel>, val context: Context):

    RecyclerView.Adapter<SustitucionEnPartidoAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.sustituciones_en_partido_layout, null))
    }

    override fun getItemCount(): Int {
        return sustituciones.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val evento = sustituciones[position]
        val binding = SustitucionesEnPartidoLayoutBinding.bind(holder.itemView)

        val sale = evento.jugadoresImplicados[0]
        val entra = evento.jugadoresImplicados[1]

        binding.saleNombre.text = "${sale.numero}. ${sale.nombre}"
        binding.entraNombre.text = "${entra.numero}. ${entra.nombre}"

    }

}