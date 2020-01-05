package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import kotlin.math.pow

/**
 * シェルピンスキーのカーペット
 */
class SCarpet : Graph() {
    init {
        complexityMin = 1
        complexityMax = 5
        info.graphKind = DGCommon.GraphKind.SIERPINSKI_CARPET
    }

    private fun sums(): Int {
        var sum = 1
        for (i in 2..info.complexity) {
            sum += 8.0.pow((i - 2).toDouble()).toInt()
        }
        sum *= 4
        return sum
    }

    override val pointMax: Int
        get() {
            nOrders = if (info.complexity == 1) 4 else sums()
            return nOrders
        }

    override fun allocatePoints() {
        // 新法
        while (point.size > pointMax) {
            point.removeAt(0)
        }
        while (point.size < pointMax) {
            point.add(PointF())
        }
        pointBase.clear()

        orderPoints = Array(nOrders){ Point() }
        calculateOrder()
    }

    public override fun setRelativePoint() {
        allocatePoints()

        val x = floatArrayOf(GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MIN) // 三角形のx座標(左・右・中)
        val y = floatArrayOf(GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MIN, GraphInfo.GRAPH_POS_MIN) // 三角形のy座標(下・下・上)

        pointBase.add(PointF(x[0], y[0]))
        pointBase.add(PointF(x[1], y[1]))
        pointBase.add(PointF(x[2], y[2]))
        pointBase.add(PointF(x[3], y[3]))

        dividePoints(1, x, y)
        isAllocated = true
    }

    private fun dividePoints(base: Int, x: FloatArray, y: FloatArray) {
        if (base >= info.complexity) return
        if (base == -1) {
            pointBase.add(PointF(x[0], y[0]))
            pointBase.add(PointF(x[1], y[1]))
            pointBase.add(PointF(x[2], y[2]))
            pointBase.add(PointF(x[3], y[3]))
            return
        }

        var childX: FloatArray
        var childY: FloatArray

        // 各辺の中点を結んだ三角形の描画
        val divX = FloatArray(2)
        val divY = FloatArray(2)

        for (i in 1..2) {
            divX[i - 1] = x[0] * (3 - i) / 3.0f + x[1] * i / 3.0f
            divY[i - 1] = y[1] * (3 - i) / 3.0f + y[2] * i / 3.0f
        }

        // 一番下の階層にて、全ての点を登録する

        // 更に分割(全部、左下から反時計回り)
        childX = floatArrayOf(x[0], divX[0], divX[0], x[0])
        childY = floatArrayOf(y[0], y[0], divY[0], divY[0])
        dividePoints(base + 1, childX, childY) // 左下

        childX = floatArrayOf(divX[0], divX[1], divX[1], divX[0])
        childY = floatArrayOf(y[0], y[0], divY[0], divY[0])
        dividePoints(base + 1, childX, childY) // 下

        childX = floatArrayOf(divX[1], x[1], x[1], divX[1])
        childY = floatArrayOf(y[0], y[0], divY[0], divY[0])
        dividePoints(base + 1, childX, childY) // 右下

        childX = floatArrayOf(divX[1], x[1], x[1], divX[1])
        childY = floatArrayOf(divY[0], divY[0], divY[1], divY[1])
        dividePoints(base + 1, childX, childY) // 右

        childX = floatArrayOf(divX[1], x[1], x[1], divX[1])
        childY = floatArrayOf(divY[1], divY[1], y[3], y[3])
        dividePoints(base + 1, childX, childY) // 右上

        childX = floatArrayOf(divX[0], divX[1], divX[1], divX[0])
        childY = floatArrayOf(divY[1], divY[1], y[3], y[3])
        dividePoints(base + 1, childX, childY) // 上

        childX = floatArrayOf(x[0], divX[0], divX[0], x[0])
        childY = floatArrayOf(divY[1], divY[1], y[3], y[3])
        dividePoints(base + 1, childX, childY) // 左上

        childX = floatArrayOf(x[0], divX[0], divX[0], x[0])
        childY = floatArrayOf(divY[0], divY[0], divY[1], divY[1])
        dividePoints(base + 1, childX, childY) // 左

        childX = floatArrayOf(divX[0], divX[1], divX[1], divX[0])
        childY = floatArrayOf(divY[0], divY[0], divY[1], divY[1])
        dividePoints(-1, childX, childY) // 真ん中(ここだけ、次のステップで再帰を終了)
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax) {
            val dst = if (i % 4 == 3) i - 3 else i + 1
            orderPoints[pointMax - 1 - i] = Point(i, dst)
        }
    }
}
