package unicot.app.fractalvisualizer.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import unicot.app.fractalvisualizer.R

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
}