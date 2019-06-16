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
    var complexityMin = 2
        protected set
    var complexityMax = 20
        protected set

    protected var isAllocated: Boolean = false
    private val mDrawPath: Path
    private val pen: Paint = Paint() // 描画設定
    protected lateinit var orderPoints: Array<Point> // 描画順
    private val mPenColors: IntArray // 描画色リスト

    val info: GraphInfo = GraphInfo() // グラフ情報

    // pointBase => point => translate, rot_speed, scaling => point'
    protected var pointBase = ArrayList<PointF>(0) // グラフの基本形状のみ
    protected var point = ArrayList<PointF>(0) // 変形後のグラフ

    protected var nOrders: Int = 0 // 線分の数( != pointMax)

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
     * この関数にて点の描画順序数(nOrders)も確定させる
     * @return 描画する点の個数(デフォルトでは正N角形の個数)
     */
    protected open val pointMax: Int
        get() {
            nOrders = info.complexity
            return nOrders
        }

    init {

        pen.strokeWidth = info.mLineThickness
        pen.isAntiAlias = true
        pen.style = Paint.Style.STROKE

        mPenColors = IntArray(COLOR_MAX)
        isAllocated = false

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
        this.info.setDrawSettings(src.draw_kind, src.mLineThickness, src.mIsColorEach, src.mEachLineHistory)

        if (isFullCopy)
            this.info.mCurrentDrawOrder = src.mCurrentDrawOrder
        this.info.mBrushType = src.mBrushType
        for (i in COLOR_MAX - 1 downTo 0) {
            mPenColors[i] = this.info.cp.doPattern()
        }

        pen.strokeWidth = info.mLineThickness
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

        when {
            tmp.matches(STR_BRUSHTYPE_LINE.toRegex()) -> info.mBrushType = BRUSHTYPE_LINE
            tmp.matches(STR_BRUSHTYPE_TRIANGLE.toRegex()) -> info.mBrushType = BRUSHTYPE_TRIANGLE
            tmp.matches(STR_BRUSHTYPE_CRESCENT.toRegex()) -> info.mBrushType = BRUSHTYPE_CRESCENT
            tmp.matches(STR_BRUSHTYPE_TWIN_CIRCLE.toRegex()) -> info.mBrushType = BRUSHTYPE_TWIN_CIRCLE
            else -> info.mBrushType = BRUSHTYPE_LINE
        }
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
     * @param x0 x-axis position(相対座標)
     * @param y0 y-axis position(相対座標)
     */
    fun setPosition(x0: Float, y0: Float) {
        var x = x0
        var y = y0
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
        when {
            cmp < complexityMin -> info.complexity = complexityMin
            cmp > complexityMax -> info.complexity = complexityMax
            else -> info.complexity = cmp
        }
        isAllocated = false
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
        isAllocated = false
    }

    /**
     * 変異率(角度のみ)を設定
     *
     * @param angle
     * 変異率(角度のみ)
     */
    fun setMutationAngle(angle: Float) {
        info.mutation.angle = angle
        isAllocated = false
    }

    /**
     * 乱雑度(サイズのみ)を設定
     *
     * @param size
     * 乱雑度(サイズ)
     */
    fun setRandomizerSize(size: Float) {
        info.randomize.size = size
        isAllocated = false
    }

    /**
     * 乱雑度(角度のみ)を設定
     *
     * @param angle
     * 乱雑度(角度)
     */
    fun setRandomizerAngle(angle: Float) {
        info.randomize.angle = angle
        isAllocated = false
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
        val size = pointBase.size
        for (i in 0 until size) {
            pointBase[i].set(SinInt.SI().cos(360 * i / size - 180), SinInt.SI().sin(360 * i / size - 180))
        }
        isAllocated = true
    }

    /**
     * グラフの点群を確保する
     */
    protected open fun allocatePoints() {
        while (point.size > pointMax) {
            point.removeAt(0)
            pointBase.removeAt(0)
        }
        while (point.size < pointMax) {
            pointBase.add(PointF())
            point.add(PointF())
        }
        orderPoints = Array(nOrders){ Point() }
        calculateOrder()
    }

    /**
     * 線分による描画順序を算出する。\n
     * ※ 複雑さを変更後、先にgetPointMax()の実行が必要
     */
    protected open fun calculateOrder() {
        for (i in 0 until nOrders) {
            val dst = if (i + 1 >= pointMax) i + 1 - pointMax else i + 1
            orderPoints[i] = Point(i, dst)
        }
    }

    /**
     * pointにpoint_baseをコピーする
     */
    protected fun copyBasePoint() {
        var ptTmp: PointF

        for (i in 0 until pointMax) {
            mTmpPointF = this.point[i]
            ptTmp = this.pointBase[i]

            mTmpPointF!!.x = ptTmp.x
            mTmpPointF!!.y = ptTmp.y
        }
    }

    /**
     * グラフの回転
     */
    protected fun rotateRelativePoint() {
        val sinZ = SinInt.SI().sin(info.angle.toInt())
        val cosZ = SinInt.SI().cos(info.angle.toInt())

        var tx: Float
        var ty: Float

        for (i in 0 until pointMax) {
            mTmpPointF = this.point[i]
            tx = cosZ * mTmpPointF!!.x - sinZ * mTmpPointF!!.y
            ty = sinZ * mTmpPointF!!.x + cosZ * mTmpPointF!!.y

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
        if (!isAllocated)
            setRelativePoint() // 　複雑さなどが変わったらグラフを設定し直す

        copyBasePoint() // pointBase => point, 以後pointを操作
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

        val len = orderPoints.size // 線分の数だけ描画(DRAW_ALL時)

        val screenSize = DGCore.screenSize
        when (info.mBrushType) {
            BRUSHTYPE_LINE -> pen.style = Paint.Style.STROKE
            BRUSHTYPE_TRIANGLE -> pen.style = Paint.Style.FILL
            BRUSHTYPE_CRESCENT -> pen.style = Paint.Style.FILL
            BRUSHTYPE_TWIN_CIRCLE -> pen.style = Paint.Style.FILL
        }
        var relSrcPtTmp: PointF
        var relDstPtTmp: PointF
        when (info.draw_kind) {
            DRAW_ALL // 線分を全て描画
            -> for (i in 0 until len) {
                if (info.mIsColorEach)
                // 設定値に応じ、色を個別変化させる
                    pen.color = mPenColors[i % COLOR_MAX]

                relSrcPtTmp = point[orderPoints[i].x]
                relDstPtTmp = point[orderPoints[i].y]
                mPointSrc.set((screenSize.x / 2 * (relSrcPtTmp.x + 1.0f)).toInt(),
                        (screenSize.y / 2 * (relSrcPtTmp.y + 1.0f)).toInt())
                mPointDst.set((screenSize.x / 2 * (relDstPtTmp.x + 1.0f)).toInt(),
                        (screenSize.y / 2 * (relDstPtTmp.y + 1.0f)).toInt())

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

                    relSrcPtTmp = point[orderPoints[tt].x]
                    relDstPtTmp = point[orderPoints[tt].y]
                    mPointSrc.set((screenSize.x / 2 * (relSrcPtTmp.x + 1.0f)).toInt(),
                            (screenSize.y / 2 * (relSrcPtTmp.y + 1.0f)).toInt())
                    mPointDst.set((screenSize.x / 2 * (relDstPtTmp.x + 1.0f)).toInt(),
                            (screenSize.y / 2 * (relDstPtTmp.y + 1.0f)).toInt())

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
        val ex: Double = (dst.x - src.x) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())
        val ey: Double = (dst.y - src.y) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())

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
        val ex: Double = (dst.x - src.x) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())
        val ey: Double = (dst.y - src.y) / Math.sqrt(((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y)).toDouble())

        mDrawPath.reset()

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

        val dx = (dst.x - src.x).toFloat()
        val dy = (dst.y - src.y).toFloat()

        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        canvas.drawCircle(src.x + dx * DRAW_TWIN_CIRCLE_SMALL_XY_SEC, src.y + dy * DRAW_TWIN_CIRCLE_SMALL_XY_SEC, width * dist * DRAW_TWIN_CIRCLE_SMALL_RADIUS, pen)
        canvas.drawCircle(src.x + dx * DRAW_TWIN_CIRCLE_LARGE_XY_SEC, src.y + dy * DRAW_TWIN_CIRCLE_LARGE_XY_SEC, width * dist * DRAW_TWIN_CIRCLE_LARGE_RADIUS, pen)
    }

    companion object {

        /* 描画方法 */
        const val DRAW_ALL = 0
        const val DRAW_IN_ORDER = 1

        /* 筆のタイプ */
        const val BRUSHTYPE_LINE = 0
        const val BRUSHTYPE_TRIANGLE = 1
        const val BRUSHTYPE_CRESCENT = 2
        const val BRUSHTYPE_TWIN_CIRCLE = 3

        private const val COLOR_MAX = 255

        /* 筆の名前文字列 */
        private const val STR_BRUSHTYPE_LINE = "draw_line"
        private const val STR_BRUSHTYPE_TRIANGLE = "draw_triangle"
        private const val STR_BRUSHTYPE_CRESCENT = "draw_crescent"
        private const val STR_BRUSHTYPE_TWIN_CIRCLE = "draw_twin_circle"

        private const val DRAW_CRESCENT_WIDTH = 1.5f
        private const val DRAW_CRESCENT_INNER_ARC_SCALE = 1.5f
        private const val DRAW_CRESCENT_OUTER_ARC_SCALE = 2.0f

        private const val DRAW_TWIN_CIRCLE_WIDTH_OFFSET = 0.1f

        private const val DRAW_TWIN_CIRCLE_SMALL_XY_SEC = 1 / 3.0f
        private const val DRAW_TWIN_CIRCLE_SMALL_RADIUS = 2 / 3.0f / 2
        private const val DRAW_TWIN_CIRCLE_LARGE_XY_SEC = 2 / 3.0f
        private const val DRAW_TWIN_CIRCLE_LARGE_RADIUS = 1 / 3.0f / 2
    }
}
