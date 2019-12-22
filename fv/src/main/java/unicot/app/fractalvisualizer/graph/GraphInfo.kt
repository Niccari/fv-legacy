package unicot.app.fractalvisualizer.graph

import android.graphics.PointF
import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.graph.Graph.Companion.BRUSHTYPE_LINE
import unicot.app.fractalvisualizer.graph.Graph.Companion.DRAW_ALL
import unicot.app.fractalvisualizer.graph.Graph.Companion.DRAW_IN_ORDER
import unicot.app.fractalvisualizer.model.DimensionF
import unicot.app.fractalvisualizer.model.GraphDisplacement

/**
 * データクラス
 */
class GraphInfo {

    /** グラフの種類(定数値はDGCommonクラスにて定義) */
    var graphKind: DGCommon.GraphKind = DGCommon.GraphKind.NPOINT
    /** 描画方法(定数値はGraphクラスにて定義) */
    var drawKind: Int = 0
    /** 再帰曲線のグラフか？ */
    var isRecursive: Boolean = false
    /** 線の太さ */
    var mLineThickness: Float = 0.toFloat()
    /** 線分ごとに色を付けるか？ */
    var mIsColorEach: Boolean = false
    /** 最大何本まで描画するか */
    var mEachLineHistory: Int = 0
    /** ブラシの種別(定数値はGraphクラスにて定義) */
    var mBrushType: Int = 0
    /** (DRAW_IN_ORDER時)現在の描画位置 */
    var mCurrentDrawOrder: Int = 0
    /** グラフの中心位置 */
    var pos: PointF = PointF(0.0f, 0.0f)
    /** サイズ(画面に対する相対値, > 0.0) */
    var size: DimensionF = DimensionF(0.5f, 0.5f)
    /** 角度angle */
    var angle: Float = 0.toFloat()
    /** 角速度rot_speed */
    var rotSpeed: Float = 0.toFloat()
    /** 複雑さ */
    var complexity: Int = 0
    /** (再帰曲線のみ有効) グラフの変形率 */
    var mutation: GraphDisplacement
    /** (再帰曲線のみ有効) グラフの乱雑度 */
    var randomize: GraphDisplacement
    /** 色の遷移パターン(現在の色なども管理) */
    var cp: ColorPattern

    init {
        angle = 0.0f
        rotSpeed = 0.0f

        mutation = GraphDisplacement(1.0f, 1.0f)
        randomize = GraphDisplacement(0.0f, 0.0f)

        cp = ColorPattern()

        complexity = 3

        isRecursive = false

        mCurrentDrawOrder = 0

        drawKind = DRAW_ALL

        mLineThickness = 3.0f
        mEachLineHistory = 10
        mIsColorEach = false
        mBrushType = BRUSHTYPE_LINE
    }

    /** 描画設定を一括設定する */
    fun setDrawSettings(kind: String, thickness: Float, colorEach: Boolean, history: Int, corder: Int, brush: Int) {
        drawKind = if (kind.matches("each".toRegex())) {
            DRAW_IN_ORDER
        } else {
            DRAW_ALL
        }
        if (thickness > 0.0f)
            mLineThickness = thickness
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
     * @param colorEach
     * 色を線分ごとに変化させるか？
     * @param history
     * 一部描画時に何本線分を描画するか？
     */
    internal fun setDrawSettings(kind: Int, thickness: Float, colorEach: Boolean, history: Int) {
        drawKind = kind
        mLineThickness = thickness
        mIsColorEach = colorEach
        mEachLineHistory = history
    }

    companion object {
        /*
         * グラフ位置の最小値(相対座標)
         */
        const val GRAPH_POS_MIN = -1.0f
        /*
         * グラフ位置の最大値(相対座標)
         */
        const val GRAPH_POS_MAX = 1.0f
        /*
         * グラフ位置の中間値(相対座標)
         */
        const val GRAPH_POS_MID = (GRAPH_POS_MAX + GRAPH_POS_MIN) / 2

        /*
         * グラフサイズの最小値(相対座標)
         */
        private const val GRAPH_SIZE_MIN = 0.0f
        /*
         * グラフサイズの最大値(相対座標)
         */
        const val GRAPH_SIZE_MAX = 2.0f
        /*
         * グラフサイズの中間値(相対座標)
         */
        const val GRAPH_SIZE_MID = (GRAPH_SIZE_MAX + GRAPH_SIZE_MIN) / 2
    }
}