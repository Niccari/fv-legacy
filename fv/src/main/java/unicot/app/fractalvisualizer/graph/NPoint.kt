package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * 正多角形
 */
class NPoint : Graph() {
    init {
        info.graphKind = DGCommon.GraphKind.NPOINT
    }
}
