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
import com.google.firebase.firestore.toObject
import com.myTeams.app.AddPartidoActivity
import com.myTeams.app.EditarPartidoActivity
import com.myTeams.app.MostrarPartidoActivity
import com.myTeams.app.R
import com.myTeams.app.databinding.PartidoLayoutBinding
import com.myTeams.app.model.JugadorEnPartido
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class PartidoAdapter(private var partidos: ArrayList<PartidoModel>, val context: Context, private val db: FirebaseFirestore, val equipo: TeamModel):

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
                        val editarPartidoActivityIntent = Intent(context, EditarPartidoActivity::class.java)

                        editarPartidoActivityIntent.putExtra("equipo", equipo)
                        editarPartidoActivityIntent.putExtra("jornadaActual", partido.numeroJornada)
                        editarPartidoActivityIntent.putExtra("partido", partido)
                        context.startActivity(editarPartidoActivityIntent)


                        true
                    }
                    R.id.borrarEquipoOption ->{

                        val jugadoresRef =
                            db.collection("teams").document(partido.equipoId).collection("players")

                        //buscar equipoDB
                        var equipoDb = TeamModel()
                        db.collection("teams").document(partido.equipoId)
                            .get()
                            .addOnSuccessListener {document ->
                                equipoDb = document.toObject<TeamModel>()!!
                                equipoDb.id = document.id

                                db.collection("teams").document(partido.equipoId).update(
                                    hashMapOf<String, Any>(
                                        "golesMarcados" to equipoDb.golesMarcados - partido.golesMarcados,
                                        "golesEncajados" to equipoDb.golesEncajados - partido.golesEncajados,
                                        "partidosJugados" to equipoDb.partidosJugados - 1
                                    )
                                )
                                    .addOnSuccessListener {
                                        //restar victoria, derrota o empate
                                        if(partido.golesMarcados > partido.golesEncajados){
                                            db.collection("teams").document(partido.equipoId).update(
                                                hashMapOf<String, Any>(
                                                    "victorias" to equipoDb.victorias - 1
                                                )
                                            )
                                        }else if(partido.golesMarcados < partido.golesEncajados){
                                            db.collection("teams").document(partido.equipoId).update(
                                                hashMapOf<String, Any>(
                                                    "derrotas" to equipoDb.derrotas - 1
                                                )
                                            )
                                        }else{
                                            db.collection("teams").document(partido.equipoId).update(
                                                hashMapOf<String, Any>(
                                                    "empates" to equipoDb.empates - 1
                                                )
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error" , Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error" , Toast.LENGTH_SHORT).show()
                            }


                        //borrar goleadores
                        for(gol in partido.goles){
                            //buscar jugador
                            var goleador= JugadorModel()
                            jugadoresRef.document(gol.jugadoresImplicados[0].id)
                                .get()
                                .addOnSuccessListener { document ->
                                    if(document != null){
                                        goleador = document.toObject(JugadorModel::class.java)!!
                                        goleador.id = document.id

                                        //quitar gol al goleador
                                        jugadoresRef.document(goleador.id).update(
                                            hashMapOf<String,Any>(
                                                "golesMarcados" to goleador.golesMarcados - 1
                                            )
                                        )
                                    }
                                }
                        }


                        //quitar veces cambiado
                        for(cambio in partido.sustituciones){
                            //buscar jugador
                            var cambiado= JugadorModel()
                            jugadoresRef.document(cambio.jugadoresImplicados[1].id)
                                .get()
                                .addOnSuccessListener { document ->
                                    cambiado = document.toObject<JugadorModel>()!!
                                    cambiado.id = document.id

                                    //quitar cambio al cambiado
                                    jugadoresRef.document(cambiado.id).update(
                                        hashMapOf<String,Any>(
                                            "vecesCambiado" to cambiado.vecesCambiado - 1
                                        )
                                    )
                                }

                        }

                        //quitar amonestaciones
                        for(tarjeta in partido.amonestaciones) {
                            //buscar jugador
                            var amonestado = JugadorModel()
                            jugadoresRef.document(tarjeta.jugadoresImplicados[0].id)
                                .get()
                                .addOnSuccessListener { document ->
                                    amonestado = document.toObject<JugadorModel>()!!
                                    amonestado.id = document.id

                                    if (tarjeta.tipoEventoId == 1) { //tarjeta amarilla
                                        //quitar amarilla al jugador
                                        jugadoresRef.document(amonestado.id).update(
                                            hashMapOf<String, Any>(
                                                "tarjetasAmarillas" to amonestado.tarjetasAmarillas - 1
                                            )
                                        )

                                    } else {
                                        //quitar roja al jugador
                                        jugadoresRef.document(amonestado.id).update(
                                            hashMapOf<String, Any>(
                                                "tarjetasRojas" to amonestado.tarjetasRojas - 1
                                            )
                                        )
                                    }
                                }
                        }

                        var jugadoresMinutosJugados: ArrayList<JugadorEnPartido> = ArrayList()
                        //quitar minutos
                        //primero busca minutos jugados
                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("minutosJugados")
                            .get()
                            .addOnSuccessListener { documents->
                                for (documento in documents){
                                    val jugadorMinutos = JugadorEnPartido(jugador= JugadorModel(id= documento.getString("jugador")!!),
                                        jugoMinutos = documento.get("minutos")!!.toString().toInt())
                                    jugadoresMinutosJugados.add(jugadorMinutos)
                                    documento.reference.delete()
                                }

                                //restar minutos
                                for(jugadorMinutos in jugadoresMinutosJugados){
                                    //busca jugador
                                    var jugador= JugadorModel()
                                    jugadoresRef.document(jugadorMinutos.jugador.id)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            jugador = document.toObject<JugadorModel>()!!
                                            jugador.id = document.id

                                            //resta minutos
                                            jugadoresRef.document(jugador.id).update(
                                                hashMapOf<String, Any>(
                                                    "minutosJugados" to jugador.minutosJugados - jugadorMinutos.jugoMinutos,
                                                    "partidosJugados" to jugador.partidosJugados - 1
                                                )
                                            )
                                        }
                                }
                                //titularidades
                            }


                        var titularesId: ArrayList<String> = ArrayList()
                        //suplentes
                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("titulares")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    val titularId = documento.getString("id")!!
                                    titularesId.add(titularId)
                                    documento.reference.delete()
                                }

                                for (id in titularesId){
                                    var jugador = JugadorModel()
                                    jugadoresRef.document(id)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            jugador = document.toObject<JugadorModel>()!!
                                            jugador.id = document.id
                                            partido.titulares.add(jugador)

                                            //resta convocatorias
                                            jugadoresRef.document(jugador.id).update(
                                                hashMapOf<String, Any>(
                                                    "partidosTitular" to jugador.partidosTitular - 1,
                                                    "partidosConvocado" to jugador.partidosConvocado - 1
                                                )
                                            )
                                        }
                                }

                            }

                        var suplentesId: ArrayList<String> = ArrayList()
                        //suplentes
                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("suplentes")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    val suplenteId = documento.getString("id")!!
                                    suplentesId.add(suplenteId)
                                    documento.reference.delete()
                                }


                                for (id in suplentesId){
                                    var jugador = JugadorModel()
                                    jugadoresRef.document(id)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            jugador = document.toObject<JugadorModel>()!!
                                            jugador.id = document.id
                                            partido.suplentes.add(jugador)

                                            //resta convocatorias
                                            jugadoresRef.document(jugador.id).update(
                                                hashMapOf<String, Any>(
                                                    "partidosConvocado" to jugador.partidosConvocado - 1
                                                )
                                            )
                                        }
                                }

                            }

                        //borar subcolecciones

                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("goles")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    documento.reference.delete()
                                }
                            }

                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("sustituciones")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    documento.reference.delete()
                                }
                            }

                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("amonestaciones")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    documento.reference.delete()
                                }
                            }
                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .collection("minutosJugados")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (documento in documents) {
                                    documento.reference.delete()
                                }
                            }

                        //borrar partido
                        db.collection("partidos").document(partido.equipoId).collection("liga").document(partido.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Vamos a borrar a la jornada ${partido.numeroJornada}", Toast.LENGTH_SHORT).show()
                                db.collection("partidos").document(partido.equipoId)
                                    .collection("liga").document(partido.id).delete()



                                partidos.removeAt(position)
                                notifyDataSetChanged()
                                updateDataSet(partidos)

                            }

                        true
                    }
                    else ->{
                        false
                    }
                }
            }
        }
        binding.contenedorLayout.setOnClickListener{
            val mostrarPartidoActivityIntent = Intent(context, MostrarPartidoActivity::class.java)
            mostrarPartidoActivityIntent.putExtra("equipo", equipo)
            mostrarPartidoActivityIntent.putExtra("partido", partidos[position])
            context.startActivity(mostrarPartidoActivityIntent)
        }
    }



    private fun updateDataSet(list: ArrayList<PartidoModel>){
        partidos = list
        notifyItemRangeInserted(0, itemCount)
    }

}


