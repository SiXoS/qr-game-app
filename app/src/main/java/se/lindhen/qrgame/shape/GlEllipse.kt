package se.lindhen.qrgame.shape

import kotlin.math.cos
import kotlin.math.sin

class GlEllipse(x: Double, y: Double, width: Double, height: Double): GlShape(generateCircle(width.toFloat(), height.toFloat()), x, y) {

    companion object {

        private val radsPerDeg = Math.PI / 180.0

        private fun generateCircle(width: Float, height: Float): FloatArray {
            val numPolygons = 20
            val steps = 360/numPolygons
            val coordsPerPolygon = 3*3 // three vertices with three coordinates each
            val arr = FloatArray(numPolygons * coordsPerPolygon)
            val xRadius = width / 2.0f
            val yRadius = height / 2.0f
            for (polygon in 0 until numPolygons) {
                val (xOffset, yOffset) = coordsFromDegrees(steps * polygon)
                val (xOffsetTurned, yOffsetTurned) = coordsFromDegrees(steps * (polygon + 1))
                val i = polygon * coordsPerPolygon
                arr[i] = 0.0f
                arr[i+1] = 0.0f
                arr[i+2] = 0.0f
                arr[i+3] = xOffset * xRadius
                arr[i+4] = yOffset * yRadius
                arr[i+5] = 0.0f
                arr[i+6] = xOffsetTurned * xRadius
                arr[i+7] = yOffsetTurned * yRadius
                arr[i+8] = 0.0f
            }
            return arr
        }

        private fun coordsFromDegrees(degrees: Int): Pair<Float, Float> {
            val rad = degrees * radsPerDeg
            return Pair(cos(rad).toFloat(), sin(rad).toFloat())
        }

    }

    override fun getType(): Type {
        return Type.ELLIPSE
    }

}