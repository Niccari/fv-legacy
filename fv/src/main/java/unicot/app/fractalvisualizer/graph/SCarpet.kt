package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * シェルピンスキーのカーペット
 */
class SCarpet : Graph() {
    init {
        complexityMin = 1
        complexityMax = 5
        info.graph_kind = DGCommon.SIERPINSKI_CARPET
    }

    private fun sums(): Int {
        var sum = 1
        for (i in 2..info.complexity) {
            sum += Math.pow(8.0, (i - 2).toDouble()).toInt()
        }
        sum *= 4
        return sum
    }

    override val pointMax: Int
        get() {
            n_orders = if (info.complexity == 1) 4 else sums()
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

        val x = floatArrayOf(GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MIN) // 三角形のx座標(左・右・中)
        val y = floatArrayOf(GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MIN) // 三角形のy座標(下・下・上)

        point_base.add(PointF(x[0], y[0]))
        point_base.add(PointF(x[1], y[1]))
        point_base.add(PointF(x[2], y[2]))
        point_base.add(PointF(x[3], y[3]))

        dividePoints(1, x, y)
        is_allocated = true
    }

    private fun dividePoints(base: Int, x: FloatArray, y: FloatArray) {
        if (base >= info.complexity) return
        if (base == -1) {
            point_base.add(PointF(x[0], y[0]))
            point_base.add(PointF(x[1], y[1]))
            point_base.add(PointF(x[2], y[2]))
            point_base.add(PointF(x[3], y[3]))
            return
        }

        val x_div: FloatArray
        val y_div: FloatArray
        var x_child: FloatArray
        var y_child: FloatArray

        // 各辺の中点を結んだ三角形の描画
        x_div = FloatArray(2)
        y_div = FloatArray(2)

        for (i in 1..2) {
            x_div[i - 1] = x[0] * (3 - i) / 3.0f + x[1] * i / 3.0f
            y_div[i - 1] = y[1] * (3 - i) / 3.0f + y[2] * i / 3.0f
        }

        // 一番下の階層にて、全ての点を登録する

        // 更に分割(全部、左下から反時計回り)
        x_child = floatArrayOf(x[0], x_div[0], x_div[0], x[0])
        y_child = floatArrayOf(y[0], y[0], y_div[0], y_div[0])
        dividePoints(base + 1, x_child, y_child) // 左下

        x_child = floatArrayOf(x_div[0], x_div[1], x_div[1], x_div[0])
        y_child = floatArrayOf(y[0], y[0], y_div[0], y_div[0])
        dividePoints(base + 1, x_child, y_child) // 下

        x_child = floatArrayOf(x_div[1], x[1], x[1], x_div[1])
        y_child = floatArrayOf(y[0], y[0], y_div[0], y_div[0])
        dividePoints(base + 1, x_child, y_child) // 右下

        x_child = floatArrayOf(x_div[1], x[1], x[1], x_div[1])
        y_child = floatArrayOf(y_div[0], y_div[0], y_div[1], y_div[1])
        dividePoints(base + 1, x_child, y_child) // 右

        x_child = floatArrayOf(x_div[1], x[1], x[1], x_div[1])
        y_child = floatArrayOf(y_div[1], y_div[1], y[3], y[3])
        dividePoints(base + 1, x_child, y_child) // 右上

        x_child = floatArrayOf(x_div[0], x_div[1], x_div[1], x_div[0])
        y_child = floatArrayOf(y_div[1], y_div[1], y[3], y[3])
        dividePoints(base + 1, x_child, y_child) // 上

        x_child = floatArrayOf(x[0], x_div[0], x_div[0], x[0])
        y_child = floatArrayOf(y_div[1], y_div[1], y[3], y[3])
        dividePoints(base + 1, x_child, y_child) // 左上

        x_child = floatArrayOf(x[0], x_div[0], x_div[0], x[0])
        y_child = floatArrayOf(y_div[0], y_div[0], y_div[1], y_div[1])
        dividePoints(base + 1, x_child, y_child) // 左

        x_child = floatArrayOf(x_div[0], x_div[1], x_div[1], x_div[0])
        y_child = floatArrayOf(y_div[0], y_div[0], y_div[1], y_div[1])
        dividePoints(-1, x_child, y_child) // 真ん中(ここだけ、次のステップで再帰を終了)
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax) {
            val dst = if (i % 4 == 3) i - 3 else i + 1
            order_points[pointMax - 1 - i] = Point(i, dst)
        }
    }
}
