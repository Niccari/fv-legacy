package unicot.app.fractalvisualizer.graph

import android.graphics.Point

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * 星型
 */
class NStar : Graph() {
    init {
        complexityMin = 3
        complexityMax = 12
        info.graph_kind = DGCommon.NSTAR
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax) {
            val dst = if (i + 2 >= pointMax) i + 2 - pointMax else i + 2
            orderPoints[i] = Point(i, dst)
        }
    }
}
