package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * ランダム(固定)
 */
class Random : Graph() {
    init {
        info.graph_kind = DGCommon.RANDOMSHAPE
    }

    public override fun setRelativePoint() {
        allocatePoints()
        for (i in 0 until pointMax) {
            point_base[i].set(GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat(),
                    GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat())
        }
        is_allocated = true
    }
}
