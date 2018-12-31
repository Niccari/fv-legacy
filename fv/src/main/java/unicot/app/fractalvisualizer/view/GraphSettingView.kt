package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.gui_shape.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.graph.GraphInfo
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
import unicot.app.fractalvisualizer.struct.GraphDisplacement

/**
 * グラフ変形
 */
class GraphSettingView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
    private lateinit var dgc: DGCore

    private var complexity_min = 0

    val listener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(sb: SeekBar) {}

        override fun onProgressChanged(sb: SeekBar, progress: Int, fromTouch: Boolean) {
            val max = sb.max.toFloat()
            val v: Float

            when (sb.id) {
                R.id.seekBar_gss_complexity_setting -> {
                    dgc.operate(DGCore.OP_COMPLEXITY, progress + complexity_min)
                }
                R.id.seekBar_gss_rotate_setting -> {
                    v = progress / max * (GraphInfo.GRAPH_ROTATE_MAX - GraphInfo.GRAPH_ROTATE_MIN) + GraphInfo.GRAPH_ROTATE_MIN
                    dgc.operate(DGCore.OP_GRAPH_ROTATE, v)
                }
                R.id.seekBar_gss_mutation_size -> {
                    v = progress / max * (GraphDisplacement.SIZE_MAX - GraphDisplacement.SIZE_MIN) + GraphDisplacement.SIZE_MIN
                    dgc.operate(DGCore.OP_MUTATION_SIZE, v) // -1.1 to 1.1 with
                }
                R.id.seekBar_gss_mutation_angle -> {
                    v = progress / max * (GraphDisplacement.ANGLE_MAX - GraphDisplacement.ANGLE_MIN) + GraphDisplacement.ANGLE_MIN
                    dgc.operate(DGCore.OP_MUTATION_ANGLE, v) // -2.0 to 2.0
                }
                R.id.seekBar_gss_randomizer_size -> {
                    v = progress / max * (GraphDisplacement.SIZE_MAX - GraphDisplacement.SIZE_MIN) + GraphDisplacement.SIZE_MIN
                    dgc.operate(DGCore.OP_RANDOMIZER_SIZE, v) // -1.1 to 1.1
                }
                R.id.seekBar_gss_randomizer_angle -> {
                    v = progress / max * (GraphDisplacement.ANGLE_MAX - GraphDisplacement.ANGLE_MIN) + GraphDisplacement.ANGLE_MIN
                    dgc.operate(DGCore.OP_RANDOMIZER_ANGLE, v) // -2.0 to 2.0
                }
                R.id.seekBar_gss_leaf_branch -> {
                    v = progress.toFloat() + 1
                    dgc.operate(DGCore.OP_LEAF_BRANCH, v.toInt()) // 1 - max(5)
                }

                R.id.seekBar_gss_sgasket_skew -> {
                    v = progress / max * (SGasket.SKEW_MAX - SGasket.SKEW_MIN) + SGasket.SKEW_MIN
                    dgc.operate(DGCore.OP_SGASKET_SKEW, v)
                }
            }
        }

        override fun onStopTrackingTouch(sb: SeekBar) {}
    }

    init{
        View.inflate(context, R.layout.gui_shape, this)
    }

    fun setDGCore(dgc: DGCore){
        this.dgc = dgc
        setEvent()
    }

    fun refresh(){
        val g_selected = DGCore.selectedGraph[0]
        val gi = g_selected.info
        complexity_min = g_selected.complexityMin

        seekBar_gss_complexity_setting.setOnSeekBarChangeListener(null)
        seekBar_gss_complexity_setting.max = g_selected.complexityMax - complexity_min
        seekBar_gss_complexity_setting.progress = gi.complexity - complexity_min
        seekBar_gss_complexity_setting.setOnSeekBarChangeListener(listener)
        seekBar_gss_rotate_setting.progress = (seekBar_gss_rotate_setting.getMax() * (gi.rot_speed - GraphInfo.GRAPH_ROTATE_MIN) / (GraphInfo.GRAPH_ROTATE_MAX - GraphInfo.GRAPH_ROTATE_MIN)).toInt()

        if (gi.is_recursive) {
            gui_shape_recursive_ll_root.visibility = View.VISIBLE

            seekBar_gss_mutation_size.progress = ((gi.mutation.size - GraphDisplacement.SIZE_MIN) / (GraphDisplacement.SIZE_MAX - GraphDisplacement.SIZE_MIN) * seekBar_gss_mutation_size.getMax()).toInt()
            seekBar_gss_mutation_angle.progress = ((gi.mutation.angle - GraphDisplacement.ANGLE_MIN) / (GraphDisplacement.ANGLE_MAX - GraphDisplacement.ANGLE_MIN) * seekBar_gss_mutation_angle.getMax()).toInt()
            seekBar_gss_randomizer_size.progress = ((gi.randomize.size - GraphDisplacement.SIZE_MIN) / (GraphDisplacement.SIZE_MAX - GraphDisplacement.SIZE_MIN) * seekBar_gss_randomizer_size.getMax()).toInt()
            seekBar_gss_randomizer_angle.progress = ((gi.randomize.angle - GraphDisplacement.ANGLE_MIN) / (GraphDisplacement.ANGLE_MAX - GraphDisplacement.ANGLE_MIN) * seekBar_gss_randomizer_angle.getMax()).toInt()
        }else{
            gui_shape_recursive_ll_root.visibility = GONE
        }
        if (gi.graph_kind == DGCommon.LEAF) {
            gui_shape_leaf_ll_root.visibility = View.VISIBLE
            seekBar_gss_leaf_branch.progress = (g_selected as Leaf).getBranch() - 1
        }else{
            gui_shape_leaf_ll_root.visibility = GONE
        }

        if (gi.graph_kind == DGCommon.SIERPINSKI_GASKET) {
            gui_shape_sgasket_ll_root.visibility = View.VISIBLE
            seekBar_gss_sgasket_skew.progress = (((g_selected as SGasket).skewAngle - SGasket.SKEW_MIN) / (SGasket.SKEW_MAX - SGasket.SKEW_MIN) * seekBar_gss_sgasket_skew.getMax()).toInt()
        }else{
            gui_shape_sgasket_ll_root.visibility = View.GONE
        }

        imageView_gss_now_graph_icon.setImageResource(DGCommon.getGraphIcon(gi.graph_kind))
    }

    private fun setEvent(){
        seekBar_gss_complexity_setting.setOnSeekBarChangeListener(listener)
        seekBar_gss_rotate_setting.setOnSeekBarChangeListener(listener)

        seekBar_gss_mutation_size.setOnSeekBarChangeListener(listener)
        seekBar_gss_mutation_angle.setOnSeekBarChangeListener(listener)
        seekBar_gss_randomizer_size.setOnSeekBarChangeListener(listener)
        seekBar_gss_randomizer_angle.setOnSeekBarChangeListener(listener)

        seekBar_gss_leaf_branch.setOnSeekBarChangeListener(listener)
        seekBar_gss_sgasket_skew.setOnSeekBarChangeListener(listener)
    }
}
