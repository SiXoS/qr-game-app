package se.lindhen.qrgame

import java.lang.RuntimeException

interface GameStateChangeListener {

    fun won()
    fun lost()
    fun scoreChange(score: Int)
    fun onError(exception: RuntimeException)
    fun onIterationRun(dt: Int)

}