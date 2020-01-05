package unicot.app.fractalvisualizer.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import kotlinx.android.synthetic.main.gui_add.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCommon
import java.lang.IllegalArgumentException

/**
 * グラフ追加
 */
class GraphAddView(context: Context, attrs: AttributeSet? = null) : GridLayout(context, attrs){
    init{
        View.inflate(context, R.layout.gui_add, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setEvent(listener: OnTouchListener){
        columnCount = 5
        for( i in 0 until childCount){
            (getChildAt(i) as ImageButton).setOnTouchListener(listener)
        }
    }

    fun getGraphKindFromIcon(v: View?): DGCommon.GraphKind{
        return when(v){
            imageButton_graphIcon_nPoint -> DGCommon.GraphKind.NPOINT
            imageButton_graphIcon_starmine -> DGCommon.GraphKind.STARMINE
            imageButton_graphIcon_nStar -> DGCommon.GraphKind.NSTAR
            imageButton_graphIcon_random1 -> DGCommon.GraphKind.RANDOMSHAPE
            imageButton_graphIcon_random2 -> DGCommon.GraphKind.RANDOMSHAPE2

            imageButton_graphIcon_binaryTree -> DGCommon.GraphKind.BINARYTREE
            imageButton_graphIcon_dragonCurve -> DGCommon.GraphKind.DRAGONCURVE
            imageButton_graphIcon_dragonCurve_rev -> DGCommon.GraphKind.FOLDTRIANGLE
            imageButton_graphIcon_cCurve -> DGCommon.GraphKind.CCURVE

            imageButton_graphIcon_koch -> DGCommon.GraphKind.KOCHCURVE
            imageButton_graphIcon_kochInner -> DGCommon.GraphKind.KOCHTRIANGLE_INNER
            imageButton_graphIcon_kochOuter -> DGCommon.GraphKind.KOCHTRIANGLE_OUTER

            imageButton_graphIcon_rose -> DGCommon.GraphKind.ROSECURVE
            imageButton_graphIcon_hilbert -> DGCommon.GraphKind.HILBERT
            imageButton_graphIcon_tree -> DGCommon.GraphKind.LEAF
            imageButton_graphIcon_sierpenskiCarpet -> DGCommon.GraphKind.SIERPINSKI_CARPET
            imageButton_graphIcon_sierpenskiGasket -> DGCommon.GraphKind.SIERPINSKI_GASKET
            imageButton_graphIcon_trifold_cis -> DGCommon.GraphKind.TRIFOLD_CIS
            imageButton_graphIcon_trifold_trans -> DGCommon.GraphKind.TRIFOLD_TRANS
            else -> throw IllegalArgumentException("Invalid GUI Icon")
        }
    }
}