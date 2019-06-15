package unicot.app.fractalvisualizer.model

/**
 * 少数対応のサイズ
 */
class DimensionF constructor(width0: Float = SIZE_MIN, height0: Float = SIZE_MIN){
    var width: Float = width0
        set(field0){
            field = field0
            if (field < SIZE_MIN) field = SIZE_MIN
        }

    var height: Float = height0
        set(field0){
            field = field0
            if (field < SIZE_MIN) field = SIZE_MIN
        }

    fun set(w: Float, h: Float){
        width = w
        height = h
    }

    companion object {
        const val SIZE_MIN = 0.01f
    }
}
