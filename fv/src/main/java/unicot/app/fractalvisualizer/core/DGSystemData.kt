package unicot.app.fractalvisualizer.core

/**
 * 環境設定値(リフレッシュレートなど)を管理する
 */
class DGSystemData internal constructor() {
    var framerate: Int = FRAMERATE_INIT
        set(field0){
            field = framerateList.findLast{ it <= field0 } ?: FRAMERATE_INIT
        }
    val framerateList = intArrayOf(1, 6, 10, 30, 60)
    var graphVersion: String = ""
    var povFrame: Int = POV_FRAME_MIN
        set(povFrame) {
            field = when{
                field < POV_FRAME_MIN -> POV_FRAME_MIN
                field > POV_FRAME_MAX -> POV_FRAME_MAX
                else -> povFrame
            }
        }

    companion object {
        private const val FRAMERATE_INIT = 60

        private const val POV_FRAME_MIN = 0
        private const val POV_FRAME_MAX = 255
    }
}
