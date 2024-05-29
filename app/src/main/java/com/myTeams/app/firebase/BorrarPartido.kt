package com.myTeams.app.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.myTeams.app.model.JugadorEnPartido
import com.myTeams.app.model.JugadorModel
import com.myTeams.app.model.PartidoModel
import com.myTeams.app.model.TeamModel

class BorrarPartido {


    suspend fun borrar(partido: PartidoModel, equipo: TeamModel, db: FirebaseFirestore) :Boolean{
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
                        //Toast.makeText(context, "Error" , Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                //Toast.makeText(context, "Error" , Toast.LENGTH_SHORT).show()
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
                //Toast.makeText(context, "Vamos a borrar a la jornada ${partido.numeroJornada}", Toast.LENGTH_SHORT).show()
                db.collection("partidos").document(partido.equipoId)
                    .collection("liga").document(partido.id).delete()

            }

        return true
    }
}