package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 再帰曲線(ドラゴン曲線、三角曲線、LeviのCカーブ)
 */
class FoldCurve(private val curve_kind: Int) : Graph() {

    init {
        complexityMin = 2
        complexityMax = 10

        when (curve_kind) {
            DRAGON -> info.graph_kind = DGCommon.DRAGONCURVE
            TRIANGLE -> info.graph_kind = DGCommon.FOLDTRIANGLE
            CCURVE -> info.graph_kind = DGCommon.CCURVE
        }
        info.is_recursive = true
        setRelativePoint()
    }

    override val pointMax: Int
        get() {
            nOrders = Math.pow(2.0, (info.complexity - 1).toDouble()).toInt()
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

    private fun setRelRecursivePoint(depth: Int, arm: Float, theta: Float) {
        var arm = arm
        var theta = theta
        if (depth >= info.complexity) return

        // 折り曲げた後の線分のベクトルを計算していく。
        var add_point: PointF
        arm *= info.mutation.size + info.randomize.size * Math.random().toFloat()
        theta *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.SI().sin(theta.toInt())
        val cos = SinInt.SI().cos(theta.toInt())
        var src: PointF
        var dst: PointF
        var vct: PointF

        for (i in Math.pow(2.0, (depth - 1).toDouble()).toInt() downTo 1) {
            src = pointBase[i]
            dst = pointBase[i - 1]
            vct = PointF(dst.x - src.x, dst.y - src.y)

            if (isLeftFold(i, depth))
                add_point = PointF(src.x + arm * (cos * vct.x - sin * vct.y),
                        src.y + arm * (sin * vct.x + cos * vct.y))
            else
                add_point = PointF(src.x + arm * (cos * vct.x + sin * vct.y),
                        src.y + arm * (-sin * vct.x + cos * vct.y))
            pointBase.add(i, add_point)
        }
        setRelRecursivePoint(depth + 1, arm, theta)
    }

    private fun isLeftFold(i: Int, depth: Int): Boolean {
        when (curve_kind) {
            DRAGON -> return i % 2 == 0
            TRIANGLE -> return (i + depth) % 2 == 0
            CCURVE -> return true
            else -> return false
        }
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            orderPoints[pointMax - 1 - i] = Point(src, i)
        }
    }

    companion object {
        val DRAGON = 0
        val TRIANGLE = 1
        val CCURVE = 2

        private val GRAPH_FOLD_INIT_ARM_RATE = 1.0f / Math.sqrt(2.0).toFloat()
        private val GRAPH_FOLD_INIT_THETA_RATE = 45.0f
    }
}
