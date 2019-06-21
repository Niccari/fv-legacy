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
    private var valMin: Float = 0.0f
    private var valMax: Float = 100.0f
    private var valStep: Float = 1.0f
    private var valDefault: Float = 50.0f
    private var isInt: Boolean
    private var isSeekInvoke: Boolean
    private var isValueVisible: Boolean

    val value: Float
        get() = valMin + (valMax - valMin)*sbwl_sb.progress/sbwl_sb.max


    fun setValue(value: Float){
        sbwl_sb.progress = getProgress(value)
    }

    private fun getProgress(value: Float): Int{
        return ((value - valMin + valStep/2)/(valMax - valMin)*sbwl_sb.max).toInt()
    }

    fun renewRange(valMin0: Float? = null, valMax0: Float? = null,
                   valStep0: Float? = null, valDefault0: Float? = null){
        val listener = this.listener
        this.listener = null
        valMin0?.let{ valMin = it }
        valMax0?.let{ valMax = it }
        valStep0?.let{ valStep = it }
        valDefault0?.let{ valDefault = it }

        if(valMin > valMax || (valMin - valMax).absoluteValue < valStep){
            throw IllegalArgumentException()
        }
        if(valDefault < valMin || valDefault > valMax){
            throw IllegalArgumentException()
        }
        sbwl_sb.max = ((valMax - valMin)/valStep).toInt()
        this.listener = listener
    }

    init{
        val customParams = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarWithLabel,
                0, 0)

        val valMin0: Float
        val valMax0: Float
        val valStep0: Float
        val valDefault0: Float
        val titleStr: String
        val unitStr: String
        try {
            valMin0 = customParams.getFloat(R.styleable.SeekBarWithLabel_val_min, 0.0f)
            valMax0 = customParams.getFloat(R.styleable.SeekBarWithLabel_val_max, 100.0f)
            valDefault0 = customParams.getFloat(R.styleable.SeekBarWithLabel_val_default, 50.0f)
            valStep0 = customParams.getFloat(R.styleable.SeekBarWithLabel_val_step, 1.0f)

            titleStr = customParams.getString(R.styleable.SeekBarWithLabel_title) ?: ""
            unitStr = customParams.getString(R.styleable.SeekBarWithLabel_units) ?: ""
            isInt = customParams.getBoolean(R.styleable.SeekBarWithLabel_is_int, false)
            isSeekInvoke = customParams.getBoolean(R.styleable.SeekBarWithLabel_is_seek_invoke, false)
            isValueVisible = customParams.getBoolean(R.styleable.SeekBarWithLabel_is_value_visible, false)
        } finally {
            customParams.recycle()
        }

        val view = View.inflate(context, R.layout.seekbar_with_label, this)
        renewRange(valMin0, valMax0, valStep0, valDefault0)
        if(titleStr.isNotEmpty()) {
            sbwl_tv_title.text = titleStr
            view.sbwl_tv_title.setOnClickListener {
                view.sbwl_sb.progress = getProgress(valDefault)
                listener?.invoke(valDefault)
            }
        }else {
            sbwl_tv_title.visibility = View.GONE
        }
        if(unitStr.isNotEmpty()) {
            sbwl_tv_unit.text = unitStr
        }else {
            sbwl_tv_unit.visibility = View.GONE
        }
        if(!isValueVisible)
            sbwl_tv_number.visibility = View.GONE

        view.sbwl_sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                listener?.invoke(value)
                val valueStr = if(isInt) value.toInt().toString() else String.format("%.2f", value)
                view.sbwl_tv_number.text = valueStr
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(isSeekInvoke)
                    listener?.invoke(value)
                val valueStr = if(isInt) value.toInt().toString() else String.format("%.2f", value)
                view.sbwl_tv_number.text = valueStr
            }
        })
        sbwl_sb.progress = getProgress(valDefault)
        view.sbwl_tv_number.text = if(isInt) value.toInt().toString() else String.format("%.2f", value)
    }
}