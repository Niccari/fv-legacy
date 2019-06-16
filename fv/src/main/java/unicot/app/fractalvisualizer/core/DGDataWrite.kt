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

    private var isFileSpecified = false

    // タグおよびその属性を扱うための内部クラス。
    private inner class XmlItem internal constructor() {
        internal var stat: Int = 0            // タグ種類
        var tag: String? = null          // タグ名
        internal var attrName: Array<String> = Array(NUM_ATTR) { "" }  // 属性名
        internal var attrValue: Array<String> = Array(NUM_ATTR) { "" } // 属性値
        internal var mNumOfAttr: Int = 0  // 属性の総数

        init {
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
                attrName[i] = attr_name0[i]
                attrValue[i] = attr_value0[i]
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
        isFileSpecified = true
        return writeXml()
    }

    // ////////////////////////////////////////////////////////////
    // テキストを追加する
    // データをXML用にシリアライズ(逆順に入れる)
    private fun setData() {
        var item: XmlItem

        val attrName = Array(NUM_ATTR){ "" }
        val attrValue = Array(NUM_ATTR){ "" }
        var attrNum: Int

        val dgraph = DGCore.graph    // グラフ
        val sysData = DGCore.systemData    // 環境設定値

        // システム情報(環境設定値)
        // systemタグ
        item = XmlItem()
        attrName[0] = ATTR_SYSTEM_FRAMERATE
        attrValue[0] = Integer.toString(sysData.framerate)
        attrName[1] = ATTR_SYSTEM_POV_FRAME
        attrValue[1] = Integer.toString(sysData.povFrame)
        attrNum = 2

        item[STAT_START, TAG_SYSTEM, attrName, attrValue] = attrNum
        mItemList.add(item)

        // ここからグラフ情報
        // graphlistタグ 開始
        item = XmlItem()
        attrName[0] = ATTR_GRAPHLIST_VERSION
        attrValue[0] = version
        attrNum = 1

        item[STAT_START, TAG_GRAPHLIST, attrName, attrValue] = attrNum
        mItemList.add(item)

        for (n in dgraph.indices) {

            val gi = dgraph[n].info

            // graphタグ 開始
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_KIND
            attrValue[0] = DGCommon.getGraphKindString(gi.graph_kind)
            attrName[1] = ATTR_GRAPH_COMPLEXITY
            attrValue[1] = Integer.toString(gi.complexity)
            when {
                gi.graph_kind == DGCommon.LEAF -> {
                    attrName[2] = ATTR_GRAPH_LEAF_BRANCH
                    attrValue[2] = Integer.toString((dgraph[n] as Leaf).getBranch())
                    attrNum = 3
                }
                gi.graph_kind == DGCommon.SIERPINSKI_GASKET -> {
                    attrName[2] = ATTR_GRAPH_SGASKET_SKEW
                    attrValue[2] = java.lang.Float.toString((dgraph[n] as SGasket).skewAngle)
                    attrNum = 3
                }
                else -> attrNum = 2
            }
            item[STAT_START, TAG_GRAPH, attrName, attrValue] = attrNum
            mItemList.add(item)

            // 以下のタグはgraphタグの子要素
            // positionタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_POSITION_X
            attrValue[0] = java.lang.Float.toString(gi.pos.x)
            attrName[1] = ATTR_GRAPH_POSITION_Y
            attrValue[1] = java.lang.Float.toString(gi.pos.y)
            attrNum = 2

            item[STAT_NORMAL, TAG_GRAPH_POSITION, attrName, attrValue] = attrNum
            mItemList.add(item)

            // sizeタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_SIZE_WIDTH
            attrValue[0] = java.lang.Float.toString(gi.size.width)
            attrName[1] = ATTR_GRAPH_SIZE_HEIGHT
            attrValue[1] = java.lang.Float.toString(gi.size.height)
            attrNum = 2

            item[STAT_NORMAL, TAG_GRAPH_SIZE, attrName, attrValue] = attrNum
            mItemList.add(item)

            // rotateタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_ROTATE_VALUE
            attrValue[0] = java.lang.Float.toString(gi.rot_speed)
            attrNum = 1

            item[STAT_NORMAL, TAG_GRAPH_ROTATE, attrName, attrValue] = attrNum
            mItemList.add(item)

            // angleタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_ANGLE_VALUE
            attrValue[0] = java.lang.Float.toString(gi.angle)
            attrNum = 1

            item[STAT_NORMAL, TAG_GRAPH_ANGLE, attrName, attrValue] = attrNum
            mItemList.add(item)

            // randomizeタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_RANDOMIZE_SIZE
            attrValue[0] = java.lang.Float.toString(gi.randomize.size)
            attrName[1] = ATTR_GRAPH_RANDOMIZE_ANGLE
            attrValue[1] = java.lang.Float.toString(gi.randomize.angle)
            attrNum = 2

            item[STAT_NORMAL, TAG_GRAPH_RANDOMIZE, attrName, attrValue] = attrNum
            mItemList.add(item)

            // mutationタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_MUTATION_SIZE
            attrValue[0] = java.lang.Float.toString(gi.mutation.size)
            attrName[1] = ATTR_GRAPH_MUTATION_ANGLE
            attrValue[1] = java.lang.Float.toString(gi.mutation.angle)
            attrNum = 2


            item[STAT_NORMAL, TAG_GRAPH_MUTATION, attrName, attrValue] = attrNum
            mItemList.add(item)

            // drawタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_DRAW_KIND
            attrValue[0] = if (gi.draw_kind == Graph.DRAW_ALL) VALUE_GRAPH_DRAW_KIND_ALL else VALUE_GRAPH_DRAW_KIND_EACH
            attrName[1] = ATTR_GRAPH_DRAW_THICKNESS
            attrValue[1] = java.lang.Float.toString(gi.mLineThickness)
            attrName[2] = ATTR_GRAPH_DRAW_COLOREACH
            attrValue[2] = java.lang.Boolean.toString(gi.mIsColorEach)
            attrName[3] = ATTR_GRAPH_DRAW_HISTORY
            attrValue[3] = Integer.toString(gi.mEachLineHistory)
            attrName[4] = ATTR_GRAPH_DRAW_CURORDER
            attrValue[4] = Integer.toString(gi.mCurrentDrawOrder)
            attrName[5] = ATTR_GRAPH_DRAW_BRUSH
            attrValue[5] = Integer.toString(gi.mBrushType)
            attrNum = 6

            item[STAT_NORMAL, TAG_GRAPH_DRAW, attrName, attrValue] = attrNum
            mItemList.add(item)

            // colorタグ
            item = XmlItem()
            attrName[0] = ATTR_GRAPH_COLOR_MODE
            attrValue[0] = gi.cp.colModeInString
            attrName[1] = ATTR_GRAPH_COLOR_COLOR
            attrValue[1] = Integer.toHexString(gi.cp.color)
            attrName[2] = ATTR_GRAPH_COLOR_SHIFT
            attrValue[2] = Integer.toString(gi.cp.shiftSpeed)
            attrName[3] = ATTR_GRAPH_COLOR_TRANS
            attrValue[3] = Integer.toString(gi.cp.getTrans())
            attrNum = 4

            item[STAT_NORMAL, TAG_GRAPH_COLOR, attrName, attrValue] = attrNum
            mItemList.add(item)

            // graphタグ 終了
            item = XmlItem()
            item[STAT_END] = TAG_GRAPH
            mItemList.add(item)

        }

        item = XmlItem()
        item[STAT_END] = TAG_GRAPHLIST
        mItemList.add(item)
    }

    // ファイルに書き込む
    private fun writeXml(): Boolean {
        try {
            this.setData()

            if (!isFileSpecified) {
                basename = DGCommon.currentDateString + ".xml"
                isFileSpecified = false
            }
            // ファイル出力ストリームを作る
            val f = File("$root_dir/")

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
                    xmlse.attribute(null, curr.attrName[n], curr.attrValue[n])

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
        private const val STAT_NORMAL = 0 // 一過性タグ
        private const val STAT_START = 1  // タグ初め
        private const val STAT_END = 2    // タグ終了

        // 1つのタグ辺りの属性の最大数
        private const val NUM_ATTR = 8
    }
}
