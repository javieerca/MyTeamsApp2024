package com.myTeams.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myTeams.app.R
import com.myTeams.app.databinding.TarjetasLayoutBinding
import com.myTeams.app.model.EventoModel
import com.myTeams.app.model.PartidoModel

class TarjetaAdapter (private var tarjetas: ArrayList<EventoModel>, val context: Context, private val partido: PartidoModel):

    RecyclerView.Adapter<TarjetaAdapter.ItemViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(layoutInflater.inflate(R.layout.tarjetas_layout, null))
    }

    override fun getItemCount(): Int {
        return tarjetas.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val evento = tarjetas[position]
        //holder.bind(item)
        val binding = TarjetasLayoutBinding.bind(holder.itemView)

        holder.itemView.setOnClickListener {
            //intent de TeamActivity
        }

        binding.nameTextView2.text = evento.jugadoresImplicados[0].nombre
        binding.dorsalTextView2.text = evento.jugadoresImplicados[0].numero.toString()
        binding.minutoTextView.text = evento.minuto.toString()
        if(evento.tipoEventoId == 1){
            binding.tarjetaRojaimageView.visibility = View.INVISIBLE
        }
        else{ //seria 2
            binding.tarjetaAmarillaimageView.visibility = View.INVISIBLE
            binding.tarjetaRojaimageView.visibility = View.VISIBLE

        }

        binding.imageView7.setOnClickListener{
            Toast.makeText(context, "Vamos a borrar la tarjeta del minuto " + evento.minuto, Toast.LENGTH_SHORT)
                .show()

            partido.goles.removeAt(position)
            notifyDataSetChanged()
            updateDataSet(tarjetas)
        }

    }

    private fun updateDataSet(list: ArrayList<EventoModel>) {
        tarjetas = list
        notifyItemRangeInserted(0, itemCount)
    }





}