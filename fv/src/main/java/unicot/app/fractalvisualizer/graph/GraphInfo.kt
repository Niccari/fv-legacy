package unicot.app.fractalvisualizer.graph

import android.graphics.PointF
import unicot.app.fractalvisualizer.graph.Graph.Companion.BRUSHTYPE_LINE
import unicot.app.fractalvisualizer.graph.Graph.Companion.DRAW_ALL
import unicot.app.fractalvisualizer.graph.Graph.Companion.DRAW_IN_ORDER
import unicot.app.fractalvisualizer.model.DimensionF
import unicot.app.fractalvisualizer.model.GraphDisplacement

/**
 * データクラス
 */
class GraphInfo {

    /*
         * グラフの種類(定数値はDGCommonクラスにて定義)
         */
    var graph_kind: Int = 0
    /*
         * 描画方法(定数値はGraphクラスにて定義)
         */
    var draw_kind: Int = 0
    /*
         * 再帰曲線のグラフか？
         */
    var is_recursive: Boolean = false
    /*
         * 線の太さ
         */
    var mLineThickness: Float = 0.toFloat()
    /*
         * アンチエイリアスフラグ
         */
    var mIsAntiAlias: Boolean = false
    /*
         * 各色描画フラグ
         */
    var mIsColorEach: Boolean = false
    /*
         * 個々描画時の描画線分数
         */
    var mEachLineHistory: Int = 0
    /*
         * ブラシの種別(定数値はGraphクラスにて定義)
         */
    var mBrushType: Int = 0
    /*
         * (DRAW_IN_ORDER時)現在の描画位置
         */
    var mCurrentDrawOrder: Int = 0

    /*
         * グラフの中心位置
         */
    var pos: PointF
    /*
         * サイズ(画面に対する相対値, > 0.0)
         */
    var size: DimensionF
    /*
         * 固定角度angle
         */
    var angle: Float = 0.toFloat()
    /*
         * 角速度rot_speed
         */
    var rot_speed: Float = 0.toFloat()

    /*
         * グラフの複雑さ
         */
    var complexity: Int = 0

    /*
         * (再帰曲線のみ有効) グラフの変形率
         */
    var mutation: GraphDisplacement
    /*
         * (再帰曲線のみ有効) グラフの乱雑度
         */
    var randomize: GraphDisplacement

    /*
         * 色の遷移パターン(現在の色なども管理)
         */
    var cp: ColorPattern

    /**
     * Constructor. グラフ情報を初期化する
     */
    init {
        /* グラフ系 */
        pos = PointF(GRAPH_POS_X_INIT, GRAPH_POS_Y_INIT)
        size = DimensionF(GRAPH_SIZE_WIDTH_INIT, GRAPH_SIZE_HEIGHT_INIT)

        angle = GRAPH_ANGLE_INIT
        rot_speed = GRAPH_ROT_SPEED_INIT

        mutation = GraphDisplacement(GRAPH_MUTATION_SCALE_INIT, GRAPH_MUTATION_ANGLE_INIT)
        randomize = GraphDisplacement(GRAPH_RANDOMIZE_SCALE_INIT, GRAPH_RANDOMIZE_ANGLE_INIT)

        cp = ColorPattern()

        complexity = COMPLEXITY_INIT

        is_recursive = GRAPH_IS_RECURSIVE_INIT

        /* Draw系*/
        mCurrentDrawOrder = GRAPH_DRAW_CUR_ORDER_INIT

        draw_kind = GRAPH_DRAW_KIND_INIT

        mLineThickness = GRAPH_DRAW_THICKNESS_INIT.toFloat()
        mEachLineHistory = GRAPH_DRAW_HISTORY_INIT
        mIsAntiAlias = GRAPH_DRAW_IS_ANTIALIAS_INIT
        mIsColorEach = GRAPH_DRAW_IS_COLOREACH_INIT
        mBrushType = GRAPH_DRAW_BRUSHTYPE_INIT
    }

    /**
     * 描画設定を一括設定する
     */
    fun setDrawSettings(kind: String, thickness: Float, antialias: Boolean, colorEach: Boolean, history: Int, corder: Int, brush: Int) {
        if (kind.matches("each".toRegex())) {
            draw_kind = DRAW_IN_ORDER
        } else {
            draw_kind = DRAW_ALL
        }
        if (thickness > 0.0f)
            mLineThickness = thickness
        mIsAntiAlias = antialias
        mIsColorEach = colorEach
        if (history > 0)
            mEachLineHistory = history
        if (corder > 0)
            mCurrentDrawOrder = corder
        if (brush > 0)
            mBrushType = brush
    }

    /**
     * 描画設定を一括設定する
     *
     * @param kind
     * 描画方法の値(全描画、一部描画)
     * @param thickness
     * 線の太さ
     * @param antialias
     * アンチエイリアスフラグ
     * @param colorEach
     * 色を線分ごとに変化させるか？
     * @param history
     * 一部描画時に何本線分を描画するか？
     */
    internal fun setDrawSettings(kind: Int, thickness: Float, antialias: Boolean, colorEach: Boolean, history: Int) {
        draw_kind = kind
        mLineThickness = thickness
        mIsAntiAlias = antialias
        mIsColorEach = colorEach
        mEachLineHistory = history
    }

    companion object {
        /*
         * グラフ位置の最小値(相対座標)
         */
        val GRAPH_POS_MIN = -1.0f
        /*
         * グラフ位置の最大値(相対座標)
         */
        val GRAPH_POS_MAX = 1.0f
        /*
         * グラフ位置の中間値(相対座標)
         */
        val GRAPH_POS_MID = (GRAPH_POS_MAX + GRAPH_POS_MIN) / 2

        /*
         * グラフサイズの最小値(相対座標)
         */
        val GRAPH_SIZE_MIN = 0.0f
        /*
         * グラフサイズの最大値(相対座標)
         */
        val GRAPH_SIZE_MAX = 2.0f
        /*
         * グラフサイズの中間値(相対座標)
         */
        val GRAPH_SIZE_MID = (GRAPH_SIZE_MAX + GRAPH_SIZE_MIN) / 2

        val GRAPH_ROTATE_MIN = -10.0f
        val GRAPH_ROTATE_MAX =  10.0f
        /*
         * 初期設定：グラフの複雑さ
         */
        val COMPLEXITY_INIT = 3

        // 初期設定：グラフ
        private val GRAPH_POS_X_INIT = 0.0f
        private val GRAPH_POS_Y_INIT = 0.0f

        private val GRAPH_SIZE_WIDTH_INIT = 0.5f
        private val GRAPH_SIZE_HEIGHT_INIT = 0.5f

        private val GRAPH_ANGLE_INIT = 0.0f
        private val GRAPH_ROT_SPEED_INIT = 0.0f

        private val GRAPH_MUTATION_SCALE_INIT = 1.0f
        private val GRAPH_MUTATION_ANGLE_INIT = 1.0f

        private val GRAPH_RANDOMIZE_SCALE_INIT = 0.0f
        private val GRAPH_RANDOMIZE_ANGLE_INIT = 0.0f

        private val GRAPH_IS_RECURSIVE_INIT = false

        // 初期設定：描画
        private val GRAPH_DRAW_CUR_ORDER_INIT = 0
        private val GRAPH_DRAW_KIND_INIT = DRAW_ALL
        private val GRAPH_DRAW_THICKNESS_INIT = 3
        private val GRAPH_DRAW_HISTORY_INIT = 10
        private val GRAPH_DRAW_IS_ANTIALIAS_INIT = true
        private val GRAPH_DRAW_IS_COLOREACH_INIT = false
        private val GRAPH_DRAW_BRUSHTYPE_INIT = BRUSHTYPE_LINE
    }
}