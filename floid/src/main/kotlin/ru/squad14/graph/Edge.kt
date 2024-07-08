import ru.squad14.graph.Vertex

data class Edge (
    val from: Vertex,
    val to: Vertex,
    val cost: Int
) {
    override fun toString(): String {
        return cost.toString()
    }
}
