package unicot.app.fractalvisualizer.core

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
import java.util.*

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

    private val diff_pos: Point = Point(-1, -1)
    private val cog_graphs: PointF = PointF()   // 選択したグラフ郡の重心
    private var action: Int = -1               // 操作種類(グラフの移動・回転・拡大縮小に使用, -1 : 無し / >= 0 : 操作)
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
        get() = if (isGraphSelected) cog_graphs else PointF()

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
        for (i in graph.indices)
            graph[i].runningGraph()
    }


    /**
     * 全グラフを描画する\n
     * グラフは生成順に描画される(古いものほど奥に描画)
     * @param canvas drawViewクラスで確保した画面バッファ
     */
    fun draw(canvas: Canvas) {

        for (i in graph.indices)
            graph[i].draw(canvas)
    }

    /**
     * タップした四角領域を基にグラフを選択する。
     * @param pt_old 最初にタップした座標(絶対座標)
     * @param pt_new 最後にタップした座標(絶対座標)
     * @return Selected_graph_indices
     */
    fun select(pt_old: Point = dummyPoint, pt_new: Point = dummyPoint): ArrayList<Int> {
        val min_x: Float
        val min_y: Float
        val max_x: Float
        val max_y: Float
        selectedGraph.clear()
        mSelectedGraphIndex.clear()
        mSelectedGraphDistance.clear()

        // x座標を整列
        if (pt_old.x < pt_new.x) {
            min_x = pt_old.x.toFloat()
            max_x = pt_new.x.toFloat()
        } else {
            min_x = pt_new.x.toFloat()
            max_x = pt_old.x.toFloat()
        }
        // y座標を整列
        if (pt_old.y < pt_new.y) {
            min_y = pt_old.y.toFloat()
            max_y = pt_new.y.toFloat()
        } else {
            min_y = pt_new.y.toFloat()
            max_y = pt_old.y.toFloat()
        }

        // グラフ座標系を画面座標系に変更
        for (i in graph.indices) {
            val gi = graph[i].info

            val pos = DGCommon.getAbsCntPoint(gi.pos)

            // グラフの中心が四角領域内にあるか?
            if (min_x <= pos.x && pos.x <= max_x &&
                    min_y <= pos.y && pos.y <= max_y) {
                mSelectedGraphIndex.add(i)
                selectedGraph.add(graph[i])
            }
        }
        isGraphSelected = selectedGraph.size > 0

        // グラフグループの重心を計算
        if (isGraphSelected) {
            var pos_tmp: PointF
            cog_graphs.set(GRAPH_COG_X_DEFAULT, GRAPH_COG_Y_DEFAULT)

            for (i in selectedGraph.indices) {
                pos_tmp = selectedGraph[i].info.pos
                cog_graphs.set(cog_graphs.x + pos_tmp.x, cog_graphs.y + pos_tmp.y)
            }
            cog_graphs.set(cog_graphs.x / selectedGraph.size, cog_graphs.y / selectedGraph.size)

            // 以下、選択されたグラフと重心間のベクトルを計算
            var my_pos: PointF
            for (i in selectedGraph.indices) {
                my_pos = selectedGraph[i].info.pos
                mSelectedGraphDistance.add(PointF(my_pos.x - cog_graphs.x, my_pos.y - cog_graphs.y))
            }
        }

        return mSelectedGraphIndex
    }

    /**
     * タップ座標(グラフ郡の重心からの距離)に応じて処理変更
     * @param pt タップ座標(絶対座標)
     */
    fun collision(pt: Point) {
        val pt_rel = DGCommon.getRelCntPoint(pt)
        val dist = Math.sqrt(((cog_graphs.x - pt_rel.x) * (cog_graphs.x - pt_rel.x) + (cog_graphs.y - pt_rel.y) * (cog_graphs.y - pt_rel.y)).toDouble()).toFloat()
        if (dist < DIST_THRESH_GRAPH_SELECT_TRANSLATE) {
            action = DGCore.OP_TRANSLATE
        } else if (dist < DIST_THRESH_GRAPH_SELECT_ROTATE) {
            action = DGCore.OP_ROTATE
        } else if (dist < DIST_THRESH_GRAPH_SELECT_SCALING) {
            action = DGCore.OP_SCALING
        } else {
            action = -1
            isGraphSelected = false
        }
    }

    /**
     * 選択したグラフを回転・移動・拡大縮小する
     * @param pt_old 操作前座標
     * @param pt_new 操作後座標
     */
    fun affineTransformGraphs(pt_old: Point, pt_new: Point) {
        val vct_rel: PointF
        if (pt_old.x < 0 && pt_old.y < 0) {
            diff_pos.set(-1, -1)
            return
        }
        when (action) {
            OP_TRANSLATE    // 移動
            -> {
                var vct_cog: PointF
                vct_rel = DGCommon.getRelCntPoint(pt_new)
                for (i in selectedGraph.indices) {
                    vct_cog = mSelectedGraphDistance[i]                        // グラフ重心 - グループ重心

                    selectedGraph[i].setPosition(vct_rel.x + vct_cog.x, vct_rel.y + vct_cog.y)   // グラフを移動
                }
                cog_graphs.set(vct_rel)
            }
            OP_ROTATE    // 回転
            -> {
                if (diff_pos.x < 0 && diff_pos.y < 0) {
                    diff_pos.set(pt_new.x, pt_new.y)
                }
                // 変化ベクトル
                vct_rel = PointF((pt_new.x - diff_pos.x) / screenSize.x.toFloat(),
                        (pt_new.y - diff_pos.y) / screenSize.y.toFloat())

                val diff_pos_rel = DGCommon.getRelCntPoint(diff_pos)

                // 重心から変化ベクトルまでのベクトル(内積計算のため、右に90度回転)
                val r_vct = PointF(diff_pos_rel.y - cog_graphs.y, -(diff_pos_rel.x - cog_graphs.x))
                val rad = -2f * 360.0f * (vct_rel.x * r_vct.x + vct_rel.y * r_vct.y)

                for (i in selectedGraph.indices)
                    selectedGraph[i].info.angle += rad

                diff_pos.set(pt_new.x, pt_new.y)
            }
            OP_SCALING    // 拡大
            -> {
                if (diff_pos.x < 0 && diff_pos.y < 0) {
                    diff_pos.set(pt_new.x, pt_new.y)
                    sgn.set(1, 1)
                    if (cog_graphs.x > 2.0f * (pt_old.x / screenSize.x.toFloat() - 0.5f)) {
                        sgn.x = -sgn.x
                    }
                    if (cog_graphs.y < 2.0f * (pt_old.y / screenSize.y.toFloat() - 0.5f)) {
                        sgn.y = -sgn.y
                    }
                }

                vct_rel = PointF(sgn.x * (pt_new.x - diff_pos.x) / screenSize.x.toFloat(),
                        -sgn.y * (pt_new.y - diff_pos.y) / screenSize.y.toFloat())

                //            Log.d( "FV", "vct_rel:"+vct_rel.x + ", "+vct_rel.y);
                for (i in selectedGraph.indices) {
                    val dim = selectedGraph[i].info.size
                    val new_dx = dim.width + vct_rel.x * 2.0f
                    val new_dy = dim.height + vct_rel.y * 2.0f
                    dim.set(new_dx, new_dy)
                }
                diff_pos.set(pt_new.x, pt_new.y)
            }
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
    fun transformGraph(kind: Int, value: Float) {
        if(selectedGraph.size != 1) return
        val graph = selectedGraph.firstOrNull() ?: return

        when (kind) {
            OP_COMPLEXITY       -> graph.setComplexity(value.toInt())
            OP_GRAPH_ROTATE     -> graph.setRotate(value)
            OP_MUTATION_SIZE    -> graph.setMutationSize(value)
            OP_MUTATION_ANGLE   -> graph.setMutationAngle(value)
            OP_RANDOMIZER_SIZE  -> graph.setRandomizerSize(value)
            OP_RANDOMIZER_ANGLE -> graph.setRandomizerAngle(value)

            OP_LEAF_BRANCH  -> (graph as Leaf).setBranch(value.toInt())
            OP_SGASKET_SKEW -> (graph as SGasket).skewAngle = value
        }
    }
    /**
     * 描画設定を更新する
     * @param kind 処理コード
     * @param value 更新値
     */
    fun changeDrawSetting(kind: Int, value: Int) {
        when (kind) {
            OP_THICKNESS     -> selectedGraph.map{ it.setThickness(value) }
            OP_DRAW_EACH     -> selectedGraph.map{ it.setDrawEach(value) }
            OP_COLOR_SHIFT   -> selectedGraph.map{ it.info.cp.shiftSpeed = value }
            OP_SET_COLOR     -> selectedGraph.map{ it.info.cp.color = value }
            OP_SET_ALPHA     -> selectedGraph.map{ it.info.cp.alpha = value }
            OP_DRAW_EACH_PCT -> selectedGraph.map{ it.setDrawEachLength(value.toFloat()) }
        }
    }

    /**
     * 描画設定を更新する
     * @param kind 処理コード
     */
    fun changeDrawSetting(kind: Int, arg: Boolean) {
        when (kind) {
            OP_COLOREACH -> selectedGraph.map{ it.setColorRange(arg) }
        }
    }

    /**
     * 描画設定を更新する
     * @param value 処理コード
     */
    fun changeDrawSetting(kind: Int, value: String) {
        when (kind) {
            OP_COLORPATTERN -> selectedGraph.map{ it.info.cp.setColMode(value) }
            OP_BRUSHTYPE    -> selectedGraph.map{ it.setBrushType(value) }
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
        private val GRAPH_COG_X_DEFAULT = 0.0f
        private val GRAPH_COG_Y_DEFAULT = 0.0f

        private val dummyPoint = Point(-1, -1)
        /**
         * グラフ変形内容判定部 しきい値：平行移動
         */
        val DIST_THRESH_GRAPH_SELECT_TRANSLATE = 0.12f
        /**
         * グラフ変形内容判定部 しきい値：回転
         */
        val DIST_THRESH_GRAPH_SELECT_ROTATE = 0.24f
        /**
         * グラフ変形内容判定部 しきい値：拡大縮小
         */
        val DIST_THRESH_GRAPH_SELECT_SCALING = 0.40f

        /**
         * 制御番号：グラフの平行移動
         */
        private val OP_TRANSLATE = 0
        /**
         * 制御番号：グラフの拡大縮小
         */
        private val OP_SCALING = 1
        /**
         * 制御番号：グラフの回転
         */
        private val OP_ROTATE = 2

        /**
         * 制御番号：変異率の大きさ
         */
        val OP_MUTATION_SIZE = 10
        /**
         * 制御番号：変異率の方向
         */
        val OP_MUTATION_ANGLE = 11
        /**
         * 制御番号：乱雑度の大きさ
         */
        val OP_RANDOMIZER_SIZE = 12
        /**
         * 制御番号：乱雑度の方向
         */
        val OP_RANDOMIZER_ANGLE = 13

        /**
         * 制御番号：葉型グラフの枝数
         */
        val OP_LEAF_BRANCH = 19
        /**
         * 制御番号：シェルピンスキーのギャスケットの水平スキュー角度
         */
        val OP_SGASKET_SKEW = 18

        /**
         * 制御番号：ペンの太さ(グラフ描画設定)
         */
        val OP_THICKNESS = 20
        /**
         * 制御番号：色を線分ごとに変更(グラフ描画設定)
         */
        val OP_COLOREACH = 21
        /**
         * 制御番号：色遷移パターンの変更(グラフ描画設定)
         */
        val OP_COLORPATTERN = 23
        /**
         * 制御番号：色の設定(グラフ描画設定)
         */
        val OP_SET_COLOR = 24
        /**
         * 制御番号：グラフの個別設定のトグル(グラフ描画設定)
         */
        val OP_DRAW_EACH = 25
        /**
         * 制御番号：グラフの個別描画線分数(パーセント表示)(グラフ描画設定)
         */
        val OP_DRAW_EACH_PCT = 26
        /**
         * 制御番号：色の遷移速度変更(グラフ描画設定)
         */
        val OP_COLOR_SHIFT = 29
        /**
         * 制御番号：ペンの種類(グラフ描画設定)
         */
        val OP_BRUSHTYPE = 30
        /**
         * 制御番号：ペンの透明度(グラフ描画設定)
         */
        val OP_SET_ALPHA = 31

        /**
         * 制御番号：複雑さの変更(グラフ変形)
         */
        val OP_COMPLEXITY = 41
        /**
         * 制御番号：グラフの自転速度の変更(グラフ変形)
         */
        val OP_GRAPH_ROTATE = 44
    }
}
