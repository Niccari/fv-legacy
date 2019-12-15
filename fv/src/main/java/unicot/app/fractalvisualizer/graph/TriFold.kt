package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt
import kotlin.math.pow

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
            CIS -> info.graphKind = DGCommon.TRIFOLD_CIS
            TRANS -> info.graphKind = DGCommon.TRIFOLD_TRANS
        }
        info.isRecursive = true
        setRelativePoint()
    }


    override val pointMax: Int
        get() {
            nOrders = 4.0.pow(info.complexity.toDouble()).toInt()
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

        setRelRecursivePoint(1, 1.0f, 90.0f)
        isAllocated = true
    }

    private fun setRelRecursivePoint(depth: Int, parentLength: Float, parentDegree: Float) {
        var length = parentLength
        var degree = parentDegree
        if (depth > info.complexity)
            return

        // 折り曲げた後の線分のベクトルを計算していく。
        var addPoint: PointF
        length *= info.mutation.size + info.randomize.size * Math.random().toFloat()
        degree *= info.mutation.angle + info.randomize.angle * Math.random().toFloat()
        val sin = SinInt.getInstance().sin(degree.toInt())
        val cos = SinInt.getInstance().cos(degree.toInt())
        var src: PointF
        var mid: PointF
        var dst: PointF
        var smVector: PointF
        var midVector: PointF

        for (i in 4.0.pow((depth - 1).toDouble()).toInt() downTo 1) {
            src = pointBase[i]
            dst = pointBase[i - 1]

            mid = PointF((dst.x + src.x) / 2.0f, (dst.y + src.y) / 2.0f)

            smVector = PointF(mid.x - src.x, mid.y - src.y)
            midVector = PointF(dst.x - mid.x, dst.y - mid.y)
            if (curve_kind == CIS) {
                addPoint = PointF(src.x - length * (cos * smVector.x + sin * smVector.y), src.y - length * (-sin * smVector.x + cos * smVector.y))
                pointBase.add(i, addPoint)
                pointBase.add(i, mid)

                addPoint = PointF(dst.x + length * (cos * midVector.x - sin * midVector.y), dst.y + length * (sin * midVector.x + cos * midVector.y))
                pointBase.add(i, addPoint)
            } else {
                if (i % 2 == 0) {
                    addPoint = PointF(src.x + length * (cos * smVector.x - sin * smVector.y), src.y + length * (sin * smVector.x + cos * smVector.y))
                    pointBase.add(i, addPoint)
                    pointBase.add(i, mid)
                    addPoint = PointF(dst.x - length * (cos * midVector.x - sin * midVector.y), dst.y - length * (sin * midVector.x + cos * midVector.y))
                    pointBase.add(i, addPoint)
                } else {
                    addPoint = PointF(src.x + length * (cos * smVector.x + sin * smVector.y), src.y + length * (-sin * smVector.x + cos * smVector.y))
                    pointBase.add(i, addPoint)
                    pointBase.add(i, mid)
                    addPoint = PointF(dst.x - length * (cos * midVector.x + sin * midVector.y), dst.y - length * (-sin * midVector.x + cos * midVector.y))
                    pointBase.add(i, addPoint)
                }

            }
        }
        setRelRecursivePoint(depth + 1, length, degree)
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            orderPoints[pointMax - 1 - i] = Point(src, i)
        }
    }

    companion object {
        const val CIS = 0
        const val TRANS = 1
    }
}
