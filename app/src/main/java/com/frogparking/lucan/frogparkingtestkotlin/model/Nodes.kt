package com.frogparking.lucan.frogparkingtestkotlin.model

import android.app.Application
import java.util.ArrayList

class Nodes : INodes {
    init {
        val a = Node("A")
        val b = Node("B")
        val c = Node("C")
        val d = Node("D")
        val e = Node("E")

        //Routes From A
        a.addRoute(Route(a, b, 5))
        a.addRoute(Route(a, d, 5))
        a.addRoute(Route(a, e, 7))

        //Routes From B
        b.addRoute(Route(b, c, 4))

        //Routes From C
        c.addRoute(Route(c, d, 8))
        c.addRoute(Route(c, e, 2))

        //Routes From D
        d.addRoute(Route(d, c, 8))
        d.addRoute(Route(d, e, 6))

        //Routes From E
        e.addRoute(Route(e, b, 3))

        Nodes.add(a)
        Nodes.add(b)
        Nodes.add(c)
        Nodes.add(d)
        Nodes.add(e)
    }

    override fun getNode(node: String): Node {
        Nodes.forEach {
            if (it.name == node) {
                return it
            }
        }
        return Node("Error")
    }

    companion object {
        private val Nodes = ArrayList<Node>()
    }
}

