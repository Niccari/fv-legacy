package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * コッホ曲線
 */
open class KochCurve : Graph() {
    protected open val nsub: Int
        get() = 1

    protected val GRAPH_KOCH_INIT_ARM_RATE = 1.0f  // 複雑さ+1での腕の長さ比率
    protected val GRAPH_KOCH_INIT_THETA_RATE = 60.0f // 複雑さ+1での角度比率

    init {
        complexityMin = 2
        complexityMax = 5

        info.graph_kind = DGCommon.KOCHCURVE
        info.is_recursive = true
    }

    override val pointMax: Int
        get() {
            nOrders = Math.pow(2.0, (2 * (info.complexity - 1)).toDouble()).toInt() + 1 - nsub
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

        setRelRecursivePoint(1, GRAPH_KOCH_INIT_ARM_RATE, GRAPH_KOCH_INIT_THETA_RATE)    //Generator Line
        isAllocated = true
    }

    protected fun setRelRecursivePoint(depth: Int, arm: Float, theta: Float) {
        var arm = arm
        var theta = theta
        if (depth >= info.complexity) return

        var add_point: PointF
        val src = PointF()
        val dst = PointF()
        var vct: PointF
        arm *= info.mutation.size + info.randomize.size * Math.random().toFloat()
        theta *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.SI().sin(theta.toInt())
        val cos = SinInt.SI().cos(theta.toInt())

        for (i in 0 until Math.pow(2.0, (2 * depth).toDouble()).toInt()) {
            if (i % 4 <= 1) {    // 分割線("_/\_"のうち、"_"の部分)
                src.set(pointBase[i])
                dst.set(pointBase[i + 1])
                vct = PointF(dst.x - src.x, dst.y - src.y)
                add_point = PointF(src.x + arm / (3 - i % 4).toFloat() * vct.x,
                        src.y + arm / (3 - i % 4).toFloat() * vct.y)
                pointBase.add(i + 1, add_point)
            } else if (i % 4 == 2) {    // 三角部の点("_/\_"のうち、"/\"の部分)
                src.set(pointBase[i - 1])
                dst.set(pointBase[i])
                vct = PointF(dst.x - src.x, dst.y - src.y)
                add_point = PointF(src.x + arm * (cos * vct.x - sin * vct.y),
                        src.y + arm * (sin * vct.x + cos * vct.y))
                pointBase.add(i, add_point)
            }
        }
        setRelRecursivePoint(depth + 1, arm, theta)
    }

    override fun calculateOrder() {
        for (i in nOrders downTo 1) {
            val src = i - 1
            orderPoints[nOrders - i] = Point(src, i)
        }
    }
}
