package unicot.app.fractalvisualizer.core

/**
 * 環境設定値(リフレッシュレートなど)を管理する
 */
class DGSystemData internal constructor() {
    var framerate: Int = 0
        set(framerate){
            field = when{
                field < FRAMERATE_MIN -> FRAMERATE_MIN
                field > FRAMERATE_MAX -> FRAMERATE_MAX
                else -> framerate
            }
        }
    var graphVersion: String? = null
    var povFrame: Int = 0
        set(povFrame) {
            field = when{
                field < POV_FRAME_MIN -> POV_FRAME_MIN
                field > POV_FRAME_MAX -> POV_FRAME_MAX
                else -> povFrame
            }
        }

    init {
        framerate = FRAMERATE_INIT
        povFrame  = POV_FRAME_INIT

        graphVersion = ""
    }

    companion object {
        private const val FRAMERATE_MIN = 1
        private const val FRAMERATE_MAX = 60

        private const val POV_FRAME_MIN = 0
        private const val POV_FRAME_MAX = 255

        private const val FRAMERATE_INIT = 60
        private const val POV_FRAME_INIT = 0
    }
}
