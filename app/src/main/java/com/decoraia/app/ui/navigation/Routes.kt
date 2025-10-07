package com.decoraia.app.ui.navigation

sealed class Route(val route: String) {
    data object Inicio   : Route("inicio")
    data object Login    : Route("login")
    data object Registro : Route("registro")
}
