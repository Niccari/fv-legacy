package unicot.app.fractalvisualizer.graph

import unicot.app.fractalvisualizer.core.DGCommon

/**
 * ランダム図形(不定)
 */
class RandomDynamic : Graph() {
    init {
        info.graph_kind = DGCommon.RANDOMSHAPE2
    }

    public override fun setRelativePoint() {
        allocatePoints()
        for (i in 0 until pointMax) { // グラフ形状を変形
            pointBase[i].x = GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat()
            pointBase[i].y = GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat()
        }
        isAllocated = true
    }

    override fun runningGraph() {
        if (!isAllocated)
            setRelativePoint()

        copyBasePoint() // pointBase => point, 以後pointを操作
        for (i in 0 until pointMax) { // グラフ形状を変形
            pointBase[i].x = GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat()
            pointBase[i].y = GraphInfo.GRAPH_SIZE_MID - GraphInfo.GRAPH_SIZE_MAX * Math.random().toFloat()
        }
        rotateRelativePoint() // 回転
        translateRelativePoint() // 拡大・移動
    }
}
