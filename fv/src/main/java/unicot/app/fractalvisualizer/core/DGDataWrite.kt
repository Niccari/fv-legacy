package unicot.app.fractalvisualizer.core

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.HashMap

/**
 * xmlファイルに環境設定値およびグラフ情報を書き出すクラス<br></br>
 * 【主な機能】<br></br>
 * - グラフ情報の書き出し<br></br>
 * <br></br>
 */
object DGDataWrite : DGDataInfo() {
    // ////////////////////////////////////////////////////////////
    // テキストを追加する
    // データをXML用にシリアライズ(逆順に入れる)
    fun save(file: File) {
        val date = Date()
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("/thumb/${date.time}.jpg")
        val uploadTask = ref.putStream(FileInputStream(file))

        try {
            uploadTask.continueWithTask(
                    Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { exc ->
                                throw exc
                            }
                        }
                        return@Continuation ref.downloadUrl
                    }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result ?: return@addOnCompleteListener

                    saveGraphs(downloadUri.toString(), date)
                }
            }
        }finally {
            file.delete()
        }
    }

    private fun saveGraphs(thumbUrl: String, date: Date){
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val dgraph = DGCore.graph    // グラフ
        val docName = "${date.time}"

        val graphList = ArrayList<HashMap<String, Any>>()
        for(iv in dgraph.withIndex()){
            val g = iv.value
            val gi = g.info

            val graph = HashMap<String, Any>()

            // Graph
            graph[GRAPH_KIND] = DGCommon.getGraphKindString(gi.graph_kind)
            graph[GRAPH_COMPLEXITY] = gi.complexity
            graph[GRAPH_X] = gi.pos.x
            graph[GRAPH_Y] = gi.pos.y
            graph[GRAPH_WIDTH]  = gi.size.width
            graph[GRAPH_HEIGHT] = gi.size.height
            graph[GRAPH_ROTATE] = gi.rot_speed
            graph[GRAPH_ANGLE]  = gi.angle
            when {
                gi.graph_kind == DGCommon.LEAF -> {
                    graph[GRAPH_LEAF_BRANCH] = (g as Leaf).getBranch()
                }
                gi.graph_kind == DGCommon.SIERPINSKI_GASKET -> {
                    graph[GRAPH_SGASKET_SKEW] = (g as SGasket).skewAngle
                }
            }
            if(gi.is_recursive) {
                graph[GRAPH_MUTATION_SIZE]   = gi.mutation.size
                graph[GRAPH_MUTATION_ANGLE]  = gi.mutation.angle
                graph[GRAPH_RANDOMIZE_SIZE]  = gi.randomize.size
                graph[GRAPH_RANDOMIZE_ANGLE] = gi.randomize.angle
            }

            // draw
            graph[DRAW_KIND]       = if (gi.draw_kind == Graph.DRAW_ALL) DRAW_KIND_ALL else DRAW_KIND_EACH
            graph[DRAW_THICKNESS]  = gi.mLineThickness
            graph[DRAW_COLOR_EACH] = gi.mIsColorEach
            graph[DRAW_HISTORY]    = gi.mEachLineHistory
            graph[DRAW_CUR_ORDER]  = gi.mCurrentDrawOrder
            graph[DRAW_BRUSH]      = gi.mBrushType

            graph[COLOR_MODE]  = gi.cp.colModeInString
            graph[COLOR_COLOR] = gi.cp.color
            graph[COLOR_SHIFT] = gi.cp.shiftSpeed
            graph[COLOR_TRANS] = gi.cp.getTrans()

            graphList.add(graph)
        }
        val sessionDetail = HashMap<String, Any>()
        val sysData = DGCore.systemData
        sessionDetail[DETAIL_VIEW_FPS]  = sysData.framerate
        sessionDetail[DETAIL_VIEW_POV]  = sysData.povFrame
        sessionDetail[DETAIL_VERSION]   = GRAPH_VERSION
        sessionDetail[DETAIL_GRAPH_LIST] = graphList
        db.collection(COLLECTION_SESSION_DETAIL).document(docName).set(sessionDetail)

        val sessionInfo = HashMap<String, Any>()
        sessionInfo[INFO_THUMB_URL] = thumbUrl
        sessionInfo[INFO_DATE] = Timestamp(date)
        val docRef = db.collection(COLLECTION_SESSION_INFO).document(docName)
        docRef.set(sessionInfo)
    }
}
