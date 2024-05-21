package com.myTeams.app.model

import java.io.Serializable

data class EventoModel(
    var id: String ="",
    val tipoEventoId: Int=0,        //0 = gol, 1= tarjeta amarilla, 2= tarjeta roja, 3= cambio
    val tipoEventoNombre: String="",
    val minuto: Int = 0,
    val jugadoresImplicados: ArrayList<JugadorModel>
) : Serializable

