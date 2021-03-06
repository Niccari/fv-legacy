package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.gui_shape.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.core.DGCore.Companion.GraphSetting.*
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket

/**
 * グラフ変形
 */
class GraphSettingView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
    private lateinit var dgc: DGCore

    init{
        View.inflate(context, R.layout.gui_shape, this)
    }

    fun setDGCore(dgc: DGCore){
        this.dgc = dgc
        setEvent()
    }

    fun refresh(){
        val gSelected = DGCore.selectedGraph[0]
        val gi = gSelected.info

        gui_graph_sb_complexity.renewRange(gSelected.complexityMin.toFloat(),
                                           gSelected.complexityMax.toFloat())
        gui_graph_sb_complexity.setValue(gi.complexity.toFloat())
        gui_graph_sb_rorate.setValue(gi.rotSpeed)

        if (gi.isRecursive) {
            gui_shape_recursive_ll_root.visibility = View.VISIBLE

            gui_graph_sb_mut_size.setValue(gi.mutation.size)
            gui_graph_sb_mut_angle.setValue(gi.mutation.angle)
            gui_graph_sb_rand_size.setValue(gi.randomize.size)
            gui_graph_sb_rand_angle.setValue(gi.randomize.angle)
        }else{
            gui_shape_recursive_ll_root.visibility = GONE
        }
        if (gi.graphKind == DGCommon.GraphKind.LEAF) {
            gui_graph_sb_leaf_branch.visibility = View.VISIBLE
            gui_graph_sb_leaf_branch.setValue((gSelected as Leaf).getBranch().toFloat())
        }else{
            gui_graph_sb_leaf_branch.visibility = GONE
        }

        if (gi.graphKind == DGCommon.GraphKind.SIERPINSKI_GASKET) {
            gui_graph_sb_sgasket_skew.visibility = View.VISIBLE
            gui_graph_sb_sgasket_skew.setValue((gSelected as SGasket).skewAngle)
        }else{
            gui_graph_sb_sgasket_skew.visibility = View.GONE
        }

        imageView_gss_now_graph_icon.setImageResource(DGCommon.getGraphIcon(gi.graphKind))
    }

    private fun setEvent(){
        gui_graph_sb_complexity.listener = { dgc.transformGraph(COMPLEXITY, it) }
        gui_graph_sb_rorate.listener     = { dgc.transformGraph(ROT_SPEED, it) }
        gui_graph_sb_mut_size.listener   = { dgc.transformGraph(MUTATION_SIZE, it) }
        gui_graph_sb_mut_angle.listener  = { dgc.transformGraph(MUTATION_ANGLE, it) }
        gui_graph_sb_rand_size.listener  = { dgc.transformGraph(RANDOMIZER_SIZE, it) }
        gui_graph_sb_rand_angle.listener = { dgc.transformGraph(RANDOMIZER_ANGLE, it) }

        gui_graph_sb_rand_size.listener    = { dgc.transformGraph(RANDOMIZER_SIZE, it) }
        gui_graph_sb_rand_angle.listener   = { dgc.transformGraph(RANDOMIZER_ANGLE, it) }
        gui_graph_sb_leaf_branch.listener  = { dgc.transformGraph(LEAF_BRANCH, it) }
        gui_graph_sb_sgasket_skew.listener = { dgc.transformGraph(SGASKET_SKEW, it) }
    }
}
