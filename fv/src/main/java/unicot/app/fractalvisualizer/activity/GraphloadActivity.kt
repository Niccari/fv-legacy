package unicot.app.fractalvisualizer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import kotlinx.android.synthetic.main.graphload.*
import unicot.app.fractalvisualizer.R
import java.io.File
import java.util.*
import java.util.regex.Pattern
import java.io.FileOutputStream
import java.io.IOException


/**
 * グラフ読込、削除画面
 */
open class GraphloadActivity : Activity() {
    private val fileUrls: ArrayList<FileData> = ArrayList(0)
    private var windowSize: Point = Point()

    private var adapter: IVAdapter? = null

    private var isSelecting = false

    protected class FileData constructor(var fname: String = "", var img: ImageView, var isSelected: Boolean = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphload)

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(windowSize)

        STR_APP_ROOT_DIR = filesDir.path + "/"

        val data = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val isFirstBoot = data.getBoolean("isFirstBoot", true)
        if(isFirstBoot){
            copyAssetsFile()

            val e = data.edit()
            e.putBoolean("isFirstBoot", false)
            e.apply()
        }
        init()
    }

    private fun copyAssetsFile(): Boolean {
        try {
            for(fname in assets.list("")){
                if(fname.endsWith(".jpg") || fname.endsWith(".xml")) {
                    val inputStream = assets.open(fname)
                    val fos = FileOutputStream(File(STR_APP_ROOT_DIR, fname))

                    val buffer = ByteArray(1024)
                    while(true) {
                        val length = inputStream.read(buffer)
                        if(length < 0)  break
                        fos.write(buffer, 0, length)
                    }
                    inputStream.close()
                    fos.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun init() {
        if(!createFileLists()){
            finish()
            return
        }

        adapter = IVAdapter(this, -1, fileUrls)
        graphload_gv?.adapter = adapter
        graphload_gv?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (isSelecting) {
                fileUrls[position].isSelected = !fileUrls[position].isSelected
                adapter?.notifyDataSetInvalidated()
            } else {
                val intent = Intent()
                intent.putExtra("xml", fileUrls[position].fname + ".xml")
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        graphload_gv?.onItemLongClickListener = AdapterView.OnItemLongClickListener {
            _, _, position, _ ->

            if(!isSelecting) {
                changeSelectState()
            }
            false
        }
    }

    private fun createFileLists(): Boolean {
        val dir = File(STR_APP_ROOT_DIR)
        val files = dir.listFiles() ?: return false

        Arrays.sort(files, Collections.reverseOrder<Any>())

        // xmlとjpgはペアのはず
        val xmlFiles = arrayOfNulls<File>(files.size / 2)
        val jpgFiles = arrayOfNulls<File>(files.size / 2)

        var j = 0
        var k = 0

        var fileEx = ""
        for (file in files) {
            val m = Pattern.compile(".*/.*?(\\..*)").matcher(file.toString())
            if (m.matches())
                fileEx = m.group(1)
            if (fileEx.matches(".xml".toRegex())) {
                xmlFiles[j++] = file
            } else if (fileEx.matches(".jpg".toRegex())) {
                jpgFiles[k++] = file

            }
        }

        // 3. 各グラフのサムネイルを作成する。
        // 全グラフサムネイル数（＝保存グラフ数）
        fileUrls.clear()
        var tmp: Bitmap
        for (i in 0 until files.size / 2) {
            val file = jpgFiles[i] ?: break
            tmp = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(STR_APP_ROOT_DIR + file.name),
                    windowSize.x / GRID_COL - 3 * 10 * GRID_COL,
                    windowSize.y / GRID_COL - 6 * 10 * GRID_COL, false)
            val iv = ImageView(this)
            iv.setImageBitmap(tmp)
            var xmlUrl = xmlFiles[i].toString()
            xmlUrl = xmlUrl.substring(0, xmlUrl.lastIndexOf('.'))
            fileUrls.add(FileData(xmlUrl, iv))
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (!isSelecting) {
            menuInflater.inflate(R.menu.graphload_normal, menu)
        } else {
            menuInflater.inflate(R.menu.graphload_selecting, menu)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun changeSelectState(){
        isSelecting = !isSelecting
        fileUrls.map { it.isSelected = false }
        title = if (isSelecting) {
            getString(R.string.action_select)
        }else{
            getString(R.string.select_graph_title)
        }
        invalidateOptionsMenu()
        adapter?.notifyDataSetInvalidated()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_select, R.id.action_cancel -> {
                changeSelectState()
            }
            R.id.action_delete -> {
                val delList: ArrayList<FileData> = ArrayList(0)
                for(file_url in fileUrls.withIndex()){
                    if(file_url.value.isSelected) {
                        val fname = file_url.value.fname
                        File("$fname.jpg").delete()
                        File("$fname.xml").delete()
                        delList.add(file_url.value)
                    }
                }
                delList.map{ fileUrls.remove(it) }

                isSelecting = false
                invalidateOptionsMenu()
                adapter?.notifyDataSetInvalidated()
            }
        }
        return true
    }

    private inner class IVAdapter constructor(context: Context, resource: Int, val dataList: List<FileData>) : ArrayAdapter<FileData>(context, resource, dataList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: ImageView = if(convertView != null){
                convertView as ImageView
            }else{
                dataList[position].img
            }
            val data = dataList[position]
            view.setImageDrawable(data.img.drawable)

            if (data.isSelected) {
                val color = ContextCompat.getColor(context, R.color.blue)
                view.drawable.setColorFilter( color, PorterDuff.Mode.SRC_ATOP)
            } else {
                view.drawable.clearColorFilter()
            }
            return view
        }
    }

    companion object {
        // TODO: アプリ内で共通化すること
        private var STR_APP_ROOT_DIR: String = ""
        // TODO: レイアウトに縛られているのはまずい
        private const val GRID_COL = 3
    }
}
