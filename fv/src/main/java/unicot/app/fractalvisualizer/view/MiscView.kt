package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.gui_misc.view.*
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCore

/**
 * アプリ設定
 */
class MiscView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs){
    init{
        View.inflate(context, R.layout.gui_misc, this)
    }

    fun setEvent(listener: (Action) -> Unit){
        gui_misc_sb_fps.listener = {
            if(it.toInt() < DGCore.systemData.framerateList.size) {
                DGCore.systemData.framerate = DGCore.systemData.framerateList[it.toInt()]
                listener.invoke(Action.FPS)
            }
        }
        gui_misc_sb_pov.listener = {
            DGCore.systemData.povFrame = it.toInt()
            listener.invoke(Action.POV)
        }
        gui_misc_ib_load.setOnClickListener{
            listener.invoke(Action.LOAD)
        }
        gui_misc_ib_save.setOnClickListener{
            listener.invoke(Action.SAVE)
        }

        gui_misc_ib_capture.setOnClickListener{
            listener.invoke(Action.CAPTURE)
        }
        gui_misc_ib_prefecture.setOnClickListener{
            listener.invoke(Action.PREFERENCE)
        }
    }

    fun sync(){
        gui_misc_sb_fps.setValue(DGCore.systemData.framerate.toFloat())
        gui_misc_sb_pov.setValue(DGCore.systemData.povFrame.toFloat())
    }

    enum class Action{
        FPS, POV, LOAD, SAVE, CAPTURE, PREFERENCE
    }
}
