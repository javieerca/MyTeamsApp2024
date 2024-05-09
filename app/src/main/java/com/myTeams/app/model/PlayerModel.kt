package com.myTeams.app.model

data class PlayerModel(
    var id: String ="",
    val name: String = "",
    val teamId: String="",
    val position: String ="",
    val number: Int = 0,
    val gamesPlayed: Int =0,
    val goalsScored: Int=0
)
