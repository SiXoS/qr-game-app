package se.lindhen.qrgame.shape

class GlTriangle(x: Double, y: Double, width: Double, height: Double): GlShape(
    generateTriangle(width.toFloat(), height.toFloat()), x, y
) {

    companion object {

        private fun generateTriangle(width: Float, height: Float): FloatArray {
            val leftX = -width / 2.0f
            val topY = -height / 2.0f
            val rightX = width / 2.0f
            val bottomY = height / 2.0f
            return floatArrayOf(
                0f, topY, 0f,
                leftX, bottomY, 0f,
                rightX, bottomY, 0f)
        }

    }

    override fun getType(): Type {
        return Type.TRIANGLE
    }
}
