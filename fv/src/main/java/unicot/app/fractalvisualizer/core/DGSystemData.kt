package unicot.app.fractalvisualizer.core

/**
 * 環境設定値(リフレッシュレートなど)を管理する
 */
class DGSystemData internal constructor() {
    var framerate: Int = 0
        set(frate){
            field = frate
            if (field < FRAMERATE_MIN) field = FRAMERATE_MIN
            if (field > FRAMERATE_MAX) field = FRAMERATE_MAX
        }

    /**
     * 許容メモリ負荷率を取得
     * @return 許容メモリ負荷率
     */
    var memoryUsage: Int = 0
        private set
    /**
     * 許容CPU負荷率を取得
     * @return 許容CPU負荷率
     */
    var loadUsage: Int = 0
        private set
    /**
     * GUIオフ時間を取得
     * @return GUIオフ時間
     */
    var dismissTime: Int = 0
        private set
    private var view_alpha: Int = 0
    private var pov_frame: Int = 0
    /**
     * インディケーター表示フラグを取得
     * @return インディケーター表示フラグ
     */
    var isIndicator: Boolean = false
        private set
    /**
     * グラフのバージョン情報を取得
     * @return グラフのバージョン情報
     */
    /**
     * グラフのバージョン情報を設定
     */
    var graphVersion: String? = null

    /**
     * 背景透明度を取得
     * @return 背景透明度
     */
    /**
     * 背景透明度を設定
     * @param alpha 背景透明度
     */
    var viewAlpha
        get() = view_alpha
        set(alpha) {
            view_alpha = alpha
            if (view_alpha < VIEW_ALPHA_MIN) view_alpha = VIEW_ALPHA_MIN
            if (view_alpha > VIEW_ALPHA_MAX) view_alpha = VIEW_ALPHA_MAX
        }

    /**
     * 残像フレーム数を取得
     * @return 残像フレーム数
     */
    /**
     * 残像フレーム数を設定
     * @param pov 残像フレーム数
     */
    var povFrame: Int
        get() = pov_frame
        set(pov) {
            pov_frame = pov
            if (pov_frame < POV_FRAME_MIN) pov_frame = POV_FRAME_MIN
            if (pov_frame > POV_FRAME_MAX) pov_frame = POV_FRAME_MAX
        }

    init {
        framerate = FRAMERATE_INIT
        memoryUsage = LOAD_MEMORY_INIT
        loadUsage = LOAD_USAGE_INIT

        isIndicator = IS_INDICATOR_INIT
        dismissTime = GUI_DISMISS_TIME_INIT

        view_alpha = VIEW_ALPHA_INIT
        pov_frame = POV_FRAME_INIT

        graphVersion = ""
    }

    /**
     * リフレッシュレート、許容メモリ負荷率、許容CPU負荷率を設定
     * @param frate リフレッシュレート
     * @param mem   許容メモリ負荷率
     * @param load  許容CPU負荷率
     */
    operator fun set(frate: Int, mem: Int, load: Int) {
        framerate = frate
        set(mem, load)
    }

    /**
     * 許容メモリ負荷率、許容CPU負荷率を設定
     * @param mem   許容メモリ負荷率
     * @param load  許容CPU負荷率
     */
    operator fun set(mem: Int, load: Int) {
        memoryUsage = mem
        if (memoryUsage < LOAD_MEMORY_MIN) memoryUsage = LOAD_MEMORY_MIN
        if (memoryUsage > LOAD_MEMORY_MAX) memoryUsage = LOAD_MEMORY_MAX

        loadUsage = load
        if (loadUsage < LOAD_USAGE_MIN) loadUsage = LOAD_USAGE_MIN
        if (loadUsage > LOAD_USAGE_MAX) loadUsage = LOAD_USAGE_MAX
    }

    companion object {
        /**
         * グラフ更新・描画周期：最小値
         */
        private val FRAMERATE_MIN = 1
        /**
         * グラフ更新・描画周期：最大値
         */
        private val FRAMERATE_MAX = 60

        /**
         * 許容メモリ負荷率：最小値
         */
        private val LOAD_MEMORY_MIN = 30
        /**
         * 許容メモリ負荷率：最大値
         */
        private val LOAD_MEMORY_MAX = 100

        /**
         * 許容CPU負荷率：最小値
         */
        private val LOAD_USAGE_MIN = 30
        /**
         * 許容CPU負荷率：最大値
         */
        private val LOAD_USAGE_MAX = 100

        /**
         * 残像フレーム数：最小値
         */
        val POV_FRAME_MIN = 0
        /**
         * 残像フレーム数：最大値
         */
        private val POV_FRAME_MAX = 255

        /**
         * 背景透明度：最小値
         */
        private val VIEW_ALPHA_MIN = 0
        /**
         * 背景透明度：最大値
         */
        val VIEW_ALPHA_MAX = 255

        private val FRAMERATE_INIT = 60
        private val LOAD_MEMORY_INIT = 50
        private val LOAD_USAGE_INIT = 50
        private val VIEW_ALPHA_INIT = 255
        private val POV_FRAME_INIT = 0

        private val IS_INDICATOR_INIT = false
        private val GUI_DISMISS_TIME_INIT = 30
    }
}
