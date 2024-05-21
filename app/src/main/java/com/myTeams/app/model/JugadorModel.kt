package com.myTeams.app.model

import java.io.Serializable

data class JugadorModel(
    var id: String ="",
    val nombre: String = "",
    val equipoId: String="",
    val posicion: String ="",
    val posicionId: Int=0,      //Portero=0, Defensa=1, Centrocampista=2, Delantero=3
    val numero: Int = 0,
    val partidosJugados: Int =0,
    var golesMarcados: Int=0,
    val minutosJugados: Int=0,
    val partidosTitular: Int=0,
    val partidosConvocado: Int=0,
    val vecesCambiado: Int = 0,
    var tarjetasAmarillas: Int=0,
    var tarjetasRojas: Int=0
): Serializable
