package com.frogparking.lucan.frogparkingtestkotlin.model

data

class Route(var origin: Node?, val destination: Node, var distance: Int?) {
    override fun toString(): String {
        return "Origin: " + origin!!.name + ", Destination: " + destination.name + ", Distance: " + distance
    }
}