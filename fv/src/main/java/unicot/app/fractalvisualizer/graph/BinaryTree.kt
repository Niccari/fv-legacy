package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt
import kotlin.math.pow

/**
 * 二分木
 */
class BinaryTree : Graph() {
    init {
        complexityMin = 2
        complexityMax = 8
        info.graphKind = DGCommon.BINARYTREE
        info.isRecursive = true
    }

    override val pointMax: Int
        get() {
            nOrders = 2.0.pow((info.complexity + 1).toDouble()).toInt() + 1 - 2
            return nOrders + 2
        }

    public override fun setRelativePoint() {
        allocatePoints()

        pointBase[0].set(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MIN)
        pointBase[1].set(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MID)

        setRelRecursivePoint(1, GRAPH_BINTREE_INIT_ARM_RATE, GRAPH_BINTREE_INIT_THETA_RATE)
        isAllocated = true
    }

    private fun setRelRecursivePoint(base: Int, parentLength: Float, parentDegree: Float) {
        if (base >= pointMax / 2)
            return

        val length = FloatArray(2)
        val degree = FloatArray(2)
        val sin = FloatArray(2)
        val cos = FloatArray(2)

        val src = pointBase[base / 2]
        val dst = pointBase[base]
        val vct = PointF(dst.x - src.x, dst.y - src.y)

        for (i in 0..1) {
            length[i] = (parentLength
                    * 1.0f
                    * (info.mutation.size + info.randomize.size * Math.random().toFloat()))
            degree[i] = parentDegree * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            sin[i] = SinInt.getInstance().sin(degree[i].toInt())
            cos[i] = SinInt.getInstance().cos(degree[i].toInt())
            if (i == 1) {
                sin[i] = -sin[i] // 同じ深さ、同じ親枝をもつ枝同士は親枝に対し等角
            }

            pointBase[2 * base + i].set(
                    dst.x + length[i] * (cos[i] * vct.x - sin[i] * vct.y),
                    dst.y + length[i] * (sin[i] * vct.x + cos[i] * vct.y))
        }
        for (i in 0..1)
            setRelRecursivePoint(2 * base + i, length[i], degree[i])
    }

    override fun calculateOrder() {
        orderPoints[0] = Point(0, 1)

        calculateOrderRecursive(1, 1)
    }

    private fun calculateOrderRecursive(base: Int, src: Int) {
        if (base >= pointMax / 2) return

        val dstLeft  = 2 * base
        val dstRight = 2 * base + 1

        orderPoints[dstLeft  - 1] = Point(src, dstLeft)
        orderPoints[dstRight - 1] = Point(src, dstRight)

        calculateOrderRecursive(dstLeft,  dstLeft)
        calculateOrderRecursive(dstRight, dstRight)
    }

    companion object {
        private const val GRAPH_BINTREE_INIT_ARM_RATE = 0.85f
        private const val GRAPH_BINTREE_INIT_THETA_RATE = 45.0f
    }
}
