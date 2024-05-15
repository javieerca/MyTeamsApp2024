package com.myTeams.app.model

import java.io.Serializable


data class PartidoModel(
    var id: String ="",
    val rival: String = "",
    var equipoId: String="",
    val equipoNombre: String="",
    val local: Boolean = true,
    val golesEncajados: Int = 0,
    var golesMarcados: Int = 0,
    val numeroJornada: Int =0,
    val observaciones: String = "",
    val titularesId: ArrayList<String> = ArrayList(),
    val suplentesId: ArrayList<String> = ArrayList(),
    val goles: ArrayList<EventoModel> = ArrayList(),
    val amonestaciones: ArrayList<EventoModel> = ArrayList(),
    val sustituciones: ArrayList<EventoModel> = ArrayList()
): Serializable



