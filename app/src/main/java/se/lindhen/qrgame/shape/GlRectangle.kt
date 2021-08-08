package se.lindhen.qrgame.shape

class GlRectangle(x: Double, y: Double, width: Double, height: Double): GlShape(
    generateRectangle(width.toFloat(), height.toFloat()), x, y
) {

    companion object {

        private fun generateRectangle(width: Float, height: Float): FloatArray {
            val leftX = -width / 2.0f
            val topY = -height / 2.0f
            val rightX = width / 2.0f
            val bottomY = height / 2.0f
            return floatArrayOf(
                leftX, topY, 0f,
                leftX, bottomY, 0f,
                rightX, bottomY, 0f,
                leftX, topY, 0f,
                rightX, bottomY, 0f,
                rightX, topY, 0f)
        }

    }

    override fun getType(): Type {
        return Type.RECTANGLE
    }
}
