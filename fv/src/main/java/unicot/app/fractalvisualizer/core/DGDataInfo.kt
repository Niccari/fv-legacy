package unicot.app.fractalvisualizer.core

/**
 * XMLデータ(グラフ保存データ)用のタグ、属性名などを定義する
 */
open class DGDataInfo {

    companion object {
        @JvmStatic protected val TAG_SYSTEM = "system"

        @JvmStatic protected val TAG_GRAPHLIST = "graphlist"

        @JvmStatic protected val TAG_GRAPH = "graph"
        @JvmStatic protected val TAG_GRAPH_POSITION = "position"
        @JvmStatic protected val TAG_GRAPH_SIZE = "size"
        @JvmStatic protected val TAG_GRAPH_ANGLE = "angle"
        @JvmStatic protected val TAG_GRAPH_ROTATE = "rotate"
        @JvmStatic protected val TAG_GRAPH_MUTATION = "mutation"
        @JvmStatic protected val TAG_GRAPH_RANDOMIZE = "randomize"
        @JvmStatic protected val TAG_GRAPH_COLOR = "color"
        @JvmStatic protected val TAG_GRAPH_DRAW = "draw"

        /* 属性：systemタグ */
        @JvmStatic protected val ATTR_SYSTEM_FRAMERATE = "framerate"
        @JvmStatic protected val ATTR_SYSTEM_POV_FRAME = "pov_frame"

        /* 属性：graphlistタグ */
        @JvmStatic protected val ATTR_GRAPHLIST_VERSION = "version"

        /* 属性：graphタグ*/
        @JvmStatic protected val ATTR_GRAPH_KIND = "kind"
        @JvmStatic protected val ATTR_GRAPH_COMPLEXITY = "complexity"
        @JvmStatic protected val ATTR_GRAPH_LEAF_BRANCH = "branch"
        @JvmStatic protected val ATTR_GRAPH_SGASKET_SKEW = "skew"

        /* 属性：positionタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_POSITION_X = "x"
        @JvmStatic protected val ATTR_GRAPH_POSITION_Y = "y"

        /* 属性：sizeタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_SIZE_WIDTH = "width"
        @JvmStatic protected val ATTR_GRAPH_SIZE_HEIGHT = "height"

        /* 属性：angle, rotateタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_ANGLE_VALUE = "value"
        @JvmStatic protected val ATTR_GRAPH_ROTATE_VALUE = ATTR_GRAPH_ANGLE_VALUE

        /* 属性：mutationタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_MUTATION_SIZE = "size"
        @JvmStatic protected val ATTR_GRAPH_MUTATION_ANGLE = "angle"

        /* 属性：randomizeタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_RANDOMIZE_SIZE = ATTR_GRAPH_MUTATION_SIZE
        @JvmStatic protected val ATTR_GRAPH_RANDOMIZE_ANGLE = ATTR_GRAPH_MUTATION_ANGLE

        /* 属性：colorタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_COLOR_MODE = "mode"
        @JvmStatic protected val ATTR_GRAPH_COLOR_COLOR = "color"
        @JvmStatic protected val ATTR_GRAPH_COLOR_SHIFT = "shift"
        @JvmStatic protected val ATTR_GRAPH_COLOR_TRANS = "trans"

        /* 属性：drawタグ(graphタグ内) */
        @JvmStatic protected val ATTR_GRAPH_DRAW_KIND = "kind"
        @JvmStatic protected val ATTR_GRAPH_DRAW_THICKNESS = "thickness"
        @JvmStatic protected val ATTR_GRAPH_DRAW_ANTIALIAS = "antialias"
        @JvmStatic protected val ATTR_GRAPH_DRAW_COLOREACH = "colorEach"
        @JvmStatic protected val ATTR_GRAPH_DRAW_HISTORY = "history"
        @JvmStatic protected val ATTR_GRAPH_DRAW_CURORDER = "currentOrder"
        @JvmStatic protected val ATTR_GRAPH_DRAW_BRUSH = "brush"

        /* 値：kind属性(drawタグ) */
        @JvmStatic protected val VALUE_GRAPH_DRAW_KIND_ALL = "all"
        @JvmStatic protected val VALUE_GRAPH_DRAW_KIND_EACH = "each"
    }
}
