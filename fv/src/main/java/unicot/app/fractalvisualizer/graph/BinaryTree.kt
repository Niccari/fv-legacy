package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 二分木
 */
class BinaryTree : Graph() {
    init {
        complexityMin = 2
        complexityMax = 8
        info.graph_kind = DGCommon.BINARYTREE
        info.is_recursive = true
    }

    override val pointMax: Int
        get() {
            nOrders = Math.pow(2.0, (info.complexity + 1).toDouble()).toInt() + 1 - 2
            return nOrders + 2
        }

    public override fun setRelativePoint() {
        allocatePoints()

        pointBase[0].set(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MIN)
        pointBase[1].set(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MID)

        setRelRecursivePoint(1, GRAPH_BINTREE_INIT_ARM_RATE, GRAPH_BINTREE_INIT_THETA_RATE)
        isAllocated = true
    }

    private fun setRelRecursivePoint(base: Int, arm: Float, theta: Float) {
        if (base >= pointMax / 2)
            return

        val c_arm = FloatArray(2)
        val c_theta = FloatArray(2)
        val sin = FloatArray(2)
        val cos = FloatArray(2)

        val src = pointBase[base / 2]
        val dst = pointBase[base]
        val vct = PointF(dst.x - src.x, dst.y - src.y)

        for (i in 0..1) {
            c_arm[i] = (arm
                    * 1.0f
                    * (info.mutation.size + info.randomize.size * Math.random().toFloat()))
            c_theta[i] = theta * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            sin[i] = SinInt.SI().sin(c_theta[i].toInt())
            cos[i] = SinInt.SI().cos(c_theta[i].toInt())
            if (i == 1) {
                sin[i] = -sin[i] // 同じ深さ、同じ親枝をもつ枝同士は親枝に対し等角
            }

            pointBase[2 * base + i].set(
                    dst.x + c_arm[i] * (cos[i] * vct.x - sin[i] * vct.y),
                    dst.y + c_arm[i] * (sin[i] * vct.x + cos[i] * vct.y))
        }
        for (i in 0..1)
            setRelRecursivePoint(2 * base + i, c_arm[i], c_theta[i])
    }

    override fun calculateOrder() {
        orderPoints[0] = Point(0, 1)

        calculateOrderRecursive(1, 1)
    }

    private fun calculateOrderRecursive(base: Int, src: Int) {
        if (base >= pointMax / 2) return

        val dst_l = 2 * base
        val dst_r = 2 * base + 1

        orderPoints[dst_l - 1] = Point(src, dst_l)
        orderPoints[dst_r - 1] = Point(src, dst_r)

        calculateOrderRecursive(dst_l, dst_l)
        calculateOrderRecursive(dst_r, dst_r)
    }

    companion object {
        private val GRAPH_BINTREE_INIT_ARM_RATE = 0.85f
        private val GRAPH_BINTREE_INIT_THETA_RATE = 45.0f
    }
}
