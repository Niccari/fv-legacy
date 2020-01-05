package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon
import kotlin.math.pow

/**
 * ヒルベルト曲線
 */
class Hilbert : Graph() {

    private var hx: Float = 0.toFloat()
    private var hy: Float = 0.toFloat()   // スタート地点の座標を設定
    private var pidx: Int = 0     // 点の番号
    private var dxy: Float = 0.toFloat()      // 移動量

    init {
        complexityMin = 1
        complexityMax = 5
        info.graphKind = DGCommon.GraphKind.HILBERT
    }

    override val pointMax: Int
        get() {
            nOrders = 4.0.pow(info.complexity.toDouble()).toInt() - 1
            return nOrders + 1
        }

    private fun ldr(n: Int) {  // ←  ↓ →
        if (n <= 0) return

        dlu(n - 1)
        hx -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
        hy += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
        hx += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
    }

    private fun urd(n: Int) {  // ↑ →  ↓
        if (n <= 0) return

        rul(n - 1)
        hy -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
        hx += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
        hy += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
    }

    private fun rul(n: Int) {  //  →  ↑ ←
        if (n <= 0) return

        urd(n - 1)
        hx += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
        hy -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
        hx -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
    }

    private fun dlu(n: Int) {  //  ↓ ←  ↑
        if (n <= 0) return

        ldr(n - 1)
        hy += dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
        hx -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
        hy -= dxy
        pointBase.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
    }

    public override fun setRelativePoint() {
        allocatePoints()

        // 最初は右上から(大きさを1に正規化するため、0.5にしている)
        hx = GraphInfo.GRAPH_SIZE_MID / 2
        hy = -GraphInfo.GRAPH_SIZE_MID / 2

        pidx = 0
        dxy = (1.0f / (2.0.pow(info.complexity.toDouble()) - 1)).toFloat()    // 移動量

        pointBase.add(pidx++, PointF(hx, hy))
        ldr(info.complexity)
        isAllocated = true
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            orderPoints[pointMax - 1 - i] = Point(src, i)
        }
    }
}
