package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.gui_paint.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.core.DGCore.Companion.DrawSetting.*
import unicot.app.fractalvisualizer.graph.ColorPattern.ColorName
import unicot.app.fractalvisualizer.graph.Graph

/**
 * グラフ描画設定
 */
class PaintView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
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
        val gi = DGCore.selectedGraph[0].info

        checkBox_gsp_draw_each.isChecked = gi.drawKind == Graph.DRAW_IN_ORDER

        if (checkBox_gsp_draw_each.isChecked) {
            gui_paint_sb_draw_each_length.visibility = View.VISIBLE
        } else {
            gui_paint_sb_draw_each_length.visibility = View.INVISIBLE
        }
        checkBox_gsp_color_each.isChecked = gi.mIsColorEach

        gui_paint_sb_thickness.setValue(gi.mLineThickness)
        gui_paint_sb_draw_color_shift.setValue(gi.cp.shiftSpeed.toFloat())

        gui_paint_sb_draw_color_alpha.setValue(gi.cp.alpha.toFloat())
        if (gi.cp.colMode == ColorName.SINGLE) {
            gui_paint_colors_sb_red.progress   = gi.cp.red
            gui_paint_colors_sb_green.progress = gi.cp.green
            gui_paint_colors_sb_blue.progress  = gi.cp.blue
        }

        mIBCurrentColor?.setImageResource(R.drawable.color_focus_item)
        mIBCurrentColor = when (gi.cp.colMode) {
            ColorName.SINGLE  -> imageButton_gsp_single_icon
            ColorName.RAINBOW -> imageButton_gsp_rainbow_icon
            ColorName.FIRE    -> imageButton_gsp_fire_icon
            ColorName.FOREST  -> imageButton_gsp_green_icon
            ColorName.COOL    -> imageButton_gsp_cool_icon
            ColorName.DAWN    -> imageButton_gsp_dawn_icon
            ColorName.DEEPSEA -> imageButton_gsp_sea_icon
            ColorName.HEAT    -> imageButton_gsp_heat_icon
            ColorName.BW      -> imageButton_gsp_monochro_icon
            ColorName.PASTEL  -> imageButton_gsp_pastel_icon
        }

        if (gi.cp.colMode == ColorName.SINGLE) {
            gui_paint_colors_ll_root.visibility = View.VISIBLE
            gui_paint_sb_draw_color_shift.visibility = View.GONE
        } else {
            gui_paint_colors_ll_root.visibility = View.GONE
            gui_paint_sb_draw_color_shift.visibility = View.VISIBLE
        }
        mIBCurrentColor?.setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentBrush?.setImageResource(R.drawable.color_focus_item)
        mIBCurrentBrush = when (gi.mBrushType) {
            Graph.BRUSHTYPE_LINE        -> imageButton_gsp_brush_line
            Graph.BRUSHTYPE_TRIANGLE    -> imageButton_gsp_brush_triangle
            Graph.BRUSHTYPE_CRESCENT    -> imageButton_gsp_brush_crescent
            Graph.BRUSHTYPE_TWIN_CIRCLE -> imageButton_gsp_brush_twin_circle
            else                        -> imageButton_gsp_brush_line
        }
        mIBCurrentBrush?.setImageResource(R.drawable.color_activated_focus_item)
    }

    private fun setEvent(){
        // 以下、描画設定関連
        checkBox_gsp_draw_each.setOnCheckedChangeListener {
            _, checked ->
            if (checked) {
                dgc.changeDrawSetting(DRAW_EACH, 1)
                gui_paint_sb_draw_each_length.visibility = View.VISIBLE
            } else {
                dgc.changeDrawSetting(DRAW_EACH, 0)
                gui_paint_sb_draw_each_length.visibility = View.INVISIBLE
            }
        }

        checkBox_gsp_color_each.setOnCheckedChangeListener {
            _, checked ->
            dgc.changeDrawSetting(COLOR_EACH, checked)
        }

        gui_paint_sb_thickness.listener = {
            dgc.changeDrawSetting(THICKNESS, it.toInt())
        }
        gui_paint_sb_draw_each_length.listener = {
            dgc.changeDrawSetting(DRAW_EACH_PCT, it.toInt())
        }
        gui_paint_sb_draw_color_shift.listener = {
            dgc.changeDrawSetting(COLOR_SHIFT, it.toInt())

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
                dgc.changeDrawSetting(COLOR_ALPHA, it.toInt())
        }

        gui_paint_colors_sb_red.setOnSeekBarChangeListener(colorChangeListener)
        gui_paint_colors_sb_green.setOnSeekBarChangeListener(colorChangeListener)
        gui_paint_colors_sb_blue.setOnSeekBarChangeListener(colorChangeListener)
    }

    fun onColorPatternClicked(v: View){
        if (v.tag.toString() == "single") {
            gui_paint_sb_draw_color_shift.visibility = View.GONE
            gui_paint_colors_ll_root.visibility = View.VISIBLE
        }else {
            gui_paint_sb_draw_color_shift.visibility = View.VISIBLE
            gui_paint_colors_ll_root.visibility = View.GONE
        }

        mIBCurrentColor?.setImageResource(R.drawable.color_focus_item)
        (v as ImageButton).setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentColor = v

        dgc.changeDrawSetting(COLOR_PATTERN, mIBCurrentColor?.tag.toString())
    }

    fun onBrushButtonClicked(v: View){
        mIBCurrentBrush?.setImageResource(R.drawable.color_focus_item)
        (v as ImageButton).setImageResource(R.drawable.color_activated_focus_item)
        mIBCurrentBrush = v

        dgc.changeDrawSetting(BRUSH_TYPE, mIBCurrentBrush?.tag.toString())
    }

    private fun changeSingleColorButtonColor() {
        if (!dgc.isGraphSelected)
            return  // グラフ選択してなければ非実行

        val alpha = gui_paint_sb_draw_color_alpha.value.toInt()
        val red   = 0xFF and gui_paint_colors_sb_red.progress
        val green = 0xFF and gui_paint_colors_sb_green.progress
        val blue  = 0xFF and gui_paint_colors_sb_blue.progress

        val mSingleColor = alpha shl 24 or (red shl 16) or (green shl 8) or blue
        imageButton_gsp_single_icon.setBackgroundColor(mSingleColor)
        dgc.changeDrawSetting(COLOR_RGB, mSingleColor)
    }
}
