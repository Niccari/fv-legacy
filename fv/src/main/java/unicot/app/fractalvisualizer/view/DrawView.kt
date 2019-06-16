package unicot.app.fractalvisualizer.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * 画面の描画を行う
 */
class DrawView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    interface DrawListener {
        fun onDraw(canvas: Canvas)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    init {
        holder.setFormat(PixelFormat.TRANSLUCENT)
        holder.addCallback(this)
    }

    fun draw(listener: DrawListener) {
        var c: Canvas? = null
        try {
            c = holder.lockCanvas()
            if (c != null) {
                listener.onDraw(c)
            }
        }finally {
            if(c != null){
                holder.unlockCanvasAndPost(c)
            }
        }
    }
}
