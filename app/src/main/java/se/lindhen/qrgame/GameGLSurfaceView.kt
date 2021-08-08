package se.lindhen.qrgame

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import se.lindhen.qrgame.parser.ProgramParser
import se.lindhen.qrgame.program.Program

class GameGLSurfaceView: GLSurfaceView {

    private val renderer: GameRenderer

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = GameRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY

    }

    fun setGameStateChangedListener(gameChangeListener: GameStateChangeListener) {
        renderer.setGameStateChangedListener(gameChangeListener)
    }

    fun pause() {
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun resume() {
        renderer.resume()
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun startGame(program: Program) {
        renderer.setProgram(program)
        requestRender()
    }

}
