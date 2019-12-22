package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * バラ曲線
 */
class Rose : Graph() {
    init {
        complexityMin = 1
        complexityMax = 8
        info.graphKind = DGCommon.GraphKind.ROSECURVE
    }

    override val pointMax: Int
        get() {
            nOrders = GRAPH_ROSE_DENSITY * info.complexity
            return nOrders
        }

    public override fun setRelativePoint() {
        var nsin: Float
        allocatePoints()
        for (i in 0 until pointMax) {
            nsin = SinInt.getInstance().sin(360 * info.complexity * i / pointMax)
            pointBase[i].set(nsin * SinInt.getInstance().cos(360 * i / pointMax - 180), nsin * SinInt.getInstance().sin(360 * i / pointMax - 180))
        }
        isAllocated = true
    }

    companion object {
        private const val GRAPH_ROSE_DENSITY = 30
    }
}
