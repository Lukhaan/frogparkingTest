package com.frogparking.lucan.frogparkingtestkotlin.model

interface INode {

    fun doesRouteExist(destination: Node): Route? {return null}
    fun getRoute(origin: Node, destination: Node): Route? {return null}
    fun addRoute(route: Route) {}

}