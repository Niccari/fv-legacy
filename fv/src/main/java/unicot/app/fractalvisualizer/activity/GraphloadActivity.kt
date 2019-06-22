package unicot.app.fractalvisualizer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
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

    protected class FileData constructor(var id: String = "", var isLocked: Boolean = false,
                                         var imgUrl: String = "", var isSelected: Boolean = false)

    private var isSelecting = false
    private var isDeleting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graphload)

        fetch()
    }

    private fun fetch() {
        val db = FirebaseFirestore.getInstance()

        fileUrls.clear()
        db.collection("session_info").get().addOnCompleteListener {
            task ->
            if(isFinishing) return@addOnCompleteListener
            graphload_pb.visibility = View.INVISIBLE
            if(task.isSuccessful) {
                task.result?.map { session ->
                    val id = session.id
                    val isLocked = session["is_locked"] as? Boolean ?: true
                    val imageUrl = session["thumb_url"] as? String ?: ""
                    fileUrls.add(FileData(id, isLocked, imageUrl))
                }
                fileUrls.sortByDescending { it.id }
                adapter?.notifyDataSetInvalidated()
                initView()
            }else{
                if(!isFinishing)
                    Toast.makeText(this@GraphloadActivity, R.string.hud_graph_saved, Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun initView(){
        adapter = IVAdapter(this, fileUrls)
        graphload_gv?.adapter = adapter
        graphload_gv?.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (isDeleting) return@OnItemClickListener
            if (isSelecting) {
                if(!fileUrls[position].isLocked){
                    fileUrls[position].isSelected = !fileUrls[position].isSelected
                    adapter?.notifyDataSetInvalidated()
                }
            } else {
                val intent = Intent()
                intent.putExtra("id", fileUrls[position].id)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        graphload_gv?.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, _, _ ->
            if (!isSelecting && !isDeleting) {
                changeSelectState()
            }
            false
        }
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
                val delList = fileUrls.filter { it.isSelected }.map{ it.id }
                if(delList.isEmpty()){
                    changeSelectState()
                    return false
                }
                val db = FirebaseFirestore.getInstance()
                val batch: WriteBatch = db.batch()
                delList.map{
                    batch.delete(db.collection("session_info").document(it))
                    batch.delete(db.collection("session_detail").document(it))
                }
                isDeleting = true
                graphload_pb.visibility = View.VISIBLE
                batch.commit().addOnCompleteListener{
                    task ->
                    if(task.isSuccessful){
                        if(!isFinishing)
                            Toast.makeText(this, R.string.hud_graph_deleted, Toast.LENGTH_SHORT).show()
                        fetch()
                    }else{
                        if(!isFinishing)
                            Toast.makeText(this, R.string.hud_error_connection, Toast.LENGTH_SHORT).show()
                    }
                    isDeleting = false
                }

                Thread(Runnable{
                    delList.map{
                        val store = FirebaseStorage.getInstance()
                        store.reference.child("/thumb/$it.jpg").delete().addOnCompleteListener {
                            task ->
                            if(!task.isSuccessful && !isFinishing){
                                Toast.makeText(this, R.string.hud_error_connection, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }).run()
            }
        }
        return true
    }

    private class ViewHolder {
        internal var image: ImageView? = null
        internal var select: ImageView? = null
        internal var lock: ImageView? = null
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

                holder.image  = view.graphload_iv_image
                holder.select = view.graphload_iv_select
                holder.lock   = view.graphload_iv_lock

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
            if (data.isLocked){
                holder.lock?.visibility = View.VISIBLE
            }else{
                holder.lock?.visibility = View.INVISIBLE
            }
            return view!!
        }
    }
}
