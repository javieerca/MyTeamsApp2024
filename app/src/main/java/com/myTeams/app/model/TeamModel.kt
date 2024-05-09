package com.myTeams.app.model

import java.io.Serializable

data class TeamModel(
    var id: String ="",
    val name: String = "",
    val kitColor: String ="",
    val user: String ="",
    val gamesPlayed: Int =0,
    val gamesWon: Int=0,
    val goalsScored: Int=0
): Serializable

fun TeamModel.toMap() = mapOf<String, Any>(
    "id" to id,
    "name" to name,
    "kitColor" to kitColor,
    "user" to user
)
