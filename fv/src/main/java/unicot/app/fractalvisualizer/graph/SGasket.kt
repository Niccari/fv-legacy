package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * シェルピンスキーの三角形
 */
class SGasket : Graph() {
    private var mSkewAngle: Float = 0.toFloat()

    var skewAngle: Float
        get() = mSkewAngle
        set(s) {
            if (SKEW_MIN <= s && SKEW_MAX >= s) {
                mSkewAngle = s
            }
        }

    init {
        complexityMin = 1
        complexityMax = 5

        mSkewAngle = SKEW_INIT

        info.graph_kind = DGCommon.SIERPINSKI_GASKET
    }

    override val pointMax: Int
        get() {
            n_orders = (Math.pow(3.0, info.complexity.toDouble()) + 0.5).toInt()    // intでの四捨五入に注意
            return n_orders
        }

    override fun allocatePoints() {
        // 新法
        while (point.size > pointMax) {
            point.removeAt(0)
        }
        while (point.size < pointMax) {
            point.add(PointF())
        }
        point_base.clear()

        order_points = Array(n_orders){ Point() }
        calculateOrder()
    }

    public override fun setRelativePoint() {
        allocatePoints()

        val x = floatArrayOf(Math.sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MIN, Math.sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MID) // 三角形のx座標(左・右・中)
        val y = floatArrayOf(0.5f * GraphInfo.GRAPH_POS_MAX, 0.5f * GraphInfo.GRAPH_POS_MAX, Math.sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MIN) // 三角形のy座標(下・下・上)

        dividePoints(1, x, y) // 三角形描画の呼び出し

        is_allocated = true
    }

    private fun dividePoints(base: Int, x: FloatArray, y: FloatArray) {

        val x_m: FloatArray
        val y_m: FloatArray
        var x_child: FloatArray
        var y_child: FloatArray

        // 各辺の中点を結んだ三角形の描画
        x_m = floatArrayOf((x[0] + x[1]) / 2, (x[1] + x[2]) / 2, (x[2] + x[0]) / 2)
        y_m = floatArrayOf((y[0] + y[1]) / 2, (y[1] + y[2]) / 2, (y[2] + y[0]) / 2)

        // 一番下の階層にて、全ての点を登録する
        if (base == info.complexity) {
            point_base.add(PointF(x[0], y[0]))
            point_base.add(PointF(x[1], y[1]))
            point_base.add(PointF(x[2], y[2]))
            return
        }

        // 更に分割
        x_child = floatArrayOf(x[0], x_m[0], x_m[2])
        y_child = floatArrayOf(y[0], y_m[0], y_m[2])
        dividePoints(base + 1, x_child, y_child) // 左下

        x_child = floatArrayOf(x_m[0], x[1], x_m[1])
        y_child = floatArrayOf(y_m[0], y[1], y_m[1])
        dividePoints(base + 1, x_child, y_child) // 右下

        x_child = floatArrayOf(x_m[2], x_m[1], x[2])
        y_child = floatArrayOf(y_m[2], y_m[1], y[2])
        dividePoints(base + 1, x_child, y_child) // 上
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax) {
            val dst = if (i % 3 == 2) i - 2 else i + 1
            order_points[i] = Point(i, dst)
        }
    }

    override fun runningGraph() {
        if (!is_allocated) setRelativePoint()

        copyBasePoint()

        var x_tmp: Float
        var y_tmp: Float
        for (n in point.indices) {
            x_tmp = (point[n].x + point[n].y * Math.tan(Math.toRadians(mSkewAngle.toDouble()))).toFloat()
            y_tmp = point[n].y

            point[n].x = x_tmp
            point[n].y = y_tmp
        }

        rotateRelativePoint()
        translateRelativePoint()
    }

    companion object {
        val SKEW_MIN = -85.0f
        val SKEW_MAX = 85.0f
        private val SKEW_INIT = 0.0f
    }
}
