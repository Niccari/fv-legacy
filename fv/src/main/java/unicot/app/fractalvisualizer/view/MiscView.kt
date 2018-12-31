package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
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
        val framerate0 = DGCore.systemData.framerate
        seekBar_fps.setProgress(framerate0 - 1)
        seekBar_fps.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(mMiscFPSSeekBar: SeekBar) {}
            override fun onProgressChanged(mMiscFPSSeekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                textView_fps.setText((progress + 1).toString())
            }

            override fun onStopTrackingTouch(sb: SeekBar) {
                textView_fps.setText((sb.progress + 1).toString())

                DGCore.systemData.framerate = (sb.progress + 1)
                listener.invoke("fps")
            }
        })
        textView_fps.setText(framerate0.toString())

        val bgcolor0 = DGCore.systemData.viewAlpha and 0xFF
        textView_bgcolor_value.setText(bgcolor0.toString())
        seekBar_bgcolor.setProgress(bgcolor0)
        seekBar_bgcolor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(mMiscFPSSeekBar: SeekBar) {}
            override fun onProgressChanged(mMiscFPSSeekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                textView_bgcolor_value.setText((progress and 0xFF).toString())
                DGCore.systemData.viewAlpha = (progress and 0xFF)
                listener.invoke("bgcolor")
            }

            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        val pov_frame0 = DGCore.systemData.povFrame and 0xFF
        textView_povframe_value.setText(pov_frame0.toString())
        seekBar_povframe.setProgress(pov_frame0)
        seekBar_povframe.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(mMiscFPSSeekBar: SeekBar) {}
            override fun onProgressChanged(mMiscFPSSeekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                textView_povframe_value.setText(progress.toString())
                DGCore.systemData.povFrame = (progress and 0xFF)
                listener.invoke("pov_frame")
            }

            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        imageButton_graphLoad.setOnClickListener{
            listener.invoke("load_graph")
        }

        imageButton_graphSave.setOnClickListener{
            listener.invoke("save_graph")
        }

        imageButton_camera.setOnClickListener{
            listener.invoke("capture")
        }
    }

    fun sync(){
        val fps = DGCore.systemData.framerate
        seekBar_fps.setProgress(fps-1)
        textView_fps.setText(fps.toString())

        val bgcolor = DGCore.systemData.viewAlpha
        seekBar_bgcolor.setProgress(bgcolor)
        textView_bgcolor_value.setText(bgcolor.toString())

        val pov_frame = DGCore.systemData.povFrame
        seekBar_povframe.setProgress(pov_frame)
        textView_povframe_value.setText(pov_frame.toString())
    }
}
