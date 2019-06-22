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
    const val NPOINT = 0
    const val STARMINE = 1
    const val NSTAR = 2
    const val RANDOMSHAPE = 3
    const val RANDOMSHAPE2 = 4
    const val BINARYTREE = 5
    const val DRAGONCURVE = 6
    const val FOLDTRIANGLE = 7
    const val CCURVE = 8
    const val KOCHCURVE = 9
    const val KOCHTRIANGLE_INNER = 10
    const val KOCHTRIANGLE_OUTER = 11
    const val ROSECURVE = 12
    const val HILBERT = 13
    const val LEAF = 14
    const val SIERPINSKI_GASKET = 15
    const val SIERPINSKI_CARPET = 16
    const val TRIFOLD_CIS = 17
    const val TRIFOLD_TRANS = 18
    val kind = mapOf(
            "NPOINT" to NPOINT,
            "STARMINE" to STARMINE,
            "NSTAR" to NSTAR,
            "RANDOMSHAPE" to RANDOMSHAPE,
            "RANDOMSHAPE2" to RANDOMSHAPE2,
            "BINARYTREE" to BINARYTREE,
            "DRAGONCURVE" to DRAGONCURVE,
            "FOLDTRIANGLE" to FOLDTRIANGLE,
            "CCURVE" to CCURVE,
            "KOCHCURVE" to KOCHCURVE,
            "KOCHTRIANGLE_INNER" to KOCHTRIANGLE_INNER,
            "KOCHTRIANGLE_OUTER" to KOCHTRIANGLE_OUTER,
            "ROSECURVE" to ROSECURVE,
            "HILBERT" to HILBERT,
            "LEAF" to LEAF,
            "SIERPINSKI_GASKET" to SIERPINSKI_GASKET,
            "SIERPINSKI_CARPET" to SIERPINSKI_CARPET,
            "TRIFOLD_CIS" to TRIFOLD_CIS,
            "TRIFOLD_TRANS" to TRIFOLD_TRANS
    )

    /**
     * 現時刻
     */
    val currentDateString: String
        get() = DateFormat.format("yyyyMMdd-HHmmss", Date()).toString()

    /**
     * 新しいグラフを作成する。\n is_old_copyが真ならば、事前に作成したグラフの特性を引き継ぐ。
     *
     * @param kind
     * グラフの種類
     * @param is_old_copy
     * グラフ複製フラグ
     * @return Success_flag(true:OK, false:NG)
     */
    fun copyGraph(kind: Int, is_old_copy: Boolean): Boolean {
        val dgraph = DGCore.graph
        /* コピー実施時、最新のグラフを使う */
        val gd_old = if (is_old_copy) DGCore.selectedGraph.last() else null
        val gi_old = if (is_old_copy) gd_old!!.info else null
        when (kind) {
            NPOINT -> dgraph.add(NPoint())
            STARMINE -> dgraph.add(Starmine())
            NSTAR -> dgraph.add(NStar())
            RANDOMSHAPE -> dgraph.add(Random())
            RANDOMSHAPE2 -> dgraph.add(RandomDynamic())
            BINARYTREE -> dgraph.add(BinaryTree())
            DRAGONCURVE -> dgraph.add(FoldCurve(FoldCurve.DRAGON))
            FOLDTRIANGLE -> dgraph.add(FoldCurve(FoldCurve.TRIANGLE))
            CCURVE -> dgraph.add(FoldCurve(FoldCurve.CCURVE))
            KOCHCURVE -> dgraph.add(KochCurve())
            KOCHTRIANGLE_INNER -> dgraph.add(KochTri(KochTri.INNER))
            KOCHTRIANGLE_OUTER -> dgraph.add(KochTri(KochTri.OUTER))
            ROSECURVE -> dgraph.add(Rose())
            HILBERT -> dgraph.add(Hilbert())
            LEAF -> dgraph.add(Leaf())
            SIERPINSKI_GASKET -> dgraph.add(SGasket())
            SIERPINSKI_CARPET -> dgraph.add(SCarpet())
            TRIFOLD_CIS -> dgraph.add(TriFold(TriFold.CIS))
            TRIFOLD_TRANS -> dgraph.add(TriFold(TriFold.TRANS))
            else -> {}
        }

        // 新しいグラフは前のグラフの特性を受け継ぐ。
        val gd = dgraph[dgraph.size - 1]
        if (is_old_copy && gi_old != null) {
            gd.setInfo(gi_old, false)
            gd.info.cp.alpha = gi_old.cp.alpha
            gd.info.angle = 0.0f // 回転角度はリセットする。

            when (kind) {
                LEAF -> (gd as Leaf).setBranch((gd_old as Leaf).getBranch())
                SIERPINSKI_GASKET -> (gd as SGasket).skewAngle = (gd_old as SGasket).skewAngle
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
        val w_area = DGCore.screenSize
        return Point((w_area.x / 2 * (rel_pos.x + 1.0f)).toInt(), (w_area.y / 2 * (rel_pos.y + 1.0f)).toInt())
    }

    /**
     * 絶対座標→相対座標(-1.0 ~ 1.0)に変換
     *
     * @param abs_pos
     * 絶対座標(画面上の位置)
     * @return 相対座標(-1.0 ~ 1.0)
     */
    fun getRelCntPoint(abs_pos: Point): PointF {
        val w_area = DGCore.screenSize
        return PointF(2.0f * abs_pos.x / w_area.x - 1.0f, 2.0f * abs_pos.y / w_area.y - 1.0f)
    }

    /**
     * グラフ番号取得
     */
    fun getKind(kind_str: String): Int {
        return kind[kind_str.toUpperCase()] ?: NPOINT
    }

    /**
     * グラフ名称取得
     */
    fun getGraphKindString(value: Int): String {
        return this.kind.filterValues { it == value }.keys.firstOrNull() ?: ""
    }

    /**
     * グラフアイコンID取得
     *
     * @param kind
     * グラフ種別
     * @return グラフアイコンID
     */
    fun getGraphIcon(kind: Int): Int {
        when (kind) {
            NPOINT -> return R.drawable.npoint
            STARMINE -> return R.drawable.starmine
            NSTAR -> return R.drawable.nstar
            RANDOMSHAPE -> return R.drawable.random_stat
            RANDOMSHAPE2 -> return R.drawable.random_every

            BINARYTREE -> return R.drawable.binarytree
            DRAGONCURVE -> return R.drawable.dragoncurve
            FOLDTRIANGLE -> return R.drawable.dragoncurve_backward
            CCURVE -> return R.drawable.ccurve

            KOCHCURVE -> return R.drawable.kochcurve
            KOCHTRIANGLE_INNER -> return R.drawable.kochcurve_inner
            KOCHTRIANGLE_OUTER -> return R.drawable.kochcurve_outer

            ROSECURVE -> return R.drawable.rosecurve
            HILBERT -> return R.drawable.hilbert
            LEAF -> return R.drawable.leaf
            SIERPINSKI_GASKET -> return R.drawable.gasket_tri
            SIERPINSKI_CARPET -> return R.drawable.gasket_carpet
            TRIFOLD_CIS -> return R.drawable.trifold_ll
            TRIFOLD_TRANS -> return R.drawable.trifold_lr
            else -> return -1
        }
    }
}
