package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.SinInt

/**
 * 花火型
 */
class Starmine : Graph() {
    init {
        info.graphKind = DGCommon.STARMINE
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
                pointBase[i].set(SinInt.getInstance().cos(degree),
                        SinInt.getInstance().sin(degree))
            } else {
                pointBase[i].set(SinInt.getInstance().cos(degree) / 4,
                        SinInt.getInstance().sin(degree) / 4)
            }
        }
    }
}
