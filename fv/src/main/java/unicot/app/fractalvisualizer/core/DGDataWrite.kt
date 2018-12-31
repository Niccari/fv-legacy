package unicot.app.fractalvisualizer.core

import android.util.Xml
import unicot.app.fractalvisualizer.graph.Graph
import unicot.app.fractalvisualizer.graph.Leaf
import unicot.app.fractalvisualizer.graph.SGasket
import java.io.*
import java.util.*

/**
 * xmlファイルに環境設定値およびグラフ情報を書き出すクラス<br></br>
 * 【主な機能】<br></br>
 * - グラフ情報の書き出し<br></br>
 * <br></br>
 */
class DGDataWrite constructor(private val root_dir: String, private val version: String) : DGDataInfo() {
    private var mItemList: ArrayList<XmlItem> = ArrayList(0)   // 保存するタグリスト
    private var basename: String = ""           // 保存ファイルの名称

    private var is_file_specified = false

    // タグおよびその属性を扱うための内部クラス。
    private inner class XmlItem internal constructor() {
        internal var stat: Int = 0            // タグ種類
        var tag: String? = null          // タグ名
        internal var attr_name: Array<String>  // 属性名
        internal var attr_value: Array<String> // 属性値
        internal var mNumOfAttr: Int = 0  // 属性の総数

        init {
            attr_name = Array(NUM_ATTR) { "" }
            attr_value = Array(NUM_ATTR) { "" }

            set(STAT_NORMAL, null)
        }

        // タグのみを設定
        operator fun set(stat0: Int, tag0: String?) {
            stat = stat0
            tag = tag0
            mNumOfAttr = 0
        }

        // タグおよびその属性を設定
        operator fun set(stat0: Int, tag0: String, attr_name0: Array<String>,
                         attr_value0: Array<String>, numOfAttr: Int) {
            stat = stat0
            tag = tag0
            mNumOfAttr = numOfAttr

            for (i in 0 until mNumOfAttr) {
                attr_name[i] = attr_name0[i]
                attr_value[i] = attr_value0[i]
            }
        }
    }

    /**
     * 指定されたグラフ作成者の情報を指定されたファイル名に出力する
     * @param name XMLファイルのファイル名(拡張子除く)
     * @return 書き込み結果(OK:true, NG:false)
     */
    fun writeXml(name: String): Boolean {
        basename = "$name.xml"
        is_file_specified = true
        return writeXml()
    }

    // ////////////////////////////////////////////////////////////
    // テキストを追加する
    // データをXML用にシリアライズ(逆順に入れる)
    private fun setData() {
        var item: XmlItem

        val attr_name = Array(NUM_ATTR){ "" }
        val attr_value = Array(NUM_ATTR){ "" }
        var attr_num = 0

        val dgraph = DGCore.graph    // グラフ
        val sys_data = DGCore.systemData    // 環境設定値

        // システム情報(環境設定値)
        // systemタグ
        item = XmlItem()
        attr_name[0] = DGDataInfo.ATTR_SYSTEM_FRAMERATE
        attr_value[0] = Integer.toString(sys_data.framerate)
        attr_name[1] = DGDataInfo.ATTR_SYSTEM_MEMORY_RATE
        attr_value[1] = Integer.toString(sys_data.memoryUsage)
        attr_name[2] = DGDataInfo.ATTR_SYSTEM_LOAD_RATE
        attr_value[2] = Integer.toString(sys_data.loadUsage)
        attr_name[3] = DGDataInfo.ATTR_SYSTEM_IS_INDICATOR
        attr_value[3] = java.lang.Boolean.toString(sys_data.isIndicator)
        attr_name[4] = DGDataInfo.ATTR_SYSTEM_GUI_DISMISS_TIME
        attr_value[4] = Integer.toString(sys_data.dismissTime)
        attr_name[5] = DGDataInfo.ATTR_SYSTEM_VIEW_ALPHA
        attr_value[5] = Integer.toString(sys_data.viewAlpha)
        attr_name[6] = DGDataInfo.ATTR_SYSTEM_POV_FRAME
        attr_value[6] = Integer.toString(sys_data.povFrame)
        attr_num = 7


        item.set(STAT_START, DGDataInfo.TAG_SYSTEM, attr_name, attr_value, attr_num)
        mItemList.add(item)

        // ここからグラフ情報
        // graphlistタグ 開始
        item = XmlItem()
        attr_name[0] = DGDataInfo.ATTR_GRAPHLIST_VERSION
        attr_value[0] = version
        attr_num = 1

        item.set(STAT_START, DGDataInfo.TAG_GRAPHLIST, attr_name, attr_value, attr_num)
        mItemList.add(item)

        for (n in dgraph.indices) {

            val gi = dgraph[n].info

            // graphタグ 開始
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_KIND
            attr_value[0] = DGCommon.getGraphKindString(gi.graph_kind)
            attr_name[1] = DGDataInfo.ATTR_GRAPH_COMPLEXITY
            attr_value[1] = Integer.toString(gi.complexity)
            if (gi.graph_kind == DGCommon.LEAF) {
                attr_name[2] = DGDataInfo.ATTR_GRAPH_LEAF_BRANCH
                attr_value[2] = Integer.toString((dgraph[n] as Leaf).getBranch())
                attr_num = 3

            } else if (gi.graph_kind == DGCommon.SIERPINSKI_GASKET) {
                attr_name[2] = DGDataInfo.ATTR_GRAPH_SGASKET_SKEW
                attr_value[2] = java.lang.Float.toString((dgraph[n] as SGasket).skewAngle)
                attr_num = 3
            } else {
                attr_num = 2
            }
            item.set(STAT_START, DGDataInfo.TAG_GRAPH, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // 以下のタグはgraphタグの子要素
            // positionタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_POSITION_X
            attr_value[0] = java.lang.Float.toString(gi.pos.x)
            attr_name[1] = DGDataInfo.ATTR_GRAPH_POSITION_Y
            attr_value[1] = java.lang.Float.toString(gi.pos.y)
            attr_num = 2

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_POSITION, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // sizeタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_SIZE_WIDTH
            attr_value[0] = java.lang.Float.toString(gi.size.width)
            attr_name[1] = DGDataInfo.ATTR_GRAPH_SIZE_HEIGHT
            attr_value[1] = java.lang.Float.toString(gi.size.height)
            attr_num = 2

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_SIZE, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // rotateタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_ROTATE_VALUE
            attr_value[0] = java.lang.Float.toString(gi.rot_speed)
            attr_num = 1

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_ROTATE, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // angleタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_ANGLE_VALUE
            attr_value[0] = java.lang.Float.toString(gi.angle)
            attr_num = 1

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_ANGLE, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // randomizeタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_RANDOMIZE_SIZE
            attr_value[0] = java.lang.Float.toString(gi.randomize.size)
            attr_name[1] = DGDataInfo.ATTR_GRAPH_RANDOMIZE_ANGLE
            attr_value[1] = java.lang.Float.toString(gi.randomize.angle)
            attr_num = 2

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_RANDOMIZE, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // mutationタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_MUTATION_SIZE
            attr_value[0] = java.lang.Float.toString(gi.mutation.size)
            attr_name[1] = DGDataInfo.ATTR_GRAPH_MUTATION_ANGLE
            attr_value[1] = java.lang.Float.toString(gi.mutation.angle)
            attr_num = 2


            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_MUTATION, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // drawタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_DRAW_KIND
            attr_value[0] = if (gi.draw_kind == Graph.DRAW_ALL) DGDataInfo.VALUE_GRAPH_DRAW_KIND_ALL else DGDataInfo.VALUE_GRAPH_DRAW_KIND_EACH
            attr_name[1] = DGDataInfo.ATTR_GRAPH_DRAW_THICKNESS
            attr_value[1] = java.lang.Float.toString(gi.mLineThickness)
            attr_name[2] = DGDataInfo.ATTR_GRAPH_DRAW_ANTIALIAS
            attr_value[2] = java.lang.Boolean.toString(gi.mIsAntiAlias)
            attr_name[3] = DGDataInfo.ATTR_GRAPH_DRAW_COLOREACH
            attr_value[3] = java.lang.Boolean.toString(gi.mIsColorEach)
            attr_name[4] = DGDataInfo.ATTR_GRAPH_DRAW_HISTORY
            attr_value[4] = Integer.toString(gi.mEachLineHistory)
            attr_name[5] = DGDataInfo.ATTR_GRAPH_DRAW_CURORDER
            attr_value[5] = Integer.toString(gi.mCurrentDrawOrder)
            attr_name[6] = DGDataInfo.ATTR_GRAPH_DRAW_BRUSH
            attr_value[6] = Integer.toString(gi.mBrushType)
            attr_num = 7

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_DRAW, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // colorタグ
            item = XmlItem()
            attr_name[0] = DGDataInfo.ATTR_GRAPH_COLOR_MODE
            attr_value[0] = gi.cp.colModeInString
            attr_name[1] = DGDataInfo.ATTR_GRAPH_COLOR_COLOR
            attr_value[1] = Integer.toHexString(gi.cp.color)
            attr_name[2] = DGDataInfo.ATTR_GRAPH_COLOR_SHIFT
            attr_value[2] = Integer.toString(gi.cp.shiftSpeed)
            attr_name[3] = DGDataInfo.ATTR_GRAPH_COLOR_TRANS
            attr_value[3] = Integer.toString(gi.cp.getTrans())
            attr_num = 4

            item.set(STAT_NORMAL, DGDataInfo.TAG_GRAPH_COLOR, attr_name, attr_value, attr_num)
            mItemList.add(item)

            // graphタグ 終了
            item = XmlItem()
            item[STAT_END] = DGDataInfo.TAG_GRAPH
            mItemList.add(item)

        }

        item = XmlItem()
        item[STAT_END] = DGDataInfo.TAG_GRAPHLIST
        mItemList.add(item)
    }

    // ファイルに書き込む
    private fun writeXml(): Boolean {
        try {
            this.setData()

            if (!is_file_specified) {
                basename = DGCommon.currentDateString + ".xml"
                is_file_specified = false
            }
            // ファイル出力ストリームを作る
            val f = File(root_dir + "/")

            if (!f.exists()) {
                f.mkdirs()
            }

            val bufferWriter = BufferedWriter(
                    OutputStreamWriter(FileOutputStream("$root_dir/$basename", false)))
            val xmlse = Xml.newSerializer()
            xmlse.setOutput(bufferWriter)

            // ドキュメントのスタート
            xmlse.startDocument(null, java.lang.Boolean.TRUE)
            xmlse.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true)

            // ここから書き出し
            for (curr in mItemList) {
                if(curr.tag == null) continue
                if (curr.stat != STAT_END)
                    xmlse.startTag(null, curr.tag)

                for (n in 0 until curr.mNumOfAttr)
                    xmlse.attribute(null, curr.attr_name[n], curr.attr_value[n])

                if (curr.stat != STAT_START)
                    xmlse.endTag(null, curr.tag)
            }

            // タグの終了と書き出し
            xmlse.endDocument()
            xmlse.flush()

            mItemList.clear()
            return true
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

    }

    companion object {


        // タグ種類
        private val STAT_NORMAL = 0 // 一過性タグ
        private val STAT_START = 1  // タグ初め
        private val STAT_END = 2    // タグ終了

        // 1つのタグ辺りの属性の最大数
        private val NUM_ATTR = 8
    }
}
