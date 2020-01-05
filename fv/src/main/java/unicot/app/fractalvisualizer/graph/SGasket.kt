package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * シェルピンスキーの三角形
 */
class SGasket : Graph() {
    var skewAngle: Float = 0.0f

    init {
        complexityMin = 1
        complexityMax = 5

        skewAngle = 0.0f

        info.graphKind = DGCommon.GraphKind.SIERPINSKI_GASKET
    }

    override val pointMax: Int
        get() {
            nOrders = (3.0.pow(info.complexity.toDouble()) + 0.5).toInt()    // intでの四捨五入に注意
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

        val x = floatArrayOf(sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MIN, sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MAX, GraphInfo.GRAPH_POS_MID) // 三角形のx座標(左・右・中)
        val y = floatArrayOf(0.5f * GraphInfo.GRAPH_POS_MAX, 0.5f * GraphInfo.GRAPH_POS_MAX, sqrt(3.0).toFloat() / 2 * GraphInfo.GRAPH_POS_MIN) // 三角形のy座標(下・下・上)

        dividePoints(1, x, y) // 三角形描画の呼び出し

        isAllocated = true
    }

    private fun dividePoints(base: Int, x: FloatArray, y: FloatArray) {

        var childX: FloatArray
        var childY: FloatArray

        // 各辺の中点を結んだ三角形の描画
        val midX: FloatArray = floatArrayOf((x[0] + x[1]) / 2, (x[1] + x[2]) / 2, (x[2] + x[0]) / 2)
        val midY: FloatArray = floatArrayOf((y[0] + y[1]) / 2, (y[1] + y[2]) / 2, (y[2] + y[0]) / 2)

        // 一番下の階層にて、全ての点を登録する
        if (base == info.complexity) {
            pointBase.add(PointF(x[0], y[0]))
            pointBase.add(PointF(x[1], y[1]))
            pointBase.add(PointF(x[2], y[2]))
            return
        }

        // 更に分割
        childX = floatArrayOf(x[0], midX[0], midX[2])
        childY = floatArrayOf(y[0], midY[0], midY[2])
        dividePoints(base + 1, childX, childY) // 左下

        childX = floatArrayOf(midX[0], x[1], midX[1])
        childY = floatArrayOf(midY[0], y[1], midY[1])
        dividePoints(base + 1, childX, childY) // 右下

        childX = floatArrayOf(midX[2], midX[1], x[2])
        childY = floatArrayOf(midY[2], midY[1], y[2])
        dividePoints(base + 1, childX, childY) // 上
    }

    override fun calculateOrder() {
        for (i in 0 until pointMax) {
            val dst = if (i % 3 == 2) i - 2 else i + 1
            orderPoints[i] = Point(i, dst)
        }
    }

    override fun runningGraph() {
        if (!isAllocated) setRelativePoint()

        copyBasePoint()

        var tmpX: Float
        var tmpY: Float
        for (n in point.indices) {
            tmpX = (point[n].x + point[n].y * tan(Math.toRadians(skewAngle.toDouble()))).toFloat()
            tmpY = point[n].y

            point[n].x = tmpX
            point[n].y = tmpY
        }

        rotateRelativePoint()
        translateRelativePoint()
    }
}
