package unicot.app.fractalvisualizer.core

import com.google.firebase.firestore.FirebaseFirestore
import unicot.app.fractalvisualizer.graph.ColorPattern
import java.lang.Exception

/**
 * xmlファイルに環境設定値およびグラフ情報を書き出すクラス<br></br>
 * 【主な機能】<br></br>
 * - グラフ情報の書き出し<br></br>
 * <br></br>
 */
object DGDataLoad : DGDataInfo() {
    fun load(id: String, listener: ((Boolean) -> Unit)) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val dgraph = DGCore.graph    // グラフ
        val sysData: DGSystemData = DGCore.systemData

        dgraph.clear()

        db.collection(COLLECTION_SESSION_DETAIL).document(id).get().addOnCompleteListener {
            task ->
            val sessionDetail = task.result
            if(!task.isSuccessful || sessionDetail == null){
                listener.invoke(false)
                return@addOnCompleteListener
            }
            try {
                sysData.framerate = (sessionDetail[DETAIL_VIEW_FPS] as Long).toInt()
                sysData.povFrame = (sessionDetail[DETAIL_VIEW_POV] as Long).toInt()
                sysData.graphVersion = sessionDetail[DETAIL_VERSION] as String
                if (sessionDetail["graph_list"] is ArrayList<*>) {
                    val graphDetails = (sessionDetail["graph_list"] as ArrayList<HashMap<String, Any>>)
                    graphDetails.mapIndexed { n, graphDetail ->
                        val kind = graphDetail[GRAPH_KIND] as String

                        if (!DGCommon.copyGraph(DGCommon.getKind(kind), false))
                            return@addOnCompleteListener

                        val gd = dgraph[n]
                        val gi = gd.info

                        gi.complexity = numberToInt(graphDetail[GRAPH_COMPLEXITY])
                        gi.pos.x = doubleToFloat(graphDetail[GRAPH_X])
                        gi.pos.y = doubleToFloat(graphDetail[GRAPH_Y])
                        gi.size.width = doubleToFloat(graphDetail[GRAPH_WIDTH])
                        gi.size.height = doubleToFloat(graphDetail[GRAPH_HEIGHT])
                        gi.angle = doubleToFloat(graphDetail[GRAPH_ANGLE])
                        gi.rot_speed = doubleToFloat(graphDetail[GRAPH_ROTATE])
                        gi.mutation.angle = doubleToFloat(graphDetail[GRAPH_MUTATION_ANGLE])
                        gi.mutation.size = doubleToFloat(graphDetail[GRAPH_MUTATION_SIZE])
                        gi.randomize.angle = doubleToFloat(graphDetail[GRAPH_RANDOMIZE_ANGLE])
                        gi.randomize.size = doubleToFloat(graphDetail[GRAPH_RANDOMIZE_SIZE])
                        if (gi.graph_kind == DGCommon.LEAF) {
                            (gd as unicot.app.fractalvisualizer.graph.Leaf)
                                    .setBranch(numberToInt(graphDetail[GRAPH_LEAF_BRANCH]))
                        }
                        if (gi.graph_kind == DGCommon.SIERPINSKI_GASKET) {
                            (gd as unicot.app.fractalvisualizer.graph.SGasket).skewAngle =
                                    doubleToFloat(graphDetail[GRAPH_SGASKET_SKEW] as Double)
                        }

                        gi.cp.setColMode(graphDetail[COLOR_MODE] as String)
                        gi.cp.color = numberToInt(graphDetail[COLOR_COLOR])
                        gi.cp.shiftSpeed = numberToInt(graphDetail[COLOR_SHIFT])
                        gi.cp.setTrans(numberToInt(graphDetail[COLOR_TRANS]))

                        val drawKind = graphDetail[DRAW_KIND] as String
                        val drawThickness = doubleToFloat(graphDetail[DRAW_THICKNESS])
                        val drawColorEach = graphDetail[DRAW_COLOR_EACH] as Boolean
                        val drawHistory = numberToInt(graphDetail[DRAW_HISTORY])
                        val drawCurOrder = numberToInt(graphDetail[DRAW_CUR_ORDER])
                        val drawBrush = numberToInt(graphDetail[DRAW_BRUSH])
                        gi.setDrawSettings(drawKind, drawThickness, drawColorEach, drawHistory,
                                drawCurOrder, drawBrush)
                        gd.setInfo(gi, true)
                    }
                    listener.invoke(true)
                }
            }catch(e: Exception){
                e.printStackTrace()
                listener.invoke(false)
            }
        }
    }

    private fun numberToInt(value: Any?, default: Int = 0): Int{
        return (value as? Long)?.toInt() ?: default
    }

    private fun doubleToFloat(value: Any?, default: Float = 0.0f): Float{
        return (value as? Double)?.toFloat() ?: default
    }
}