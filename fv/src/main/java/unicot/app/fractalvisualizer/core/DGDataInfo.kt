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
    }
}
