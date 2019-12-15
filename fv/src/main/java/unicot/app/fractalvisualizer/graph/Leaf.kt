package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt
import kotlin.math.pow

/**
 * 葉脈(あるいは木の形)
 */
class Leaf : Graph() {

    private var branch: Int = 0

    init {
        complexityMin = 1
        complexityMax = 5

        branch = GRAPH_LEAF_INIT_BRANCH
        info.graphKind = DGCommon.LEAF

        isAllocated = false
        info.isRecursive = true
    }

    override val pointMax: Int
        get() {
            val nm: Int = if (branch != 1) {
                branch * (1 - branch.toDouble().pow(info.complexity.toDouble()).toInt()) / (1 - branch) + 1
            } else {
                1 + info.complexity
            }
            nOrders = nm
            return 2 * nOrders
        }

    fun getBranch(): Int {
        return branch
    }

    fun setBranch(b: Int) {
        branch = b
        isAllocated = false
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

        pointBase.add(0, PointF(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MIN))
        pointBase.add(1, PointF(GraphInfo.GRAPH_POS_MID, GraphInfo.GRAPH_POS_MID))

        setRelRecursivePoint(1, 0, GRAPH_LEAF_INIT_ARM_RATE, GRAPH_LEAF_INIT_THETA_RATE)
        isAllocated = true
    }

    private fun setRelRecursivePoint(base: Int, parentIndex: Int, parentLength: Float, parentDegree: Float) {
        if (base > info.complexity)
            return

        val nextParent = IntArray(branch)

        var length: Float
        var degree: Float
        var sin: Float
        var cos: Float

        for (i in 0 until branch) {
            val diff = 1.0f * i.toFloat() / branch + 1.0f / (2.0f * branch)

            val parentSrcPoint = pointBase[parentIndex]
            val parentDstPoint = pointBase[parentIndex + 1]
            val vct = PointF(parentDstPoint.x - parentSrcPoint.x, parentDstPoint.y - parentSrcPoint.y) // 親枝
            val childSrcPoint = PointF(parentSrcPoint.x + vct.x * diff, parentSrcPoint.y + vct.y * diff)

            length = parentLength * 1.0f * (info.mutation.size + info.randomize.size * Math.random().toFloat())
            degree = parentDegree * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            sin = SinInt.getInstance().sin(degree.toInt())
            cos = SinInt.getInstance().cos(degree.toInt())
            if (i % 2 == 1)
                sin = -sin

            pointBase.add(childSrcPoint)
            nextParent[i] = pointBase.size - 1
            pointBase.add(PointF(childSrcPoint.x + length * (cos * vct.x - sin * vct.y), childSrcPoint.y + length * (sin * vct.x + cos * vct.y)))
        }

        for (i in 0 until branch) { // 各枝について、更に小枝を計算
            length = parentLength * 1.0f * (info.mutation.size + info.randomize.size * Math.random().toFloat())
            degree = parentDegree * (info.mutation.angle + info.randomize.angle * Math.random().toFloat())

            setRelRecursivePoint(base + 1, nextParent[i], length, degree)
        }
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax / 2) {
            val src = 2 * i
            val dst = 2 * i + 1
            orderPoints[i] = Point(src, dst)
        }
    }

    companion object {
        private const val GRAPH_LEAF_INIT_ARM_RATE = 0.8f
        private const val GRAPH_LEAF_INIT_THETA_RATE = 30.0f

        private const val GRAPH_LEAF_INIT_BRANCH = 3
    }
}
