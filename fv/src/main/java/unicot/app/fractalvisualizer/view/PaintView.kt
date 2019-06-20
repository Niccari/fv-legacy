package unicot.app.fractalvisualizer.view

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.gui_paint.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.graph.ColorPattern
import unicot.app.fractalvisualizer.graph.Graph

/**
 * グラフ描画設定
 */
class PaintView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
    private val STR_SINGLE_COLOR = "single"

    private var mIBCurrentColor: ImageButton? = null
    private var mIBCurrentBrush: ImageButton? = null

    private lateinit var dgc: DGCore

    init{
        View.inflate(context, R.layout.gui_paint, this)
    }

    fun setDGCore(dgc: DGCore){
        this.dgc = dgc
        setEvent()
    }

    fun refresh(){
        val g_selected = DGCore.selectedGraph[0]
        val gi = g_selected.info

        checkBox_gsp_draw_each.isChecked = gi.draw_kind == Graph.DRAW_IN_ORDER

        if (checkBox_gsp_draw_each.isChecked) {
            gui_paint_sb_draw_each_length.visibility = View.VISIBLE
        } else {
            gui_paint_sb_draw_each_length.visibility = View.INVISIBLE
        }
        checkBox_gsp_color_each.setChecked(gi.mIsColorEach)

        gui_paint_sb_thickness.setValue(gi.mLineThickness)
        gui_paint_sb_draw_color_shift.setValue(gi.cp.shiftSpeed.toFloat())

        gui_paint_sb_draw_color_alpha.setValue(gi.cp.alpha.toFloat())
        if (gi.cp.colMode == ColorPattern.SINGLE) {
            val cr = gi.cp.red
            val cg = gi.cp.green
            val cb = gi.cp.blue
            gui_paint_colors_sb_red.setProgress(cr)
            gui_paint_colors_sb_green.setProgress(cg)
            gui_paint_colors_sb_blue.setProgress(cb)
        }

        mIBCurrentColor?.setImageResource(R.drawable.color_focus_item)
        // TODO: 本来case値はColorPatternからとってくるべき
        when (gi.cp.colMode) {
            -1 -> mIBCurrentColor = imageButton_gsp_single_icon
            0 -> mIBCurrentColor = imageButton_gsp_rainbow_icon
            1 -> mIBCurrentColor = imageButton_gsp_fire_icon
            2 -> mIBCurrentColor = imageButton_gsp_green_icon
            3 -> mIBCurrentColor = imageButton_gsp_cool_icon
            4 -> mIBCurrentColor = imageButton_gsp_dawn_icon
            5 -> mIBCurrentColor = imageButton_gsp_sea_icon
            6 -> mIBCurrentColor = imageButton_gsp_heat_icon
            7 -> mIBCurrentColor = imageButton_gsp_monochro_icon    //ColorPattern.Companion.getBW():
            8 -> mIBCurrentColor = imageButton_gsp_pastel_icon
        }

        if (gi.cp.colMode == ColorPattern.SINGLE) {
            gui_paint_colors_ll_root.visibility = View.VISIBLE
            gui_paint_sb_draw_color_shift.visibility = View.GONE
        } else {
            gui_paint_colors_ll_root.visibility = View.GONE
            gui_paint_sb_draw_color_shift.visibility = View.VISIBLE
        }
        mIBCurrentColor?.setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentBrush?.setImageResource(R.drawable.color_focus_item)
        when (gi.mBrushType) {
            Graph.BRUSHTYPE_LINE -> mIBCurrentBrush = imageButton_gsp_brush_line
            Graph.BRUSHTYPE_TRIANGLE -> mIBCurrentBrush = imageButton_gsp_brush_triangle
            Graph.BRUSHTYPE_CRESCENT -> mIBCurrentBrush = imageButton_gsp_brush_crescent
            Graph.BRUSHTYPE_TWIN_CIRCLE -> mIBCurrentBrush = imageButton_gsp_brush_twin_circle
        }
        mIBCurrentBrush?.setImageResource(R.drawable.color_activated_focus_item)
    }

    private fun setEvent(){
        // 以下、描画設定関連
        checkBox_gsp_draw_each.setOnCheckedChangeListener {
            box, checked ->
            if (checked) {
                dgc.operate(DGCore.OP_DRAW_EACH, 1)
                gui_paint_sb_draw_each_length.visibility = View.VISIBLE
            } else {
                dgc.operate(DGCore.OP_DRAW_EACH, 0)
                gui_paint_sb_draw_each_length.visibility = View.INVISIBLE
            }
        }

        checkBox_gsp_color_each.setOnCheckedChangeListener {
            box, checked ->
            dgc.operate(DGCore.OP_COLOREACH, checked)
        }

        gui_paint_sb_thickness.listener = {
            dgc.operate(DGCore.OP_THICKNESS, it.toInt())
        }
        gui_paint_sb_draw_each_length.listener = {
            dgc.operate(DGCore.OP_DRAW_EACH_PERCENT, it)
        }
        gui_paint_sb_draw_color_shift.listener = {
            dgc.operate(DGCore.OP_COLOR_SHIFT, it.toInt())

        }

        val colorChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) {}

            override fun onProgressChanged(sb: SeekBar, progress: Int, fromTouch: Boolean) {
                changeSingleColorButtonColor()
            }

            override fun onStopTrackingTouch(sb: SeekBar) {}
        }

        gui_paint_sb_draw_color_alpha.listener = {
            if (dgc.isGraphSelected)
                dgc.operate(DGCore.OP_SET_ALPHA, it.toInt())
        }

        gui_paint_colors_sb_red.setOnSeekBarChangeListener(colorChangeListener)
        gui_paint_colors_sb_green.setOnSeekBarChangeListener(colorChangeListener)
        gui_paint_colors_sb_blue.setOnSeekBarChangeListener(colorChangeListener)
    }

    fun onColorPatternClicked(v: View){
        if (v.tag.toString().matches(STR_SINGLE_COLOR.toRegex())) {
            gui_paint_sb_draw_color_shift.visibility = View.GONE
            gui_paint_colors_ll_root.visibility = View.VISIBLE
        }else {
            gui_paint_sb_draw_color_shift.visibility = View.VISIBLE
            gui_paint_colors_ll_root.visibility = View.GONE
        }

        mIBCurrentColor?.setImageResource(R.drawable.color_focus_item)
        (v as ImageButton).setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentColor = v

        dgc.operate(DGCore.OP_COLORPATTERN, mIBCurrentColor?.tag.toString())
    }

    fun onBrushButtonClicked(v: View){
        mIBCurrentBrush?.setImageResource(R.drawable.color_focus_item)
        (v as ImageButton).setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentBrush = v as ImageButton

        dgc.operate(DGCore.OP_BRUSHTYPE, mIBCurrentBrush?.tag.toString())
    }

    private fun changeSingleColorButtonColor() {
        if (!dgc.isGraphSelected)
            return  // グラフ選択してなければ非実行

        val alpha = gui_paint_sb_draw_color_alpha.value.toInt()
        val red = 0xFF and gui_paint_colors_sb_red.getProgress()
        val green = 0xFF and gui_paint_colors_sb_green.getProgress()
        val blue = 0xFF and gui_paint_colors_sb_blue.getProgress()

        val mSingleColor = alpha shl 24 or (red shl 16) or (green shl 8) or blue
        imageButton_gsp_single_icon.getBackground().setColorFilter(mSingleColor, PorterDuff.Mode.SRC_IN)
        dgc.operate(DGCore.OP_SET_COLOR, mSingleColor)
    }
}
