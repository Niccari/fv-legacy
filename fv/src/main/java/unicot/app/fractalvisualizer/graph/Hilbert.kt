package unicot.app.fractalvisualizer.graph

import android.graphics.Point
import android.graphics.PointF

import unicot.app.fractalvisualizer.core.DGCommon

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
        info.graph_kind = DGCommon.HILBERT
    }

    override val pointMax: Int
        get() {
            n_orders = Math.pow(4.0, info.complexity.toDouble()).toInt() - 1
            return n_orders + 1
        }

    private fun ldr(n: Int) {  // ←  ↓ →
        if (n <= 0) return

        dlu(n - 1)
        hx -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
        hy += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
        hx += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
    }

    private fun urd(n: Int) {  // ↑ →  ↓
        if (n <= 0) return

        rul(n - 1)
        hy -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
        hx += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        urd(n - 1)
        hy += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        ldr(n - 1)
    }

    private fun rul(n: Int) {  //  →  ↑ ←
        if (n <= 0) return

        urd(n - 1)
        hx += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
        hy -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
        hx -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
    }

    private fun dlu(n: Int) {  //  ↓ ←  ↑
        if (n <= 0) return

        ldr(n - 1)
        hy += dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
        hx -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        dlu(n - 1)
        hy -= dxy
        point_base.add(pidx, PointF(hx, hy))
        pidx++
        rul(n - 1)
    }

    public override fun setRelativePoint() {
        allocatePoints()

        // 最初は右上から(大きさを1に正規化するため、0.5にしている)
        hx = GraphInfo.GRAPH_SIZE_MID / 2
        hy = -GraphInfo.GRAPH_SIZE_MID / 2

        pidx = 0
        dxy = (1.0f / (Math.pow(2.0, info.complexity.toDouble()) - 1)).toFloat()    // 移動量

        point_base.add(pidx++, PointF(hx, hy))
        ldr(info.complexity)
        is_allocated = true
    }

    override fun calculateOrder() {
        for (i in pointMax - 1 downTo 1) {
            val src = i - 1
            order_points[pointMax - 1 - i] = Point(src, i)
        }
    }
}
