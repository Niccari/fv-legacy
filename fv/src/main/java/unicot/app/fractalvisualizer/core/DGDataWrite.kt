package unicot.app.fractalvisualizer.core

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
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
    fun save() {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        val dgraph = DGCore.graph    // グラフ

        val graphList = ArrayList<DocumentReference>()
        for(g in dgraph){
            val gi = g.info

            val graph = HashMap<String, Any>()

            // Graph
            graph[GRAPH_KIND] = DGCommon.getGraphKindString(gi.graph_kind)
            graph[ATTR_GRAPH_COMPLEXITY] = gi.complexity
            when {
                gi.graph_kind == DGCommon.LEAF -> {
                    graph[GRAPH_LEAF_BRANCH] = (g as Leaf).getBranch()
                }
                gi.graph_kind == DGCommon.SIERPINSKI_GASKET -> {
                    graph[GRAPH_SGASKET_SKEW] = (g as SGasket).skewAngle
                }
            }
            graph[GRAPH_X] = gi.pos.x
            graph[GRAPH_Y] = gi.pos.y
            graph[GRAPH_WIDTH]  = gi.size.width
            graph[GRAPH_HEIGHT] = gi.size.height
            graph[GRAPH_ROTATE] = gi.rot_speed
            graph[GRAPH_ANGLE]  = gi.angle
            graph[GRAPH_MUTATION_SIZE]  = gi.mutation.size
            graph[GRAPH_MUTATION_ANGLE] = gi.mutation.angle
            graph[GRAPH_RANDOMIZE_SIZE]  = gi.randomize.size
            graph[GRAPH_RANDOMIZE_ANGLE] = gi.randomize.angle
            graph[GRAPH_HEIGHT] = gi.size.height
            graph[GRAPH_ROTATE] = gi.rot_speed
            graph[GRAPH_ANGLE]  = gi.angle

            // draw
            graph[GRAPH_DRAW_KIND]      = if (gi.draw_kind == Graph.DRAW_ALL) GRAPH_DRAW_KIND_ALL else GRAPH_DRAW_KIND_EACH
            graph[GRAPH_DRAW_THICKNESS] = gi.mLineThickness
            graph[GRAPH_DRAW_COLOREACH] = gi.mIsColorEach
            graph[GRAPH_DRAW_HISTORY]   = gi.mEachLineHistory
            graph[GRAPH_DRAW_CURORDER]  = gi.mCurrentDrawOrder
            graph[GRAPH_DRAW_BRUSH]     = gi.mBrushType

            graph[GRAPH_COLOR_MODE]  = gi.cp.colModeInString
            graph[GRAPH_COLOR_COLOR] = gi.cp.color
            graph[GRAPH_COLOR_SHIFT] = gi.cp.shiftSpeed
            graph[GRAPH_COLOR_TRANS] = gi.cp.getTrans()

            val docRef = db.collection(COLLECTION_GRAPH).document()
            docRef.set(graph)

            graphList.add(docRef)
        }

        val session = HashMap<String, Any>()
        val sysData = DGCore.systemData
        session[SESSION_VIEW_FPS] = sysData.framerate
        session[SESSION_VIEW_POV] = sysData.povFrame
        session[SESSION_VERSION] = GRAPH_VERSION
        session[SESSION_THUMB_URL] = ""   // TBD
        session[SESSION_DATE] = Timestamp(Date())
        session[SESSION_GRAPH_LIST] = graphList.toList()
        val docRef = db.collection(COLLECTION_SESSION).document()
        docRef.set(session)
    }
}
