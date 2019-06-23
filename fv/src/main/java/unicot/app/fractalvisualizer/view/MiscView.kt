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
    interface OnEventListener{
        fun invoke(key: String)
    }

    init{
        View.inflate(context, R.layout.gui_misc, this)
    }

    fun setEvent(listener: OnEventListener){
        gui_misc_sb_fps.listener = {
            if(it.toInt() < DGCore.systemData.framerateList.size) {
                DGCore.systemData.framerate = DGCore.systemData.framerateList[it.toInt()]
                listener.invoke("fps")
            }
        }
        gui_misc_sb_pov.listener = {
            DGCore.systemData.povFrame = it.toInt()
            listener.invoke("pov_frame")
        }
        imageButton_graphLoad.setOnClickListener{
            listener.invoke("load_graph")
        }
        imageButton_graphSave.setOnClickListener{
            listener.invoke("save_graph")
        }

        imageButton_camera.setOnClickListener{
            listener.invoke("capture")
        }
        imageButton_preference.setOnClickListener{
            listener.invoke("preference")
        }
    }

    fun sync(){
        gui_misc_sb_fps.setValue(DGCore.systemData.framerate.toFloat())
        gui_misc_sb_pov.setValue(DGCore.systemData.povFrame.toFloat())
    }
}
