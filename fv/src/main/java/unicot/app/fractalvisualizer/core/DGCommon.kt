package unicot.app.fractalvisualizer.core

import android.graphics.Point
import android.graphics.PointF
import android.text.format.DateFormat
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.graph.*
import unicot.app.fractalvisualizer.graph.Random
import java.util.*

/**
 * グラフ処理に関する共通処理を取り扱うクラス<br></br>
 * <br></br>
 * - グラフのコピー<br></br>
 * - グラフの座標系変換<br></br>
 * - グラフの番号・識別文字列変換<br></br>
 */
object DGCommon {
    enum class GraphKind(val str: String){
        NPOINT("NPOINT"),
        STARMINE("STARMINE"),
        NSTAR("NSTAR"),
        RANDOMSHAPE("RANDOMSHAPE"),
        RANDOMSHAPE2("RANDOMSHAPE2"),
        BINARYTREE("BINARYTREE"),
        DRAGONCURVE("DRAGONCURVE"),
        FOLDTRIANGLE("FOLDTRIANGLE"),
        CCURVE("CCURVE"),
        KOCHCURVE("KOCHCURVE"),
        KOCHTRIANGLE_INNER("KOCHTRIANGLE_INNER"),
        KOCHTRIANGLE_OUTER("KOCHTRIANGLE_OUTER"),
        ROSECURVE("ROSECURVE"),
        HILBERT("HILBERT"),
        LEAF("LEAF"),
        SIERPINSKI_GASKET("SIERPINSKI_GASKET"),
        SIERPINSKI_CARPET("SIERPINSKI_CARPET"),
        TRIFOLD_CIS("TRIFOLD_CIS"),
        TRIFOLD_TRANS("TRIFOLD_TRANS"),
    }

    /**
     * 現時刻
     */
    val currentDateString: String
        get() = DateFormat.format("yyyyMMdd-HHmmss", Date()).toString()

    private fun getGraph(kind: GraphKind): Graph {
        when (kind) {
            GraphKind.NPOINT -> return NPoint()
            GraphKind.STARMINE -> return Starmine()
            GraphKind.NSTAR -> return NStar()
            GraphKind.RANDOMSHAPE -> return Random()
            GraphKind.RANDOMSHAPE2 -> return RandomDynamic()

            GraphKind.BINARYTREE -> return BinaryTree()
            GraphKind.DRAGONCURVE -> return FoldCurve(FoldCurve.DRAGON)
            GraphKind.FOLDTRIANGLE -> return FoldCurve(FoldCurve.TRIANGLE)
            GraphKind.CCURVE -> return FoldCurve(FoldCurve.CCURVE)

            GraphKind.KOCHCURVE -> return KochCurve()
            GraphKind.KOCHTRIANGLE_INNER -> return KochTri(KochTri.INNER)
            GraphKind.KOCHTRIANGLE_OUTER -> return KochTri(KochTri.OUTER)

            GraphKind.ROSECURVE -> return Rose()
            GraphKind.HILBERT -> return Hilbert()
            GraphKind.LEAF -> return Leaf()
            GraphKind.SIERPINSKI_GASKET -> return SGasket()
            GraphKind.SIERPINSKI_CARPET -> return SCarpet()
            GraphKind.TRIFOLD_CIS -> return TriFold(TriFold.CIS)
            GraphKind.TRIFOLD_TRANS -> return TriFold(TriFold.TRANS)
        }
    }
    /**
     * 新しいグラフを作成する。\n is_old_copyが真ならば、事前に作成したグラフの特性を引き継ぐ。
     *
     * @param kind
     * グラフの種類
     * @param is_old_copy
     * グラフ複製フラグ
     * @return Success_flag(true:OK, false:NG)
     */
    fun copyGraph(kind: GraphKind, is_old_copy: Boolean): Boolean {
        val dgraph = DGCore.graph
        /* コピー実施時、最新のグラフを使う */
        val oldGraph = if (is_old_copy) DGCore.selectedGraph.last() else null
        val oldGraphInfo = if (is_old_copy) oldGraph!!.info else null
        dgraph.add(getGraph(kind))
        // 新しいグラフは前のグラフの特性を受け継ぐ。
        val gd = dgraph[dgraph.size - 1]
        if (is_old_copy && oldGraphInfo != null) {
            gd.setInfo(oldGraphInfo, false)
            gd.info.cp.alpha = oldGraphInfo.cp.alpha
            gd.info.angle = 0.0f // 回転角度はリセットする。

            when (kind) {
                GraphKind.LEAF -> (gd as Leaf).setBranch((oldGraph as Leaf).getBranch())
                GraphKind.SIERPINSKI_GASKET -> (gd as SGasket).skewAngle = (oldGraph as SGasket).skewAngle
                else -> {}
            }
        }
        return true
    }

    /**
     * 相対座標(-1.0 ~ 1.0)→絶対座標に変換
     *
     * @param rel_pos
     * 相対座標(-1.0 ~ 1.0)
     * @return 絶対座標(画面上の位置)
     */
    // 相対座標(-1.0 ~ 1.0) ▶ 絶対座標
    fun getAbsCntPoint(rel_pos: PointF): Point {
        val windowSize = DGCore.screenSize
        return Point((windowSize.x / 2 * (rel_pos.x + 1.0f)).toInt(), (windowSize.y / 2 * (rel_pos.y + 1.0f)).toInt())
    }

    /**
     * 絶対座標→相対座標(-1.0 ~ 1.0)に変換
     *
     * @param abs_pos
     * 絶対座標(画面上の位置)
     * @return 相対座標(-1.0 ~ 1.0)
     */
    fun getRelCntPoint(abs_pos: Point): PointF {
        val windowSize = DGCore.screenSize
        return PointF(2.0f * abs_pos.x / windowSize.x - 1.0f, 2.0f * abs_pos.y / windowSize.y - 1.0f)
    }

    /**
     * グラフ種別取得
     */
    fun getKind(kind_str: String): GraphKind {
        return GraphKind.valueOf(kind_str.toUpperCase(Locale.US))
    }

    /**
     * グラフアイコンID取得
     *
     * @param kind
     * グラフ種別
     * @return グラフアイコンID
     */
    fun getGraphIcon(kind: GraphKind): Int {
        when (kind) {
            GraphKind.NPOINT -> return R.drawable.npoint
            GraphKind.STARMINE -> return R.drawable.starmine
            GraphKind.NSTAR -> return R.drawable.nstar
            GraphKind.RANDOMSHAPE -> return R.drawable.random_stat
            GraphKind.RANDOMSHAPE2 -> return R.drawable.random_every

            GraphKind.BINARYTREE -> return R.drawable.binarytree
            GraphKind.DRAGONCURVE -> return R.drawable.dragoncurve
            GraphKind.FOLDTRIANGLE -> return R.drawable.dragoncurve_backward
            GraphKind.CCURVE -> return R.drawable.ccurve

            GraphKind.KOCHCURVE -> return R.drawable.kochcurve
            GraphKind.KOCHTRIANGLE_INNER -> return R.drawable.kochcurve_inner
            GraphKind.KOCHTRIANGLE_OUTER -> return R.drawable.kochcurve_outer

            GraphKind.ROSECURVE -> return R.drawable.rosecurve
            GraphKind.HILBERT -> return R.drawable.hilbert
            GraphKind.LEAF -> return R.drawable.leaf
            GraphKind.SIERPINSKI_GASKET -> return R.drawable.gasket_tri
            GraphKind.SIERPINSKI_CARPET -> return R.drawable.gasket_carpet
            GraphKind.TRIFOLD_CIS -> return R.drawable.trifold_ll
            GraphKind.TRIFOLD_TRANS -> return R.drawable.trifold_lr
        }
    }
}
