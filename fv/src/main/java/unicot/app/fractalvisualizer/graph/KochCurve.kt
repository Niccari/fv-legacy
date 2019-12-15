package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt
import kotlin.math.pow

/**
 * コッホ曲線
 */
open class KochCurve : Graph() {
    protected open val nsub: Int
        get() = 1

    protected val length0 = 1.0f  // 初期長さ
    protected val degree0 = 60.0f // 初期角度

    init {
        complexityMin = 2
        complexityMax = 5

        info.graphKind = DGCommon.KOCHCURVE
        info.isRecursive = true
    }

    override val pointMax: Int
        get() {
            nOrders = 2.0.pow((2 * (info.complexity - 1)).toDouble()).toInt() + 1 - nsub
            return nOrders + nsub
        }

    override fun allocatePoints() {
        while (point.size > pointMax) point.removeAt(0)
        while (point.size < pointMax) point.add(PointF())
        pointBase.clear()

        pointBase.add(0, PointF(GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MID))
        pointBase.add(1, PointF(GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MID))

        orderPoints = Array(nOrders){ Point() }
        calculateOrder()
    }

    public override fun setRelativePoint() {
        allocatePoints()

        setRelRecursivePoint(1, length0, degree0)    //Generator Line
        isAllocated = true
    }

    protected fun setRelRecursivePoint(depth: Int, parentLength: Float, parentDegree: Float) {
        var length = parentLength
        var degree = parentDegree
        if (depth >= info.complexity) return

        var addPoint: PointF
        val src = PointF()
        val dst = PointF()
        var vct: PointF
        length *= info.mutation.size + info.randomize.size * Math.random().toFloat()
        degree *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.getInstance().sin(degree.toInt())
        val cos = SinInt.getInstance().cos(degree.toInt())

        for (i in 0 until 2.0.pow((2 * depth).toDouble()).toInt()) {
            if (i % 4 <= 1) {    // 分割線("_/\_"のうち、"_"の部分)
                src.set(pointBase[i])
                dst.set(pointBase[i + 1])
                vct = PointF(dst.x - src.x, dst.y - src.y)
                addPoint = PointF(src.x + length / (3 - i % 4).toFloat() * vct.x,
                        src.y + length / (3 - i % 4).toFloat() * vct.y)
                pointBase.add(i + 1, addPoint)
            } else if (i % 4 == 2) {    // 三角部の点("_/\_"のうち、"/\"の部分)
                src.set(pointBase[i - 1])
                dst.set(pointBase[i])
                vct = PointF(dst.x - src.x, dst.y - src.y)
                addPoint = PointF(src.x + length * (cos * vct.x - sin * vct.y),
                        src.y + length * (sin * vct.x + cos * vct.y))
                pointBase.add(i, addPoint)
            }
        }
        setRelRecursivePoint(depth + 1, length, degree)
    }

    override fun calculateOrder() {
        for (i in nOrders downTo 1) {
            val src = i - 1
            orderPoints[nOrders - i] = Point(src, i)
        }
    }
}
