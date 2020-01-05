package unicot.app.fractalvisualizer.core

/**
 * sin, cos値を1度刻みで計算する<br></br>
 */
class SinInt {
    init {
        for (i in 0..90)
            sinQ[i] = kotlin.math.sin(Math.toRadians(i.toDouble())).toFloat()
    }

    fun sin(_theta: Int): Float {
        var theta = _theta
        while (theta < -180) theta += 360
        while (theta >  180) theta -= 360

        return when {
            theta < -90 -> -sinQ[180 + theta]
            theta < 0   -> -sinQ[-theta]
            theta < 90  ->  sinQ[theta]
            else        ->  sinQ[180 - theta]
        }
    }

    fun cos(phase: Int): Float {
        return sin(phase + 90)
    }

    companion object {
        private val sinQ = FloatArray(90 + 1)

        /**
         * 本データへのアクセス情報を返す。
         * @return 本データへのアクセス情報
         */
        fun getInstance(): SinInt {
            return SinInt()
        }
    }
}
