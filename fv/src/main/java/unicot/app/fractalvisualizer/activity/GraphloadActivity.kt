package unicot.app.fractalvisualizer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.graphload.*
import kotlinx.android.synthetic.main.graphload_gv.view.*
import unicot.app.fractalvisualizer.R
import java.util.*

/**
 * グラフ読込、削除画面
 */
open class GraphloadActivity : Activity() {
    private val fileUrls: ArrayList<FileData> = ArrayList(0)
    private var adapter: IVAdapter? = null

    protected class FileData constructor(var id: String = "", var imgUrl: String = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphload)

        init()
    }

    private fun init() {
        val db = FirebaseFirestore.getInstance()

        db.collection("session_info").get().addOnCompleteListener {
            task ->
            if(task.isSuccessful) {
                task.result?.map { session ->
                    val id = session.id
                    val imageUrl = session["thumb_url"] as? String ?: ""
                    fileUrls.add(FileData(id, imageUrl))
                }

                adapter = IVAdapter(this, fileUrls)
                graphload_gv?.adapter = adapter
                graphload_gv?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                    val intent = Intent()
            intent.putExtra("id", fileUrls[position].id)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }else{
                setResult(RESULT_CANCELED)
                finish()
            }
        }
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
            return view!!
        }
    }
}
