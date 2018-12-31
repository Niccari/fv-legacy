package unicot.app.fractalvisualizer.core

/**
 * sin, cos値を1度刻みで計算する<br></br>
 */
class SinInt {
    init {
        for (i in 0..90)
            sinQ[i] = Math.sin(Math.toRadians(i.toDouble())).toFloat()    //Calculate sin Curve(0 to 90 [deg])
    }

    fun sin(_theta: Int): Float {
        var theta = _theta
        while (theta < -180) theta += 360
        while (theta > 180) theta -= 360

        return if (theta < -90) {
            -sinQ[180 + theta]
        } else if (theta < 0) {
            -sinQ[-theta]
        } else if (theta < 90) {
            sinQ[theta]
        } else {
            sinQ[180 - theta]
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
        fun SI(): SinInt {
            return SinInt()
        }
    }
}
