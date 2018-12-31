package unicot.app.fractalvisualizer.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Xml
import android.view.*
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.main.*
import org.xmlpull.v1.XmlPullParserException
import unicot.app.fractalvisualizer.R
import unicot.app.fractalvisualizer.core.DGCommon
import unicot.app.fractalvisualizer.core.DGCore
import unicot.app.fractalvisualizer.core.DGDataLoad
import unicot.app.fractalvisualizer.core.DGDataWrite
import unicot.app.fractalvisualizer.view.*
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * メイン画面(グラフ描画)
 */
class MainActivity : Activity() {
    private var is_GUIactivated = false
    private var mXMLWriter: DGDataWrite? = null

    // GUI関連
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var mIconWindow: PopupWindow
    private lateinit var mDeleteWindow: PopupWindow
    
    private var mGui: View? = null
    private lateinit var mMisc: MiscView
    private lateinit var mSettingGraph: GraphSettingView
    private lateinit var mSettingPaint: PaintView
    private lateinit var mAddGraph: GraphAddView
    private var mVCurrentGUI: View? = null

    private lateinit var mGraphDeleteIcon: ImageButton
    private lateinit var mIVGraphOverrayIcon: ImageView
    private var mCurrentSelectedGraphIcon = ""
    private var executor: ScheduledExecutorService? = null

    /**
     * グラフおよびガイドを描画する。 この描画関数はdrawViewより定期呼び出しされる
     */
    fun draw(c: Canvas) {
        c.drawColor(Color.argb(DGCore.systemData.viewAlpha, 0, 0, 0), PorterDuff.Mode.SRC)

        if (dgc.povFrame <= 0) {
            dgc.draw(c)
        } else {
            tmpCanvas.drawColor(Color.argb((255 - dgc.povFrame) / dgc.povFrame, 0, 0, 0), PorterDuff.Mode.DST_OUT)
            dgc.draw(tmpCanvas)
            fill_paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF)
            c.drawBitmap(bmp, 0f, 0f, fill_paint)
        }

        // 以下、オーバーレイ表示
        // グラフ選択時の操作案内を表示
        if (dgc.isGraphSelected) {
            val pt = DGCommon.getAbsCntPoint(dgc.selectedCOG)
            // 拡大縮小
            fill_paint.style = Paint.Style.FILL
            fill_paint.setARGB(0x88, 0xaa, 0xaa, 0xaa)
            val w = PointF()
            val rect = RectF()

            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_SCALING * window_size.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_SCALING * window_size.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fill_paint)

            // 回転
            fill_paint.style = Paint.Style.FILL
            fill_paint.setARGB(0x88, 0xaa, 0xaa, 0x22)
            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_ROTATE * window_size.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_ROTATE * window_size.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fill_paint)

            // 移動
            fill_paint.setARGB(0x88, 0xaa, 0x22, 0x22)
            w.set(DGCore.DIST_THRESH_GRAPH_SELECT_TRANSLATE * window_size.x / 2, DGCore.DIST_THRESH_GRAPH_SELECT_TRANSLATE * window_size.y / 2)
            rect.set(pt.x - w.x, pt.y - w.y, pt.x + w.x, pt.y + w.y)
            c.drawOval(rect, fill_paint)
        }
        // 選択領域の描画
        if (is_touching && !dgc.isGraphSelected) {
            fill_paint.style = Paint.Style.FILL
            fill_paint.setARGB(0xaa, 0xaa, 0xaa, 0xaa)

            c.drawRect(touch_pt.x.toFloat(), touch_pt.y.toFloat(), touched_pt.x.toFloat(), touched_pt.y.toFloat(), fill_paint)
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
        val disp = wm.defaultDisplay
        disp.getSize(window_size)

        // スクリーンキャプチャ関連
        bmp = Bitmap.createBitmap(window_size.x, window_size.y, Bitmap.Config.ARGB_8888)

        tmpCanvas = Canvas(bmp)

        STR_APP_ROOT_DIR = filesDir.path

        // 処理を開始
        dgc.init(window_size)

        is_touching = false
        val ib_listener = View.OnClickListener { v ->
            if (!is_GUIactivated) {
                var width_param = ViewGroup.LayoutParams.WRAP_CONTENT
                if (mGui == null) setEvents()

                if (mVCurrentGUI != null)
                    (mGui as ViewGroup).removeView(mVCurrentGUI)

                when (v.id) {
                    R.id.main_ib_graph -> mVCurrentGUI = mSettingGraph
                    R.id.main_ib_paint -> {
                        mVCurrentGUI = mSettingPaint
                        width_param = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    R.id.main_ib_add_graph -> mVCurrentGUI = mAddGraph
                    R.id.main_ib_misc -> {
                        mVCurrentGUI = mMisc
                        width_param = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
                (mGui as ViewGroup).addView(mVCurrentGUI, ViewGroup.LayoutParams(width_param, ViewGroup.LayoutParams.WRAP_CONTENT))

                adjustGui()
                showGUI(v.id)
            }
        }
        main_ib_graph.setOnClickListener(ib_listener)
        main_ib_paint.setOnClickListener(ib_listener)
        main_ib_add_graph.setOnClickListener(ib_listener)
        main_ib_misc.setOnClickListener(ib_listener)

        // オーバレイ関連
        mIVGraphOverrayIcon = ImageView(this)
        mGraphDeleteIcon = ImageButton(this)
        mGraphDeleteIcon.setOnClickListener {
            dgc.operate(DGCore.OP_GRAPH_DELETE)
            changeGuiButtonStatus()

            if (dgc.selectedGraphNum == 0) {
                mGraphDeleteIcon.visibility = ImageView.INVISIBLE
            }
        }

        // グラフアイコン用ポップアップ(浮動)
        mIconWindow = PopupWindow(mIVGraphOverrayIcon, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        mIconWindow.isTouchable = true

        // グラフアイコン(浮動)
        mIVGraphOverrayIcon.visibility = ImageView.INVISIBLE // アイコンは普段見えない

        // グラフ消去ボタン(浮動)
        mDeleteWindow = PopupWindow(mGraphDeleteIcon, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        mDeleteWindow.isTouchable = true

        // Activity完全起動後に実行
        mIVGraphOverrayIcon.post {
            // mDismissTime = 0; // GUI消す時間をリセット
            mDeleteWindow.showAsDropDown(mIVGraphOverrayIcon)
        }

        // グラフ削除アイコンは普段見えない
        mGraphDeleteIcon.visibility = ImageView.INVISIBLE
        mGraphDeleteIcon.setBackgroundResource(R.drawable.delete)

        // XML出力関連
        try {
            val pInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
            mXMLWriter = DGDataWrite(STR_APP_ROOT_DIR, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

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
        executor = Executors.newSingleThreadScheduledExecutor()
        executor?.scheduleAtFixedRate({
            dgc.run()
            main_dv.draw(object : DrawView.DrawListener {
                override fun onDraw(canvas: Canvas) {
                    draw(canvas)
                }
            })
        }, 10, (1000 / DGCore.systemData.framerate).toLong(), TimeUnit.MILLISECONDS)
    }

    public override fun onDestroy() {
        super.onDestroy()
        finishAndRemoveTask()
    }

    private fun adjustGui() {
        val _mGui = mGui ?: return
        _mGui.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        mPopupWindow.height = _mGui.measuredHeight
    }

    private fun changeGuiButtonStatus() {
        main_ib_paint?.visibility = if (dgc.selectedGraphNum > 0) View.VISIBLE else View.INVISIBLE
        main_ib_graph?.visibility = if (dgc.selectedGraphNum == 1) View.VISIBLE else View.INVISIBLE
    }

    /**
     * 画面タップ時の処理。
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        touch_pt.x = event.x.toInt()
        touch_pt.y = event.y.toInt()

        if (!is_GUIactivated) { // GUIは表示されていないか？
            if (event.action == MotionEvent.ACTION_DOWN) {

                // タッチ開始、グラフの移動・拡大縮小・回転を始める
                touched_pt.x = touch_pt.x
                touched_pt.y = touch_pt.y
                is_touching = true

                dgc.collision(touch_pt) // 指の位置に応じて、選択したグラフの処理方法を決定

            } else if (event.action == MotionEvent.ACTION_UP) {
                is_touching = false
                dgc.operate(Point(-1, -1), touch_pt) // 移動値をリセット

                if (!dgc.isGraphSelected)
                // 四角領域内のグラフをすべて選択
                    dgc.select(touched_pt, touch_pt)

            } else if (event.action == MotionEvent.ACTION_MOVE) {
                if (dgc.isGraphSelected)
                    dgc.operate(touched_pt, touch_pt) // 選択したグラフについて移動・回転・拡大縮小のいずれかを行う
            }
        }
        /* グラフの選択状況に応じてGUIボタンを表示 */
        changeGuiButtonStatus()

        val x = event.x.toInt()
        val y = event.y.toInt()

        // mIconWindowがアクティブ(=GUIが稼働)か？
        if (is_GUIactivated && mIconWindow.isShowing) {
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
        if (!is_GUIactivated) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (!dgc.isGraphSelected) {
                        mGraphDeleteIcon.visibility = ImageView.INVISIBLE
                    }
                    if (dgc.isGraphSelected) {
                        mGraphDeleteIcon.visibility = ImageView.VISIBLE
                    }
                }
                MotionEvent.ACTION_DOWN ->
                    if (dgc.isGraphSelected) {
                        mGraphDeleteIcon.visibility = ImageView.VISIBLE
                    }
            }
        }
        // グラフ消去ボタンの位置設定
        if (dgc.isGraphSelected) {
            val offset = DGCommon.getAbsCntPoint(PointF(-0.8f, -0.8f))
            val cog_abs = DGCommon.getAbsCntPoint(dgc.selectedCOG)
            mDeleteWindow.update(cog_abs.x + offset.x, cog_abs.y + offset.y, -1, -1) // カーソルの位置にアイコンがくるよう調整
        }
        return super.onTouchEvent(event)
    }

    private fun dispose() {
        is_GUIactivated = false

        mPopupWindow.dismiss()
        mIconWindow.dismiss()
    }

    private fun setEvents() {
        mGui = View.inflate(this, R.layout.gui, null)

        mAddGraph = GraphAddView(this, null)
        mAddGraph.setEvent(object: View.OnTouchListener {
            override fun onTouch(v: View?, e: MotionEvent?): Boolean {
                mIVGraphOverrayIcon.setImageResource(DGCommon.getGraphIcon(DGCommon.getKind(v?.tag.toString())))
                mIVGraphOverrayIcon.invalidate()

                when (e?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mCurrentSelectedGraphIcon = v?.tag.toString()
                        v?.performClick()
                    }
                    MotionEvent.ACTION_UP -> v?.performClick()
                }

                if(e != null){
                    val ev = MotionEvent.obtain(0, 0, e.action, e.rawX, e.rawY, 1.0f, 1.0f, 0, 1.0f, 1.0f, -1, 0)
                    val ret = onTouchEvent(ev)
                    ev.recycle()
                    return ret
                }else{
                    return false
                }
            }
        })

        val activity = this
        mMisc = MiscView(this, null)
        mMisc.setEvent(object : MiscView.OnEventListener {
            override operator fun invoke(key: String) {
                if (key.matches("fps".toRegex())) {
                    stop()
                    resume()
                }
                if (key.matches("load_graph".toRegex())) {
                    stop()

                    val intent = Intent()
                    intent.setClassName(packageName, "$packageName.activity.GraphloadActivity")
                    startActivityForResult(intent, INTENT_OPEN_GRAPH)
                }
                if (key.matches("save_graph".toRegex())) {
                    val date = DGCommon.currentDateString

                    val isSucceed = mXMLWriter?.writeXml(date)
                    if (isSucceed != null && isSucceed)
                        captureView(date, false)
                }
                if (key.matches("capture".toRegex())) {
                    if(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(activity, Array(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1)
                    }else{
                        captureView("", true)
                    }
                }
            }
        })
        mSettingGraph = GraphSettingView(this, null)
        mSettingGraph.setDGCore(dgc)

        mSettingPaint = PaintView(this, null)
        mSettingPaint.setDGCore(dgc)

        mGui?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        mPopupWindow = PopupWindow(mGui, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private fun showGUI(id: Int) {
        mGraphDeleteIcon.visibility = ImageView.INVISIBLE

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

        is_GUIactivated = true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            // Externalな領域への書き込み権限入手
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureView("", true)
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

        when (requestCode) {
            INTENT_OPEN_GRAPH // ファイル読み出し(orキャンセル)
            -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val parser = Xml.newPullParser()

                    try { // 指定したファイル(filePath)を読む処理

                        val inputStream = FileInputStream(data?.getStringExtra("xml"))
                        parser.setInput(inputStream, null)

                        // 一旦画面更新止めてから、データ読み込み
                        DGDataLoad.load(parser) // 失敗時の処理は？
                        dgc.select(dummy_pt, dummy_pt) // 選択解除

                        // GUI絡みのものはUI Threadにて
                        runOnUiThread {
                            dispose() // GUIをリセット
                            mMisc.sync()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: XmlPullParserException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    /**
     * 描画スレッドを再開させてから、所定の処理を行う。
     */
    public override fun onResume() {
        super.onResume()

        if (main_ib_graph != null) {
            main_ib_graph.postDelayed({
                stop()
                resume()
            }, 100)
        }
    }

    // 現画面のスクリーンキャプチャをとる。
    // ファイル名は現時刻を基に決定する。
    private fun captureView(fname: String, isPng: Boolean): Int {
        stop()

        // 再描画
        main_dv.draw(object : DrawView.DrawListener {
            override fun onDraw(canvas: Canvas) {
                if (dgc.povFrame <= 0) {
                    tmpCanvas.drawColor(Color.BLACK)
                    dgc.draw(tmpCanvas)
                }
            }
        })

        // 保存開始
        val mPath: String?
        val mFile: String // 保存ファイルのディレクトリおよびファイル名

        if (isPng) {
            mPath = Environment.getExternalStorageDirectory().path + "/FV/"
            mFile = DGCommon.currentDateString + ".png"
        } else {
            mPath = filesDir.path
            mFile = "$fname.jpg"
        }

        val fout: OutputStream
        val dirFile = File(mPath)
        val imageFile = File("$mPath/$mFile")

        try {
            dirFile.mkdir()

            fout = FileOutputStream(imageFile)

            if (isPng) { // スクリーンキャプチャ
                bmp.compress(CompressFormat.PNG, 100, fout)
            } else { // グラフ保存時のサムネイル
                val tmp = Bitmap.createScaledBitmap(bmp, window_size.x / 4, window_size.y / 4, false)
                tmp.compress(CompressFormat.JPEG, 75, fout)
            }
            fout.flush()
            fout.close()

        } catch (e: NullPointerException) {
            e.printStackTrace()
            return CAPTURE_NG
        } catch (e: IOException) {
            e.printStackTrace()
            return CAPTURE_NG
        } finally {
            resume()
        }
        return CAPTURE_OK
    }

    companion object {
        private val CAPTURE_OK = 0
        private val CAPTURE_NG = -1
        private val INTENT_OPEN_GRAPH = 2
        private val dummy_pt = Point(-1, -1) // 選択解除用
        // クラス名など
        private lateinit var STR_APP_ROOT_DIR: String
        private val dgc: DGCore = DGCore()
        private val window_size: Point = Point()
        private val touch_pt: Point = Point()
        private val touched_pt: Point = Point()
        private val fill_paint: Paint = Paint()
        private lateinit var bmp: Bitmap // スクリーンキャプチャ用bitmap
        private lateinit var tmpCanvas: Canvas

        private var is_touching: Boolean = false
    }
}
