package ru.squad14

import Edge
import FloidAlgorithmService
import GraphVisController
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.util.Duration
import ru.squad14.graph.Graph
import ru.squad14.graph.Vertex
import ru.squad14.services.AlgLogger
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane


class MatrixIOPane(private val graphProperty: ReadOnlyObjectWrapper<Graph>, service: FloidAlgorithmService, logger: AlgLogger) : AnchorPane() {
    private var dim = SimpleIntegerProperty(0)
    private val vbox = VBox()
    private val grid = GridPane()
    private val matrixGrid = GridPane()
    private var matrixVisGrid = GridPane()
    private var logPane = StackPane()
    private val vertexCache = mutableMapOf<Int, Vertex>()


    val service = service
    var visControl: GraphVisController? = null

    private var step = 0
    private var pause = true

    fun setControllers(visuals: GraphVisController){
        visControl = visuals
    }

    private fun addMatrix(graph: Graph): GridPane {
        val gridPane = GridPane()

        gridPane.hgap = 6.0
        gridPane.vgap = 6.0

        val vertices = graph.vertices.toList()
        val n = vertices.size
        val vertexIndex = vertices.withIndex().associate { it.value to it.index }
        val stringAdjacencyMatrix = Array(n) { Array(n) { "inf" } }

        for (edge in graph.edges) {
            val fromIndex = vertexIndex[edge.from] ?: -1
            val toIndex = vertexIndex[edge.to] ?: -1
            if (edge.cost != Int.MAX_VALUE) {
                stringAdjacencyMatrix[fromIndex][toIndex] = " " + edge.toString() + " "
            }
        }

        for (i in stringAdjacencyMatrix.indices) {
            gridPane.add(Label(('A' + i).toString()), 0, i+1)
            for (j in stringAdjacencyMatrix[i].indices) {

                if(i==0) gridPane.add(Label(('A' + j).toString()), j+1, 0)

                val cellValue = stringAdjacencyMatrix[i][j]
                val label = Label(cellValue)

                if (i == step - 1 || j == step - 1) {
                    label.styleClass.add("label-r")
                } else {
                    label.styleClass.add("label-b")

                }
                gridPane.add(label, j+1, i+1)
            }
        }

        matrixGrid.add(gridPane, 1, 1)
        vbox.children.add(matrixGrid)
        matrixVisGrid = gridPane
        return gridPane
    }
    private fun colorLabel(style: String, i: Int, j: Int){
        matrixVisGrid.children.find {(GridPane.getRowIndex(it) == i+1) && (GridPane.getColumnIndex(it) == j+1)}?.styleClass?.add(style)
    }
    private fun cleanMatrix() {
        matrixGrid.children.clear()
        if (vbox.children.contains(matrixGrid)) {
            vbox.children.remove(matrixGrid)
        }
    }

    private fun highlightLabel(i: Int,j: Int,color: String="red"){
            matrixVisGrid.children[i*dim.get()+j].styleClass.add("label-r")
    }

    private fun addTextField(i: Int, j: Int) {
        val textField = TextField()
        textField.prefWidth = 50.0
        textField.promptText = "0"
        grid.add(textField, i + 1, j + 1)

        textField.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                val cost = textField.text.toIntOrNull()
                if (cost!=null){
                    val e = graphProperty.get().edges.find{it -> (it.from == graphProperty.get().vertices.elementAt(j)
                            && it.to == graphProperty.get().vertices.elementAt(i))}
                    if (e != null) {
                        graphProperty.set(
                            Graph(
                                graphProperty.get().vertices,
                                graphProperty.get().edges - e
                            )
                        )
                    }
                    val edge = Edge(vertexCache[j]!!, vertexCache[i]!!, cost)
                    graphProperty.set(
                        Graph(
                            graphProperty.get().vertices,
                            graphProperty.get().edges + edge
                        )
                    )
                }
            }
        }


}
    private fun addElement() {
        val name = ('A' + dim.get()).toString()
        vertexCache[dim.get()] = Vertex(name)
        val columnIndex = dim.get() + 1

        dim.set(dim.get() + 1)

        val vertexLabel = Label(name)
        grid.add(vertexLabel, columnIndex, 0)
        grid.add(Label(name), 0, columnIndex)

        val j = dim.get() - 1
        for (i in 0 until dim.get()) {
            if (i != j) {
                addTextField(i, j)
                addTextField(j, i)
            }
        }

        graphProperty.set(
            Graph(
                graphProperty.get().vertices + Vertex(name),
                graphProperty.get().edges
            )
        )
    }



    private fun removeElement() {
        val name = ('A' + dim.get() - 1).toString()
        for(i in graphProperty.get().vertices) {
            if (i.name.equals(name)){
                graphProperty.set(
                    Graph(
                        graphProperty.get().vertices,
                        graphProperty.get().edges.filterNot { it.to == i || it.from == i }.toSet()
                    )
                )
                graphProperty.set(
                    Graph(
                        graphProperty.get().vertices-i,
                        graphProperty.get().edges
                    )
                )
            }
        }

        grid.children.removeIf { node ->
            val toRemove = ((GridPane.getRowIndex(node) == dim.get()) || (GridPane.getColumnIndex(node) == dim.get()))
            toRemove
        }

        dim.set(dim.get() - 1)
    }

    private fun clearEdges(){
        graphProperty.set(Graph(
            graphProperty.get().vertices,
            graphProperty.get().edges - graphProperty.get().edges
            )
        )
    }

    private fun getAlgorithmResult(){
        cleanMatrix()
        val newGraph = service.compute(graphProperty.get())
        if(service.state!=FloidAlgorithmService.State.NEG_CYCLE) {
            addMatrix(newGraph)
            var oldEdges = graphProperty.get().edges
            val newEdges = (newGraph.edges - graphProperty.get().edges)
            for (e in newEdges) {
                oldEdges = oldEdges.filterNot { it -> it.to == e.to && it.from == e.from }.toSet()
            }
            visControl?.clearColors()

            graphProperty.set(
                Graph(
                    graphProperty.get().vertices,
                    oldEdges + newEdges
                )
            )
        }
    }

    private fun stepAlgorithm(){
        cleanMatrix()
        visControl?.clearColors()

        val vertices = graphProperty.get().vertices
        val w = service.w
        val u = service.u
        val v = service.v
        var uw = graphProperty.get().edges.find{it -> (it.from == vertices.elementAt(u)&&it.to == vertices.elementAt(w))}
        var wv = graphProperty.get().edges.find{it -> (it.from == vertices.elementAt(w)&&it.to == vertices.elementAt(v))}
        val uv = graphProperty.get().edges.find{it -> (it.from == vertices.elementAt(u)&&it.to == vertices.elementAt(v))}

        visControl?.changeEdgeColor("red", uv)
        visControl?.changeEdgeColor("violet",uw, wv)
        visControl?.changeVertexColor("red", vertices.elementAt(u),vertices.elementAt(v))
        visControl?.changeVertexColor("violet", vertices.elementAt(w))
        addMatrix(graphProperty.get())

        colorLabel("label-v",u,w)
        colorLabel("label-v",w,v)
        colorLabel("label-r",u,v)

//        cleanMatrix()
            val newGraph = service.step(graphProperty.get())
//            addMatrix(newGraph)
            graphProperty.set(newGraph)

    }
 private fun autoPlayAlgorithm(){
     val timeline = Timeline(KeyFrame(Duration.seconds(3.0), EventHandler {
         stepAlgorithm()
     }))
    println("in autoplay func")
     //while (service.state != FloidAlgorithmService.State.DONE && service.state != FloidAlgorithmService.State.NEG_CYCLE) {
         timeline.cycleCount = Math.pow(vertexCache.size.toDouble(),3.0).toInt()
         timeline.play()
         visControl?.updateView()
     //}
 }

//private class AnimationThread(parent: MatrixIOPane){
//    init{
//        println("in thread init")
//        val autoPlay = Runnable(){
//            fun run(){
//                while (parent.service.state != FloidAlgorithmService.State.DONE && parent.service.state != FloidAlgorithmService.State.NEG_CYCLE) {
//                    parent.stepAlgorithm()
//                    println("in new thread")
//                }
//            }
//        }
//        val t = Thread(autoPlay)
//        t.start()
//    }
//}


    init {
        val buttonAddElement = Button("Add vertex")
        val buttonRemoveElement = Button("Delete Vertex")
        val buttonClear = Button("Clear")
        val buttonInfo = Button("Info")

        val startbtn = Button("|>")
        val pausebtn = Button(" || ")
        val stepbtn = Button("->")
        val resultbtn = Button(" = ")
        val restartbtn = Button("<-")

        val buttons = HBox(
                          startbtn,
                          pausebtn,
                          stepbtn,
                          resultbtn,
                          restartbtn
        )

        //vbox.alignment = Pos.CENTER_RIGHT
        buttons.alignment = Pos.BOTTOM_CENTER
        val deleteAndAddButtons = HBox(
            buttonAddElement,
            buttonRemoveElement,
            buttonClear,
            buttonInfo
        )
        grid.alignment = Pos.BOTTOM_CENTER
        matrixGrid.alignment = Pos.BOTTOM_CENTER
        deleteAndAddButtons.alignment = Pos.BOTTOM_CENTER

        buttons.spacing = 15.0
        buttons.styleClass.add("buttons")

        buttonRemoveElement.disableProperty().bind(dim.greaterThan(2).not())
        buttonAddElement.disableProperty().bind(dim.lessThan(10).not())

        buttonAddElement.setOnMouseClicked {
            addElement()
            cleanMatrix()
        }
        buttonRemoveElement.setOnMouseClicked { removeElement() }
        buttonClear.setOnMouseClicked { clearEdges()}
        buttonInfo.setOnMouseClicked { //converted to lambda?????????
                JOptionPane.showMessageDialog(
                    null, "some info about application usage...",
                    "help", JOptionPane.INFORMATION_MESSAGE
                )
            }

        resultbtn.setOnMouseClicked { getAlgorithmResult() }
        stepbtn.setOnMouseClicked { stepAlgorithm() }
        pausebtn.setOnMouseClicked {
            pause = true
        }
        startbtn.setOnMouseClicked {
            pause = false
            service.state = FloidAlgorithmService.State.START
            autoPlayAlgorithm()
        }

        restartbtn.setOnMouseClicked {
            service.w = 0
            service.u = 0
            service.v = 0
            service.state = FloidAlgorithmService.State.START
            graphProperty.set(service.startGraph)
        }

        val logArea = TextArea()
        logger.textArea = logArea
        logArea.prefColumnCount = 100
        logArea.prefRowCount = 10
//        logArea.isWrapText = true
        val scrollPane = ScrollPane(logArea)


        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED


        // Create a StackPane to hold the ScrollPane
        val logPane = StackPane()
        logPane.children.add(logArea)
        logPane.children.add(scrollPane)

        grid.hgap = 6.0
        grid.vgap = 6.0

        matrixGrid.hgap = 20.0
        matrixGrid.vgap = 20.0

        vbox.spacing = 10.0
        vbox.prefWidth = 400.0


        vbox.children.addAll(
            deleteAndAddButtons,
            grid,
            matrixGrid,
            buttons,
            logPane

        )
        children.add(vbox)
        setLeftAnchor(vbox, 0.0)
        setRightAnchor(vbox, 0.0)
        setTopAnchor(vbox, 0.0)
        setBottomAnchor(vbox, 0.0)

        for (i in 0 until 2) {
            addElement()
        }

    }


}
