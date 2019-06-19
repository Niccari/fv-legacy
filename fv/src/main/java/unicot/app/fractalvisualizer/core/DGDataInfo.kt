package unicot.app.fractalvisualizer.core

/**
 * XMLデータ(グラフ保存データ)用のタグ、属性名などを定義する
 */
open class DGDataInfo {

    companion object {
        @JvmStatic protected val GRAPH_VERSION = "1"

        @JvmStatic protected val COLLECTION_SESSION_INFO = "session_info"
        @JvmStatic protected val INFO_DATE       = "date"
        @JvmStatic protected val INFO_THUMB_URL  = "thumb_url"

        @JvmStatic protected val COLLECTION_SESSION_DETAIL = "session_detail"
        @JvmStatic protected val DETAIL_VERSION    = "version"
        @JvmStatic protected val DETAIL_VIEW_FPS   = "framerate"
        @JvmStatic protected val DETAIL_VIEW_POV   = "pov_frame"
        @JvmStatic protected val DETAIL_GRAPH_LIST = "graph_list"
        @JvmStatic protected val GRAPH_KIND = "graph_kind"
        @JvmStatic protected val GRAPH_COMPLEXITY = "complexity"
        @JvmStatic protected val GRAPH_LEAF_BRANCH = "branch"
        @JvmStatic protected val GRAPH_SGASKET_SKEW = "skew"

        @JvmStatic protected val GRAPH_X = "x"
        @JvmStatic protected val GRAPH_Y = "y"
        @JvmStatic protected val GRAPH_WIDTH  = "width"
        @JvmStatic protected val GRAPH_HEIGHT = "height"
        @JvmStatic protected val GRAPH_ANGLE  = "angle"
        @JvmStatic protected val GRAPH_ROTATE = "rotate"
        @JvmStatic protected val GRAPH_MUTATION_SIZE   = "mutation_size"
        @JvmStatic protected val GRAPH_MUTATION_ANGLE  = "mutation_angle"
        @JvmStatic protected val GRAPH_RANDOMIZE_SIZE  = "randomize_size"
        @JvmStatic protected val GRAPH_RANDOMIZE_ANGLE = "randomize_angle"

        /* 属性：colorタグ(graphタグ内) */
        @JvmStatic protected val COLOR_MODE  = "color_mode"
        @JvmStatic protected val COLOR_COLOR = "color_color"
        @JvmStatic protected val COLOR_SHIFT = "color_shift"
        @JvmStatic protected val COLOR_TRANS = "color_trans"

        /* 属性：drawタグ(graphタグ内) */
        @JvmStatic protected val DRAW_KIND       = "draw_kind"
        @JvmStatic protected val DRAW_THICKNESS  = "draw_thickness"
        @JvmStatic protected val DRAW_COLOR_EACH = "draw_color_each"
        @JvmStatic protected val DRAW_HISTORY    = "draw_history"
        @JvmStatic protected val DRAW_CUR_ORDER  = "draw_current_order"
        @JvmStatic protected val DRAW_BRUSH      = "draw_brush"

        /* 値：kind属性(drawタグ) */
        @JvmStatic protected val DRAW_KIND_ALL = "all"
        @JvmStatic protected val DRAW_KIND_EACH = "each"

        // Old format
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
        @JvmStatic protected val ATTR_GRAPH_DRAW_COLOREACH = "colorEach"
        @JvmStatic protected val ATTR_GRAPH_DRAW_HISTORY = "history"
        @JvmStatic protected val ATTR_GRAPH_DRAW_CURORDER = "currentOrder"
        @JvmStatic protected val ATTR_GRAPH_DRAW_BRUSH = "brush"

        /* 値：kind属性(drawタグ) */
        @JvmStatic protected val VALUE_GRAPH_DRAW_KIND_ALL = "all"
        @JvmStatic protected val VALUE_GRAPH_DRAW_KIND_EACH = "each"
    }
}
