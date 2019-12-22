package unicot.app.fractalvisualizer.graph

import android.graphics.Color
import java.util.*

/**
 * グラフの色情報を制御する
 */
class ColorPattern {
    private var mColorTransientPattern: ColorName = ColorName.RAINBOW
    var shiftSpeed: Int = SHIFTSPEED_INIT

    private var ca: Int = COLOR_MAX
    private var cr: Int = COLOR_MAX
    private var cg: Int = COLOR_MIN
    private var cb: Int = COLOR_MIN
    private var trans: Int = TRANS_INIT

    var color: Int
        get() = Color.argb(ca, cr, cg, cb)
        set(col) {
            alpha = Color.alpha(col)
            red   = Color.red(col)
            green = Color.green(col)
            blue  = Color.blue(col)
        }

    var red: Int
        get() = cr
        private set(r) {
            if (r in COLOR_MIN..COLOR_MAX) {
                cr = r
            }
        }
    var green: Int
        get() = cg
        private set(g) {
            if (g in COLOR_MIN..COLOR_MAX) {
                cg = g
            }
        }
    var blue: Int
        get() = cb
        private set(b) {
            if (b in COLOR_MIN..COLOR_MAX) {
                cb = b
            }
        }
    var alpha: Int
        get() = ca
        set(a) {
            if (a in COLOR_MIN..COLOR_MAX) {
                ca = a
            }
        }

    val colMode: ColorName
        get() = mColorTransientPattern

    val colModeInString: String
        get() = colMode.value

    fun init(cp: ColorPattern, isCopy: Boolean) {
        mColorTransientPattern = cp.mColorTransientPattern
        shiftSpeed = cp.shiftSpeed
        val newColor = cp.color
        val newTrans = cp.trans
        setPattern()

        if (isCopy) {
            this.color = newColor
            trans = newTrans
        }
    }

    fun getTrans(): Int {
        return trans
    }

    fun setTrans(trans0: Int) {
        if (trans0 in COLOR_TRANS_IDX_MIN..COLOR_TRANS_IDX_MAX) {
            trans = trans0
        }
    }

    fun setColMode(str_cmode: String) {
        mColorTransientPattern =
                ColorName.valueOf(str_cmode.toUpperCase(Locale.ENGLISH))
        setPattern()
    }

    private fun setPattern() {
        when (mColorTransientPattern) {
            ColorName.SINGLE -> trans = -1
            ColorName.RAINBOW, ColorName.FIRE -> {
                trans = COLOR_TRANS_IDX_RED_TO_YELLOW
                cr = COLOR_MAX
                cg = COLOR_MIN
                cb = COLOR_MIN
            }
            ColorName.FOREST -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_PURPLE
                cr = COLOR_MIN
                cg = COLOR_MAX
                cb = COLOR_MIN
            }
            ColorName.COOL -> {
                trans = COLOR_TRANS_IDX_RED_TO_YELLOW
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            ColorName.DAWN -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_RED
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            ColorName.DEEPSEA -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_GREEN
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            ColorName.HEAT -> {
                trans = COLOR_TRANS_IDX_YELLOW_TO_RED
                cr = COLOR_MAX
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            ColorName.BW -> {
                trans = COLOR_TRANS_IDX_WHITE
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MIN
            }
            ColorName.PASTEL -> {
                trans = COLOR_TRANS_IDX_MAGENTA_TO_YELLOW
                cr = COLOR_MAX
                cg = COLOR_PASTEL
                cb = COLOR_PASTEL
            }
        }
    }

    fun doPattern(): Int {
        if (this.transPattern(trans)){
            when (mColorTransientPattern) {
                ColorName.SINGLE -> {}
                ColorName.RAINBOW -> {
                    if (shiftSpeed > 0) {
                        trans++
                        if (trans > COLOR_TRANS_IDX_PURPLE_TO_RED)
                            trans = COLOR_TRANS_IDX_RED_TO_YELLOW
                    } else {
                        trans--
                        if (trans < COLOR_TRANS_IDX_MIN)
                            trans = COLOR_TRANS_IDX_PURPLE_TO_RED
                    }
                }
                ColorName.FIRE, ColorName.FOREST, ColorName.COOL -> {
                    if (shiftSpeed > 0) {
                        trans += COLOR_PATTERN_RGB_TRANS
                        if (trans > COLOR_TRANS_IDX_PURPLE_TO_RED)
                            trans -= COLOR_TRANS_IDX_WHITE
                    } else {
                        trans -= COLOR_PATTERN_RGB_TRANS
                        if (trans < COLOR_TRANS_IDX_RED_TO_YELLOW)
                            trans += COLOR_TRANS_IDX_WHITE
                    }
                }
                ColorName.DAWN -> {
                    if (shiftSpeed > 0) {
                        trans++
                        if (trans > COLOR_TRANS_IDX_RED_TO_BLUE)
                            trans = COLOR_TRANS_IDX_BLUE_TO_RED
                    } else {
                        trans--
                        if (trans < COLOR_TRANS_IDX_BLUE_TO_RED)
                            trans = COLOR_TRANS_IDX_RED_TO_BLUE
                    }
                }
                ColorName.DEEPSEA -> {
                    if (shiftSpeed > 0) {
                        trans++
                        if (trans > COLOR_TRANS_IDX_GREEN_TO_BLUE)
                            trans = COLOR_TRANS_IDX_BLUE_TO_GREEN
                    } else {
                        trans--
                        if (trans < COLOR_TRANS_IDX_BLUE_TO_GREEN)
                            trans = COLOR_TRANS_IDX_GREEN_TO_BLUE
                    }
                }
                ColorName.HEAT -> {
                    if (shiftSpeed > 0) {
                        trans++

                        if (trans > COLOR_TRANS_IDX_RED_TO_YELLOW_H)
                            trans = COLOR_TRANS_IDX_YELLOW_TO_RED
                    } else {
                        trans--

                        if (trans < COLOR_TRANS_IDX_YELLOW_TO_RED)
                            trans = COLOR_TRANS_IDX_RED_TO_YELLOW_H
                    }
                }
                ColorName.BW -> {
                    trans = if (trans == COLOR_TRANS_IDX_WHITE) COLOR_TRANS_IDX_BLACK else COLOR_TRANS_IDX_WHITE
                }
                ColorName.PASTEL -> {
                    if (shiftSpeed > 0) {
                        trans++
                        if (trans > COLOR_TRANS_IDX_YELLOW_TO_MAGENTA)
                            trans = COLOR_TRANS_IDX_MAGENTA_TO_YELLOW
                    } else {
                        trans--
                        if (trans < COLOR_TRANS_IDX_MAGENTA_TO_YELLOW)
                            trans = COLOR_TRANS_IDX_YELLOW_TO_MAGENTA
                    }
                }
            }
        }
        return Color.argb(ca, cr, cg, cb)
    }

    private fun transPattern(trans: Int): Boolean {
        when (trans) {
            COLOR_TRANS_IDX_RED_TO_YELLOW     // Green on
                , COLOR_TRANS_IDX_RED_TO_YELLOW_H   // For heat
            -> {
                cg += shiftSpeed
                if (cg >= COLOR_MAX) {
                    cg = COLOR_MAX
                    return true
                } else if (cg <= COLOR_MIN) {
                    cg = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_YELLOW_TO_GREEN // Red off
            -> {
                cr -= shiftSpeed
                if (cr >= COLOR_MAX) {
                    cr = COLOR_MAX
                    return true
                } else if (cr <= COLOR_MIN) {
                    cr = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_GREEN_TO_CYAN // Blue on
            -> {
                cb += shiftSpeed
                if (cb >= COLOR_MAX) {
                    cb = COLOR_MAX
                    return true
                } else if (cb <= COLOR_MIN) {
                    cb = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_CYAN_TO_BLUE // Green off
            -> {
                cg -= shiftSpeed
                if (cg >= COLOR_MAX) {
                    cg = COLOR_MAX
                    return true
                } else if (cg <= COLOR_MIN) {
                    cg = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_BLUE_TO_PURPLE // Red on
            -> {
                cr += shiftSpeed
                if (cr >= COLOR_MAX) {
                    cr = COLOR_MAX
                    return true
                } else if (cr <= COLOR_MIN) {
                    cr = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_PURPLE_TO_RED // Blue off
            -> {
                cb -= shiftSpeed
                if (cb >= COLOR_MAX) {
                    cb = COLOR_MAX
                    return true
                } else if (cb <= COLOR_MIN) {
                    cb = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_WHITE // Black to White
            -> {
                cr += shiftSpeed
                cg = cr
                cb = cr
                if (cr >= COLOR_MAX) {
                    cr = COLOR_MAX
                    cg = cr
                    cb = cr
                    return true
                } else if (cr <= COLOR_MIN) {
                    cr = COLOR_MIN
                    cg = cr
                    cb = cr
                    return true
                }
            }
            COLOR_TRANS_IDX_BLACK // White to Black
            -> {
                cr -= shiftSpeed
                cb = cr
                cg = cr
                if (cr >= COLOR_MAX) {
                    cr = COLOR_MAX
                    cg = cr
                    cb = cr
                    return true
                } else if (cr <= COLOR_MIN) {
                    cr = COLOR_MIN
                    cg = cr
                    cb = cr
                    return true
                }
            }
            COLOR_TRANS_IDX_BLUE_TO_RED   // For DAWN
                , COLOR_TRANS_IDX_BLUE_TO_RED_H // For HEAT
            -> {

                cr += shiftSpeed
                cb -= shiftSpeed
                cg = COLOR_MIN
                if (cr >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MAX
                    cg = COLOR_MIN
                    cb = COLOR_MIN
                    return true
                } else if (cr <= COLOR_MIN) {   // Backword direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                }
            }
            COLOR_TRANS_IDX_RED_TO_BLUE   // For DAWN
                , COLOR_TRANS_IDX_RED_TO_BLUE_H // For HEAT
            -> {
                cr -= shiftSpeed
                cb += shiftSpeed
                cg = COLOR_MIN
                if (cb >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                } else if (cb <= COLOR_MIN) {   // Backword direction
                    cr = COLOR_MAX
                    cg = COLOR_MIN
                    cb = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_BLUE_TO_GREEN   // For DEEPSEA
            -> {
                cr = COLOR_MIN
                cb -= shiftSpeed
                cg += shiftSpeed
                if (cg >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MIN
                    cg = COLOR_MAX
                    cb = COLOR_MIN
                    return true
                } else if (cg <= COLOR_MIN) {   // Backword direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                }
            }
            COLOR_TRANS_IDX_GREEN_TO_BLUE   // For DEEPSEA
            -> {
                cr = COLOR_MIN
                cg -= shiftSpeed
                cb += shiftSpeed
                if (cb >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                } else if (cb <= COLOR_MIN) {   // Backword direction
                    cr = COLOR_MIN
                    cg = COLOR_MAX
                    cb = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_YELLOW_TO_RED   // For HEAT
            -> {
                cr = COLOR_MAX
                cg -= shiftSpeed
                cb = COLOR_MIN
                if (cg <= COLOR_MIN) {          // Forward direction
                    cr = COLOR_MAX
                    cg = COLOR_MIN
                    cb = COLOR_MIN
                    return true
                } else if (cg >= COLOR_MAX) {   // Backword direction
                    cr = COLOR_MAX
                    cg = COLOR_MAX
                    cb = COLOR_MIN
                    return true
                }
            }
            COLOR_TRANS_IDX_BLUE_TO_BLACK   // For HEAT
            -> {
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb -= shiftSpeed
                if (cb <= COLOR_MIN) {          // Forward direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MIN
                    return true
                } else if (cb >= COLOR_MAX) {   // Backword direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                }
            }
            COLOR_TRANS_IDX_BLACK_TO_BLUE   // For HEAT
            -> {
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb += shiftSpeed
                if (cb <= COLOR_MIN) {          // Forward direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MIN
                    return true
                } else if (cb >= COLOR_MAX) {   // Backword direction
                    cr = COLOR_MIN
                    cg = COLOR_MIN
                    cb = COLOR_MAX
                    return true
                }
            }

            COLOR_TRANS_IDX_MAGENTA_TO_YELLOW   // For PASTEL
            -> {
                cr = COLOR_MAX
                cg += shiftSpeed
                cb = COLOR_PASTEL
                if (cg >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MAX
                    cg = COLOR_MAX
                    cb = COLOR_PASTEL
                    return true
                } else if (cg <= COLOR_PASTEL) {   // Backward direction
                    cr = COLOR_MAX
                    cg = COLOR_PASTEL
                    cb = COLOR_PASTEL
                    return true
                }
            }
            COLOR_TRANS_IDX_YELLOW_TO_CYAN   // For PASTEL
            -> {
                cr -= shiftSpeed
                cg = COLOR_MAX
                cb += shiftSpeed
                if (cb >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_PASTEL
                    cg = COLOR_MAX
                    cb = COLOR_MAX
                    return true
                } else if (cb <= COLOR_PASTEL) {   // Backward direction
                    cr = COLOR_MAX
                    cg = COLOR_MAX
                    cb = COLOR_PASTEL
                    return true
                }
            }
            COLOR_TRANS_IDX_CYAN_TO_YELLOW   // For PASTEL
            -> {
                cr += shiftSpeed
                cg = COLOR_MAX
                cb -= shiftSpeed
                if (cr >= COLOR_MAX) {          // Forward direction
                    cr = COLOR_MAX
                    cg = COLOR_MAX
                    cb = COLOR_PASTEL
                    return true
                } else if (cr <= COLOR_PASTEL) {   // Backward direction
                    cr = COLOR_PASTEL
                    cg = COLOR_MAX
                    cb = COLOR_MAX
                    return true
                }
            }
            COLOR_TRANS_IDX_YELLOW_TO_MAGENTA   // For PASTEL
            -> {
                cr = COLOR_MAX
                cg -= shiftSpeed
                cb = COLOR_PASTEL
                if (cg <= COLOR_PASTEL) {          // Forward direction
                    cr = COLOR_MAX
                    cg = COLOR_PASTEL
                    cb = COLOR_PASTEL
                    return true
                } else if (cg >= COLOR_MAX) {   // Backward direction
                    cr = COLOR_MAX
                    cg = COLOR_MAX
                    cb = COLOR_PASTEL
                    return true
                }
            }
            else -> return false
        }
        return false
    }

    enum class ColorName(val value: String){
        SINGLE("SINGLE"),
        RAINBOW("RAINBOW"),
        FIRE("FIRE"),
        FOREST("FOREST"),
        COOL("COOL"),
        DAWN("DAWN"),
        DEEPSEA("DEEPSEA"),
        HEAT("HEAT"),
        BW("BW"),
        PASTEL("PASTEL")
    }
    companion object {
        const val COLOR_MIN = 0
        const val COLOR_PASTEL = (255*0.6).toInt()
        const val COLOR_MAX = 255

        private const val COLOR_TRANS_IDX_RED_TO_YELLOW = 0
        private const val COLOR_TRANS_IDX_YELLOW_TO_GREEN = 1
        private const val COLOR_TRANS_IDX_GREEN_TO_CYAN = 2
        private const val COLOR_TRANS_IDX_CYAN_TO_BLUE = 3
        private const val COLOR_TRANS_IDX_BLUE_TO_PURPLE = 4
        private const val COLOR_TRANS_IDX_PURPLE_TO_RED = 5
        private const val COLOR_TRANS_IDX_WHITE = 6
        private const val COLOR_TRANS_IDX_BLACK = 7
        private const val COLOR_TRANS_IDX_BLUE_TO_RED = 8
        private const val COLOR_TRANS_IDX_RED_TO_BLUE = 9
        private const val COLOR_TRANS_IDX_BLUE_TO_GREEN = 10
        private const val COLOR_TRANS_IDX_GREEN_TO_BLUE = 11
        private const val COLOR_TRANS_IDX_YELLOW_TO_RED = 12
        private const val COLOR_TRANS_IDX_RED_TO_BLUE_H = 13
        private const val COLOR_TRANS_IDX_BLUE_TO_BLACK = 14
        private const val COLOR_TRANS_IDX_BLACK_TO_BLUE = 15
        private const val COLOR_TRANS_IDX_BLUE_TO_RED_H = 16
        private const val COLOR_TRANS_IDX_RED_TO_YELLOW_H = 17

        private const val COLOR_TRANS_IDX_MAGENTA_TO_YELLOW = 18
        private const val COLOR_TRANS_IDX_YELLOW_TO_CYAN = 19
        private const val COLOR_TRANS_IDX_CYAN_TO_YELLOW = 20
        private const val COLOR_TRANS_IDX_YELLOW_TO_MAGENTA = 21

        private const val COLOR_TRANS_IDX_MIN = COLOR_TRANS_IDX_RED_TO_YELLOW
        private const val COLOR_TRANS_IDX_MAX = COLOR_TRANS_IDX_YELLOW_TO_MAGENTA

        private const val COLOR_PATTERN_RGB_TRANS = 3

        private const val TRANS_INIT = COLOR_TRANS_IDX_RED_TO_YELLOW

        private const val SHIFTSPEED_INIT = 1
    }
}
