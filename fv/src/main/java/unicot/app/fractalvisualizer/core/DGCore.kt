package unicot.app.fractalvisualizer.core

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
import java.util.*
import kotlin.math.sqrt

/**
 * グラフの計算・描画など、グラフに関する処理を行う<br></br>
 * このクラスがMainActivity(≒GUI)とグラフを結びつける<br></br>
 * 【主な機能】<br></br>
 * - グラフの選択処理<br></br>
 * - グラフの描画・計算指示処理<br></br>
 * - グラフ情報の取得<br></br>
 * - GUIイベントの処理<br></br>
 */
class DGCore {
    private val mSelectedGraphIndex: ArrayList<Int> = ArrayList(0)        // 選択しているグラフ番号
    private val mSelectedGraphDistance: ArrayList<PointF> = ArrayList(0)  // 選択しているグラフの重心からのベクトル

    private val diffPos: Point = Point(-1, -1)
    private val graphsCog: PointF = PointF()   // 選択したグラフ郡の重心
    private var action: GraphMotion = GraphMotion.NULL           // 操作種類(グラフの移動・回転・拡大縮小に使用, -1 : 無し / >= 0 : 操作)
    private val sgn: Point = Point( 1, 1)

    /**
     * グラフを選択しているかどうかを取得する
     * @return グラフ選択有無
     */
    var isGraphSelected = false
        private set    // グラフを選択しているか

    val povFrame: Int
        get() = systemData.povFrame

    /**
     * 選択したグラフ群の個数を取得する
     * @return 選択したグラフ群の個数
     */
    val selectedGraphNum: Int
        get() = selectedGraph.size

    /**
     * 選択したグラフ群の重心を取得する
     * @return 選択したグラフ群の重心(相対座標)
     */
    val selectedCOG: PointF
        get() = if (isGraphSelected) graphsCog else PointF()

    /**
     * 初期化：インスタンス取得、データロードなど
     * @param size 画面サイズ(絶対座標)
     */
    fun setScreenSize(size: Point) {
        screenSize.set(size.x, size.y)
    }

    /**
     * グラフを更新する
     */
    fun run() {
        graph.map {
            it.runningGraph()
            it.renewColorTable()
        }
    }


    /**
     * 全グラフを描画する\n
     * グラフは生成順に描画される(古いものほど奥に描画)
     * @param canvas drawViewクラスで確保した画面バッファ
     */
    fun draw(canvas: Canvas) {
        graph.map { it.draw(canvas) }
    }

    /**
     * タップした四角領域を基にグラフを選択する。
     * @param pt_old 最初にタップした座標(絶対座標)
     * @param pt_new 最後にタップした座標(絶対座標)
     * @return Selected_graph_indices
     */
    fun select(pt_old: Point = dummyPoint, pt_new: Point = dummyPoint): ArrayList<Int> {
        val minX: Float
        val minY: Float
        val maxX: Float
        val maxY: Float
        selectedGraph.clear()
        mSelectedGraphIndex.clear()
        mSelectedGraphDistance.clear()

        // x座標を整列
        if (pt_old.x < pt_new.x) {
            minX = pt_old.x.toFloat()
            maxX = pt_new.x.toFloat()
        } else {
            minX = pt_new.x.toFloat()
            maxX = pt_old.x.toFloat()
        }
        // y座標を整列
        if (pt_old.y < pt_new.y) {
            minY = pt_old.y.toFloat()
            maxY = pt_new.y.toFloat()
        } else {
            minY = pt_new.y.toFloat()
            maxY = pt_old.y.toFloat()
        }

        // グラフ座標系を画面座標系に変更
        for (i in graph.indices) {
            val gi = graph[i].info

            val pos = DGCommon.getAbsCntPoint(gi.pos)

            // グラフの中心が四角領域内にあるか?
            if (pos.x.toFloat() in minX..maxX && pos.y.toFloat() in minY..maxY) {
                mSelectedGraphIndex.add(i)
                selectedGraph.add(graph[i])
            }
        }
        isGraphSelected = selectedGraph.size > 0

        // グラフグループの重心を計算
        if (isGraphSelected) {
            var tmpPos: PointF
            graphsCog.set(GRAPH_COG_X_DEFAULT, GRAPH_COG_Y_DEFAULT)

            for (i in selectedGraph.indices) {
                tmpPos = selectedGraph[i].info.pos
                graphsCog.set(graphsCog.x + tmpPos.x, graphsCog.y + tmpPos.y)
            }
            graphsCog.set(graphsCog.x / selectedGraph.size, graphsCog.y / selectedGraph.size)

            // 以下、選択されたグラフと重心間のベクトルを計算
            var myPos: PointF
            for (i in selectedGraph.indices) {
                myPos = selectedGraph[i].info.pos
                mSelectedGraphDistance.add(PointF(myPos.x - graphsCog.x, myPos.y - graphsCog.y))
            }
        }

        return mSelectedGraphIndex
    }

    /**
     * タップ座標(グラフ郡の重心からの距離)に応じて処理変更
     * @param pt タップ座標(絶対座標)
     */
    fun collision(pt: Point) {
        val relPoint = DGCommon.getRelCntPoint(pt)
        val dist = sqrt(((graphsCog.x - relPoint.x) * (graphsCog.x - relPoint.x) + (graphsCog.y - relPoint.y) * (graphsCog.y - relPoint.y)).toDouble()).toFloat()
        when {
            dist < DIST_THRESH_GRAPH_SELECT_TRANSLATE -> {
                action = GraphMotion.TRANSLATE
            }
            dist < DIST_THRESH_GRAPH_SELECT_ROTATE -> {
                action = GraphMotion.ROTATE
            }
            dist < DIST_THRESH_GRAPH_SELECT_SCALING -> {
                action = GraphMotion.SCALING
            }
            else -> {
                action = GraphMotion.NULL
                isGraphSelected = false
            }
        }
    }

    /**
     * 選択したグラフを回転・移動・拡大縮小する
     * @param pt_old 操作前座標
     * @param pt_new 操作後座標
     */
    fun affineTransformGraphs(pt_old: Point, pt_new: Point) {
        val relVector: PointF
        if (pt_old.x < 0 && pt_old.y < 0) {
            diffPos.set(-1, -1)
            return
        }
        when (action) {
            GraphMotion.TRANSLATE    // 移動
            -> {
                var vectorCog: PointF
                relVector = DGCommon.getRelCntPoint(pt_new)
                for (i in selectedGraph.indices) {
                    vectorCog = mSelectedGraphDistance[i]                        // グラフ重心 - グループ重心

                    selectedGraph[i].setPosition(relVector.x + vectorCog.x, relVector.y + vectorCog.y)   // グラフを移動
                }
                graphsCog.set(relVector)
            }
            GraphMotion.ROTATE    // 回転
            -> {
                if (diffPos.x < 0 && diffPos.y < 0) {
                    diffPos.set(pt_new.x, pt_new.y)
                }
                // 変化ベクトル
                relVector = PointF((pt_new.x - diffPos.x) / screenSize.x.toFloat(),
                        (pt_new.y - diffPos.y) / screenSize.y.toFloat())

                val diffRelPos = DGCommon.getRelCntPoint(diffPos)

                // 重心から変化ベクトルまでのベクトル(内積計算のため、右に90度回転)
                val armVector = PointF(diffRelPos.y - graphsCog.y, -(diffRelPos.x - graphsCog.x))
                val rad = -2f * 360.0f * (relVector.x * armVector.x + relVector.y * armVector.y)

                for (i in selectedGraph.indices)
                    selectedGraph[i].info.angle += rad

                diffPos.set(pt_new.x, pt_new.y)
            }
            GraphMotion.SCALING    // 拡大
            -> {
                if (diffPos.x < 0 && diffPos.y < 0) {
                    diffPos.set(pt_new.x, pt_new.y)
                    sgn.set(1, 1)
                    if (graphsCog.x > 2.0f * (pt_old.x / screenSize.x.toFloat() - 0.5f)) {
                        sgn.x = -sgn.x
                    }
                    if (graphsCog.y < 2.0f * (pt_old.y / screenSize.y.toFloat() - 0.5f)) {
                        sgn.y = -sgn.y
                    }
                }

                relVector = PointF(sgn.x * (pt_new.x - diffPos.x) / screenSize.x.toFloat(),
                        -sgn.y * (pt_new.y - diffPos.y) / screenSize.y.toFloat())

                //            Log.d( "FV", "vct_rel:"+vct_rel.x + ", "+vct_rel.y);
                for (i in selectedGraph.indices) {
                    val dim = selectedGraph[i].info.size
                    val newDx = dim.width  + relVector.x * 2.0f
                    val newDy = dim.height + relVector.y * 2.0f
                    dim.set(newDx, newDy)
                }
                diffPos.set(pt_new.x, pt_new.y)
            }
            else -> {}
        }
    }

    /**
     * 現在選択している全てのグラフを削除する
     */
    fun deleteSelectedGraphs() {
        selectedGraph.map{ graph.remove(it) }
        mSelectedGraphIndex.clear()
        mSelectedGraphDistance.clear()
        selectedGraph.clear()
        this.isGraphSelected = false
    }

    /**
     * グラフ形状を変更する
     * @param kind 処理コード
     * @param value 更新値
     */
    fun transformGraph(kind: GraphSetting, value: Float) {
        if(selectedGraph.size != 1) return
        val graph = selectedGraph.firstOrNull() ?: return

        when (kind) {
            GraphSetting.COMPLEXITY       -> graph.setComplexity(value.toInt())
            GraphSetting.ROT_SPEED        -> graph.setRotate(value)
            GraphSetting.MUTATION_SIZE    -> graph.setMutationSize(value)
            GraphSetting.MUTATION_ANGLE   -> graph.setMutationAngle(value)
            GraphSetting.RANDOMIZER_SIZE  -> graph.setRandomizerSize(value)
            GraphSetting.RANDOMIZER_ANGLE -> graph.setRandomizerAngle(value)

            GraphSetting.LEAF_BRANCH  -> (graph as Leaf).setBranch(value.toInt())
            GraphSetting.SGASKET_SKEW -> (graph as SGasket).skewAngle = value
        }
    }
    /**
     * 描画設定を更新する
     * @param kind 処理コード
     * @param value 更新値
     */
    fun changeDrawSetting(kind: DrawSetting, value: Int) {
        when (kind) {
            DrawSetting.THICKNESS     -> selectedGraph.map{ it.setThickness(value) }
            DrawSetting.DRAW_EACH     -> selectedGraph.map{ it.setDrawEach(value) }
            DrawSetting.COLOR_SHIFT   -> selectedGraph.map{ it.info.cp.shiftSpeed = value }
            DrawSetting.COLOR_RGB         -> selectedGraph.map{ it.info.cp.color = value }
            DrawSetting.COLOR_ALPHA         -> selectedGraph.map{ it.info.cp.alpha = value }
            DrawSetting.DRAW_EACH_PCT -> selectedGraph.map{ it.setDrawEachLength(value.toFloat()) }
            else -> {}
        }
    }

    /**
     * 描画設定を更新する
     * @param kind 処理コード
     */
    fun changeDrawSetting(kind: DrawSetting, arg: Boolean) {
        when (kind) {
            DrawSetting.COLOR_EACH -> selectedGraph.map{ it.setColorRange(arg) }
            else -> {}
        }
    }

    /**
     * 描画設定を更新する
     * @param value 処理コード
     */
    fun changeDrawSetting(kind: DrawSetting, value: String) {
        when (kind) {
            DrawSetting.COLOR_PATTERN -> selectedGraph.map{ it.info.cp.setColMode(value) }
            DrawSetting.BRUSH_TYPE    -> selectedGraph.map{ it.setBrushType(value) }
            else -> {}
        }
    }

    companion object {
        /**
         * システム情報を取得する
         * @return システム情報
         */
        var systemData = DGSystemData()
            private set        // システム情報(リフレッシュレートなど)

        /**
         * (Common用) 全グラフのリストを取得
         * @return 全グラフのリスト
         */
        val graph: ArrayList<Graph> = ArrayList(0)

        /**
         * (Common用) 画面サイズを取得
         * @return 画面サイズを取得
         */
        var screenSize: Point = Point()
            private set    // 画面のピクセル数(横・縦)

        /**
         * 選択したグラフ群を取得する
         * @return 選択したグラフ群
         */
        val selectedGraph: ArrayList<Graph> = ArrayList(0)

        /* *** 定数 *** */

        // 重心のデフォルト値
        private const val GRAPH_COG_X_DEFAULT = 0.0f
        private const val GRAPH_COG_Y_DEFAULT = 0.0f

        private val dummyPoint = Point(-1, -1)
        /**
         * グラフ変形内容判定部 しきい値：平行移動
         */
        const val DIST_THRESH_GRAPH_SELECT_TRANSLATE = 0.12f
        /**
         * グラフ変形内容判定部 しきい値：回転
         */
        const val DIST_THRESH_GRAPH_SELECT_ROTATE = 0.24f
        /**
         * グラフ変形内容判定部 しきい値：拡大縮小
         */
        const val DIST_THRESH_GRAPH_SELECT_SCALING = 0.40f

        enum class GraphMotion{
            TRANSLATE,
            SCALING,
            ROTATE,
            NULL
        }

        enum class GraphSetting{
            ROT_SPEED,
            COMPLEXITY,
            MUTATION_SIZE,
            MUTATION_ANGLE,
            RANDOMIZER_SIZE,
            RANDOMIZER_ANGLE,
            LEAF_BRANCH,
            SGASKET_SKEW,
        }

        enum class DrawSetting{
            THICKNESS,
            COLOR_EACH,
            COLOR_PATTERN,
            COLOR_RGB,
            COLOR_ALPHA,
            COLOR_SHIFT,
            DRAW_EACH,
            DRAW_EACH_PCT,
            BRUSH_TYPE,
        }
    }
}
