package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 葉脈(あるいは木の形)
 */
class Leaf : Graph() {

    private var branch: Int = 0

    init {
        complexityMin = 1
        complexityMax = 5

        branch = GRAPH_LEAF_INIT_BRANCH
        info.graph_kind = DGCommon.LEAF

        is_allocated = false
        info.is_recursive = true
    }

    override val pointMax: Int
        get() {
            val nm: Int

            if (branch != 1) {
                nm = branch * (1 - Math.pow(branch.toDouble(), info.complexity.toDouble()).toInt()) / (1 - branch) + 1
            } else {
                nm = 1 + info.complexity
            }
            n_orders = nm
            return 2 * n_orders
        }

    fun getBranch(): Int {
        return branch
    }

    fun setBranch(b: Int) {
        branch = b
        is_allocated = false
    }

    override fun allocatePoints() {
        while (point.size > pointMax)
            point.removeAt(0)
        while (point.size < pointMax)
            point.add(PointF())
        point_base.clear()

        order_points = Array(n_orders){ Point() }
        calculateOrder()
    }

    public override fun setRelativePoint() {
        allocatePoints()

        point_base.add(0, PointF(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MIN))
        point_base.add(1, PointF(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MID))

        setRelRecursivePoint(1, 0, GRAPH_LEAF_INIT_ARM_RATE, GRAPH_LEAF_INIT_THETA_RATE)
        is_allocated = true
    }

    private fun setRelRecursivePoint(base: Int, parent_index: Int, arm: Float, theta: Float) {
        if (base > info.complexity)
            return

        val next_parent = IntArray(branch)

        var child_arm: Float
        var child_theta: Float
        var sin: Float
        var cos: Float

        for (i in 0 until branch) {
            val diff = 1.0f * i.toFloat() / branch + 1.0f / (2.0f * branch)

            val parent_src = point_base[parent_index]
            val parent_dst = point_base[parent_index + 1]
            val vct = PointF(parent_dst.x - parent_src.x, parent_dst.y - parent_src.y) // 親枝
            val child_src = PointF(parent_src.x + vct.x * diff, parent_src.y + vct.y * diff)

            child_arm = arm * 1.0f * (info.mutation.size + info.randomize.size * Math.random().toFloat())
            child_theta = theta * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            sin = SinInt.SI().sin(child_theta.toInt())
            cos = SinInt.SI().cos(child_theta.toInt())
            if (i % 2 == 1)
                sin = -sin

            point_base.add(child_src)
            next_parent[i] = point_base.size - 1
            point_base.add(PointF(child_src.x + child_arm * (cos * vct.x - sin * vct.y), child_src.y + child_arm * (sin * vct.x + cos * vct.y)))
        }

        for (i in 0 until branch) { // 各枝について、更に小枝を計算
            child_arm = arm * 1.0f * (info.mutation.size + info.randomize.size * Math.random().toFloat())
            child_theta = theta * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            setRelRecursivePoint(base + 1, next_parent[i], child_arm, child_theta)
        }
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax / 2) {
            val src = 2 * i
            val dst = 2 * i + 1
            order_points[i] = Point(src, dst)
        }
    }

    companion object {
        private val GRAPH_LEAF_INIT_ARM_RATE = 0.8f
        private val GRAPH_LEAF_INIT_THETA_RATE = 30.0f

        private val GRAPH_LEAF_INIT_BRANCH = 3
    }
}
