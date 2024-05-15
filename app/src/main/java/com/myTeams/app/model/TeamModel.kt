package com.myTeams.app.model

import java.io.Serializable

data class TeamModel(
    var id: String ="",
    var name: String = "",
    var kitColor: String ="",
    val user: String ="",
    var gamesPlayed: Int =0,
    var gamesWon: Int=0,
    var derrotas: Int=0,
    var empates: Int=0,
    var goalsScored: Int=0,
    var golesEncajados: Int=0
): Serializable

fun TeamModel.toMap() = mapOf<String, Any>(
    "id" to id,
    "name" to name,
    "kitColor" to kitColor,
    "user" to user
)
