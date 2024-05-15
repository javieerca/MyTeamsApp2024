package com.myTeams.app.model

import java.io.Serializable

data class PlayerModel(
    var id: String ="",
    val name: String = "",
    val teamId: String="",
    val position: String ="",
    val positionId: Int=0,      //Portero=0, Defensa=1, Centrocampista=2, Delantero=3
    val number: Int = 0,
    val gamesPlayed: Int =0,
    val goalsScored: Int=0,
    val minutesPlayed: Int=0,
    val gamesStarted: Int=0,
    val partidosConvocado: Int=0
): Serializable
