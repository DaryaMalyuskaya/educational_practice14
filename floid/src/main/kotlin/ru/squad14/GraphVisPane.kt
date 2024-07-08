package ru.squad14

import Edge
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList
import com.brunomnsilva.smartgraph.graphview.ForceDirectedSpringGravityLayoutStrategy
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties
import javafx.beans.value.ObservableValue
import javafx.scene.layout.AnchorPane
import ru.squad14.graph.Graph
import ru.squad14.graph.Vertex

class GraphVisPane(private val graphObservable: ObservableValue<Graph>) : AnchorPane() {
    private val diGraph = DigraphEdgeList<Vertex, Edge>()
    private val graphView = SmartGraphPanel(diGraph, SmartCircularSortedPlacementStrategy(),
        ForceDirectedSpringGravityLayoutStrategy(25.0,3.0,10.0, 0.1,0.001))

    val vertexCache = mutableMapOf<Vertex, com.brunomnsilva.smartgraph.graph.Vertex<Vertex>>()
    val edgeCache = mutableMapOf<Edge, com.brunomnsilva.smartgraph.graph.Edge<Edge, Vertex>>()


    fun update(){
        graphView.update()
    }

    fun init() {
        graphView.init()

        onGraphUpdated(graphObservable, Graph(setOf(), setOf()), graphObservable.value,)
    }

    init {
        val properties = SmartGraphProperties()
        graphObservable.addListener(::onGraphUpdated)


        graphView.setAutomaticLayout(false)
        children.add(graphView)

        setLeftAnchor(graphView, 0.0)
        setRightAnchor(graphView, 0.0)
        setTopAnchor(graphView, 0.0)
        setBottomAnchor(graphView, 0.0)
    }


    fun resetStyle(){
        for (e in diGraph.edges()) {
            graphView.getStylableEdge(e)?.setStyleInline("-fx-stroke: #4454c1;")//old edges
        }
        for (v in diGraph.vertices()) {
            graphView.getStylableVertex(v)?.setStyleInline("-fx-fill: #4454c1")
        }
    }

    fun changeVertexColor(color: String, vararg verts: Vertex?){
        for (v in verts) {
            graphView.getStylableVertex(v)?.setStyleInline("-fx-fill: $color;")
        }
        graphView.update()
    }

    fun changeEdgeColor(color: String, vararg edges: Edge?){
        for (e in edges) {

            graphView.getStylableEdge(e)?.setStyleInline("-fx-stroke: $color;")
        }
        graphView.update()
    }

    private fun onGraphUpdated(value: ObservableValue<out Graph>, prev: Graph, cur: Graph) {
        val addedVertices = cur.vertices - prev.vertices
        val removedVertices = prev.vertices - cur.vertices
        val addedEdges = cur.edges - prev.edges
        val removedEdges = prev.edges - cur.edges


        for (addedVertex in addedVertices) {
            vertexCache[addedVertex] = diGraph.insertVertex(addedVertex)
        }
        for (removedVertex in removedVertices) {
            diGraph.removeVertex(vertexCache[removedVertex])
            vertexCache.remove(removedVertex)
        }
        for (addedEdge in addedEdges) {
            edgeCache[addedEdge] = diGraph.insertEdge(addedEdge.from, addedEdge.to, addedEdge)
//            print(edgeCache[addedEdge]!!.element())
        }
        for (removedEdge in removedEdges) {
            diGraph.removeEdge(edgeCache[removedEdge])
            edgeCache.remove(removedEdge)
        }
//        resetStyle()

        graphView.update()

    }

}
