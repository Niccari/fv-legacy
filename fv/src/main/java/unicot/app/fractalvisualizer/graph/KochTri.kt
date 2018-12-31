package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * コッホ曲線を3つくっつけた曲線(雪型、三菱型の2種)
 */
class KochTri(private val shape_kind: Int) : KochCurve() {
    override val nsub: Int
        get() = 3

    init {

        complexityMin = 1
        complexityMax = 5

        info.graph_kind = if (shape_kind == INNER) DGCommon.KOCHTRIANGLE_INNER else DGCommon.KOCHTRIANGLE_OUTER
        info.is_recursive = true
        setRelativePoint()
    }

    override val pointMax: Int
        get() {
            val pointMax = 3 * (Math.pow(2.0, (2 * (info.complexity - 1)).toDouble()).toInt() + 1)
            n_orders = pointMax - nsub
            return pointMax
        }

    override fun calculateOrder() {
        for (i in 1 until pointMax / 3) {
            val src = i - 1
            order_points[i - 1] = Point(src, i)
        }
        for (i in pointMax / 3 + 1 until 2 * pointMax / 3) {
            val src = i - 1
            order_points[i - 2] = Point(src, i)
        }
        for (i in 2 * pointMax / 3 + 1 until pointMax) {
            val src = i - 1
            order_points[i - 3] = Point(src, i)
        }
    }

    override fun setRelativePoint() {
        allocatePoints()

        setRelRecursivePoint(1, GRAPH_KOCH_INIT_ARM_RATE, GRAPH_KOCH_INIT_THETA_RATE)

        val sin120 = -SinInt.SI().sin(60)
        val cos120 = -0.5f

        var bx: Float
        var by: Float
        // Koch曲線の一つを変形(三角の一辺を準備)
        for (i in 0 until pointMax / 3) {
            if (shape_kind == INNER)
            // 三菱型なら、上下すればよい。
                point_base[i].y = -point_base[i].y
            point_base[i].y += 1.0f / Math.sqrt(3.0).toFloat()
        }
        for (i in 0 until pointMax / 3) {
            bx = point_base[i].x
            by = point_base[i].y
            point_base.add(PointF(bx * cos120 - by * sin120,
                    bx * sin120 + by * cos120))
        }
        for (i in 0 until pointMax / 3) {
            bx = point_base[i + pointMax / 3].x
            by = point_base[i + pointMax / 3].y
            point_base.add(PointF(bx * cos120 - by * sin120,
                    bx * sin120 + by * cos120))
        }
        is_allocated = true
    }

    companion object {
        val OUTER = 0
        val INNER = 1
    }
}
