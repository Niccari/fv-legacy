package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.gui_add.view.*
import unicot.app.fractalvisualizer.R

/**
 * グラフ追加
 */
class GraphAddView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
    init{
        View.inflate(context, R.layout.gui_add, this)
    }

    fun setEvent(listener: OnTouchListener){
        val viewNStar = imageButton_graphIcon_nStar
        val viewStarmine = imageButton_graphIcon_starmine
        val viewNPoint = imageButton_graphIcon_nPoint
        val viewRandomStatic = imageButton_graphIcon_random1
        val viewRandomDynamic = imageButton_graphIcon_random2

        val viewDragonCurve = imageButton_graphIcon_dragonCurve
        val viewDragonCurveRev = imageButton_graphIcon_dragonCurve_rev
        val viewDragonCCurve = imageButton_graphIcon_cCurve
        val viewKoch = imageButton_graphIcon_koch
        val viewKochOuter = imageButton_graphIcon_kochOuter

        val viewKochInter = imageButton_graphIcon_kochInner
        val viewRose = imageButton_graphIcon_rose
        val viewSGasket = imageButton_graphIcon_sierpenskiGasket
        val viewTree = imageButton_graphIcon_tree
        val viewCarpet = imageButton_graphIcon_sierpenskiCarpet

        val viewBTree = imageButton_graphIcon_binaryTree
        val viewHilbert = imageButton_graphIcon_hilbert
        val viewTrifoldCis = imageButton_graphIcon_trifold_cis
        val viewTrifoldTrans = imageButton_graphIcon_trifold_trans

        // 以下、各グラフボタンにタップイベントを登録
        viewNStar.setOnTouchListener(listener)
        viewStarmine.setOnTouchListener(listener)
        viewNPoint.setOnTouchListener(listener)
        viewRandomStatic.setOnTouchListener(listener)
        viewRandomDynamic.setOnTouchListener(listener)

        viewDragonCurve.setOnTouchListener(listener)
        viewDragonCurveRev.setOnTouchListener(listener)
        viewDragonCCurve.setOnTouchListener(listener)
        viewKoch.setOnTouchListener(listener)
        viewKochOuter.setOnTouchListener(listener)

        viewKochInter.setOnTouchListener(listener)
        viewRose.setOnTouchListener(listener)
        viewSGasket.setOnTouchListener(listener)
        viewTree.setOnTouchListener(listener)
        viewCarpet.setOnTouchListener(listener)

        viewBTree.setOnTouchListener(listener)
        viewHilbert.setOnTouchListener(listener)
        viewTrifoldCis.setOnTouchListener(listener)
        viewTrifoldTrans.setOnTouchListener(listener)
    }
}