package com.frogparking.lucan.frogparkingtestkotlin.model

import java.util.ArrayList

class Node(val name: String) {
    val routes = ArrayList<Route>()

    fun doesRouteExist(destination: Node): Route? {
        routes.forEach {
            if (it.destination == destination) {
                return it
            }
        }
        return null
    }

    fun getRoute(origin: Node, destination: Node): Route? {
        routes.forEach {
            if (it.origin == origin && it.destination == destination) {
                return it
            }
        }
        return null
    }

    fun addRoute(route: Route) {
        this.routes.add(route)
    }
}
