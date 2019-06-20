package unicot.app.fractalvisualizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.seekbar_with_label.view.*
import unicot.app.fractalvisualizer.R
import java.lang.IllegalArgumentException
import kotlin.math.absoluteValue

class SeekBarWithLabel(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {
    var listener: ((Float) -> Unit)? = null
    private var valMin: Float
    private var valMax: Float
    private var valStep: Float
    private var valDefault: Float
    private var max: Int = 0
    private var isInt: Boolean = false

    private val progressToValue: Float
        get() = valMin + (valMax - valMin)*sbwl_sb.progress/max


    fun setValue(value: Float){
        sbwl_sb.progress = getProgress(value)
    }

    fun getProgress(value: Float): Int{
        return ((value - valMin + valStep/2)/(valMax - valMin)*max).toInt()
    }

    private fun renewRange(){
        max = ((valMax - valMin)/valStep).toInt()
        sbwl_sb.max = max
    }

    init{
        val customParams = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarWithLabel,
                0, 0)

        val titleStr: String
        val unitStr: String
        try {
            valMin = customParams.getFloat(R.styleable.SeekBarWithLabel_val_min, 0.0f)
            valMax = customParams.getFloat(R.styleable.SeekBarWithLabel_val_max, 100.0f)
            valDefault = customParams.getFloat(R.styleable.SeekBarWithLabel_val_default, 50.0f)
            valStep = customParams.getFloat(R.styleable.SeekBarWithLabel_val_step, 1.0f)

            titleStr = customParams.getString(R.styleable.SeekBarWithLabel_title) ?: ""
            unitStr = customParams.getString(R.styleable.SeekBarWithLabel_units) ?: ""
            isInt = customParams.getBoolean(R.styleable.SeekBarWithLabel_is_int, false)
        } finally {
            customParams.recycle()
        }
        if(valMin > valMax || (valMin - valMax).absoluteValue < valStep){
            throw IllegalArgumentException()
        }
        if(valDefault < valMin || valDefault > valMax){
            throw IllegalArgumentException()
        }

        val view = View.inflate(context, R.layout.seekbar_with_label, this)
        renewRange()
        sbwl_tv_title.text = titleStr
        if(unitStr.isNotEmpty())
            sbwl_tv_unit.text = unitStr
        else
            sbwl_tv_unit.visibility = View.INVISIBLE

        view.sbwl_tv_title.setOnClickListener {
            view.sbwl_sb.progress = getProgress(valDefault)
            listener?.invoke(valDefault)
        }
        view.sbwl_sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                listener?.invoke(progressToValue)
                val valueStr = if(isInt) progressToValue.toInt().toString() else progressToValue.toString()
                view.sbwl_tv_number.text = valueStr
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val valueStr = if(isInt) progressToValue.toInt().toString() else progressToValue.toString()
                view.sbwl_tv_number.text = valueStr
            }
        })
        sbwl_sb.progress = getProgress(valDefault)
    }
}