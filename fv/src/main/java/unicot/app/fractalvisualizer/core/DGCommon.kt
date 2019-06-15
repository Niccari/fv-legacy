package unicot.app.fractalvisualizer.core

import android.graphics.Point
import android.graphics.PointF
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

    // 各グラフの識別文字列
    private const val STR_NPOINT = "NPOINT"
    private const val STR_STARMINE = "STARMINE"
    private const val STR_NSTAR = "NSTAR"
    private const val STR_RANDOMSHAPE = "RANDOMSHAPE"
    private const val STR_RANDOMSHAPE2 = "RANDOMSHAPE2"
    private const val STR_BINARYTREE = "BINARYTREE"
    private const val STR_DRAGONCURVE = "DRAGONCURVE"
    private const val STR_FOLDTRIANGLE = "FOLDTRIANGLE"
    private const val STR_CCURVE = "CCURVE"
    private const val STR_KOCHCURVE = "KOCHCURVE"
    private const val STR_KOCHTRIANGLE_INNER = "KOCHINNER"
    private const val STR_KOCHTRIANGLE_OUTER = "KOCHOUTER"
    private const val STR_ROSECURVE = "ROSECURVE"
    private const val STR_HILBERT = "HILBERT"
    private const val STR_LEAF = "LEAF"
    private const val STR_SIERPINSKI_GASKET = "SIERPINSKI_GASKET"
    private const val STR_SIERPINSKI_CARPET = "SIERPINSKI_CARPET"
    private const val STR_TRIFOLD_CIS = "TRIFOLD_CIS"
    private const val STR_TRIFOLD_TRANS = "TRIFOLD_TRANS"

    /**
     * 現時刻を返す
     *
     * @return 現時刻(YYYY-MM-DD-HH-MM-SS)
     */
    // 今の時刻を取得
    val currentDateString: String
        get() {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            return year.toString() + "-" + (month + 1) + "-" + day + "-" + hour + "-" + minute + "-" + second
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
     * グラフ種別取得\n グラフの名前から種類番号を返す
     *
     * @param kind_str
     * グラフ名称
     * @return グラフ種別
     */
    fun getKind(kind_str: String): Int {
        if (kind_str.matches("".toRegex()))
            return NPOINT

        val k_str = kind_str.toUpperCase(Locale.ENGLISH) // 大・小文字問わない

        return if (k_str.matches(STR_NPOINT.toRegex()))
            NPOINT
        else if (k_str.matches(STR_STARMINE.toRegex()))
            STARMINE
        else if (k_str.matches(STR_NSTAR.toRegex()))
            NSTAR
        else if (k_str.matches(STR_RANDOMSHAPE.toRegex()))
            RANDOMSHAPE
        else if (k_str.matches(STR_RANDOMSHAPE2.toRegex()))
            RANDOMSHAPE2
        else if (k_str.matches(STR_BINARYTREE.toRegex()))
            BINARYTREE
        else if (k_str.matches(STR_DRAGONCURVE.toRegex()))
            DRAGONCURVE
        else if (k_str.matches(STR_FOLDTRIANGLE.toRegex()))
            FOLDTRIANGLE
        else if (k_str.matches(STR_CCURVE.toRegex()))
            CCURVE
        else if (k_str.matches(STR_KOCHCURVE.toRegex()))
            KOCHCURVE
        else if (k_str.matches(STR_KOCHTRIANGLE_INNER.toRegex()))
            KOCHTRIANGLE_INNER
        else if (k_str.matches(STR_KOCHTRIANGLE_OUTER.toRegex()))
            KOCHTRIANGLE_OUTER
        else if (k_str.matches(STR_ROSECURVE.toRegex()))
            ROSECURVE
        else if (k_str.matches(STR_HILBERT.toRegex()))
            HILBERT
        else if (k_str.matches(STR_LEAF.toRegex()))
            LEAF
        else if (k_str.matches(STR_SIERPINSKI_GASKET.toRegex()))
            SIERPINSKI_GASKET
        else if (k_str.matches(STR_SIERPINSKI_CARPET.toRegex()))
            SIERPINSKI_CARPET
        else if (k_str.matches(STR_TRIFOLD_CIS.toRegex()))
            TRIFOLD_CIS
        else if (k_str.matches(STR_TRIFOLD_TRANS.toRegex()))
            TRIFOLD_TRANS
        else
            -1
    }

    /**
     * グラフ名称取得
     *
     * @param kind
     * グラフ種別
     * @return グラフ名称
     */
    fun getGraphKindString(kind: Int): String {
        // グラフの名前から種類番号を返す
        when (kind) {
            NPOINT -> return STR_NPOINT
            STARMINE -> return STR_STARMINE
            NSTAR -> return STR_NSTAR
            RANDOMSHAPE -> return STR_RANDOMSHAPE
            RANDOMSHAPE2 -> return STR_RANDOMSHAPE2

            BINARYTREE -> return STR_BINARYTREE
            DRAGONCURVE -> return STR_DRAGONCURVE
            FOLDTRIANGLE -> return STR_FOLDTRIANGLE
            CCURVE -> return STR_CCURVE

            KOCHCURVE -> return STR_KOCHCURVE
            KOCHTRIANGLE_INNER -> return STR_KOCHTRIANGLE_INNER
            KOCHTRIANGLE_OUTER -> return STR_KOCHTRIANGLE_OUTER

            ROSECURVE -> return STR_ROSECURVE
            HILBERT -> return STR_HILBERT
            LEAF -> return STR_LEAF
            SIERPINSKI_GASKET -> return STR_SIERPINSKI_GASKET
            SIERPINSKI_CARPET -> return STR_SIERPINSKI_CARPET
            TRIFOLD_CIS -> return STR_TRIFOLD_CIS
            TRIFOLD_TRANS -> return STR_TRIFOLD_TRANS
            else -> return ""
        }
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
