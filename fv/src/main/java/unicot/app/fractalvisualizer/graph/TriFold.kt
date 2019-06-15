package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 再帰曲線
 */
class TriFold(private val curve_kind: Int // カーブの種類(上記DRAGONなどが入る。)
) : Graph() {

    init {
        complexityMin = 1
        complexityMax = 7

        this.info.complexity = complexityMin

        when (curve_kind) {
            CIS -> info.graph_kind = DGCommon.TRIFOLD_CIS
            TRANS -> info.graph_kind = DGCommon.TRIFOLD_TRANS
        }
        info.is_recursive = true
        setRelativePoint()
    }


    override val pointMax: Int
        get() {
            nOrders = Math.pow(4.0, info.complexity.toDouble()).toInt()
            return nOrders + 1
        }

    override fun allocatePoints() {
        while (point.size > pointMax)
            point.removeAt(0)
        while (point.size < pointMax)
            point.add(PointF())
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
        if (depth > info.complexity)
            return

        // 折り曲げた後の線分のベクトルを計算していく。
        var add_point: PointF
        arm *= info.mutation.size + info.randomize.size * Math.random().toFloat()
        theta *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.SI().sin(theta.toInt())
        val cos = SinInt.SI().cos(theta.toInt())
        var src: PointF
        var mid: PointF
        var dst: PointF
        var vct_sm: PointF
        var vct_md: PointF

        for (i in Math.pow(4.0, (depth - 1).toDouble()).toInt() downTo 1) {
            src = pointBase[i]
            dst = pointBase[i - 1]

            mid = PointF((dst.x + src.x) / 2.0f, (dst.y + src.y) / 2.0f)

            vct_sm = PointF(mid.x - src.x, mid.y - src.y)
            vct_md = PointF(dst.x - mid.x, dst.y - mid.y)
            if (curve_kind == CIS) {
                add_point = PointF(src.x - arm * (cos * vct_sm.x + sin * vct_sm.y), src.y - arm * (-sin * vct_sm.x + cos * vct_sm.y))
                pointBase.add(i, add_point)
                pointBase.add(i, mid)

                add_point = PointF(dst.x + arm * (cos * vct_md.x - sin * vct_md.y), dst.y + arm * (sin * vct_md.x + cos * vct_md.y))
                pointBase.add(i, add_point)
            } else {
                if (i % 2 == 0) {
                    add_point = PointF(src.x + arm * (cos * vct_sm.x - sin * vct_sm.y), src.y + arm * (sin * vct_sm.x + cos * vct_sm.y))
                    pointBase.add(i, add_point)
                    pointBase.add(i, mid)
                    add_point = PointF(dst.x - arm * (cos * vct_md.x - sin * vct_md.y), dst.y - arm * (sin * vct_md.x + cos * vct_md.y))
                    pointBase.add(i, add_point)
                } else {
                    add_point = PointF(src.x + arm * (cos * vct_sm.x + sin * vct_sm.y), src.y + arm * (-sin * vct_sm.x + cos * vct_sm.y))
                    pointBase.add(i, add_point)
                    pointBase.add(i, mid)
                    add_point = PointF(dst.x - arm * (cos * vct_md.x + sin * vct_md.y), dst.y - arm * (-sin * vct_md.x + cos * vct_md.y))
                    pointBase.add(i, add_point)
                }

            }
        }
        setRelRecursivePoint(depth + 1, arm, theta)
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            orderPoints[pointMax - 1 - i] = Point(src, i)
        }
    }

    companion object {
        val CIS = 0
        val TRANS = 1

        private val GRAPH_FOLD_INIT_ARM_RATE = 1.0f
        private val GRAPH_FOLD_INIT_THETA_RATE = 90.0f
    }
}
