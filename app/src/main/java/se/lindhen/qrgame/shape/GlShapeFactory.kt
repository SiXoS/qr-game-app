package se.lindhen.qrgame.shape

import se.lindhen.qrgame.program.drawings.Shape
import se.lindhen.qrgame.program.drawings.ShapeFactory

class GlShapeFactory : ShapeFactory {

    override fun createEllipse(x: Double, y: Double, width: Double, height: Double): Shape {
        return GlEllipse(x, y, width, height)
    }

    override fun createRect(x: Double, y: Double, width: Double, height: Double): Shape {
        return GlRectangle(x, y, width, height)
    }

    override fun createComposite(x: Double, y: Double, children: List<Shape>): Shape {
        return GlCompositeShape(x, y, children)
    }

    override fun createTriangle(x: Double, y: Double, width: Double, height: Double): Shape {
        return GlTriangle(x, y, width, height)
    }
}
