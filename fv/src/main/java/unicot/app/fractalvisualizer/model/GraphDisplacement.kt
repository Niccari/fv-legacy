package unicot.app.fractalvisualizer.model

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
        const val SIZE_MIN  = -1.5f
        const val SIZE_INIT = 1.0f
        const val SIZE_MAX  = 1.5f

        const val ANGLE_MIN  = -2.0f
        const val ANGLE_INIT = 0.0f
        const val ANGLE_MAX  = 2.0f
    }
}
