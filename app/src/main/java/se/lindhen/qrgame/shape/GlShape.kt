package se.lindhen.qrgame.shape;

import android.opengl.GLES20
import android.opengl.Matrix
import se.lindhen.qrgame.program.drawings.Color
import se.lindhen.qrgame.program.drawings.Shape
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class GlShape(vertices: FloatArray, posX: Double, posY: Double): Shape(posX, posY) {

    private var vertexCount = vertices.size / COORDS_PER_VERTEX
    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertices.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertices)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
    private val workMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    open fun draw(vertexPositionHandle: Int, modelMatrixPositionHandle: Int, colorHandle: Int) {
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(vertexPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            vertexPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            VERTEX_STRIDE,
            vertexBuffer
        )

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, if (color == Color.FOREGROUND) fgColor else bgColor, 0)

        // Set the model matrix
        GLES20.glUniformMatrix4fv(modelMatrixPositionHandle, 1, false, modelMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertexPositionHandle)

        if (children != null) {
            for (child in children) {
                (child as GlShape).draw(vertexPositionHandle, modelMatrixPositionHandle, colorHandle)
            }
        }
    }

    override fun update(secSinceLastFrame: Double) {
        update(secSinceLastFrame, IDENTITY_MATRIX)
    }

    override fun update(secSinceLastFrame: Double, parent: Shape) {
        update(secSinceLastFrame, (parent as GlShape).modelMatrix)
    }

    private fun update(secSinceLastFrame: Double, baseMatrix: FloatArray) {
        super.updatePosition(secSinceLastFrame)
        updateWorkMatrix()
        Matrix.multiplyMM(modelMatrix, 0, baseMatrix, 0, workMatrix, 0)
        updateChildren(secSinceLastFrame)
    }

    private fun updateWorkMatrix() {
        Matrix.setIdentityM(workMatrix, 0)
        Matrix.translateM(workMatrix, 0, posX.toFloat(), posY.toFloat(), 0.0f)
        Matrix.rotateM(workMatrix, 0, rotationDeg.toFloat(), 0.0f, 0.0f, -1.0f)
        Matrix.scaleM(workMatrix, 0, scaleX.toFloat(), scaleY.toFloat(), 1.0f)
    }

    private fun updateChildren(secSinceLastFrame: Double) {
        if (children != null) {
            for (child in children) {
                child.update(secSinceLastFrame, this)
            }
        }
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val VERTEX_STRIDE = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
        private val fgColor = floatArrayOf(101 / 255f, 129 / 255f, 92 / 255f, 1.0f)
        private val bgColor = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
        private val IDENTITY_MATRIX = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    }

}
