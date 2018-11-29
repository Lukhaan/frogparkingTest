package com.frogparking.lucan.frogparkingtestkotlin.model

import java.util.ArrayList

class Node(val name: String) : INode {
    val routes = ArrayList<Route>()

    override fun doesRouteExist(destination: Node): Route? {
        routes.forEach {
            if (it.destination == destination) {
                return it
            }
        }
        return null
    }

    override fun getRoute(origin: Node, destination: Node): Route? {
        routes.forEach {
            if (it.origin == origin && it.destination == destination) {
                return it
            }
        }
        return null
    }

    override fun addRoute(route: Route) {
        this.routes.add(route)
    }
}
