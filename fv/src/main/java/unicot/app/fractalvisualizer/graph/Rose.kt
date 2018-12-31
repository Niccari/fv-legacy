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
        info.graph_kind = DGCommon.ROSECURVE
    }

    override val pointMax: Int
        get() {
            n_orders = GRAPH_ROSE_DENSITY * info.complexity
            return n_orders
        }

    public override fun setRelativePoint() {
        var nsin: Float
        allocatePoints()
        for (i in 0 until pointMax) {
            nsin = SinInt.SI().sin(360 * info.complexity * i / pointMax)
            point_base[i].set(nsin * SinInt.SI().cos(360 * i / pointMax - 180), nsin * SinInt.SI().sin(360 * i / pointMax - 180))
        }
        is_allocated = true
    }

    companion object {
        private val GRAPH_ROSE_DENSITY = 30
    }
}
