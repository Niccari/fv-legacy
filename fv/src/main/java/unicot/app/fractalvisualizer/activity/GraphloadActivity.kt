package unicot.app.fractalvisualizer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import kotlinx.android.synthetic.main.graphload.*
import unicot.app.fractalvisualizer.R
import java.io.File
import java.util.*
import java.util.regex.Pattern
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException


/**
 * グラフ読込、削除画面
 */
class GraphloadActivity : Activity() {
    private val file_urls: ArrayList<FileData>
    private var window_size: Point

    private lateinit var mIVAdapter: IVAdapter

    private var isSelecting = false

    protected class FileData constructor(var fname: String = "", var img: ImageView, var isSelected: Boolean = false)

    init{
        file_urls = ArrayList(0)
        window_size = Point()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphload)

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val disp = wm.defaultDisplay
        disp.getSize(window_size)

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

        mIVAdapter = IVAdapter(this, -1, file_urls)

        graphload_gv?.adapter = mIVAdapter
        graphload_gv?.onItemClickListener = OnItemClickListener { parent, v, position, id ->

            if (isSelecting) {
                file_urls[position].isSelected = !file_urls[position].isSelected
                mIVAdapter.notifyDataSetInvalidated()
            } else {
                val intent = Intent()
                intent.putExtra("xml", file_urls[position].fname + ".xml")
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun createFileLists(): Boolean {
        val dir = File(STR_APP_ROOT_DIR)
        val files = dir.listFiles() ?: return false

        Arrays.sort(files, Collections.reverseOrder<Any>())

        // xmlとjpgはペアのはず
        val xml_files = arrayOfNulls<File>(files.size / 2)
        val jpg_files = arrayOfNulls<File>(files.size / 2)

        var j = 0
        var k = 0

        var fileEx = ""
        for (file in files) {
            val m = Pattern.compile(".*/.*?(\\..*)").matcher(file.toString())
            if (m.matches())
                fileEx = m.group(1)
            if (fileEx.matches(".xml".toRegex())) {
                xml_files[j++] = file
            } else if (fileEx.matches(".jpg".toRegex())) {
                jpg_files[k++] = file

            }
        }

        // 3. 各グラフのサムネイルを作成する。
        // 全グラフサムネイル数（＝保存グラフ数）
        val IMAGE_NUM = files.size / 2
        file_urls.clear()
        var tmp: Bitmap
        for (i in 0 until IMAGE_NUM) {
            val file = jpg_files[i] ?: break
            tmp = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(STR_APP_ROOT_DIR + file.getName()),
                    window_size.x / GRID_COL - 3 * 10 * GRID_COL,
                    window_size.y / GRID_COL - 6 * 10 * GRID_COL, false)
            val iv = ImageView(this)
            iv.setImageBitmap(tmp)
            var xml_url_str = xml_files[i].toString()
            xml_url_str = xml_url_str.substring(0, xml_url_str.lastIndexOf('.'))
            file_urls.add(FileData(xml_url_str, iv))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_select, R.id.action_cancel -> {
                isSelecting = !isSelecting
                for(file_url in file_urls){
                    file_url.isSelected = false
                }
                if (isSelecting) {
                    setTitle(getString(R.string.action_select))
                }else{
                    setTitle(getString(R.string.select_graph_title))
                }
                invalidateOptionsMenu()
                mIVAdapter.notifyDataSetInvalidated()
            }
            R.id.action_delete -> {
                val del_list: ArrayList<FileData> = ArrayList(0)
                for(file_url in file_urls.withIndex()){
                    if(file_url.value.isSelected) {
                        val fname = file_url.value.fname
                        File("$fname.jpg").delete()
                        File("$fname.xml").delete()
                        del_list.add(file_url.value)
                    }
                }
                for(del in del_list){
                    file_urls.remove(del)
                }

                isSelecting = false
                invalidateOptionsMenu()
                mIVAdapter.notifyDataSetInvalidated()
            }
        }
        return true
    }

    protected inner class IVAdapter constructor(context: Context, resource: Int, objects: List<FileData>) : ArrayAdapter<FileData>(context, resource, objects) {
        private val dataList: List<FileData> = objects
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: ImageView
            val data = dataList[position]
            if(convertView != null){
                view = convertView as ImageView
            }else{
                view = dataList[position].img
            }
            view.setImageDrawable(data.img.drawable)

            if (data.isSelected) {
                view.drawable.setColorFilter( resources.getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP)
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
        private val GRID_COL = 3
    }
}
