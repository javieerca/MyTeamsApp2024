package com.myTeams.app.model

data class JugadorEnPartido(
    val jugador: JugadorModel = JugadorModel(),
    val minutoEntra: Int=0,
    val minutoSale: Int = 0,
    val jugoMinutos: Int = 0
)

fun JugadorEnPartido.minutosJugados(): Int {
    return minutoSale - minutoEntra
}

