package com.myTeams.app.model

import java.io.Serializable

data class TeamModel(
    var id: String ="",
    var nombre: String = "",
    var colorEquipacion: String ="",
    val usuario: String ="",
    var partidosJugados: Int =0,
    var victorias: Int=0,
    var derrotas: Int=0,
    var empates: Int=0,
    var golesMarcados: Int=0,
    var golesEncajados: Int=0,
    var jugadores: ArrayList<JugadorModel> = ArrayList()
): Serializable

fun TeamModel.toMap() = mapOf<String, Any>(
    "id" to id,
    "nombre" to nombre,
    "colorEquipacion" to colorEquipacion,
    "usuario" to usuario
)
