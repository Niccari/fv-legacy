package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF
import unicot.app.fractalvisualizer.core.DGCommon
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * コッホ曲線を3つくっつけた曲線(雪型、三菱型の2種)
 */
class KochTri(private val shape_kind: Int) : KochCurve() {
    override val nsub: Int
        get() = 3

    init {

        complexityMin = 1
        complexityMax = 5

        info.graphKind = if (shape_kind == INNER) DGCommon.KOCHTRIANGLE_INNER else DGCommon.KOCHTRIANGLE_OUTER
        info.isRecursive = true
        setRelativePoint()
    }

    override val pointMax: Int
        get() {
            val pointMax = 3 * (2.0.pow((2 * (info.complexity - 1)).toDouble()).toInt() + 1)
            nOrders = pointMax - nsub
            return pointMax
        }

    override fun calculateOrder() {
        for (i in 1 until pointMax / 3) {
            val src = i - 1
            orderPoints[i - 1] = Point(src, i)
        }
        for (i in pointMax / 3 + 1 until 2 * pointMax / 3) {
            val src = i - 1
            orderPoints[i - 2] = Point(src, i)
        }
        for (i in 2 * pointMax / 3 + 1 until pointMax) {
            val src = i - 1
            orderPoints[i - 3] = Point(src, i)
        }
    }

    override fun setRelativePoint() {
        allocatePoints()

        setRelRecursivePoint(1, length0, degree0)

        val sin120 = -0.8660254f
        val cos120 = -0.5f

        var bx: Float
        var by: Float
        // Koch曲線の一つを変形(三角の一辺を準備)
        for (i in 0 until pointMax / 3) {
            if (shape_kind == INNER)
            // 三菱型なら、上下すればよい。
                pointBase[i].y = -pointBase[i].y
            pointBase[i].y += 1.0f / sqrt(3.0).toFloat()
        }
        for (i in 0 until pointMax / 3) {
            bx = pointBase[i].x
            by = pointBase[i].y
            pointBase.add(PointF(bx * cos120 - by * sin120, bx * sin120 + by * cos120))
        }
        for (i in 0 until pointMax / 3) {
            bx = pointBase[i + pointMax / 3].x
            by = pointBase[i + pointMax / 3].y
            pointBase.add(PointF(bx * cos120 - by * sin120, bx * sin120 + by * cos120))
        }
        isAllocated = true
    }

    companion object {
        const val OUTER = 0
        const val INNER = 1
    }
}
