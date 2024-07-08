
import ru.squad14.GraphVisPane
import ru.squad14.graph.Vertex

//class AlgContoller {
//
//
//}

class GraphVisController(graphPane: GraphVisPane?){
    private val graphPane = graphPane

    public fun changeVertexColor(color: String, vararg v: Vertex?){
        for(elem in v){graphPane?.changeVertexColor(color,elem)}
    }

    public fun changeEdgeColor(color: String, vararg e: Edge?){
        for(elem in e){graphPane?.changeEdgeColor(color,elem)}
    }

    public fun clearColors(){
        graphPane?.resetStyle()
    }

    public fun updateView(){
        graphPane?.update()
    }


}

