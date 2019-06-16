package unicot.app.fractalvisualizer.core

import android.graphics.PointF
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import unicot.app.fractalvisualizer.graph.ColorPattern
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.GraphInfo
import unicot.app.fractalvisualizer.graph.SGasket
import unicot.app.fractalvisualizer.model.DimensionF
import unicot.app.fractalvisualizer.model.GraphDisplacement
import java.io.IOException
import java.lang.NumberFormatException

/**
 * xmlファイルから環境設定値およびグラフ情報を読み出す
 */
class DGDataLoad : DGDataInfo() {
    companion object {
        private var sysData: DGSystemData = DGCore.systemData
        /**
         * グラフ設定を指定されたXMLファイルを読みだす
         */
        fun load(parser: XmlPullParser) {
            try {

                /* グラフ一覧 */
                val dgraph = DGCore.graph

                dgraph.clear() /* 現在生成されている全グラフを廃棄 */
                var eventType = parser.eventType

                var gd: Graph? = null
                var gi: GraphInfo? = null
                var n = 0
                var isCopied = false

                var tag: String
                var attr: String
                var version: String

                // getName : タグ名, getEventType : 種類, getText : タグ値
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    tag = if(parser.name != null){ parser.name } else {""}
                    // タグ開始かどうか(=タグかどうか)
                    if (eventType == XmlPullParser.START_TAG) {

                        // systemタグ : ToDo : 後で、GUIオフ時間、インディケータOn/Offも追加すること
                        if (tag == TAG_SYSTEM) {
                            getSystemTag(parser)
                        }

                        if (tag == TAG_GRAPHLIST) {
                            var sx = ""
                            for (i in 0 until parser.attributeCount) {
                                attr = parser.getAttributeName(i)
                                if (attr == ATTR_GRAPHLIST_VERSION)
                                    sx = parser.getAttributeValue(i)
                            }
                            version = sx
                            sysData.graphVersion = version
                        }


                        // graphタグ
                        if (tag == TAG_GRAPH) {
                            // this.getGraphTag(parser, n);
                            for (i in 0 until parser.attributeCount) {
                                attr = parser.getAttributeName(i)
                                if (attr == ATTR_GRAPH_KIND) {
                                    if (DGCommon.copyGraph(DGCommon.getKind(parser.getAttributeValue(i)), false)) { // グラフ生成がうまくできたらOK
                                        gd = dgraph[n++]
                                        gi = gd.info
                                        isCopied = true
                                    } else
                                        isCopied = false
                                }
                                if (attr == ATTR_GRAPH_COMPLEXITY) {
                                    gi?.complexity = getComplexity(parser.getAttributeValue(i))
                                }
                                if (gi?.graph_kind == DGCommon.LEAF) {
                                    if (attr == ATTR_GRAPH_LEAF_BRANCH) {
                                        (dgraph[n - 1] as unicot.app.fractalvisualizer.graph.Leaf).setBranch(Integer.valueOf(parser.getAttributeValue(i)))
                                    }
                                }
                                if (gi?.graph_kind == DGCommon.SIERPINSKI_GASKET) {
                                    if (attr == ATTR_GRAPH_SGASKET_SKEW) {
                                        (dgraph[n - 1] as SGasket).skewAngle = java.lang.Float.valueOf(parser
                                                .getAttributeValue(i))
                                    }
                                }
                            }
                        }

                        // 不正なグラフデータがあれば(グラフを正しく生成できない場合)飛ばす
                        if (isCopied) {
                            // positionタグ(位置)
                            if (tag == TAG_GRAPH_POSITION) {
                                var sx = ""
                                var sy = ""
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_POSITION_X)
                                        sx = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_POSITION_Y)
                                        sy = parser.getAttributeValue(i)
                                }
                                gi?.pos = getPosition(sx, sy)
                            }

                            // sizeタグ(寸法)
                            if (tag == TAG_GRAPH_SIZE) {
                                var sw = ""
                                var sh = ""
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_SIZE_WIDTH)
                                        sw = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_SIZE_HEIGHT)
                                        sh = parser.getAttributeValue(i)
                                }
                                gi?.size = getSize(sw, sh)
                            }

                            // angle | rot_speedタグ
                            if (tag == TAG_GRAPH_ANGLE || tag == TAG_GRAPH_ROTATE) {
                                var sv = ""
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_ANGLE_VALUE)
                                        sv = parser.getAttributeValue(i)
                                }
                                if (tag == TAG_GRAPH_ANGLE)
                                    gi?.angle = if (sv.matches("".toRegex())) 0.0f else java.lang.Float.parseFloat(sv)
                                if (tag == TAG_GRAPH_ROTATE)
                                    gi?.rot_speed = if (sv.matches("".toRegex())) 0.0f else java.lang.Float.parseFloat(sv)
                            }

                            // mutation | randomizeタグ
                            if (tag == TAG_GRAPH_MUTATION || tag == TAG_GRAPH_RANDOMIZE) {
                                var sw = ""
                                var sh = ""
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_MUTATION_SIZE)
                                        sw = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_MUTATION_ANGLE)
                                        sh = parser.getAttributeValue(i)
                                }
                                if (tag == TAG_GRAPH_MUTATION)
                                    gi?.mutation = getGraphDisplacement(sw, sh)
                                if (tag == TAG_GRAPH_RANDOMIZE)
                                    gi?.randomize = getGraphDisplacement(sw, sh)
                            }

                            // colorタグ(色の遷移パターン)
                            if (tag == TAG_GRAPH_COLOR) {
                                var sx = ""
                                var sy = ""
                                var sz = ""
                                var sw = ""
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_COLOR_MODE)
                                        sx = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_COLOR_COLOR)
                                        sy = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_COLOR_SHIFT)
                                        sz = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_COLOR_TRANS)
                                        sw = parser.getAttributeValue(i)

                                }
                                gi?.cp = getColorPattern(sx, sy, sz, sw)
                            }

                            // drawタグ
                            if (tag == TAG_GRAPH_DRAW) {
                                var kind = ""
                                var thickness = -1.0f
                                var colorEach = false
                                var history = -1
                                var corder = -1
                                var brush = -1
                                for (i in 0 until parser.attributeCount) {
                                    attr = parser.getAttributeName(i)
                                    if (attr == ATTR_GRAPH_DRAW_KIND)
                                        kind = parser.getAttributeValue(i)
                                    if (attr == ATTR_GRAPH_DRAW_THICKNESS)
                                        thickness = java.lang.Float.valueOf(parser.getAttributeValue(i))
                                    if (attr == ATTR_GRAPH_DRAW_COLOREACH)
                                        colorEach = java.lang.Boolean.valueOf(parser.getAttributeValue(i))
                                    if (attr == ATTR_GRAPH_DRAW_HISTORY)
                                        history = Integer.valueOf(parser.getAttributeValue(i))
                                    if (attr == ATTR_GRAPH_DRAW_CURORDER)
                                        corder = Integer.valueOf(parser.getAttributeValue(i))
                                    if (attr == ATTR_GRAPH_DRAW_BRUSH)
                                        brush = Integer.valueOf(parser.getAttributeValue(i))
                                }
                                gi?.setDrawSettings(kind, thickness, colorEach, history, corder, brush)
                            }
                        }
                    }
                    if (eventType == XmlPullParser.END_TAG) {
                        if (tag == TAG_GRAPH) {
                            if(gi != null)
                                gd?.setInfo(gi, true)
                        }
                        if (tag == TAG_GRAPHLIST) {
                            return
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: NullPointerException) {
                //             Log.e(TAG, "Maybe an unexist graph...");
                System.exit(-1)
            } catch (e: XmlPullParserException) {
                //             Log.e(TAG, "An invalid graph data. defaults.");
                e.printStackTrace()
            } catch (e: IOException) {
                //             Log.e(TAG, "Graph data file was not found. defaults.");
                e.printStackTrace()
            }

        }

        /* Systemタグをチェック */
        private fun getSystemTag(parser: XmlPullParser): Boolean {
            var framerate = ""
            var povFrame = ""
            var attr: String
            for (i in 0 until parser.attributeCount) {
                attr = parser.getAttributeName(i)
                if (attr == ATTR_SYSTEM_FRAMERATE)
                    framerate = parser.getAttributeValue(i)

                if (attr == ATTR_SYSTEM_POV_FRAME)
                    povFrame = parser.getAttributeValue(i)
            }
            try{
                sysData.povFrame = povFrame.toInt()
            }catch(e: NumberFormatException){
                sysData.povFrame = 0
            }
            try {
                sysData.framerate = framerate.toInt()
            }catch(e: NumberFormatException){
                sysData.framerate = 60
            }
            return true
        }

        // 以下、Stringを各データに合わせて型変換するメソッド
        // For position
        private fun getPosition(sx: String, sy: String): PointF {
            return if (sx.matches("".toRegex()) || sy.matches("".toRegex())) PointF() else PointF(java.lang.Float.parseFloat(sx), java.lang.Float.parseFloat(sy))

        }

        // For size
        private fun getSize(sw: String, sh: String): DimensionF {
            return if (sw.matches("".toRegex()) || sh.matches("".toRegex())) DimensionF() else DimensionF(java.lang.Float.parseFloat(sw), java.lang.Float.parseFloat(sh))
        }

        // For ColorPattern(cp)
        private fun getColorPattern(sx: String, sy: String, sz: String, sw: String): ColorPattern {
            val cpNew = ColorPattern()

            if (!sx.matches("".toRegex())) {
                cpNew.setColMode(sx)
            }
            if (!sy.matches("".toRegex())) {
                cpNew.color = java.lang.Long.parseLong(sy, 16).toInt()

            }
            if (!sz.matches("".toRegex())) {
                cpNew.shiftSpeed = Integer.parseInt(sz)
            }

            if (!sw.matches("".toRegex())) {
                cpNew.setTrans(Integer.parseInt(sw))
            }
            return cpNew
        }

        // For complexity
        private fun getComplexity(e_str: String): Int {
            return if (e_str.matches("".toRegex())) GraphInfo.COMPLEXITY_INIT else Integer.parseInt(e_str)
        }

        // For mutation, randomize
        private fun getGraphDisplacement(sw: String, sh: String): GraphDisplacement {
            return if (sw.matches("".toRegex()) || sh.matches("".toRegex())) GraphDisplacement() else GraphDisplacement(java.lang.Float.parseFloat(sw), java.lang.Float.parseFloat(sh))
        }
    }
}
