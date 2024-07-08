import ru.squad14.graph.Graph
import ru.squad14.services.AlgLogger

class FloidAlgorithmService {
    var u = 0
    var v = 0
    var w = 0
    var n = 1
    enum class State{DONE,INPROGRESS,NEG_CYCLE,START}
    var state = State.START
    var startGraph: Graph? = null
    var adjacencyMatrix = Array<DoubleArray>(1,{DoubleArray(1,{1.0})})
    var pathMatrix = initPathMatrix()
    var logger: AlgLogger? = null

    fun init(graph: Graph){
        if(w==0&&v==0&&u==0) {
            logger?.generateNewString(w)
            startGraph = graph //saving initial graph
            adjacencyMatrix = getAdjacencyMatrix(graph)
            pathMatrix = initPathMatrix()
            state = State.INPROGRESS
            println(n)
        }
    }
    fun step(graph: Graph): Graph{

        init(graph)

        val vertices = graph.vertices.toList()
        val n = vertices.size
        val inf = Double.POSITIVE_INFINITY

        val newEdges = mutableSetOf<Edge>()


        logger?.generateNewString(w,u,v,
            adjacencyMatrix[u][w].toInt(),adjacencyMatrix[w][v].toInt(),adjacencyMatrix[u][v].toInt())

        // Алгоритм Флойда
        if (adjacencyMatrix[u][w] + adjacencyMatrix[w][v] < adjacencyMatrix[u][v]) {
            adjacencyMatrix[u][v] = adjacencyMatrix[u][w] + adjacencyMatrix[w][v]
            setPath(u,v,w)
            logger?.generateNewString(u,v, adjacencyMatrix[u][v].toInt(), getPath(u,v))
        }

        logger?.generateNewString(u,v, adjacencyMatrix[u][v].toInt())

        if((u==v)&&(adjacencyMatrix[u][v]<0)){
            state = State.NEG_CYCLE
            logger?.generateNewString(getPath(u,v))
        }

        for (k in 0 until n) {
            for (i in 0 until n) {
                if (adjacencyMatrix[k][i] != inf) {
                    newEdges.add(Edge(vertices[k], vertices[i], adjacencyMatrix[k][i].toInt()))
                }
            }
        }

        if(state!=State.NEG_CYCLE){
            if(v!=n-1)
            {
                v++
            }
            else if (u!=n-1)
            {
                u++
                v=0
            }
            else if (w!=n-1)
            {
                w++
                u=0
                v=0
                logger?.generateNewString(w)
            }else{
                state = State.DONE
                logger?.generateNewString()
            }
        }
//        if(state == State.DONE) println("DONE")
        return Graph(vertices.toSet(), newEdges)
    }

    private fun getPath(u: Int, v: Int): String {
        println("calculating ${('A'+ u)} to ${('A'+ v)} path")
        println("pathMatrix[u][v] = " + pathMatrix[u][v])
        val name = 'A' + pathMatrix[u][v]
        if (name !in 'A'..'Z') {
            return "${('A'+ u)} -> ${('A'+ v)}"
        } else{
            var uwPath = getPath(u,pathMatrix[u][v])
            var wvPath = getPath(pathMatrix[u][v],v)
            if(uwPath.last()==wvPath.first()){
                uwPath = uwPath.substring(0,uwPath.length-1)
            }
            return "$uwPath $wvPath"
        }
    }

    private fun setPath(u: Int, v: Int, w: Int){
        pathMatrix[u][v] = w
    }

    private fun initPathMatrix():Array<Array<Int>>{
        pathMatrix = Array(n,{Array<Int>(n,{-1})})
        for(i in 0 until n){
            for(j in 0 until n){
                pathMatrix[i][j] = -1
            }
        }
        return pathMatrix
    }


    private fun getAdjacencyMatrix(graph: Graph): Array<DoubleArray>{
        val vertices = graph.vertices.toList()
        n = vertices.size
        val vertexIndex = vertices.withIndex().associate { it.value to it.index }
        val inf = Double.POSITIVE_INFINITY
        val adjacencyMatrix = Array(n) { DoubleArray(n) { inf } }


        for (edge in graph.edges) {
            val fromIndex = vertexIndex[edge.from] ?: -1
            val toIndex = vertexIndex[edge.to] ?: -1
            adjacencyMatrix[fromIndex][toIndex] = edge.cost.toDouble()
        }

        for (j in 0 until n) {
            for (i in 0 until n) {
                if (i == j) {
                    adjacencyMatrix[i][j] = 0.0
                }
            }
        }
        return adjacencyMatrix
    }

    fun compute(graph: Graph): Graph {
        val vertices = graph.vertices.toList()
        val inf = Double.POSITIVE_INFINITY

        init(graph)

        var resultGraph = graph

        while(state!=State.DONE && state!=State.NEG_CYCLE){
            resultGraph = step(graph)
        }

        if (state!=State.NEG_CYCLE) {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    if (adjacencyMatrix[i][j] != inf) {
                        logger?.generateNewString(i, j, adjacencyMatrix[i][j].toInt(), getPath(i, j))
                    }
                }
            }
        }

        return resultGraph

    }

}