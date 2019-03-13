package com.frogparking.lucan.frogparkingtestkotlin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.frogparking.lucan.frogparkingtestkotlin.R.layout.activity_main
import com.frogparking.lucan.frogparkingtestkotlin.model.Filters
import com.frogparking.lucan.frogparkingtestkotlin.model.Node
import com.frogparking.lucan.frogparkingtestkotlin.model.Nodes
import com.frogparking.lucan.frogparkingtestkotlin.model.Route
import kotlinx.android.synthetic.main.activity_main.*

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        //Setup some input filters as well as enable scrolling for the results text view
        txtStartNode.filters = arrayOf(InputFilter.AllCaps())
        txtEndNode.filters = arrayOf(InputFilter.AllCaps())

        txtGetDistance.filters = arrayOf(InputFilter.AllCaps())
        txtResults.movementMethod = ScrollingMovementMethod()
    }

    //On Clicks
    //These pull data from the input text boxes using setFilters function and display results in the text view.
    fun findShortestRouteClick(view: View) {
        //Sets filters then finds all available routes and searches for the one with the smallest totalDistance.
        val shortestRoute = findShortestRoute(findAllRoutes(setFilters()))

        //If the path returned isn't null build a string to display otherwise output no routes found.
        txtResults.text = shortestRoute?.toString() ?: "No routes found"

        hideKeyboardFrom(this, view)
    }

    fun findAllRoutesClick(view: View) {
        //sets filters and passes them to findAllRoutes and put the results into paths array.
        val paths = findAllRoutes(setFilters())

        //Build a string
        val resultToDisplay = StringBuilder()

        paths.forEach { resultToDisplay.append(it.toString()) }

        txtResults.text = if(paths.size == 0){
            "No routes found."
        } else {
            resultToDisplay
        }

        hideKeyboardFrom(this, view)
    }

    fun findRouteDistanceClick(view: View) {
        (findViewById<View>(R.id.txtResults) as TextView).text = findRouteDistance((findViewById<View>(R.id.txtGetDistance) as EditText).text.toString())
        hideKeyboardFrom(this, view)
    }

    //setFilters pulls data from text boxes
    private fun setFilters(): Filters {
        val filters = Filters()
        filters.startNode = txtStartNode.text.toString()
        filters.endNode = txtEndNode.text.toString()
        filters.maxStops = txtMaxStops.text.toString()
        filters.exactStops = txtExactStops.text.toString()

        return filters
    }

    //Logic
    private fun findShortestRoute(paths: ArrayList<Path>): Path? {
        //Check paths and find path with minimum total distance.
        return if (paths.size > 0) {
            var currentMin = paths[0]
            for (i in paths.indices) {
                if (paths[i].Distance!! < currentMin.Distance!!) {
                    currentMin = paths[i]
                }
            }
            currentMin
        } else {
            null
        }
    }

    private fun findAllRoutes(filters: Filters): ArrayList<Path> {
        //Create array to hold results
        val resultToDisplay = ArrayList<Path>()

        //2D array to hold filtered rawData
        val filteredData = filterData(filters)

        //Build paths to add to resultToDisplay Path array
        for (i in filteredData.indices) {
            var totalDistance = 0
            var totalStops = 0

            //Find total amount of stops and total Distance
            for (j in 0 until filteredData[i].size) {
                totalDistance += filteredData[i][j].distance!!
                totalStops++
            }
            resultToDisplay.add(Path(getRouteString(filteredData[i]), totalDistance, totalStops))
        }

        return resultToDisplay
    }

    private fun findRouteDistance(path: String): String {
        //Split string format ABC to [A,B,C]
        val nodeNames = path.split("(?!^)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        //Sets totalDistance counter
        //Bool is true unless a route is invalid eg AC
        var totalDistance: Int? = 0
        var validRoute: Boolean? = true

        for (i in 0 until nodeNames.size - 1) {
            //Node contains Name and route array eg [Origin A, Destination B, Distance 5]
            val origin = Nodes.getNode(nodeNames[i])
            val destination = Nodes.getNode(nodeNames[i + 1])

            if (origin.doesRouteExist(destination) != null) {
                totalDistance = totalDistance!! +
                        Nodes.getNode(nodeNames[i]).getRoute(origin, destination)!!.distance!!
            } else {
                validRoute = false
            }
        }
        return if (validRoute!! && totalDistance != 0) {
            "Total route distance is: " + Integer.toString(totalDistance!!)
        } else {
            "Route is not valid."
        }
    }

    private fun getRouteString(route: ArrayList<Route>): String {
        //Converts a route array to a string format A -> B -> C for easy user readability
        val routeString = StringBuilder()

        routeString.append(route[0].origin!!.name)
        for (i in route.indices) {
            routeString.append(" -> ")
            routeString.append(route[i].destination.name)
        }

        return routeString.toString()
    }

    private fun getAllRoutes(currentPath: ArrayList<Route>, currentNode: Node, finalNode: Node): ArrayList<ArrayList<Route>> {
        //Recursive function that finds all routes from X to Y
        /*
        A -> C

        A getRoutes returns B, D, E.

        call on B -> C, C is final node so return currentPath back up tree.
        call on D returns E, C

        etc

        function will break and return nothing if it comes across a node it's already been through to stop circular error.
        otherwise returns full path when it gets to the finalNode.
         */


        val getAvailablePaths = ArrayList<ArrayList<Route>>()

        val availableRoutes = getAvailableDestinations(currentNode, currentPath)

        for (i in availableRoutes.indices) {
            currentPath.add(availableRoutes[i])
            if (availableRoutes[i].destination === finalNode) {
                getAvailablePaths.add(currentPath.clone() as ArrayList<Route>)
            } else {
                getAvailablePaths.addAll(
                    getAllRoutes(
                        currentPath.clone() as ArrayList<Route>,
                        availableRoutes[i].destination,
                        finalNode
                    )
                )
            }
            currentPath.removeAt(currentPath.size - 1)
        }
        return getAvailablePaths
    }

    private fun getAvailableDestinations(node: Node, routes: ArrayList<Route>): ArrayList<Route> {
        //Takes a Node and a path taken and works out what nodes are valid to travel to and returns and array of routes to get there

        val totalRoutes = node.routes
        val availableRoutes = ArrayList<Route>()
        val visitedNodes = ArrayList<Node>()

        for (i in routes.indices) {
            visitedNodes.add(routes[i].destination)
        }

        for (j in totalRoutes.indices) {
            if (!visitedNodes.contains(totalRoutes[j].destination)) {
                availableRoutes.add(totalRoutes[j])
            }
        }
        return availableRoutes
    }

    //Filters
    private fun filterByMaxStops(routeData: ArrayList<ArrayList<Route>>, maxStops: Int): ArrayList<ArrayList<Route>> {
        //Filters by max stops

        val filteredData = ArrayList<ArrayList<Route>>()

        for (i in routeData.indices) {
            if (routeData[i].size <= maxStops) {
                filteredData.add(routeData[i])
            }
        }
        return filteredData
    }

    private fun filterByExactStops(routeData: ArrayList<ArrayList<Route>>, exactStops: Int): ArrayList<ArrayList<Route>> {
        //Filters by exact stops

        val filteredData = ArrayList<ArrayList<Route>>()

        for (i in routeData.indices) {
            if (routeData[i].size == exactStops) {
                filteredData.add(routeData[i])
            }
        }
        return filteredData
    }

    private fun filterData(filters: Filters): ArrayList<ArrayList<Route>> {
        //Function that calls both filter functions and returns filtered data

        var maxStops = -1
        var exactStops = -1

        try {
            maxStops = Integer.parseInt(filters.maxStops)
        } catch (ex: NumberFormatException) {
            //Error
        }

        try {
            exactStops = Integer.parseInt(filters.exactStops)
        } catch (ex: NumberFormatException) {
            //Error
        }


        var allPaths = getAllRoutes(
            ArrayList(),
            Nodes.getNode(filters.startNode!!),
            Nodes.getNode(filters.endNode!!)
        )


        if (maxStops != -1) {
            allPaths = filterByMaxStops(allPaths, maxStops)
        }

        if (exactStops != -1) {
            allPaths = filterByExactStops(allPaths, exactStops)
        }

        return allPaths
    }

    class Path(private var Route: String, var Distance: Int?, private var Stops: Int?) {

        //toString builds string from path in user readable format
        override fun toString(): String {
            var resultToDisplay = ""
            resultToDisplay += "Route: $Route" + System.getProperty("line.separator")
            resultToDisplay += "Total Distance: " + Integer.toString(Distance!!) + System.getProperty("line.separator")
            resultToDisplay += "Total Stops: " + Integer.toString(Stops!!) + System.getProperty("line.separator") + System.getProperty("line.separator")
            return resultToDisplay
        }
    }

    //Utils
    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}