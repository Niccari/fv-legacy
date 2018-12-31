package unicot.app.fractalvisualizer.graph

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF

import java.util.ArrayList
import java.util.Locale

import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.core.SinInt
import unicot.app.fractalvisualizer.struct.DimensionF
import unicot.app.fractalvisualizer.struct.GraphDisplacement


/**
 * グラフ雛形
 */
open class Graph
/**
 * Constructor. グラフを初期化する。\n
 * 特に指定がない限り、本クラスは正N角形を準備する
 */
// グラフの初期化
internal constructor() {
    /**
     * グラフごとの複雑さの最小値を取得する
     *
     * @return 複雑さの最小値
     */
    var complexityMin = 2
        protected set
    /**
     * グラフごとの複雑さの最大値を返す
     *
     * @return 複雑さの最大値
     */
    var complexityMax = 20
        protected set

    protected var is_allocated: Boolean = false

    private val mDrawPath: Path


    private val pen: Paint // 描画設定
    // 以下、アクセサ
    /**
     * グラフ情報を取得する。
     *
     * @return グラフ情報
     */
    val info: GraphInfo // グラフ情報
    protected lateinit var order_points: Array<Point> // 描画順
    private val mPenColors: IntArray // 描画色リスト

    // point_base => point => translate, rot_speed, scaling => point'
    protected var point_base = ArrayList<PointF>(0) // グラフの基本形状のみ
    protected var point = ArrayList<PointF>(0) // 変形後のグラフ

    protected var n_orders: Int = 0 // 線分の数( != pointMax)

    private var mTmpPointF: PointF? = null
    private val mPointSrc: Point
    private val mPointDst: Point

    /**
     * グラフの現在の色(最新の)を取得する。同時に、色の遷移を進める
     *
     * @return グラフの現在の色
     */
    val color: Int
        get() = info.cp.doPattern()

    /**
     * 点の数を算出して返す\n
     * この関数にて点の描画順序数(n_orders)も確定させる
     * @return 描画する点の個数(デフォルトでは正N角形の個数)
     */
    protected open val pointMax: Int
        get() {
            n_orders = info.complexity
            return n_orders
        }

    init {
        this.info = GraphInfo()

        pen = Paint()

        pen.strokeWidth = info.mLineThickness
        pen.isAntiAlias = info.mIsAntiAlias
        pen.style = Paint.Style.STROKE

        mPenColors = IntArray(COLOR_MAX)
        is_allocated = false

        mDrawPath = Path()
        mPointSrc = Point()
        mPointDst = Point()

        setRelativePoint()
    }

    /**
     * グラフ情報を別のグラフ情報からコピーする
     *
     * @param src
     * 元となるグラフ情報
     * @param isFullCopy
     * 色情報も含め完全コピー
     */
    fun setInfo(src: GraphInfo, isFullCopy: Boolean) {
        this.info.graph_kind = src.graph_kind

        // グラフ設定
        this.setPosition(src.pos.x, src.pos.y)
        this.info.size = src.size
        this.info.angle = src.angle
        this.info.rot_speed = src.rot_speed
        this.info.mutation = src.mutation
        this.info.randomize = src.randomize

        this.setComplexity(src.complexity)

        // 以下、描画設定
        this.info.cp.init(src.cp, isFullCopy)
        this.info.setDrawSettings(src.draw_kind, src.mLineThickness, src.mIsAntiAlias, src.mIsColorEach, src.mEachLineHistory)

        if (isFullCopy)
            this.info.mCurrentDrawOrder = src.mCurrentDrawOrder
        this.info.mBrushType = src.mBrushType
        for (i in COLOR_MAX - 1 downTo 0) {
            mPenColors[i] = this.info.cp.doPattern()
        }

        pen.strokeWidth = info.mLineThickness
        pen.isAntiAlias = info.mIsAntiAlias
        pen.style = Paint.Style.STROKE
    }

    /**
     * 線の太さを設定する
     *
     * @param value
     * 線の太さ
     */
    fun setThickness(value: Int) {
        info.mLineThickness = value.toFloat()
        pen.strokeWidth = value.toFloat()
    }

    /**
     * ブラシの種類を設定する
     *
     * @param value
     * ブラシの種類(文字列)
     */
    fun setBrushType(value: String) {
        val tmp = value.toLowerCase(Locale.ENGLISH)

        if (tmp.matches(STR_BRUSHTYPE_LINE.toRegex())) {
            info.mBrushType = BRUSHTYPE_LINE
        } else if (tmp.matches(STR_BRUSHTYPE_TRIANGLE.toRegex())) {
            info.mBrushType = BRUSHTYPE_TRIANGLE
        } else if (tmp.matches(STR_BRUSHTYPE_CRESCENT.toRegex())) {
            info.mBrushType = BRUSHTYPE_CRESCENT
        } else if (tmp.matches(STR_BRUSHTYPE_TWIN_CIRCLE.toRegex())) {
            info.mBrushType = BRUSHTYPE_TWIN_CIRCLE
        } else {
            info.mBrushType = BRUSHTYPE_LINE
        }
    }

    /**
     * アンチエイリアスを掛けるかどうか切り替えする。
     */
    fun toggleAntiAlias() {
        info.mIsAntiAlias = !info.mIsAntiAlias
        pen.isAntiAlias = info.mIsAntiAlias
    }

    /**
     * 各線分で色を変えるか設定する
     *
     * @param isEach
     * 線分ごとに色を変更するか？＿
     */
    fun setColorRange(isEach: Boolean) {
        info.mIsColorEach = isEach
    }

    /**
     * 描画時にアンチエイリアスを掛けるかどうか設定する
     *
     * @param isAA
     * アンチエイリアスフラグ
     */
    fun setAntiAlias(isAA: Boolean) {
        info.mIsAntiAlias = isAA
        pen.isAntiAlias = info.mIsAntiAlias
    }

    /**
     * 線分の個別描画をするかどうか設定する
     *
     * @param DK
     * 線分の個別描画フラグ
     */
    fun setDrawEach(DK: Int) {
        info.draw_kind = DK
    }

    /**
     * 位置情報(相対座標)を設定
     *
     * @param x x-axis position(相対座標)
     * @param y y-axis position(相対座標)
     */
    fun setPosition(x: Float, y: Float) {
        var x = x
        var y = y
        // 位置チェック(相対座標系 -1.0 ~ 1.0をオーバーするとダメ)
        if (x > GraphInfo.GRAPH_POS_MAX)
            x = GraphInfo.GRAPH_POS_MAX
        if (x < GraphInfo.GRAPH_POS_MIN)
            x = GraphInfo.GRAPH_POS_MIN
        if (y > GraphInfo.GRAPH_POS_MAX)
            y = GraphInfo.GRAPH_POS_MAX
        if (y < GraphInfo.GRAPH_POS_MIN)
            y = GraphInfo.GRAPH_POS_MIN

        info.pos.set(x, y)
    }

    /**
     * 複雑さを設定
     *
     * @param cmp
     * 複雑さ
     */
    fun setComplexity(cmp: Int) {
        if (cmp < complexityMin)
            info.complexity = complexityMin
        else if (cmp > complexityMax)
            info.complexity = complexityMax
        else
            info.complexity = cmp
        is_allocated = false
    }

    /**
     * 角速度を設定
     *
     * @param rot_speed
     * 角速度
     */
    fun setRotate(rot_speed: Float) {
        info.rot_speed = rot_speed
    }

    /**
     * 変異率(サイズのみ)を設定
     *
     * @param size
     * 変異率(サイズ)
     */
    fun setMutationSize(size: Float) {
        info.mutation.size = size
        is_allocated = false
    }

    /**
     * 変異率(角度のみ)を設定
     *
     * @param angle
     * 変異率(角度のみ)
     */
    fun setMutationAngle(angle: Float) {
        info.mutation.angle = angle
        is_allocated = false
    }

    /**
     * 乱雑度(サイズのみ)を設定
     *
     * @param size
     * 乱雑度(サイズ)
     */
    fun setRandomizerSize(size: Float) {
        info.randomize.size = size
        is_allocated = false
    }

    /**
     * 乱雑度(角度のみ)を設定
     *
     * @param angle
     * 乱雑度(角度)
     */
    fun setRandomizerAngle(angle: Float) {
        info.randomize.angle = angle
        is_allocated = false
    }

    /**
     * 線分を個別描画時、線分の長さをpercent値で設定する。
     *
     * @param len
     * 線分の長さ(percent)
     */
    fun setDrawEachLength(len: Float) {
        info.mEachLineHistory = (pointMax * len / 100).toInt()
    }

    /**
     * グラフの種類に応じて、グラフの点を形状に合わせて設定する デフォルトでは、正N角形を作る
     */
    protected open fun setRelativePoint() {
        allocatePoints()
        val size = point_base.size
        for (i in 0 until size) {
            point_base[i].set(SinInt.SI().cos(360 * i / size - 180), SinInt.SI().sin(360 * i / size - 180))
        }
        is_allocated = true
    }

    /**
     * グラフの点群を確保する
     */
    protected open fun allocatePoints() {
        while (point.size > pointMax) {
            point.removeAt(0)
            point_base.removeAt(0)
        }
        while (point.size < pointMax) {
            point_base.add(PointF())
            point.add(PointF())
        }
        order_points = Array(n_orders){ Point() }
        calculateOrder()
    }

    /**
     * 線分による描画順序を算出する。\n
     * ※ 複雑さを変更後、先にgetPointMax()の実行が必要
     */
    protected open fun calculateOrder() {
        for (i in 0 until n_orders) {
            val dst = if (i + 1 >= pointMax) i + 1 - pointMax else i + 1
            order_points[i] = Point(i, dst)
        }
    }

    /**
     * pointにpoint_baseをコピーする
     */
    protected fun copyBasePoint() {
        var pt_tmp: PointF

        for (i in 0 until pointMax) {
            mTmpPointF = this.point[i]
            pt_tmp = this.point_base[i]

            mTmpPointF!!.x = pt_tmp.x
            mTmpPointF!!.y = pt_tmp.y
        }
    }

    /**
     * グラフの回転
     */
    protected fun rotateRelativePoint() {
        val sin_z = SinInt.SI().sin(info.angle.toInt())
        val cos_z = SinInt.SI().cos(info.angle.toInt())

        var tx: Float
        var ty: Float

        for (i in 0 until pointMax) {
            mTmpPointF = this.point[i]
            tx = cos_z * mTmpPointF!!.x - sin_z * mTmpPointF!!.y
            ty = sin_z * mTmpPointF!!.x + cos_z * mTmpPointF!!.y

            mTmpPointF!!.x = tx
            mTmpPointF!!.y = ty
        }

        info.angle += info.rot_speed // 角度を角速度分追加
    }

    /**
     * グラフの拡大・移動
     */
    protected fun translateRelativePoint() {
        for (i in 0 until pointMax) {
            mTmpPointF = this.point[i]
            mTmpPointF!!.x = mTmpPointF!!.x * this.info.size.width + this.info.pos.x
            mTmpPointF!!.y = mTmpPointF!!.y * this.info.size.height + this.info.pos.y
        }
    }

    /**
     * 各フレームで呼び出し，グラフの回転・移動・拡大を行う\n
     * 通常時、下記の流れにしたがって動作\n
     * 0. (複雑さなど変更時) グラフ点群を再構築\n
     * 1. 変形前グラフ点群を変形用グラフ点群へコピー\n
     * 2. 点群を回転処理\n
     * 3. 点群を拡大後、移動\n
     * ※ この部分の処理は処理最適化している。\n
     * 　 点の位置を相対座標→絶対座標にする処理を\n
     * 　 ベタ打ちしているが、本来関数化すべき。
     */
    open fun runningGraph() {
        if (!is_allocated)
            setRelativePoint() // 　複雑さなどが変わったらグラフを設定し直す

        copyBasePoint() // point_base => point, 以後pointを操作
        rotateRelativePoint() // 回転
        translateRelativePoint() // 拡大・移動
    }

    fun draw(canvas: Canvas?) {
        if (canvas == null)
            return  // canvasがnullなら描画不能

        // カラーテーブルを更新
        System.arraycopy(mPenColors, 0, mPenColors, 1, COLOR_MAX - 1)
        mPenColors[0] = this.info.cp.doPattern()
        pen.color = mPenColors[0]

        val len = order_points.size // 線分の数だけ描画(DRAW_ALL時)

        val w_area = DGCore.screenSize
        when (info.mBrushType) {
            BRUSHTYPE_LINE -> pen.style = Paint.Style.STROKE
            BRUSHTYPE_TRIANGLE -> pen.style = Paint.Style.FILL
            BRUSHTYPE_CRESCENT -> pen.style = Paint.Style.FILL
            BRUSHTYPE_TWIN_CIRCLE -> pen.style = Paint.Style.FILL
        }
        var rel_src_pt_tmp: PointF
        var rel_dst_pt_tmp: PointF
        when (info.draw_kind) {
            DRAW_ALL // 線分を全て描画
            -> for (i in 0 until len) {
                if (info.mIsColorEach)
                // 設定値に応じ、色を個別変化させる
                    pen.color = mPenColors[i % COLOR_MAX]

                rel_src_pt_tmp = point[order_points[i].x]
                rel_dst_pt_tmp = point[order_points[i].y]
                mPointSrc.set((w_area.x / 2 * (rel_src_pt_tmp.x + 1.0f)).toInt(),
                        (w_area.y / 2 * (rel_src_pt_tmp.y + 1.0f)).toInt())
                mPointDst.set((w_area.x / 2 * (rel_dst_pt_tmp.x + 1.0f)).toInt(),
                        (w_area.y / 2 * (rel_dst_pt_tmp.y + 1.0f)).toInt())

                when (info.mBrushType) {
                    BRUSHTYPE_LINE -> canvas.drawLine(mPointSrc.x.toFloat(), mPointSrc.y.toFloat(), mPointDst.x.toFloat(), mPointDst.y.toFloat(), pen)
                    BRUSHTYPE_TRIANGLE -> drawTriangle(canvas, mPointSrc, mPointDst, pen)
                    BRUSHTYPE_CRESCENT -> drawCrescent(canvas, mPointSrc, mPointDst, pen)
                    BRUSHTYPE_TWIN_CIRCLE -> drawTwinCircles(canvas, mPointSrc, mPointDst, pen)
                }
            }
            DRAW_IN_ORDER // 線分を個々に描画
            -> {
                var tt: Int
                val history = if (info.mEachLineHistory > len) len else info.mEachLineHistory

                for (i in 0 until history) {
                    if (info.mIsColorEach)
                    // 設定値に応じ、色を個別変化させる
                        pen.color = mPenColors[i % COLOR_MAX]
                    tt = if (info.mCurrentDrawOrder - i < 0) len - (i - info.mCurrentDrawOrder) else info.mCurrentDrawOrder - i

                    rel_src_pt_tmp = point[order_points[tt].x]
                    rel_dst_pt_tmp = point[order_points[tt].y]
                    mPointSrc.set((w_area.x / 2 * (rel_src_pt_tmp.x + 1.0f)).toInt(),
                            (w_area.y / 2 * (rel_src_pt_tmp.y + 1.0f)).toInt())
                    mPointDst.set((w_area.x / 2 * (rel_dst_pt_tmp.x + 1.0f)).toInt(),
                            (w_area.y / 2 * (rel_dst_pt_tmp.y + 1.0f)).toInt())

                    when (info.mBrushType) {
                        BRUSHTYPE_LINE -> canvas.drawLine(mPointSrc.x.toFloat(), mPointSrc.y.toFloat(), mPointDst.x.toFloat(), mPointDst.y.toFloat(), pen)
                        BRUSHTYPE_TRIANGLE -> drawTriangle(canvas, mPointSrc, mPointDst, pen)
                        BRUSHTYPE_CRESCENT -> drawCrescent(canvas, mPointSrc, mPointDst, pen)
                        BRUSHTYPE_TWIN_CIRCLE -> drawTwinCircles(canvas, mPointSrc, mPointDst, pen)
                    }
                }
                // 描画する場所を更新(部分描画向け)
                if (++info.mCurrentDrawOrder >= len)
                    info.mCurrentDrawOrder = 0
            }
            else -> {
            }
        }
    }

    private fun drawTriangle(canvas: Canvas, src: Point, dst: Point, pen: Paint) {
        val ex: Double
        val ey: Double
        ex = (dst.x - src.x) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())
        ey = (dst.y - src.y) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())

        // 時計順(dst, src-a, src+a)
        mDrawPath.reset()
        mDrawPath.moveTo(dst.x.toFloat(), dst.y.toFloat())
        mDrawPath.lineTo((src.x + ey * info.mLineThickness).toFloat(), (src.y - ex * info.mLineThickness).toFloat())
        mDrawPath.lineTo((src.x - ey * info.mLineThickness).toFloat(), (src.y + ex * info.mLineThickness).toFloat())
        mDrawPath.close()
        canvas.drawPath(mDrawPath, pen)
    }

    private fun drawCrescent(canvas: Canvas, src: Point, dst: Point, pen: Paint) {

        val width = DRAW_CRESCENT_WIDTH
        val ex: Double
        val ey: Double // 法線方向、単位ベクトル
        ex = (dst.x - src.x) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())
        ey = (dst.y - src.y) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())

        mDrawPath.reset()

        // Upper
        mDrawPath.moveTo(src.x.toFloat(), src.y.toFloat())
        mDrawPath.quadTo((src.x.toDouble() + ((dst.x - src.x) / 2).toDouble() + DRAW_CRESCENT_INNER_ARC_SCALE.toDouble() * width.toDouble() * ey * info.mLineThickness.toDouble()).toFloat(),
                (src.y + (dst.y - src.y) / 2 - DRAW_CRESCENT_INNER_ARC_SCALE.toDouble() * width.toDouble() * ex * info.mLineThickness.toDouble()).toFloat(), dst.x.toFloat(), dst.y.toFloat())
        mDrawPath.moveTo(dst.x.toFloat(), dst.y.toFloat())
        mDrawPath.quadTo((src.x.toDouble() + ((dst.x - src.x) / 2).toDouble() + DRAW_CRESCENT_OUTER_ARC_SCALE.toDouble() * width.toDouble() * ey * info.mLineThickness.toDouble()).toFloat(),
                (src.y + (dst.y - src.y) / 2 - DRAW_CRESCENT_OUTER_ARC_SCALE.toDouble() * width.toDouble() * ex * info.mLineThickness.toDouble()).toFloat(), src.x.toFloat(), src.y.toFloat())

        canvas.drawPath(mDrawPath, pen)
    }

    private fun drawTwinCircles(canvas: Canvas, src: Point, dst: Point, pen: Paint) {
        val width = Math.log((info.mLineThickness + DRAW_TWIN_CIRCLE_WIDTH_OFFSET).toDouble()).toFloat()

        val delta_x = (dst.x - src.x).toFloat()
        val delta_y = (dst.y - src.y).toFloat()

        val dist = Math.sqrt((delta_x * delta_x + delta_y * delta_y).toDouble()).toFloat()

        canvas.drawCircle(src.x + delta_x * DRAW_TWIN_CIRCLE_SMALL_XY_SEC, src.y + delta_y * DRAW_TWIN_CIRCLE_SMALL_XY_SEC, width * dist * DRAW_TWIN_CIRCLE_SMALL_RADIUS, pen)
        canvas.drawCircle(src.x + delta_x * DRAW_TWIN_CIRCLE_LARGE_XY_SEC, src.y + delta_y * DRAW_TWIN_CIRCLE_LARGE_XY_SEC, width * dist * DRAW_TWIN_CIRCLE_LARGE_RADIUS, pen)
    }

    companion object {

        /* 描画方法 */
        val DRAW_ALL = 0
        val DRAW_IN_ORDER = 1

        /* 筆のタイプ */
        val BRUSHTYPE_LINE = 0
        val BRUSHTYPE_TRIANGLE = 1
        val BRUSHTYPE_CRESCENT = 2
        val BRUSHTYPE_TWIN_CIRCLE = 3

        private val COLOR_MAX = 255

        /* 筆の名前文字列 */
        private val STR_BRUSHTYPE_LINE = "draw_line"
        private val STR_BRUSHTYPE_TRIANGLE = "draw_triangle"
        private val STR_BRUSHTYPE_CRESCENT = "draw_crescent"
        private val STR_BRUSHTYPE_TWIN_CIRCLE = "draw_twin_circle"

        private val DRAW_CRESCENT_WIDTH = 1.5f
        private val DRAW_CRESCENT_INNER_ARC_SCALE = 1.5f
        private val DRAW_CRESCENT_OUTER_ARC_SCALE = 2.0f

        private val DRAW_TWIN_CIRCLE_WIDTH_OFFSET = 0.1f

        private val DRAW_TWIN_CIRCLE_SMALL_XY_SEC = 0.3333f
        private val DRAW_TWIN_CIRCLE_SMALL_RADIUS = 0.6666f / 2
        private val DRAW_TWIN_CIRCLE_LARGE_XY_SEC = 0.6666f
        private val DRAW_TWIN_CIRCLE_LARGE_RADIUS = 0.3333f / 2
    }
}
