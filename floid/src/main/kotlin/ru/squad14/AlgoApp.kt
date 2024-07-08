package ru.squad14

import FloidAlgorithmService
import GraphVisController
import javafx.application.Application
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import ru.squad14.graph.Graph
import ru.squad14.services.AlgLogger

class AlgoApp : Application() {
    private val graphProperty = ReadOnlyObjectWrapper(Graph(setOf(), setOf()))
    val readonlyGraphProperty: ObservableValue<Graph> = graphProperty

    val service = FloidAlgorithmService()
    val logger = AlgLogger(service)

    var matrixIOPane = MatrixIOPane(graphProperty, service, logger)
    var graphVisPane: GraphVisPane? = null
    var splitPane = SplitPane()

    override fun start(primaryStage: Stage) {
        service.logger = logger

        this.graphVisPane = GraphVisPane(readonlyGraphProperty)

        matrixIOPane.setControllers(GraphVisController(graphVisPane))
        this.splitPane = SplitPane(
            graphVisPane,
            matrixIOPane,
        )

        val scene = Scene(splitPane)
        scene.stylesheets.add("styles.css")
        primaryStage.scene = scene
        primaryStage.title = "floid"
        primaryStage.height = 700.0
        primaryStage.width = 1000.0
        primaryStage.show()

        graphVisPane?.init()

        readonlyGraphProperty.addListener { _, _, value ->
            println(value)
        }
    }
}
fun main(vararg args: String) {
    Application.launch(AlgoApp::class.java, *args)
}
