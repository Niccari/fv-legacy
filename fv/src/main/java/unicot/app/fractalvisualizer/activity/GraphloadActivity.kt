package unicot.app.fractalvisualizer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.graphload.*
import kotlinx.android.synthetic.main.graphload_gv.view.*
import unicot.app.fractalvisualizer.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


/**
 * グラフ読込、削除画面
 */
open class GraphloadActivity : Activity() {
    private val fileUrls: ArrayList<FileData> = ArrayList(0)
    private var windowSize: Point = Point()

    private var adapter: IVAdapter? = null

    private var isSelecting = false

    protected class FileData constructor(var xmlUrl: String = "", var imgUrl: String = "", var isSelected: Boolean = false)

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
            assets.list("").filter { it.endsWith(".jpg") || it.endsWith(".xml") }.map{
                basename ->

                val inputStream = assets.open(basename)
                val fos = FileOutputStream(File(STR_APP_ROOT_DIR, basename))

                val buffer = ByteArray(1024)
                while(true) {
                    val length = inputStream.read(buffer)
                    if(length < 0)  break
                    fos.write(buffer, 0, length)
                }
                inputStream.close()
                fos.close()
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

        adapter = IVAdapter(this, fileUrls)
        graphload_gv?.adapter = adapter
        graphload_gv?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (isSelecting) {
                fileUrls[position].isSelected = !fileUrls[position].isSelected
                adapter?.notifyDataSetInvalidated()
            } else {
                val intent = Intent()
                intent.putExtra("xml", fileUrls[position].xmlUrl + ".xml")
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        graphload_gv?.onItemLongClickListener = AdapterView.OnItemLongClickListener {
            _, _, _, _ ->

            if(!isSelecting) {
                changeSelectState()
            }
            false
        }
    }

    private fun createFileLists(): Boolean {
        val dir = File(STR_APP_ROOT_DIR)
        val files = dir.listFiles() ?: return false
        val numItems = files.size / 2

        Arrays.sort(files, Collections.reverseOrder<Any>())

        // xmlとjpgはペアのはず
        val xmlFiles = arrayOfNulls<File>(numItems)
        val jpgFiles = arrayOfNulls<File>(numItems)

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
        for (i in 0 until numItems) {
            val file = jpgFiles[i] ?: break
            var xmlUrl = xmlFiles[i].toString()
            xmlUrl = xmlUrl.substring(0, xmlUrl.lastIndexOf('.'))
            fileUrls.add(FileData(xmlUrl, STR_APP_ROOT_DIR + file.name))
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
                        val filename = file_url.value.xmlUrl
                        File("$filename.jpg").delete()
                        File("$filename.xml").delete()
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

    private class ViewHolder {
        internal var image: ImageView? = null
        internal var select: ImageView? = null
    }

    private inner class IVAdapter constructor(val context: Context,val dataList: List<FileData>) : BaseAdapter() {
        override fun getItem(p0: Int): Any {
            return dataList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return dataList.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder
            if(view == null){
                holder = ViewHolder()
                view = View.inflate(context, R.layout.graphload_gv, null)

                holder.image = view.graphload_iv_image
                holder.select = view.graphload_iv_select

                view.tag = holder
            }else{
                if (view.tag !is ViewHolder)
                    return view
                holder = view.tag as ViewHolder
            }

            val data = dataList[position]
            holder.image?.let{
                Glide.with(context)
                     .load(data.imgUrl)
                     .centerCrop()
                     .transition(DrawableTransitionOptions.withCrossFade())
                     .into(it) }
            if (data.isSelected) {
                holder.select?.visibility = View.VISIBLE
                holder.image?.setColorFilter(ContextCompat.getColor( context, R.color.lighten))
            } else {
                holder.select?.visibility = View.INVISIBLE
                holder.image?.clearColorFilter()
            }
            return view!!
        }
    }

    companion object {
        private var STR_APP_ROOT_DIR: String = ""
    }
}
