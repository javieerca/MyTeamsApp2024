package com.myTeams.app.model

import java.io.Serializable

data class EventoModel(
    val id: String ="",
    val tipoEventoId: Int=0,        //0 = gol, 1= tarjeta amarilla
    val tipoEventoNombre: String="",
    val minuto: Int = 0,
    val jugadoresImplicados: ArrayList<PlayerModel>
) : Serializable

