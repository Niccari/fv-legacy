package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt
import kotlin.math.pow

/**
 * 再帰曲線(ドラゴン曲線、三角曲線、LeviのCカーブ)
 */
class FoldCurve(private val curve_kind: Int) : Graph() {

    init {
        complexityMin = 2
        complexityMax = 10

        when (curve_kind) {
            DRAGON -> info.graphKind = DGCommon.DRAGONCURVE
            TRIANGLE -> info.graphKind = DGCommon.FOLDTRIANGLE
            CCURVE -> info.graphKind = DGCommon.CCURVE
        }
        info.isRecursive = true
        setRelativePoint()
    }

    override val pointMax: Int
        get() {
            nOrders = 2.0.pow((info.complexity - 1).toDouble()).toInt()
            return nOrders + 1
        }

    override fun allocatePoints() {
        while (point.size > pointMax) point.removeAt(0)
        while (point.size < pointMax) point.add(PointF())
        pointBase.clear()

        orderPoints = Array(nOrders){ Point() }
        calculateOrder()
    }


    public override fun setRelativePoint() {
        allocatePoints()

        // まず、折り曲げる前の線分を作る。
        pointBase.add(0, PointF(GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MID))
        pointBase.add(1, PointF(GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MID))

        setRelRecursivePoint(1, GRAPH_FOLD_INIT_ARM_RATE, GRAPH_FOLD_INIT_THETA_RATE)
        isAllocated = true
    }

    private fun setRelRecursivePoint(depth: Int, parentLength: Float, parentDegree: Float) {
        var length = parentLength
        var degree = parentDegree
        if (depth >= info.complexity) return

        // 折り曲げた後の線分のベクトルを計算していく。
        var addPoint: PointF
        length *= info.mutation.size  + info.randomize.size  * Math.random().toFloat()
        degree *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.getInstance().sin(degree.toInt())
        val cos = SinInt.getInstance().cos(degree.toInt())
        var src: PointF
        var dst: PointF
        var vct: PointF

        for (i in 2.0.pow((depth - 1).toDouble()).toInt() downTo 1) {
            src = pointBase[i]
            dst = pointBase[i - 1]
            vct = PointF(dst.x - src.x, dst.y - src.y)

            addPoint = if (isLeftFold(i, depth))
                PointF(src.x + length * (cos * vct.x - sin * vct.y),
                       src.y + length * (sin * vct.x + cos * vct.y))
            else
                PointF(src.x + length * (cos * vct.x + sin * vct.y),
                       src.y + length * (-sin * vct.x + cos * vct.y))
            pointBase.add(i, addPoint)
        }
        setRelRecursivePoint(depth + 1, length, degree)
    }

    private fun isLeftFold(i: Int, depth: Int): Boolean {
        return when (curve_kind) {
            DRAGON   -> i % 2 == 0
            TRIANGLE -> (i + depth) % 2 == 0
            CCURVE   -> true
            else     -> false
        }
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            orderPoints[pointMax - 1 - i] = Point(src, i)
        }
    }

    companion object {
        const val DRAGON = 0
        const val TRIANGLE = 1
        const val CCURVE = 2

        private const val GRAPH_FOLD_INIT_ARM_RATE   = 0.70710678f
        private const val GRAPH_FOLD_INIT_THETA_RATE = 45.0f
    }
}
