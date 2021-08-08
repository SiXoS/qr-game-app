package se.lindhen.qrgame.shape

import se.lindhen.qrgame.program.drawings.Shape

class GlCompositeShape(x: Double, y: Double, children: List<Shape>) : GlShape(FloatArray(0), x, y) {

    override fun getType() = Type.COMPOSITE

    init {
        this.children = children
    }

    override fun draw(vertexPositionHandle: Int, modelMatrixPositionHandle: Int, colorHandle: Int) {
        if (children != null) {
            for (child in children) {
                (child as GlShape).draw(vertexPositionHandle, modelMatrixPositionHandle, colorHandle)
            }
        }
    }
}
