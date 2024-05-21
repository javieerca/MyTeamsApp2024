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
    var titulares: ArrayList<JugadorModel> = ArrayList(),
    var suplentes: ArrayList<JugadorModel> = ArrayList(),
    var convocados: ArrayList<JugadorModel> = ArrayList(),
    var goles: ArrayList<EventoModel> = ArrayList(),
    var amonestaciones: ArrayList<EventoModel> = ArrayList(),
    var sustituciones: ArrayList<EventoModel> = ArrayList()
): Serializable



