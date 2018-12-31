package unicot.app.fractalvisualizer.struct

/**
 * グラフの変形量データ(大きさ・角度)
 */
class GraphDisplacement constructor(size0: Float = SIZE_INIT, angle0: Float = ANGLE_INIT) {
    var size: Float = size0
        set(field0) {
            field = field0
            if (SIZE_MIN > field) field = SIZE_MIN
            if (SIZE_MAX < field) field = SIZE_MAX
        }

    var angle: Float = angle0
        set(field0) {
            field = field0
            if (ANGLE_MIN > field) field = ANGLE_MIN
            if (ANGLE_MAX < field) field = ANGLE_MAX
        }

    companion object {
        val SIZE_MIN  = -1.5f
        val SIZE_INIT = 1.0f
        val SIZE_MAX  = 1.5f

        val ANGLE_MIN  = -2.0f
        val ANGLE_INIT = 0.0f
        val ANGLE_MAX  = 2.0f
    }
}
