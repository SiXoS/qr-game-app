package se.lindhen.qrgame

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import se.lindhen.qrgame.program.GameStatus
import se.lindhen.qrgame.program.Program
import se.lindhen.qrgame.shape.*
import java.lang.RuntimeException
import java.util.function.Consumer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates

class GameRenderer: GLSurfaceView.Renderer {

    private var crashed = false
    private var gameStateChangeListener: GameStateChangeListener? = null
    private var programIteration: Consumer<Int>? = null
    private var shaderProgram by Delegates.notNull<Int>()
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vPMatrix = FloatArray(16)
    private var prevScore = Int.MIN_VALUE
    private var prevState = GameStatus.RUNNING
    private var program: Program? = null
    private var prevRender = System.currentTimeMillis()

    fun setProgram(program: Program) {
        this.program = program
        program.shapeFactory = GlShapeFactory()
        programIteration = try {
            program.initializeAndPrepareRun()
        } catch (e: RuntimeException) {
            gameStateChangeListener?.onError(e)
            crashed = true
            null
        }
    }

    fun resume() {
        prevRender = System.currentTimeMillis()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        shaderProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.frustumM(projectionMatrix, 0, 0f, -1000f, 1000f, 0f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {

        if (program == null || programIteration == null || crashed) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            return
        }

        if (prevState != GameStatus.RUNNING) {
            return
        }

        val newRender = System.currentTimeMillis()
        val dt = newRender - prevRender
        prevRender = newRender

        try {
            programIteration!!.accept(dt.toInt())
        } catch (e: RuntimeException) {
            crashed = true
            gameStateChangeListener?.onError(e)
        }

        if (program!!.score != prevScore) {
            prevScore = program!!.score
            gameStateChangeListener?.scoreChange(prevScore)
        }
        if  (program!!.status != prevState) {
            prevState = program!!.status
            when (program!!.status!!) {
                GameStatus.WON -> gameStateChangeListener?.won()
                GameStatus.LOST -> gameStateChangeListener?.lost()
                GameStatus.RUNNING -> {}
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(shaderProgram)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        val vertexPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val vPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        val modelMatrixPositionHandle = GLES20.glGetUniformLocation(shaderProgram, "modelMatrix")
        val colorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor")
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0)
        program!!.drawings.forEach {
            if (it is GlShape) {
                it.draw(vertexPositionHandle, modelMatrixPositionHandle, colorHandle)
            }
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun setGameStateChangedListener(gameChangeListener: GameStateChangeListener) {
        gameStateChangeListener = gameChangeListener
    }

    companion object {

        private val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 modelMatrix;
            
            attribute vec4 vPosition;
            
            void main() {
                gl_Position = uMVPMatrix * modelMatrix * vPosition;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
              gl_FragColor = vColor;
            }
        """.trimIndent()


    }

}