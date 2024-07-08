package ru.squad14.services

import FloidAlgorithmService
import javafx.scene.control.TextArea

class AlgLogger(alg: FloidAlgorithmService) {
    enum class Mode{ALL,UPDATE_ONLY,RESULT_ONLY}
    var mode = Mode.ALL
    var algService= alg
    var textArea: TextArea? = null
    var textLog = ""
    val inf = Int.MAX_VALUE
    fun updateView(s: String){
        textArea?.appendText(s)
    }

    fun generateNewString(w: Int){
        val W = ('A'+w).toString()
        val s = "New middle vertex - $W\n"
        textLog += s
        updateView(s)
    }

    fun generateNewString(w: Int,u: Int,v: Int, uw: Int,wv: Int,uv: Int){
        val W = ('A'+w).toString()
        val U = ('A'+u).toString()
        val V = ('A'+v).toString()
        val s = "$U->$V = ${if(uv<inf) uv else "inf"} or $U->$W->$V = ${if(uw<inf) uw else "inf"} + ${if(wv<inf) wv else "inf"}\n"
        textLog += s
        updateView(s)
    }
    fun generateNewString(u: Int,v: Int, e: Int,path:String){
        var s = ""
        val U = ('A'+u).toString()
        val V = ('A'+v).toString()
        s += "New $U to $V path: $path\n"

        textLog += s
        updateView(s)
    }

    fun generateNewString(u: Int,v: Int, e: Int){
        var s = ""
        val U = ('A'+u).toString()
        val V = ('A'+v).toString()
        s += "$U->$V = ${if(e<inf) e else "inf"}\n"


        textLog += s
        updateView(s)

    }
    fun generateNewString(cycle: String){
        var s = ""
        s += "Algorithm found negative cycle : $cycle\n"
        textLog += s
        updateView(s)

    }

    fun generateNewString(){
        var s = ""
        when(algService?.state){
            FloidAlgorithmService.State.DONE ->{s = "Algorithm ended successfully\n"}
//            FloidAlgorithmService.State.NEG_CYCLE ->{s = "Algorithm found negative cycle : \n"}
            else ->{}
        }
        textLog += s
        updateView(s)
    }
}