package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 花火型
 */
class Starmine : Graph() {
    init {
        info.graph_kind = DGCommon.STARMINE
    }

    override val pointMax: Int
        get() {
            nOrders = 2 * info.complexity
            return nOrders
        }

    public override fun setRelativePoint() {
        allocatePoints()
        for (i in 0 until pointMax) {
            val degree = 360 * i / pointMax - 180
            if (i % 2 == 0) {
                pointBase[i].set(SinInt.SI().cos(degree),
                        SinInt.SI().sin(degree))
            } else {
                pointBase[i].set(SinInt.SI().cos(degree) / 4,
                        SinInt.SI().sin(degree) / 4)
            }
        }
    }
}
