package unicot.app.fractalvisualizer.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.*
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.main.*
import org.xmlpull.v1.XmlPullParserException
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.core.DGDataLoad
import unicot.app.fractalvisualizer.core.DGDataWrite
import unicot.app.fractalvisualizer.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/** メイン画面(グラフ描画)*/
class MainActivity : Activity() {
    private var isGuiActivated = false

    private val intentOpenGraph = 2

    private val dgc: DGCore = DGCore()
    private val windowSize: Point = Point()
    private val touchPoint: Point = Point()
    private val touchPointBefore: Point = Point()
    private val fillPaint: Paint = Paint()
    private lateinit var bmp: Bitmap // スクリーンキャプチャ用bitmap
    private lateinit var tmpCanvas: Canvas

    private var isTouching: Boolean = false

    // GUI関連
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var mIconWindow: PopupWindow
    private lateinit var mCopyWindow: PopupWindow
    private lateinit var mDeleteWindow: PopupWindow

    private var mGui: View? = null
    private lateinit var mMisc: MiscView
    private lateinit var mSettingGraph: GraphSettingView
    private lateinit var mSettingPaint: PaintView
    private lateinit var mAddGraph: GraphAddView
    private var mVCurrentGUI: View? = null

    private lateinit var mGraphDeleteIcon: ImageButton
    private lateinit var mCopyIcon: ImageButton
    private lateinit var mIVGraphOverrayIcon: ImageView
    private var mCurrentSelectedGraphIcon = ""
    private var executor: ScheduledExecutorService? = null

    /**
     * グラフおよびガイドを描画する。 この描画関数はdrawViewより定期呼び出しされる
     */
    fun draw(c: Canvas) {
        c.drawColor(Color.BLACK, PorterDuff.Mode.SRC)

        if (dgc.povFrame <= 0) {
            dgc.draw(c)
        } else {
            tmpCanvas.drawColor(Color.argb((255 - dgc.povFrame) / dgc.povFrame, 0, 0, 0), PorterDuff.Mode.DST_OUT)
            dgc.draw(tmpCanvas)
            fillPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF)
            c.drawBitmap(bmp, 0f, 0f, fillPaint)
        }

        // 以下、オーバーレイ表示
        // グラフ選択時の操作案内を表示
        if (dgc.isGraphSelected) {
            val pt = DGCommon.getAbsCntPoint(dgc.selectedCOG)
            // 拡大縮小
            fillPaint.style = Paint.Style.FILL
            fillPaint.setARGB(0x88, 0xaa, 0xaa, 0xaa)
            val w = PointF()
            val rect = RectF()

            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_SCALING * windowSize.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_SCALING * windowSize.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fillPaint)

            // 回転
            fillPaint.style = Paint.Style.FILL
            fillPaint.setARGB(0x88, 0xaa, 0xaa, 0x22)
            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_ROTATE * windowSize.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_ROTATE * windowSize.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fillPaint)

            // 移動
            fillPaint.setARGB(0x88, 0xaa, 0x22, 0x22)
            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_TRANSLATE * windowSize.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_TRANSLATE * windowSize.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fillPaint)
        }
        // 選択領域の描画
        if (isTouching && !dgc.isGraphSelected) {
            fillPaint.style = Paint.Style.FILL
            fillPaint.setARGB(0xaa, 0xaa, 0xaa, 0xaa)

            c.drawRect(touchPoint.x.toFloat(), touchPoint.y.toFloat(), touchPointBefore.x.toFloat(), touchPointBefore.y.toFloat(), fillPaint)
        }
    }

    /**
     * 本Activity起動時に実施。<br></br>
     * GUIの準備を行い、イベント関連を登録する
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面をフルスクリーンに設定
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.main)

        // 画面サイズの取得
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(windowSize)

        // スクリーンキャプチャ関連
        bmp = Bitmap.createBitmap(windowSize.x, windowSize.y, Bitmap.Config.ARGB_8888)

        tmpCanvas = Canvas(bmp)

        // 処理を開始
        dgc.setScreenSize(windowSize)

        isTouching = false
        val ibListener = View.OnClickListener { v ->
            if (!isGuiActivated) {
                var widthParam = LayoutParams.WRAP_CONTENT
                if (mGui == null) setEvents()

                if (mVCurrentGUI != null)
                    (mGui as ViewGroup).removeView(mVCurrentGUI)

                when (v.id) {
                    R.id.main_ib_graph -> mVCurrentGUI = mSettingGraph
                    R.id.main_ib_paint -> {
                        mVCurrentGUI = mSettingPaint
                        widthParam = LayoutParams.MATCH_PARENT
                    }
                    R.id.main_ib_add_graph -> mVCurrentGUI = mAddGraph
                    R.id.main_ib_misc -> {
                        mVCurrentGUI = mMisc
                        widthParam = LayoutParams.MATCH_PARENT
                    }
                }
                (mGui as ViewGroup).addView(mVCurrentGUI, LayoutParams(widthParam, LayoutParams.WRAP_CONTENT))

                adjustGui()
                showGUI(v.id)
            }
        }
        main_ib_graph.setOnClickListener(ibListener)
        main_ib_paint.setOnClickListener(ibListener)
        main_ib_add_graph.setOnClickListener(ibListener)
        main_ib_misc.setOnClickListener(ibListener)

        // オーバレイ関連
        mIVGraphOverrayIcon = ImageView(this)
        mGraphDeleteIcon = ImageButton(this)
        mCopyIcon = ImageButton(this)
        mGraphDeleteIcon.setOnClickListener {
            stop()
            // グラフの計算、描画中の可能性があるので2/60sほど待機
            Handler().postDelayed({
                dgc.deleteSelectedGraphs()
                changeGuiButtonStatus()

                if (dgc.selectedGraphNum == 0) {
                    mGraphDeleteIcon.visibility = ImageView.INVISIBLE
                    mCopyIcon.visibility = ImageView.INVISIBLE
                }
            }, 30)
            resume()
        }
        mCopyIcon.setOnClickListener{
            stop()
            DGCommon.copyGraph(DGCore.selectedGraph[0].info.graphKind, true)
            mCopyWindow
            resume()
        }

        // グラフアイコン用ポップアップ(浮動)
        mIconWindow = PopupWindow(mIVGraphOverrayIcon, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        mIconWindow.isTouchable = true

        // グラフアイコン(浮動)
        mIVGraphOverrayIcon.visibility = ImageView.INVISIBLE // アイコンは普段見えない

        // グラフ消去ボタン(浮動)
        mDeleteWindow = PopupWindow(mGraphDeleteIcon, 180, 180)
        mDeleteWindow.isTouchable = true
        mCopyWindow = PopupWindow(mCopyIcon, 180, 180)
        mCopyWindow.isTouchable = true

        // Activity完全起動後に実行
        mIVGraphOverrayIcon.post {
            if(isFinishing || isDestroyed) return@post
            mDeleteWindow.showAsDropDown(mIVGraphOverrayIcon)
            mCopyWindow.showAsDropDown(mIVGraphOverrayIcon)
        }

        // グラフ削除アイコンは普段見えない
        mGraphDeleteIcon.visibility = ImageView.INVISIBLE
        mGraphDeleteIcon.setBackgroundResource(R.drawable.delete)
        mCopyIcon.visibility = ImageView.INVISIBLE
        mCopyIcon.setBackgroundResource(R.drawable.copy_graph)

        this.onResume()
    }

    public override fun onPause() {
        super.onPause()
        stop()
    }

    private fun stop() {
        executor?.shutdownNow()
    }

    private fun resume() {
        var execCount = 0
        var retryCount = 0
        executor = Executors.newSingleThreadScheduledExecutor()
        executor?.scheduleAtFixedRate({
            try {
                val isGraphChange = (execCount >= 60/DGCore.systemData.framerate)
                if(isGraphChange){
                    dgc.run()
                    execCount = 0
                }
                main_dv.draw(object : DrawView.DrawListener {
                    override fun onDraw(canvas: Canvas) {
                        draw(canvas)
                    }
                })
                retryCount = 0
            }catch (e: Exception){
                e.printStackTrace()
                if (retryCount < 3){
                    resume()
                }else{
                    if(!isFinishing){
                        runOnUiThread {
                            Toast.makeText(this, R.string.hud_error_thread, Toast.LENGTH_SHORT).show()
                        }
                    }
                    stop()
                    retryCount = 0
                }
            }
            execCount++
        }, 10, (1000 / 60).toLong(), TimeUnit.MILLISECONDS)
    }

    public override fun onDestroy() {
        super.onDestroy()
        finishAndRemoveTask()
    }

    private fun adjustGui() {
        mGui?.let{
            it.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            mPopupWindow.height = it.measuredHeight
        }
    }

    private fun changeGuiButtonStatus() {
        main_ib_paint?.visibility = if (dgc.selectedGraphNum > 0) View.VISIBLE else View.INVISIBLE
        main_ib_graph?.visibility = if (dgc.selectedGraphNum == 1) View.VISIBLE else View.INVISIBLE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchPoint.x = event.x.toInt()
        touchPoint.y = event.y.toInt()

        if (!isGuiActivated) { // GUIは表示されていないか？
            if (event.action == MotionEvent.ACTION_DOWN) {

                // タッチ開始、グラフの移動・拡大縮小・回転を始める
                touchPointBefore.x = touchPoint.x
                touchPointBefore.y = touchPoint.y
                isTouching = true

                dgc.collision(touchPoint) // 指の位置に応じて、選択したグラフの処理方法を決定

            } else if (event.action == MotionEvent.ACTION_UP) {
                isTouching = false
                dgc.affineTransformGraphs(Point(-1, -1), touchPoint) // 移動値をリセット

                if (!dgc.isGraphSelected)
                    dgc.select(touchPointBefore, touchPoint)

            } else if (event.action == MotionEvent.ACTION_MOVE) {
                if (dgc.isGraphSelected)
                    dgc.affineTransformGraphs(touchPointBefore, touchPoint) // 選択したグラフについて移動・回転・拡大縮小のいずれかを行う
            }
        }
        /* グラフの選択状況に応じてGUIボタンを表示 */
        changeGuiButtonStatus()

        val x = event.x.toInt()
        val y = event.y.toInt()

        // mIconWindowがアクティブ(=GUIが稼働)か？
        if (isGuiActivated && mIconWindow.isShowing) {
            val w = mIconWindow.width
            val h = mIconWindow.height
            mIconWindow.update(x + w, y + h, -1, -1) // カーソルの位置にアイコンがくるよう調整

            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    mIVGraphOverrayIcon.visibility = ImageView.GONE // グラフアイコンのオーバレイ消す

                    stop()

                    if (DGCommon.copyGraph(DGCommon.getKind(mCurrentSelectedGraphIcon), false)) {
                        val g = DGCore.graph
                        val pt = DGCommon.getRelCntPoint(Point(x, y))
                        g.last().setPosition(pt.x, pt.y)
                    }
                    resume()
                }
                MotionEvent.ACTION_DOWN ->
                    // グラフアイコンを表示開始(タップ時)
                    mIVGraphOverrayIcon.visibility = ImageView.VISIBLE
            }
        }

        // グラフ消去ボタンの表示切り替え
        if (!isGuiActivated) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (!dgc.isGraphSelected) {
                        mGraphDeleteIcon.visibility = ImageView.INVISIBLE
                        mCopyIcon.visibility = ImageView.INVISIBLE
                    }else{
                        mGraphDeleteIcon.visibility = ImageView.VISIBLE
                        if(dgc.selectedGraphNum == 1)
                            mCopyIcon.visibility = ImageView.VISIBLE
                    }
                }
                MotionEvent.ACTION_DOWN ->
                    if (dgc.isGraphSelected) {
                        mGraphDeleteIcon.visibility = ImageView.VISIBLE
                        if(dgc.selectedGraphNum == 1)
                            mCopyIcon.visibility = ImageView.VISIBLE
                    }
            }
        }
        // グラフ消去ボタンの位置設定
        if (dgc.isGraphSelected) {
            val copyIconOffset = DGCommon.getAbsCntPoint(PointF(-0.8f, -1.05f))
            val deleteIconOffset = DGCommon.getAbsCntPoint(PointF(-0.8f, -0.8f))
            val cogAbs = DGCommon.getAbsCntPoint(dgc.selectedCOG)
            mDeleteWindow.update(cogAbs.x + deleteIconOffset.x, cogAbs.y + deleteIconOffset.y, -1, -1) // カーソルの位置にアイコンがくるよう調整
            mCopyWindow.update(cogAbs.x + copyIconOffset.x, cogAbs.y + copyIconOffset.y, -1, -1)
        }
        return super.onTouchEvent(event)
    }

    private fun dispose() {
        isGuiActivated = false

        mPopupWindow.dismiss()
        mIconWindow.dismiss()
    }

    private fun setEvents() {
        mGui = View.inflate(this, R.layout.gui, null)

        mAddGraph = GraphAddView(this, null)
        mAddGraph.setEvent(View.OnTouchListener { v, e ->
            mIVGraphOverrayIcon.setImageResource(DGCommon.getGraphIcon(DGCommon.getKind(v?.tag.toString())))
            mIVGraphOverrayIcon.invalidate()

            when (e?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mCurrentSelectedGraphIcon = v?.tag.toString()
                    v?.performClick()
                }
                MotionEvent.ACTION_UP -> v?.performClick()
            }

            if (e != null) {
                val ev = MotionEvent.obtain(0, 0, e.action, e.rawX, e.rawY, 1.0f, 1.0f, 0, 1.0f, 1.0f, -1, 0)
                val ret = onTouchEvent(ev)
                ev.recycle()
                ret
            } else {
                false
            }
        })

        val activity = this
        mMisc = MiscView(this, null)
        mMisc.setEvent(object : MiscView.OnEventListener {
            override operator fun invoke(key: String) {
                if (key.matches("pov_frame".toRegex())) {
                    if(DGCore.systemData.povFrame == 0){
                        // 前回ロードしたときのグラフの残像が残っている可能性があるのでクリア
                        tmpCanvas.drawColor(Color.BLACK)
                    }
                }
                if (key.matches("load_graph".toRegex())) {
                    val intent = Intent()
                    intent.setClassName(packageName, "$packageName.activity.GraphloadActivity")
                    startActivityForResult(intent, intentOpenGraph)
                }
                if (key.matches("save_graph".toRegex())) {
                    captureView("save_graph")?.let{
                        main_pb.visibility = View.VISIBLE
                        // 保存は通信を必要とするため、いつ終わるかわからない
                        DGDataWrite.save(it){
                            result ->
                            if(isFinishing) return@save
                            main_pb.visibility = View.INVISIBLE
                            if(result){
                                Toast.makeText(this@MainActivity, R.string.hud_graph_saved, Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this@MainActivity, R.string.hud_error_connection, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                if (key.matches("capture".toRegex())) {
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, Array(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1)
                    } else {
                        captureView("capture")
                    }
                }
                if (key.matches("preference".toRegex())) {
                    val intent = Intent()
                    intent.setClassName(packageName, "$packageName.activity.PreferenceActivity")
                    startActivityForResult(intent, intentOpenGraph)
                }
            }
        })
        mSettingGraph = GraphSettingView(this, null)
        mSettingGraph.setDGCore(dgc)

        mSettingPaint = PaintView(this, null)
        mSettingPaint.setDGCore(dgc)

        mGui?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        mPopupWindow = PopupWindow(mGui, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private fun showGUI(id: Int) {
        mGraphDeleteIcon.visibility = ImageView.INVISIBLE
        mCopyIcon.visibility = ImageView.INVISIBLE

        // グラフ選択時、一番古いグラフを元に設定値を更新
        if (dgc.selectedGraphNum >= 1) {
            if (id == R.id.main_ib_graph) mSettingGraph.refresh()
            if (id == R.id.main_ib_paint) mSettingPaint.refresh()
        }

        adjustGui()

        mPopupWindow.showAsDropDown(main_ib_misc)
        mIconWindow.showAsDropDown(main_ib_misc)

        mPopupWindow.isTouchable = true
        mPopupWindow.isFocusable = true
        mPopupWindow.update()
        mPopupWindow.setOnDismissListener { dispose() }

        isGuiActivated = true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureView("capture")
                }
            }
        }
    }

    /**
     * ブラシ設定変更
     */
    fun onBrushButtonClicked(v: View) {
        mSettingPaint.onBrushButtonClicked(v)
    }

    /**
     * 色パターン変更
     */
    fun onColorPatternClicked(v: View) {
        mSettingPaint.onColorPatternClicked(v)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // データロード
        if(requestCode == intentOpenGraph && resultCode == RESULT_OK){
            try {
                val id = data?.getStringExtra("id") ?: return

                DGDataLoad.load(id){
                    if(it){
                        runOnUiThread {
                            dgc.select()
                            mMisc.sync()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 描画スレッドを再開させてから、所定の処理を行う。
     */
    public override fun onResume() {
        super.onResume()

        main_ib_graph?.postDelayed({
            stop()
            resume()
        }, 100)
    }

    // 現画面のスクリーンキャプチャをとる。
    // ファイル名は現時刻を基に決定する。
    private fun captureView(mode: String): File? {
        stop()

        // 残像0のときのみ、スクリーンショット用のキャンバスに再描画
        if (dgc.povFrame <= 0) {
            main_dv.draw(object : DrawView.DrawListener {
                override fun onDraw(canvas: Canvas) {
                    tmpCanvas.drawColor(Color.BLACK)
                    dgc.draw(tmpCanvas)
                }
            })
        }
        val path: String
        val filename: String

        if (mode == "save_graph") {
            path = filesDir.path
            filename = "tmp.jpg"
        } else {
            path = "${Environment.getExternalStorageDirectory().path}/FV"
            filename = "${DGCommon.currentDateString}.png"
        }

        val outputStream: OutputStream
        val dirFile = File(path)
        val imageFile = File("$path/$filename")

        try {
            dirFile.mkdir()

            outputStream = FileOutputStream(imageFile)

            if (mode == "save_graph") {
                val tmp = Bitmap.createScaledBitmap(bmp, windowSize.x / 4, windowSize.y / 4, false)
                tmp.compress(CompressFormat.JPEG, 75, outputStream)
            } else {
                bmp.compress(CompressFormat.PNG, 100, outputStream)
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                contentValues.put("_data", imageFile.path)
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
            outputStream.flush()
            outputStream.close()

        } catch (e: NullPointerException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            resume()
        }
        return imageFile
    }
}
