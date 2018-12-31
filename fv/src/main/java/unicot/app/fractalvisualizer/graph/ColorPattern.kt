package unicot.app.fractalvisualizer.graph

import android.graphics.Color
import android.util.Log
import java.util.*

/**
 * グラフの色情報を制御する
 */
class ColorPattern {

    private var mColorTransientPattern: Int = 0
    var shiftSpeed: Int = 0

    private var ca: Int = 0
    private var cr: Int = 0
    private var cg: Int = 0
    private var cb: Int = 0
    private var trans: Int = 0

    var color: Int
        get() = Color.argb(ca, cr, cg, cb)
        set(col) {
            alpha = Color.alpha(col)
            red = Color.red(col)
            green = Color.green(col)
            blue = Color.blue(col)
        }

    var red: Int
        get() = cr
        private set(r) {
            if (r >= COLOR_MIN && r <= COLOR_MAX) {
                cr = r
            }
        }
    var green: Int
        get() = cg
        private set(g) {
            if (g >= COLOR_MIN && g <= COLOR_MAX) {
                cg = g
            }
        }
    var blue: Int
        get() = cb
        private set(b) {
            if (b >= COLOR_MIN && b <= COLOR_MAX) {
                cb = b
            }
        }
    var alpha: Int
        get() = ca
        set(a) {
            if (a >= COLOR_MIN && a <= COLOR_MAX) {
                ca = a
            }
        }

    var colMode: Int
        get() = mColorTransientPattern
        private set(cmode) {
            if (cmode >= COLOR_PATTERN_IDX_MIN && cmode <= COLOR_PATTERN_IDX_MAX) {
                mColorTransientPattern = cmode
            }

            setPattern()
        }

    val colModeInString: String
        get() {
            when (mColorTransientPattern) {
                SINGLE -> return STR_SINGLE
                RAINBOW -> return STR_RAINBOW
                FIRE -> return STR_FIRE
                FOREST -> return STR_FOREST
                COOL -> return STR_COOL
                DAWN -> return STR_DAWN
                DEEPSEA -> return STR_DEEPSEA
                HEAT -> return STR_HEAT
                BW -> return STR_BW
                else -> return STR_RAINBOW
            }
        }

    init {
        mColorTransientPattern = RAINBOW
        ca = COLOR_MAX
        cr = COLOR_MAX
        cg = COLOR_MIN
        cb = COLOR_MIN
        trans = TRANS_INIT
        shiftSpeed = COLOR_CHANGE_INIT
    }

    fun init(cp: ColorPattern, isCopy: Boolean) {
        mColorTransientPattern = cp.mColorTransientPattern
        shiftSpeed = cp.shiftSpeed
        val new_color = cp.color
        val new_trans = cp.trans
        this.setPattern()

        if (isCopy) {
            this.color = new_color
            trans = new_trans
        }

    }

    fun getTrans(): Int {
        return trans
    }

    fun setTrans(trans0: Int) {
        if (trans0 >= COLOR_TRANS_IDX_MIN && trans0 <= COLOR_TRANS_IDX_MAX) {
            trans = trans0
        }
    }

    fun setColMode(str_cmode: String) {
        if (str_cmode.matches(STR_NULL.toRegex()))
            return
        val c_str = str_cmode.toUpperCase(Locale.ENGLISH)

        if (c_str.matches(STR_SINGLE.toRegex()))
            mColorTransientPattern = SINGLE
        else if (c_str.matches(STR_RAINBOW.toRegex()))
            mColorTransientPattern = RAINBOW
        else if (c_str.matches(STR_FIRE.toRegex()))
            mColorTransientPattern = FIRE
        else if (c_str.matches(STR_FOREST.toRegex()))
            mColorTransientPattern = FOREST
        else if (c_str.matches(STR_COOL.toRegex()))
            mColorTransientPattern = COOL
        else if (c_str.matches(STR_DAWN.toRegex()))
            mColorTransientPattern = DAWN
        else if (c_str.matches(STR_DEEPSEA.toRegex()))
            mColorTransientPattern = DEEPSEA
        else if (c_str.matches(STR_HEAT.toRegex()))
            mColorTransientPattern = HEAT
        else if (c_str.matches(STR_BW.toRegex()))
            mColorTransientPattern = BW
        else
            return

        setPattern()
    }

    private fun setPattern() {
        when (mColorTransientPattern) {
            SINGLE -> trans = VALUE_NULL
            RAINBOW, FIRE -> {
                trans = COLOR_TRANS_IDX_RED_TO_YELLOW
                cr = COLOR_MAX
                cg = COLOR_MIN
                cb = COLOR_MIN
            }
            FOREST -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_PURPLE
                cr = COLOR_MIN
                cg = COLOR_MAX
                cb = COLOR_MIN
            }
            COOL -> {
                trans = COLOR_TRANS_IDX_RED_TO_YELLOW
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            DAWN -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_RED
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            DEEPSEA -> {
                trans = COLOR_TRANS_IDX_BLUE_TO_GREEN
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            HEAT -> {
                trans = COLOR_TRANS_IDX_YELLOW_TO_RED
                cr = COLOR_MAX
                cg = COLOR_MIN
                cb = COLOR_MAX
            }
            BW -> {
                trans = COLOR_TRANS_IDX_WHITE
                cr = COLOR_MIN
                cg = COLOR_MIN
                cb = COLOR_MIN
            }
            else -> {
                Log.e(TAG, "That color pattern is invalid : at ColorPattern")
                colMode = 0
            }
        }
    }

    fun doPattern(): Int {
        when (mColorTransientPattern) {
            SINGLE -> {
            }
            RAINBOW -> if (this.transPattern(trans)) {
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
            FIRE, FOREST, COOL -> if (this.transPattern(trans)) {
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
            DAWN -> if (this.transPattern(trans)) {
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
            DEEPSEA -> if (this.transPattern(trans)) {
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
            HEAT -> if (this.transPattern(trans)) {
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
            BW -> if (this.transPattern(trans)) {
                trans = if (trans == COLOR_TRANS_IDX_WHITE) COLOR_TRANS_IDX_BLACK else COLOR_TRANS_IDX_WHITE
            }
            else -> {
                Log.e(TAG, "Invalid color pattern!! Switch to single.")
                this.colMode = SINGLE
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
            else -> return false
        }
        return false
    }

    companion object {
        val SINGLE = -1
        val RAINBOW = 0
        val FIRE = 1
        val FOREST = 2
        val COOL = 3
        val DAWN = 4
        val DEEPSEA = 5
        val HEAT = 6
        val BW = 7

        private val STR_SINGLE = "SINGLE"
        private val STR_RAINBOW = "RAINBOW"
        private val STR_FIRE = "FIRE"
        private val STR_FOREST = "FOREST"
        private val STR_COOL = "COOL"
        private val STR_DAWN = "DAWN"
        private val STR_DEEPSEA = "DEEPSEA"
        private val STR_HEAT = "HEAT"
        private val STR_BW = "BW"

        private val STR_NULL = ""

        val COLOR_MIN = 0
        val COLOR_MAX = 255

        private val COLOR_PATTERN_IDX_MIN = -1
        private val COLOR_PATTERN_IDX_MAX = 5
        private val COLOR_TRANS_IDX_MIN = 0
        private val COLOR_TRANS_IDX_MAX = 17

        private val COLOR_TRANS_IDX_RED_TO_YELLOW = 0
        private val COLOR_TRANS_IDX_YELLOW_TO_GREEN = 1
        private val COLOR_TRANS_IDX_GREEN_TO_CYAN = 2
        private val COLOR_TRANS_IDX_CYAN_TO_BLUE = 3
        private val COLOR_TRANS_IDX_BLUE_TO_PURPLE = 4
        private val COLOR_TRANS_IDX_PURPLE_TO_RED = 5
        private val COLOR_TRANS_IDX_WHITE = 6
        private val COLOR_TRANS_IDX_BLACK = 7
        private val COLOR_TRANS_IDX_BLUE_TO_RED = 8
        private val COLOR_TRANS_IDX_RED_TO_BLUE = 9
        private val COLOR_TRANS_IDX_BLUE_TO_GREEN = 10
        private val COLOR_TRANS_IDX_GREEN_TO_BLUE = 11
        private val COLOR_TRANS_IDX_YELLOW_TO_RED = 12
        private val COLOR_TRANS_IDX_RED_TO_BLUE_H = 13
        private val COLOR_TRANS_IDX_BLUE_TO_BLACK = 14
        private val COLOR_TRANS_IDX_BLACK_TO_BLUE = 15
        private val COLOR_TRANS_IDX_BLUE_TO_RED_H = 16
        private val COLOR_TRANS_IDX_RED_TO_YELLOW_H = 17

        private val COLOR_PATTERN_RGB_TRANS = 3

        private val TRANS_INIT = 0

        private val COLOR_CHANGE_INIT = 1

        private val VALUE_NULL = -1

        private val TAG = ColorPattern::class.java.name
    }
}
